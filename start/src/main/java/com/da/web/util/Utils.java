package com.da.web.util;

import com.da.po.User;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

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
 * Time: 19:42
 * 工具类
 */
public class Utils {
    //    创建工具类的实例
    private static final Utils UTILS = new Utils();
    //    保存类型转换工具的类
    private static final Map<String, Function<String, Object>> typeMap = new HashMap<>();

    //    填充转换器map
    static {
        typeMap.put("java.lang.String", str -> str);
        typeMap.put("byte", Byte::valueOf);
        typeMap.put("java.lang.Byte", Byte::valueOf);
        typeMap.put("boolean", Boolean::valueOf);
        typeMap.put("java.lang.Boolean", Boolean::valueOf);
        typeMap.put("short", Short::valueOf);
        typeMap.put("java.lang.Short", Short::valueOf);
        typeMap.put("char", str -> str.charAt(0));
        typeMap.put("java.lang.Character", str -> str.charAt(0));
        typeMap.put("int", Integer::valueOf);
        typeMap.put("java.lang.Integer", Integer::valueOf);
        typeMap.put("long", Long::valueOf);
        typeMap.put("java.lang.Long", Long::valueOf);
        typeMap.put("float", Float::valueOf);
        typeMap.put("java.lang.Float", Float::valueOf);
        typeMap.put("double", Double::valueOf);
        typeMap.put("java.lang.Double", Double::valueOf);
    }

    //    私有构造
    private Utils() {
    }

