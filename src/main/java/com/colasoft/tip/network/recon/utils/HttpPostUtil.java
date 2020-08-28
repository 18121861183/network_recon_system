package com.colasoft.tip.network.recon.utils;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.GZIPInputStream;

public class HttpPostUtil {
    private int timeout;
    private URL url;
    private HttpURLConnection conn;
    private String boundary = "--------" + StringUtils.getUUID32();
    private HashMap<String, String> textParams = new HashMap<>();
    private HashMap<String, File> fileparams = new HashMap<>();
    private DataOutputStream outputStream;
    private String method = "POST";
    private boolean isFormData = true;

    public HttpPostUtil(String url) throws Exception {
        this(url,"POST",30*1000);
    }

    public HttpPostUtil(String url, int timeout) throws Exception {
        this(url,"POST",timeout);
    }


    public HttpPostUtil(String url,String method, int timeout)  {
        this(url,method,"post".equalsIgnoreCase(method),timeout, null);
    }

    public HttpPostUtil(String url,String method,boolean isFormData, int timeout,Map<String,String> headerMap) {
        try {
            this.isFormData = isFormData;
            this.url = new URL(url);
            this.timeout = timeout;
            this.method = method;
            initConnection(headerMap);
            conn.connect();
            if("GET".equals(method)){
                return ;
            }
            outputStream = new DataOutputStream(conn.getOutputStream());
        } catch (Exception e) {
            throw  new RuntimeException(e);
        }
    }



    /**
     * 重新设置要请求的服务器地址，即上传文件的地址。
     *
     * @param url
     * @throws Exception
     */
    public void setUrl(String url) throws Exception {
        this.url = new URL(url);
    }

    /**
     * 增加一个普通字符串数据到form表单数据中
     *
     * @param name
     * @param value
     */
    public void addParameter(String name, String value) {
        textParams.put(name, value);
    }

    /**
     * 增加一个文件到form表单数据中
     *
     * @param name
     * @param value
     */
    public void addParameter(String name, File value) {
        fileparams.put(name, value);
    }
    
    /**
     * 增加一个文件到form表单数据中
     *
     * @param fieldName
     * @param value
     * @throws Exception 
     */
    public void addParameter(String fieldName, byte[] value, String filename) throws Exception {
    	outputStream.writeBytes("--" + boundary + "\r\n");
        outputStream.write(("Content-Disposition: form-data; name=\"" + fieldName + "\"; filename=\"" + filename + "\"\r\n").getBytes("utf-8"));
        outputStream.writeBytes("Content-Type: " + "application/octet-stream" + "\r\n");
        outputStream.writeBytes("\r\n");
        outputStream.write(value);
        outputStream.writeBytes("\r\n");
    }

