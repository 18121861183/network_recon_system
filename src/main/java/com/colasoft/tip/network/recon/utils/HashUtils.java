package com.colasoft.tip.network.recon.utils;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;

import java.util.UUID;

public class HashUtils {

    //base64
    public static String base64Encode(String data) {
        return Base64.encodeBase64String(data.getBytes());
    }

    public static String base64Encode(byte[] bytes) {
        return Base64.encodeBase64String(bytes);
    }

    public static byte[] base64Decode(String data) {
        return Base64.decodeBase64(data.getBytes());
    }

    //MD5
    public static String md5(String data) {
        return DigestUtils.md5Hex(data);
    }

    //sha1
    public static String sha1(String data) {
        return DigestUtils.sha1Hex(data);
    }

    //sha256Hex
    public static String sha256Hex(String data) {
        return DigestUtils.sha256Hex(data);
    }

    public static String getUuid32() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    public static void main(String[] args) {
        String str = "!@12qazWSX34";
        System.out.println(base64Encode(str));
    }

}
