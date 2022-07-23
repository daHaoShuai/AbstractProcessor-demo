package com.da.web.core;

import com.da.web.annotations.Component;
import com.da.web.annotations.Inject;
import com.da.web.annotations.Path;
import com.da.web.function.Handler;
import com.da.web.util.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.IntStream;

/**
 * Author Da
 * Description: <br/>
 * 三十年生死两茫茫，写程序，到天亮。
 * 千行代码，Bug何处藏。
 * 纵使上线又怎样，朝令改，夕断肠。
 * 领导每天新想法，天天改，日日忙。
 * 相顾无言，惟有泪千行。
 * 每晚灯火阑珊处，夜难寐，又加班。
 * Date: 2022-05-31
 * Time: 18:33
 * 服务器启动类
 */
public class DApp {
    //    路由表
    private final Map<String, Handler> routes = new HashMap<>();
    //    保存扫描出来的bean
    private final Map<String, Object> beans = new HashMap<>();
    //    静态目录名字
    private String staticDirName = "static";
    //    静态path对应的资源文件
    private final Map<String, File> staticFiles = new HashMap<>();
    //    服务初始化时间
    private final long startTime;
    //    是否开启服务器
    private boolean isStart = false;
    //    从8080开始找可用的端口
    private int PORT = 8080;
    //    配置文件解析
    private Properties properties = null;

    /**
     * 空参构造不会扫描注入bean
     */
    public DApp() {
//        记录初始化时间
        this.startTime = System.currentTimeMillis();
//        获取并且解析配置文件
        getAndParseConfig();
//        扫描静态资源目录添加到路由表中
        scanStaticFile();
    }

    //        获取并且解析配置文件
    private void getAndParseConfig() {
//        解析端口号
        final String port = getCfgInfo("port");
        if (Utils.isNotBlank(port)) PORT = Integer.parseInt(port);
//        解析静态资源目录
        final String staticPath = getCfgInfo("static");
        if (Utils.isNotBlank(staticPath)) staticDirName = staticPath;
    }

    //        扫描静态资源目录添加静态资源map中去
    private void scanStaticFile() {
        File rootFile = Utils.getResourceFile(this.staticDirName);
        if (rootFile != null) {
            List<File> files = Utils.scanFileToList(rootFile);
//        处理扫描出来的文件添加对应的路由到路由表中
            files.forEach(this::createRouteToStaticFile);
        }
    }

    //    处理扫描出来的文件添加到对应的map中去
    private void createRouteToStaticFile(File file) {
//        获取当前文件的绝对路径
        final String absolutePath = file.getAbsolutePath().replaceAll("\\\\", "/");
//        裁掉静态目录的路径就是路由路径
        String path = absolutePath.substring(absolutePath.indexOf(this.staticDirName) + this.staticDirName.length());
//        让静态资源目录下的index.html文件为/
        if ("/index.html".equals(path)) path = "/";
        staticFiles.put(path, file);
    }

    /**
     * @param clz 配置类,容器会扫描配置类的包及其子包下面的所有文件
     */
    public DApp(Class<?> clz) {
//        记录初始化时间
        this.startTime = System.currentTimeMillis();
//        获取并且解析配置文件
        getAndParseConfig();
//        扫描静态资源目录添加到路由表中
        scanStaticFile();
//        扫描注册路由和bean组件
        initScan(clz);
//        给component的bean注入属性
        injectValueToComponentBean();
    }

    //    给bean注入属性
    private void injectValueToComponentBean() {
        beans.forEach((k, v) -> {
//            以/开头的
            if (!k.startsWith("/")) {
//                并且没有实现Handler接口的就是Component注解标记的类
                if (!Utils.isInterface(v.getClass(), Handler.class)) {
                    Class<?> clz = v.getClass();
                    for (Field field : clz.getDeclaredFields()) {
                        if (field.isAnnotationPresent(Inject.class)) {
//                            是beanName或者是值
                            String beanNameOrValue = field.getAnnotation(Inject.class).value();
//                            给属性注入值
                            InjectBaseTypeValue(field, beanNameOrValue, v);
                        }
                    }
                }
            }
        });
    }

    //    给属性注入值
    private void InjectBaseTypeValue(Field field, String beanNameOrValue, Object bean) {
//        当前属性的类型
        String fieldType = field.getType().getName();
//        获取基本类型的转换器
        Function<String, Object> conv = Utils.getTypeConv(fieldType);
//        不为空的时候就是基本数据类型和String
        if (null != conv) {
            field.setAccessible(true);
            try {
//              转换成对应的数据类型注入
                Object o = conv.apply(beanNameOrValue);
                field.set(bean, o);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                field.setAccessible(false);
            }
        }
//        注入bean
        else if (beans.containsKey(beanNameOrValue)) {
            Object o = beans.get(beanNameOrValue);
            field.setAccessible(true);
            try {
                field.set(bean, o);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } finally {
                field.setAccessible(true);
            }
        }
    }

