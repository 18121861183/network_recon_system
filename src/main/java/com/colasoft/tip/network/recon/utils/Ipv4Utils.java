package com.colasoft.tip.network.recon.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Ipv4Utils {


    //将127.0.0.1形式的IP地址转换成十进制整数，这里没有进行任何错误处理
    public static long ipToLong(String strIp) throws NumberFormatException{
        String[] positionArray = strIp.split("\\.");
        if (positionArray.length != 4) {
            return 0L;
        }
        long[] ip = new long[4];
        //将每个.之间的字符串转换成整型
        try {
            ip[0] = Long.parseLong(positionArray[0]);
            ip[1] = Long.parseLong(positionArray[1]);
            ip[2] = Long.parseLong(positionArray[2]);
            ip[3] = Long.parseLong(positionArray[3]);
        } catch (NumberFormatException e) {
            throw e;
        }
        return (ip[0] << 24) + (ip[1] << 16) + (ip[2] << 8) + ip[3];
    }

    public static String long2Ip(long ipLong){
        long i1 = (ipLong>>24) & 0xFF;
        long i2 = (ipLong>>16) & 0xFF;
        long i3 = (ipLong>>8) & 0xFF;
        long i4 = (ipLong>>0) & 0xFF;
        return i1+"."+i2+"."+i3+"."+i4;
    }
//
//    /**
//     * IPv4地址转换为int类型数字
//     *
//     */
//    public static int ip2Int(String ipString) {
//        // 取 ip 的各段
//        String[] ipSlices = ipString.split("\\.");
//        int rs = 0;
//        for (int i = 0; i < ipSlices.length; i++) {
//            // 将 ip 的每一段解析为 int，并根据位置左移 8 位
//            int intSlice = Integer.parseInt(ipSlices[i]) << 8 * i;
//            // 或运算
//            rs = rs | intSlice;
//        }
//        return rs;
//    }

    /**
     * 判断是否为ipv4地址
     *
     */
    public static boolean isIPv4Address(String ipv4Addr) {
        String lower = "(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])"; // 0-255的数字
        String regex = lower + "(\\." + lower + "){3}";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(ipv4Addr);
        return matcher.matches();
    }

//    /**
//     * 将int数字转换成ipv4地址
//     *
//     */
//    public static String integer2Ip(int ip) {
//        StringBuilder sb = new StringBuilder();
//        int num = 0;
//        boolean needPoint = false; // 是否需要加入'.'
//        for (int i = 0; i < 4; i++) {
//            if (needPoint) {
//                sb.append('.');
//            }
//            needPoint = true;
//            int offset = 8 * (3 - i);
//            num = (ip >> offset) & 0xff;
//            sb.append(num);
//        }
//        return sb.toString();
//    }

    /**
     * 根据掩码位数计算掩码
     * @param maskIndex 掩码位
     * @return 子网掩码
     */
    public static String getNetMask(String maskIndex) {
        StringBuilder mask = new StringBuilder();
        int inetMask = 0;
        try {
            inetMask = Integer.parseInt(maskIndex);
        } catch (NumberFormatException e) {
            System.out.println(e.getMessage());
            return null;
        }
        if (inetMask > 32) {
            return null;
        }
        // 子网掩码为1占了几个字节
        int num1 = inetMask / 8;
        // 子网掩码的补位位数
        int num2 = inetMask % 8;
        int[] array = new int[4];
        for (int i = 0; i < num1; i++) {
            array[i] = 255;
        }
        for (int i = num1; i < 4; i++) {
            array[i] = 0;
        }
        for (int i = 0; i < num2; num2--) {
            array[num1] += 1 << 8 - num2;
        }
        for (int i = 0; i < 4; i++) {
            if (i == 3) {
                mask.append(array[i]);
            } else {
                mask.append(array[i]).append(".");
            }
        }
        return mask.toString();
    }


    /**
     * 根据网段计算起始IP 网段格式:x.x.x.x/x
     * 一个网段0一般为网络地址,255一般为广播地址.
     * 起始IP计算:网段与掩码相与之后加一的IP地址
     * @param segment 网段
     * @return 起始IP
     */
    public static String getStartIp(String segment) {
        StringBuilder startIp = new StringBuilder();
        if (segment == null) {
            return null;
        }
        String[] arr = segment.split("/");
        String ip = arr[0];
        String maskIndex = arr[1];
        String mask = getNetMask(maskIndex);
        if (4 != ip.split("\\.").length || mask == null) {
            return null;
        }
        int[] ipArray = new int[4];
        int[] netMaskArray = new int[4];
        for (int i = 0; i < 4; i++) {
            try {
                ipArray[i] = Integer.parseInt(ip.split("\\.")[i]);
                netMaskArray[i] = Integer.parseInt(mask.split("\\.")[i]);
                if (ipArray[i] > 255 || ipArray[i] < 0 || netMaskArray[i] > 255 || netMaskArray[i] < 0) {
                    return null;
                }
                ipArray[i] = ipArray[i] & netMaskArray[i];
                if(i==3){
                    startIp.append(ipArray[i]+1);
                }else{
                    startIp.append(ipArray[i]).append(".");
                }
            } catch (NumberFormatException e) {
                System.out.println(e.getMessage());
            }
        }
        return startIp.toString();
    }

    /**
     * 根据网段计算结束IP
     * @param segment IP
     * @return 结束IP
     */
    public static String getEndIp(String segment) {
        StringBuilder endIp = new StringBuilder();
        String startIp = getStartIp(segment);
        if (segment == null) {
            return null;
        }
        String[] arr = segment.split("/");
        String maskIndex = arr[1];
        //实际需要的IP个数
        int hostNumber = 0;
        int[] startIpArray = new int[4];
        try {
            hostNumber=1<<32-(Integer.parseInt(maskIndex));
            for (int i = 0; i <4; i++) {
                assert startIp != null;
                startIpArray[i] = Integer.parseInt(startIp.split("\\.")[i]);
                if(i == 3){
                    startIpArray[i] = startIpArray[i] - 1;
                    break;
                }
            }
            startIpArray[3] = startIpArray[3] + (hostNumber - 1);
        } catch (NumberFormatException e) {
            System.out.println(e.getMessage());
        }

        if(startIpArray[3] >255){
            int k = startIpArray[3] / 256;
            startIpArray[3] = startIpArray[3] % 256;
            startIpArray[2] = startIpArray[2] + k;
        }
        if(startIpArray[2] > 255){
            int j = startIpArray[2] / 256;
            startIpArray[2] = startIpArray[2] % 256;
            startIpArray[1] = startIpArray[1] + j;
            if(startIpArray[1] > 255){
                int k = startIpArray[1] / 256;
                startIpArray[1] = startIpArray[1] % 256;
                startIpArray[0] = startIpArray[0] + k;
            }
        }
        for(int i = 0; i < 4; i++){
            if(i == 3){
                startIpArray[i] = startIpArray[i] - 1;
            }
            if("".equals(endIp.toString()) ||endIp.length()==0){
                endIp.append(startIpArray[i]);
            }else{
                endIp.append(".").append(startIpArray[i]);
            }
        }
        return endIp.toString();
    }

}
