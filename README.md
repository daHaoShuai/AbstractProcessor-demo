# 记录一下自定义编译时注解处理器的使用

> orm和web都是我之前写的2个小玩意

> 通过AutoMapperProcessor这个自定义的注解处理器,在javac编译的时候生成实体类对应的接口类,然后让ioc容器扫描注册这个接口

```java

@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes("com.da.AutoMapper")
public class AutoMapperProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        // 这里就是去找到有@AutoMapper注解标记的类然后去处理它
    }
}
```

> 实体类上有 @AutoMapper 这个自定义的注解就可以给自动的注解处理器去处理

```java

@AutoMapper
@Table(tableName = "user")
public class User {
    // 略
}
```

> 因为通过自定义的注解处理器生成了对应的接口,然后容器又帮生成了代理对象,这里直接注入就能拿到

```java

@Path("/")
public class UserPath implements Handler {

    @Inject("UserMapper")
    BaseMapper<User> userMapper;

    @Override
    public void callback(Context ctx) {
        ctx.send(Utils.parseListToJsonString(userMapper.list()));
    }
}
```
