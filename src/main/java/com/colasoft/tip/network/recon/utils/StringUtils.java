package com.colasoft.tip.network.recon.utils;

import java.util.List;
import java.util.UUID;

public class StringUtils {

    public static String join(String flag, List<?> list ) {
        StringBuilder builder = new StringBuilder();
        for (Object str: list) {
            if (builder.length() > 0) {
                builder.append(flag);
            }
            builder.append(str);
        }
        return builder.toString();
    }

    public static boolean isEmpty(String string) {
        return string == null || string.trim().length() == 0;
    }

    public static boolean isNotEmpty(String string) {
        return !isEmpty(string);
    }


    public static String getUUID32() {
        return UUID.randomUUID().toString().replaceAll("-","");
    }

}