    //    初始化扫描注册路由
    private void initScan(Class<?> clz) {
//        配置类的包名
        String packageName = clz.getPackage().getName();
        String rootPathName = Utils.replace(packageName, "\\.", "/");
        File rootPath = Utils.getResourceFile(rootPathName);
        List<File> files = Utils.scanFileToList(rootPath);
        files.forEach(file -> handlerScanFile(packageName, file));
    }

    //    处理扫描出来的每个文件
    private void handlerScanFile(String packageName, File file) {
//        获取文件夹的绝对路径
        String fileAbsolutePath = file.getAbsolutePath();
//        处理所有以.class结尾的文件
        if (fileAbsolutePath.endsWith(".class")) {
            String className = Utils.replace(fileAbsolutePath, "\\\\", "\\.");
            className = className.substring(className.indexOf(packageName));
            className = className.substring(0, className.lastIndexOf("."));
            handlerClassName(className);
        }
    }

    //    处理符合的class,丢到bean池中去
    @SuppressWarnings("unchecked")//忽略强转类型的警告
    private void handlerClassName(String className) {
        Class<?> clz = Utils.loadClass(className);
        if (null != clz) {
            String beanName = "";
            Object bean = null;
            if (Utils.isAnnotation(clz, Component.class)) {
                beanName = clz.getAnnotation(Component.class).value();
                bean = Utils.newInstance(clz);
            } else if (Utils.isAnnotation(clz, Path.class)) {
                beanName = clz.getAnnotation(Path.class).value();
                bean = Utils.newInstance(clz);
//                判断有没有我的orm框架
            } else if (Utils.isReadExist("com.da.orm.annotation.Mapper")) {
                try {
//                    加载orm框架的 @Mapper 注解
                    final Class<Annotation> mapper = (Class<Annotation>) Class.forName("com.da.orm.annotation.Mapper");
//                    判断当前类有没有 @Mapper 注解
                    if (Utils.isAnnotation(clz, mapper)) {
                        //                mapper工厂存在才创建代理对象
                        if (Utils.isReadExist("com.da.orm.core.MapperProxyFactory")) {
                            final Class<?> mapperProxyFactoryClz = Class.forName("com.da.orm.core.MapperProxyFactory");
                            final Object instance = mapperProxyFactoryClz.getConstructor().newInstance();
                            final Method getMapper = mapperProxyFactoryClz.getDeclaredMethod("getMapper", Class.class);
                            getMapper.setAccessible(true);
                            final Object invoke = getMapper.invoke(instance, clz);
                            beanName = clz.getSimpleName();
                            bean = invoke;
                            getMapper.setAccessible(false);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
//               符合的实例化丢到bean池中
            if (Utils.isNotBlank(beanName) && null != bean) {
                beans.put(beanName, bean);
            }
        }
    }

    /**
     * 请求注册处理
     *
     * @param path    请求的路由路径
     * @param handler 对对应路由路径的处理
     */
    public void use(String path, Handler handler) {
        routes.put(path, handler);
    }

    /**
     * 用默认端口(8080)启动服务
     */
    public void listen() {
        //    默认端口8080
        listen(PORT);
    }

    /**
     * 指定端口启动服务
     *
     * @param port 端口
     */
    public void listen(int port) {
//         不知道有没有用,反正加上也没事
        System.setProperty("java.awt.headless", Boolean.toString(true));
//        用新的线程开启监听,不会阻塞后面执行的代码
        Thread serverThread = new Thread(() -> start0(port));
//        把isStart设置为true
        isStart = !serverThread.isAlive();
//        开启新的线程
        serverThread.start();
//        jvm关闭的时候关闭循环和执行的线程,可以不用写这段,写了也无所谓
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            shutdown();
            serverThread.stop();
        }));
    }

    //    启动服务器
    private void start0(int port) {
        try {
//            初始化服务器
            initServer(port);
        } catch (IOException e) {
//            默认端口号+1,重新监听
            PORT = PORT + 1;
            listen();
        }
    }

    //    初始化服务器
    private void initServer(int port) throws IOException {
//        当前线程为Boss线程
        Thread.currentThread().setName("Boss");
//            打开选择器
        Selector boss = Selector.open();
//            打开服务端通道
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
//            绑定端口
        serverSocketChannel.socket().bind(new InetSocketAddress(port));
//            设置为非阻塞
        serverSocketChannel.configureBlocking(false);
//            注册到选择器,等待连接
        serverSocketChannel.register(boss, SelectionKey.OP_ACCEPT);
//            打印初始化信息
        printInitMessage(port, startTime);
//        启动循环监听
        startServer(boss, serverSocketChannel);
    }

    //    启动服务器
    private void startServer(Selector boss, ServerSocketChannel serverSocketChannel) throws IOException {
//        worker线程的数量(cpu的核心数)
        final int workerNum = Runtime.getRuntime().availableProcessors();
        final Worker[] workers = new Worker[workerNum];
//        创建worker线程
        IntStream.range(0, workerNum).forEach(i -> workers[i] = new Worker("worker-" + i));
//        用于负载均衡的原子整数
        AtomicInteger robin = new AtomicInteger(0);
//        循环监听
        while (isStart) {
//                有连接进来
            if (boss.select() > 0) {
//                    关注事件的集合
                Set<SelectionKey> selectionKeys = boss.selectedKeys();
//                    迭代器
                Iterator<SelectionKey> iterator = selectionKeys.iterator();
                while (iterator.hasNext()) {
                    final SelectionKey key = iterator.next();
//                    boss线程只负责Accept事件
                    if (key.isAcceptable()) {
//                        拿到浏览器的连接
                        final SocketChannel accept = serverSocketChannel.accept();
//                        设置为非阻塞
                        accept.configureBlocking(false);
//                        负载均衡,轮询分配Worker
                        workers[robin.getAndIncrement() % workers.length].register(accept);
                    }
//                    删除掉当前处理完成的key
                    iterator.remove();
                }
            }
        }
    }

    //    处理对应的路由请求
    private void handlerRoutes(Context context) {
        //            获取请求的url
        String url = context.getUrl();
//        判断路由表中有没有对应的路由
        if (routes.containsKey(url)) {
//              执行回调
            routes.get(url).callback(context);
        }
//        判断静态资源目录中有没有对应的路由路径
        else if (staticFiles.containsKey(url)) {
            //    处理静态资源目录中的路由
            handlerStaticRoute(url, context);
        }
//            在bean池中看看有没有对应的bean
        else if (beans.containsKey(url)) {
            //    处理bean池中的路由
            handBeanRoute(url, context);
        }
//        找不到就是404
        else {
            context.sendHtml("<h1 style='color: red;text-align: center;'>404 not found</h1><hr/>", Context.NOTFOUND);
        }
    }

    //    处理静态资源目录中的路由
    private void handlerStaticRoute(String url, Context context) {
        File file = staticFiles.get(url);
//        发送文件到浏览器
        context.send(file);
    }

    //    处理bean池中的路由
    private void handBeanRoute(String url, Context context) {
        //                获取对应的bean,注入属性
        Object bean = beans.get(url);
//                动态注入属性
        injectValueToPathBean(bean, context);
//                执行回调
        ((Handler) bean).callback(context);
    }

    //    注入PathBean的属性
    private void injectValueToPathBean(Object bean, Context context) {
//        当前的请求参数
        Map<String, Object> params = context.getParams();
        Class<?> clz = bean.getClass();
        Field[] fields = clz.getDeclaredFields();
        for (Field field : fields) {
//            获取转换器
            Function<String, Object> conv = Utils.getTypeConv(field.getType().getName());
//            判断有没有Inject注解
            if (field.isAnnotationPresent(Inject.class)) {
                String beanNameOrValue = field.getAnnotation(Inject.class).value();
//                注入基本类型和String
                if (null != conv) {
                    field.setAccessible(true);
                    try {
//                      转换成对应的值
                        Object value = conv.apply(beanNameOrValue);
                        field.set(bean, value);
                    } catch (Exception e) {
                        context.errPrint(e);
                    } finally {
                        field.setAccessible(false);
                    }
                }
//                注入bean池中的类
                else if (beans.containsKey(beanNameOrValue)) {
                    Object initBean = beans.get(beanNameOrValue);
                    field.setAccessible(true);
                    try {
                        field.set(bean, initBean);
                    } catch (IllegalAccessException e) {
                        context.errPrint(e);
                    } finally {
                        field.setAccessible(false);
                    }
                }
            }
//            没有Inject注解就尝试注入请求的参数
            else if (params.containsKey(field.getName())) {
//                获取请求参数的值
                String value = (String) params.get(field.getName());
//                尝试注入基本类型
                if (null != conv) {
                    field.setAccessible(true);
                    try {
//                      转换类型,可能会转换出错,所以要处理
                        Object o = conv.apply(value);
                        field.set(bean, o);
                    } catch (Exception e) {
                        context.errPrint(e);
                    } finally {
                        field.setAccessible(false);
                    }
                }
            }
        }
    }

    /**
     * 关闭服务器
     */
    public void shutdown() {
        isStart = false;
    }

    //    打印初始化信息
    private void printInitMessage(int port, long startTime) {
        String[] banner = new String[]{
                "    .___                      ___.    ", "  __| _/____    __  _  __ ____\\_ |__  ",
                " / __ |\\__  \\   \\ \\/ \\/ // __ \\| __ \\ ", "/ /_/ | / __ \\_  \\     /\\  ___/| \\_\\ \\",
                "\\____ |(____  /   \\/\\_/  \\___  >___  /", "     \\/     \\/               \\/    \\/"
        };
        // 打印banner图
        for (String s : banner) System.out.println(s);
        System.out.println("NIO服务器启动成功:");
        System.out.println("\t> 本地访问: http://localhost:" + port);
        try {
            System.out.println("\t> 网络访问: http://" + InetAddress.getLocalHost().getHostAddress() + ":" + port);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        System.out.println("\t启动总耗时: " + (System.currentTimeMillis() - startTime) + "ms\n");
    }

    /**
     * 获取扫描出来的实例化好的bean
     *
     * @param beanName bean的名字
     * @return 容器实例化好的bean
     */
    public Object getBean(String beanName) {
        return beans.get(beanName);
    }

    /**
     * 获取扫描出来的实例化好的bean,并且转好类型
     *
     * @param beanName bean的名字
     * @param t        要转成的类型
     * @return 转好类型的bean
     */
    @SuppressWarnings("unchecked") // 让编译器不报黄线
    public <T> T getBean(String beanName, Class<T> t) {
        if (null != getBean(beanName)) {
            return (T) getBean(beanName);
        }
        return null;
    }

    /**
     * 获取配置文件的信息
     *
     * @param propName 对应的属性名
     * @return 对应的属性值
     */
    public String getCfgInfo(String propName) {
//      获取配置文件,不为空的时候解析配置文件
        final File configFile = Utils.getResourceFile("app.properties");
        try {
            if (configFile != null) {
//                不重复创建Properties
                if (null == properties) {
                    properties = new Properties();
                    properties.load(new FileInputStream(configFile));
                }
//                返回配置文件中对应的值
                return properties.getProperty(propName);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    //    工作线程,处理路由事件
    class Worker implements Runnable {
        //        选择器
        private volatile Selector selector;
        //        当前的工作线程的名字
        private final String name;
        //        当前线程是否启动
        private volatile boolean started = false;
        //        同步队列,用于Boss线程与Worker线程之间的通信
        private ConcurrentLinkedQueue<Runnable> queue;

        public Worker(String name) {
            this.name = name;
        }

        //        注册读取事件
        public void register(final SocketChannel socket) throws IOException {
//            只创建一次当前的线程
            if (!started) {
                //        当前的工作线程
                Thread thread = new Thread(this, name);
                selector = Selector.open();
                queue = new ConcurrentLinkedQueue<>();
                this.started = true;
                thread.start();
            }
            // 向同步队列中添加SocketChannel的读取注册事件
            queue.add(() -> {
                try {
//                    把当前的选择器注册为读取事件
                    socket.register(selector, SelectionKey.OP_READ);
                } catch (ClosedChannelException e) {
                    e.printStackTrace();
                }
            });
            // 唤醒被阻塞的Selector select类似LockSupport中的park，wakeup的原理类似LockSupport中的unpark
            selector.wakeup();
        }

        //        获取并且执行队列中的任务(注册读取事件)
        private void getAndStartTask() {
            final Runnable task = queue.poll();
            if (task != null) {
//                获得任务,执行注册操作
                task.run();
            }
        }

        @Override
        public void run() {
            while (this.started) {
                try {
//                    有连接
                    selector.select();
//                    通过同步队列获得任务并运行
                    getAndStartTask();
//                    拿到选择器集合
                    final Set<SelectionKey> selectionKeys = selector.selectedKeys();
                    final Iterator<SelectionKey> iterator = selectionKeys.iterator();
                    while (iterator.hasNext()) {
                        final SelectionKey key = iterator.next();
//                        Worker只负责Read事件
                        if (key.isReadable()) {
//                            获取写内容的通道
                            SocketChannel channel = (SocketChannel) key.channel();
//                            创建当前通道的上下文对象,用于解析请求信息和响应内容到浏览器
                            Context context = new Context(channel);
//                            处理对应的路由请求
                            handlerRoutes(context);
                        }
//                        删除掉处理过的
                        iterator.remove();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