    /**
     * 把实体类列表转成json数组字符串
     *
     * @param list 实体类列表
     * @return json数组类型字符串
     */
    public static <T> String parseListToJsonString(List<T> list) {
        final StringBuilder builder = new StringBuilder();
        try {
            if (null != list && list.size() > 0) {
                final T t = list.get(0);
                final String listName = t.getClass().getSimpleName();
                builder.append("{\"").append(listName).append("\":[");
                final Field[] fields = t.getClass().getDeclaredFields();
                for (T i : list) {
                    builder.append("{");
                    for (Field field : fields) {
                        field.setAccessible(true);
                        builder.append("\"")
                                .append(field.getName())
                                .append("\":\"")
                                .append(field.get(i))
                                .append("\",");
                    }
                    builder.deleteCharAt(builder.length() - 1);
                    builder.append("},");
                }
                builder.deleteCharAt(builder.length() - 1);
                builder.append("]}");
            }
            return builder.toString();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * 获取工具类实例,基本不会用上
     *
     * @return 工具类的实例
     */
    public Utils getInstance() {
        return UTILS;
    }

    /**
     * 获取String对应基本类型的转换器
     *
     * @param type 要转换的类型
     * @return 对应类型的转换器
     */
    public static Function<String, Object> getTypeConv(String type) {
        return typeMap.get(type);
    }

    /**
     * 判断字符串不为空
     *
     * @param str 要判断的字符串
     * @return 判断结果
     */
    public static boolean isNotBlank(String str) {
        return null != str && !"".equals(str);
    }

    /**
     * 判断字符串为空
     *
     * @param str 要判断的字符串
     * @return 判断结果
     */
    public static boolean isBlank(String str) {
        return !isNotBlank(str);
    }

    /**
     * 判断数组不为空
     *
     * @param t 要判断的数组
     * @return 判断结果
     */
    public static <T> boolean isArrayNotNull(T[] t) {
        return null != t && t.length > 0;
    }

    /**
     * 判断数组为空
     *
     * @param t 要判断的数组
     * @return 判断结果
     */
    public static <T> boolean isArrayNull(T[] t) {
        return !isArrayNotNull(t);
    }

    /**
     * 判断列表不为null并且有值
     *
     * @param t 要判断的List
     * @return 判断结果
     */
    public static <T> boolean isListNotNull(List<T> t) {
        return null != t && t.size() > 0;
    }

    /**
     * 判断列表为null或者有值
     *
     * @param t 要判断的List
     * @return 判断结果
     */
    public static <T> boolean isListNull(List<T> t) {
        return !isListNotNull(t);
    }

    /**
     * 判断文件存在
     *
     * @param file 要判断的文件
     * @return 判断结果
     */
    public static boolean isNotNullFile(File file) {
        return null != file && file.exists();
    }

    /**
     * 判断文件为null或者为空
     *
     * @param file 要判断的文件
     * @return 判断结果
     */
    public static boolean isNullFile(File file) {
        return !isNotNullFile(file);
    }

    /**
     * 查询字符在字符串中出现第n次的坐标
     *
     * @param str     原始字符串
     * @param findStr 要查找的字符
     * @param i       字符出现的第几个位置
     * @return 查找到的坐标
     */
    public static int getStrIndex(String str, String findStr, int i) {
        int idx = 0;
        while (i > 0) {
            int tempIdx = str.indexOf(findStr);
            if (tempIdx == -1) throw new RuntimeException("在" + str + "中找不到字符" + findStr + "第" + i + "处的坐标");
            str = str.substring(0, tempIdx) + " " + str.substring(tempIdx + 1);
            idx += tempIdx;
            i--;
        }
        return idx;
    }

    /**
     * 根据资源目录下的文件名字获取资源目录下的文件
     *
     * @param fileName 资源目录下的文件名字
     * @return 资源目录下的文件
     */
    public static File getResourceFile(String fileName) {
        URL url = UTILS.getClass().getClassLoader().getResource(fileName);
        if (null == url) return null;
        return new File(url.getFile());
    }

    /**
     * 获取资源目录下的文件路径
     *
     * @param fileName 资源目录下的文件名字
     * @return 资源目录下的文件路径
     */
    public static String getResourcePath(String fileName) {
        return getResourceFile(fileName).getPath();
    }

    /**
     * 通过包名.类名加载类
     *
     * @param className 包名.类名
     * @return 加载的Class
     */
    public static Class<?> loadClass(String className) {
        Class<?> clz = null;
        try {
            clz = UTILS.getClass().getClassLoader().loadClass(className);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return clz;
    }

    /**
     * 通过加载的类来实例化对象
     *
     * @param clz 要实例化的Class
     * @return 实例化好的Class
     */
    public static Object newInstance(Class<?> clz) {
        Object o = null;
        try {
            o = clz.getConstructor().newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return o;
    }

    /**
     * 把str1中的指定部分(str2)全部替换为str3
     *
     * @param str1 原始的字符串
     * @param str2 要替换的部分
     * @param str3 替换的内容
     * @return 替换好的内容
     */
    public static String replace(String str1, String str2, String str3) {
        return str1.replaceAll(str2, str3);
    }

    /**
     * 扫描出当前文件夹及其子文件夹的所有文件
     *
     * @param root 要扫描的文件根路径
     * @return 扫描出来的文件列表
     */
    public static List<File> scanFileToList(File root) {
        List<File> list = null;
        if (isNotNullFile(root)) {
            Path rootPath = Paths.get(root.getPath());
            try {
                list = Files.walk(rootPath)
                        .map(Path::toFile)
                        .filter(file -> !file.isDirectory())
                        .collect(Collectors.toList());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return list;
    }

    /**
     * 判断当前类上有没有对应的注解
     *
     * @param clz  要判断的Class
     * @param anno 对应的注解
     * @return 判断结果
     */
    public static boolean isAnnotation(Class<?> clz, Class<? extends Annotation> anno) {
        return clz.isAnnotationPresent(anno);
    }

    /**
     * 判断当前类有没有实现对应的接口
     *
     * @param clz 要判断的Class
     * @param ier 对应的接口类型
     * @return 判断结果
     */
    public static boolean isInterface(Class<?> clz, Class<?> ier) {
        Class<?>[] interfaces = clz.getInterfaces();
        if (interfaces.length == 0) return false;
        return Arrays.asList(interfaces).contains(ier);
    }

    /**
     * 读取文本文件的内容
     *
     * @param file     要读取的文本文件
     * @param encoding 读取的编码
     * @return 读取的内容
     */
    public static String readFileToString(File file, Charset encoding) {
        if (isNotNullFile(file)) {
            try {
                return Files.readAllLines(file.toPath(), encoding)
                        .stream().reduce((a, b) -> a + b).orElse("");
            } catch (IOException e) {
                e.printStackTrace();
                return "";
            }
        }
        return "";
    }

    /**
     * 读取指定后缀的文本文件
     *
     * @param file     要读取的文本文件
     * @param flag     指定的文件后缀
     * @param encoding 读取的编码
     * @return 读取的内容
     */
    public static String readFileAndFlagToString(File file, String flag, Charset encoding) {
        if (isNotNullFile(file)) {
//            只读取以指定后置结尾的文件
            if (file.getName().endsWith(flag)) {
                return readFileToString(file, encoding);
            } else {
                return "";
            }
        }
        return "";
    }

    /**
     * 指定读取以.html结尾的文件
     *
     * @param file html文件
     * @return 读取的内容
     */
    public static String readHtmlFileToString(File file) {
        return readFileAndFlagToString(file, ".html", StandardCharsets.UTF_8);
    }

    /**
     * 获取文件的类型
     *
     * @param file 要获取的类型的文件
     * @return 文件的类型
     */
    public static String getFileType(File file) {
        String type;
        try {
            type = Files.probeContentType(file.toPath());
        } catch (IOException e) {
            e.printStackTrace();
            type = "";
        }
        return type;
    }

    /**
     * 查找对应字符在字符串中出现的次数
     *
     * @param str 要查找的字符串内容
     * @param c   要查找的char字符
     * @return 对应字符在字符串中出现的次数
     */
    public static int getChatInStringNum(String str, char c) {
        int num = 0;
        for (int i = 0; i < str.length(); i++) {
            final char c1 = str.charAt(i);
            if (c1 == c) num += 1;
        }
        return num;
    }

    /**
     * @param className 要判断的类
     * @return 类存不存在
     */
    public static boolean isReadExist(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