    /**
     * 发送数据到服务器，返回一个字节包含服务器的返回结果的数组
     *
     * @return
     * @throws Exception
     */
    /**
     * 发送数据到服务器，返回一个字节包含服务器的返回结果的数组
     *
     * @return
     * @throws Exception
     */
    public String send()  {
        int code = 0;
        InputStream in = null;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        String response = "";
        try{
            in = sendRcStream();
            code = conn.getResponseCode();
            if (code > 300 || code < 200) {
                response = "responseCode: " + code + "\r\n; responseInfo: ";
            }
            byte[] buf = new byte[1024 * 8];
            int len;
            while ((len = in.read(buf)) != -1) {
                out.write(buf, 0, len);
            }
            response += new String(out.toByteArray(), "UTF-8");
            response = URLDecoder.decode(response, "UTF-8");
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            conn.disconnect();
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return response;
    }

    
    
    /**
     * 发送数据到服务器，返回一个字节包含服务器的返回结果的数组
     *
     * @return
     * @throws Exception
     */
    public InputStream sendRcStream() throws Exception {
        writeFileParams();
        writeStringParams();
        paramsEnd();
        InputStream in = null;
        String contentEncoding = getHeader("Content-Encoding");
        boolean isZip = "gzip".equals(contentEncoding);
        try {
            if(isZip){
                in = new GZIPInputStream(conn.getInputStream());
            }else{
                in = conn.getInputStream();
            }
        } catch (Exception e) {
            if(isZip){
                in = new GZIPInputStream(conn.getErrorStream());
            }else{
                in = conn.getErrorStream();
            }
        }
        return in;
    }

    public String getHeader(String key){
        return conn.getHeaderField(key);
    }

    public Map<String,List<String>> getHeaderMap(){
        return conn.getHeaderFields();
    }

    public int getResponseCode(){
        try {
            return conn.getResponseCode();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }


    /**
     * 文件上传的connection的一些必须设置
     *
     * @throws Exception
     */
    private void initConnection(Map<String,String> headerMap) throws Exception {
        trustAllHttpsCertificates();
        HostnameVerifier hv = (urlHostName, session) -> true;
        HttpsURLConnection.setDefaultHostnameVerifier(hv);
        conn = (HttpURLConnection) this.url.openConnection();
        conn.setDoOutput(true);
        conn.setUseCaches(false);
        conn.setConnectTimeout(timeout); // 连接超时为10秒
        conn.setRequestProperty("Charsert", "UTF-8");
        if("POST".equalsIgnoreCase(method)){
            conn.setRequestMethod("POST");
            if(isFormData){
                conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            }else{
                conn.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
            }

        }else{
            conn.setRequestMethod(method);
        }

        if(headerMap != null && !headerMap.isEmpty()){
            for(String key : headerMap.keySet()){
                if(headerMap.get(key) == null){
                    continue;
                }
                conn.setRequestProperty(key, headerMap.get(key));
            }
        }
    }

    private static void trustAllHttpsCertificates() throws Exception {
        javax.net.ssl.TrustManager[] trustAllCerts = new javax.net.ssl.TrustManager[1];
        javax.net.ssl.TrustManager tm = new miTM();
        trustAllCerts[0] = tm;
        javax.net.ssl.SSLContext sc = javax.net.ssl.SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, null);
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
    }

    /**
     * 普通字符串数据
     *
     * @throws Exception
     */
    private void writeStringParams() throws Exception {

        if("GET".equals(method)){
            return ;
        }

        Set<String> keySet = textParams.keySet();
        if(isFormData){
            for (Iterator<String> it = keySet.iterator(); it.hasNext(); ) {
                String name = it.next();
                String value = textParams.get(name);
                outputStream.writeBytes("--" + boundary + "\r\n");
                outputStream.writeBytes("Content-Disposition: form-data; name=\"" + name + "\"\r\n");
                outputStream.writeBytes("\r\n");
                outputStream.write(value.getBytes(StandardCharsets.UTF_8));
                outputStream.writeBytes("\r\n");
            }
        }else {
            StringBuilder sb = new StringBuilder();
            for (String key : textParams.keySet()) {
                sb.append(key).append("=").append(URLEncoder.encode(textParams.get(key), "UTF-8")).append("&");
            }
            if(sb.length() > 0){
                sb.deleteCharAt(sb.length()-1);
            }
            outputStream.writeBytes(sb.toString());
        }
    }

    /**
     * 文件数据
     *
     * @throws Exception
     */
    private void writeFileParams() throws Exception {
        if("GET".equals(method)){
            return ;
        }
        Set<String> keySet = fileparams.keySet();
        for (Iterator<String> it = keySet.iterator(); it.hasNext(); ) {
            String name = it.next();
            File value = fileparams.get(name);
            outputStream.writeBytes("--" + boundary + "\r\n");
            outputStream.write(("Content-Disposition: form-data; name=\"" + name + "\"; filename=\"" + value.getName() + "\"\r\n").getBytes("UTF-8"));
            outputStream.writeBytes("Content-Type: " + getContentType(value) + "\r\n");
            outputStream.writeBytes("\r\n");
            outputStream.write(getBytes(value));
            outputStream.writeBytes("\r\n");
        }
    }

    /**
     * 获取文件的上传类型，图片格式为image/png,image/jpeg等。非图片为application /octet-stream
     *
     * @param f
     * @return
     * @throws Exception
     */
    private String getContentType(File f) throws Exception {
        return "application/octet-stream";
    }

    /**
     * 把文件转换成字节数组
     *
     * @param f
     * @return
     * @throws Exception
     */
    private byte[] getBytes(File f) throws Exception {
        FileInputStream in = new FileInputStream(f);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] b = new byte[1024];
        int n;
        while ((n = in.read(b)) != -1) {
            out.write(b, 0, n);
        }
        in.close();
        return out.toByteArray();
    }

    /**
     * 添加结尾数据
     *
     * @throws Exception
     */
    private void paramsEnd() throws Exception {
        if("GET".equals(method)){
            return ;
        }
        if(isFormData){
            outputStream.writeBytes("--" + boundary + "--" + "\r\n");
            outputStream.writeBytes("\r\n");
            outputStream.flush();
        }
    }

    /**
     * 对包含中文的字符串进行转码，此为UTF-8。服务器那边要进行一次解码
     *
     * @param value
     * @return
     * @throws Exception
     */
    private String encode(String value) throws Exception {
        return URLEncoder.encode(value, "UTF-8");
    }

    static class miTM implements javax.net.ssl.TrustManager, javax.net.ssl.X509TrustManager {
        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
            return null;
        }

        public boolean isServerTrusted(java.security.cert.X509Certificate[] certs) {
            return true;
        }

        public boolean isClientTrusted(java.security.cert.X509Certificate[] certs) {
            return true;
        }

        public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType)
                throws java.security.cert.CertificateException {
            return;
        }

        public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType)
                throws java.security.cert.CertificateException {
            return;
        }
    }
}