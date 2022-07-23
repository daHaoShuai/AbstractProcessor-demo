package com.da.orm.utils;

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Author Da
 * @Description: <br/>
 * 三十年生死两茫茫，写程序，到天亮。
 * 千行代码，Bug何处藏。
 * 纵使上线又怎样，朝令改，夕断肠。
 * 领导每天新想法，天天改，日日忙。
 * 相顾无言，惟有泪千行。
 * 每晚灯火阑珊处，夜难寐，又加班。
 * @Date: 2022-06-23
 * @Time: 16:13
 * 字符串工具类
 */
public class StringUtil {
    //    字符串不为空
    public static boolean isEmpty(String str) {
        return null == str || "".equals(str);
    }

    //    驼峰式命名转下划线命名
    public static String convertToUnderline(String str) {
        if (isEmpty(str)) throw new RuntimeException("输入字符为空");
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            final char c = str.charAt(i);
//            处理有大写的地方
            if (Character.isUpperCase(c)) {
                if (i == 0) {
                    result.append(String.valueOf(c).toLowerCase());
                } else {
                    result.append("_").append(String.valueOf(c).toLowerCase());
                }
                continue;
            }
            result.append(c);
        }
        return result.toString();
    }

    //    下划线命名改驼峰式命名
    public static String convertToLineHump(String str) {
        final StringBuilder result = new StringBuilder();
//        用_分开
        final String[] split = str.split("_");
        for (String s : split) {
//            首字母转大写
            result.append(s.substring(0, 1).toUpperCase()).append(s.substring(1));
        }
        return result.toString();
    }

    //    拼接List并且用指定的字符隔开
    public static String join(List<String> data, String str) {
        final Optional<String> reduce = data.stream().reduce((o, n) -> o + str + n);
        return reduce.orElse("");
    }

    //    拼接String数组并且用指定的字符隔开
    public static String join(String[] data, String str) {
        final StringBuilder result = new StringBuilder();
        for (String d : data) {
            result.append(d).append(str);
        }
        return result.substring(0, result.lastIndexOf(str));
    }

    //    匹配字符串中的 #{*}
    public static Matcher regMateSql(String str) {
//        正则匹配 #{..} 的内容
        final String reg = "#\\{[a-z]+}";
        final Pattern pattern = Pattern.compile(reg);
        return pattern.matcher(str);
    }
}
