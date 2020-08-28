package com.colasoft.tip.network.recon;

import com.colasoft.tip.network.recon.utils.Ipv4Utils;

public class IPaddressTest {

    public static void main(String[] args) {

        String mask = "192.168.4.0/22";

        String startIp = Ipv4Utils.getStartIp(mask);
        String endIp = Ipv4Utils.getEndIp(mask);
        System.out.println(startIp);
        System.out.println(endIp);

        String ip = "192.168.5.150";

        long num = Ipv4Utils.ipToLong(ip);
        System.out.println(num);

        String ip1 = Ipv4Utils.long2Ip(num);
        System.out.println(ip1);

    }


}
