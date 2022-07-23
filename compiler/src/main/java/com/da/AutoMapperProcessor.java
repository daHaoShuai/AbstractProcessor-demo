package com.da;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.util.Set;

/**
 * @Author Da
 * @Description:
 * @Date: 2022-07-23
 * @Time: 14:29
 * 自定义编译时注解处理器
 */
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes("com.da.AutoMapper")
public class AutoMapperProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        final StringBuilder builder = new StringBuilder();
//        处理带有AutoMapper的实体类,生成对应的BaseMapper接口
        for (Element element : roundEnv.getElementsAnnotatedWith(AutoMapper.class)) {
//            获取包名
            final String packageName = element.toString().substring(0, element.toString().lastIndexOf("."));
            builder.append("package ").append(packageName).append(";\n\n")
                    .append("import java.util.List;\n\n")
                    .append("import com.da.orm.annotation.Mapper;\n")
                    .append("import com.da.orm.annotation.Select;\n")
                    .append("import com.da.orm.annotation.Delete;\n")
                    .append("import com.da.orm.core.BaseMapper;\n");
//            要生成的类名
            String poName = element.getSimpleName().toString();
            builder.append("import ").append(packageName).append(".").append(poName).append(";\n\n")
                    .append("@Mapper\n")
                    .append("public interface ").append(poName).append("Mapper").append(" extends BaseMapper<").append(poName).append("> {\n\n")
                    .append("\t@Select(\"select * from ").append(poName.toLowerCase()).append("\")\n")
                    .append("\tList<").append(poName).append("> list();\n")
                    .append("\t@Select(\"select * from ").append(poName.toLowerCase()).append(" where id = #{id}\")\n")
                    .append("\t").append(poName).append(" getById(").append("Integer id);\n")
                    .append("\t@Delete(\"delete from ").append(poName.toLowerCase()).append(" where id = #{id}\")\n")
                    .append("\t").append("boolean delete(").append("Integer id);\n\n")
                    .append("}");
            try {
                final JavaFileObject source = processingEnv.getFiler().createSourceFile(packageName + "." + poName + "Mapper");
                try (Writer writer = source.openWriter()) {
                    writer.write(builder.toString());
                    writer.flush();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
}
