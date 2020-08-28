package com.colasoft.tip.network.recon.utils;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.apache.tools.tar.TarEntry;
import org.apache.tools.tar.TarInputStream;
import org.apache.tools.tar.TarOutputStream;
import org.hibernate.service.spi.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class FileUtils {

    private static final Logger log = LoggerFactory.getLogger(FileUtils.class);

    /**
     * 读取文本文件
     */
    public static String readFileLineByLine(File file) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        StringBuilder builder = new StringBuilder();
        String temp = "";
        while ((temp = reader.readLine())!=null) {
            builder.append(temp);
        }
        reader.close();
        return builder.toString();
    }


    /**
     * 获取文本行数
     */
    public static Integer getFileLineNumbers(String filepath){
        int lines = 0;
        try {
            File file = new File(filepath);
            if(file.exists()){
                long fileLength = file.length();
                LineNumberReader lineNumberReader = new LineNumberReader(new FileReader(file));
                lineNumberReader.skip(fileLength);
                lines = lineNumberReader.getLineNumber();
                lineNumberReader.close();
                return lines;
            }else {
                System.out.println("File does not exists!");
            }
        }catch(IOException e) {
            e.printStackTrace();
        }
        return lines;
    }

    /**
     * 获取一个文件的md5值(可处理大文件)
     * @return md5 value
     */
    public static String getMD5(File file) {
        FileInputStream fileInputStream = null;
        try {
            MessageDigest MD5 = MessageDigest.getInstance("MD5");
            fileInputStream = new FileInputStream(file);
            byte[] buffer = new byte[8192];
            int length;
            while ((length = fileInputStream.read(buffer)) != -1) {
                MD5.update(buffer, 0, length);
            }
            return new String(Hex.encodeHex(MD5.digest()));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                if (fileInputStream != null){
                    fileInputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * @功能描述 压缩tar.gz 文件
     * @param resourceList 源文件集合
     * @param outPath 目标文件
     * @return 返回压缩结果
     * @throws Exception
     */
    public static void packet(List<File> resourceList, String outPath) throws Exception {
        //1. 参数验证, 初始化输出路径
        if(resourceList == null || resourceList.size() < 1 || StringUtils.isEmpty(outPath)){
            throw new ServiceException("文件压缩执行异常, 非法参数!");
        }
        long startTime = System.currentTimeMillis();
        // 2. 迭代源文件集合, 将文件打包为Tar
        try (FileOutputStream fileOutputStream = new FileOutputStream(outPath+".tmp");
             BufferedOutputStream bufferedOutput = new BufferedOutputStream(fileOutputStream);
             TarOutputStream tarOutputStream = new TarOutputStream(bufferedOutput);) {
            for (File resourceFile : resourceList) {
                if(!resourceFile.isFile()){
                    continue;
                }
                try(FileInputStream fileInputStream = new FileInputStream(resourceFile);
                    BufferedInputStream bufferedInput = new BufferedInputStream(fileInputStream);){
                    TarEntry entry = new TarEntry(new File(resourceFile.getName()));
                    entry.setSize(resourceFile.length());
                    tarOutputStream.putNextEntry(entry);
                    IOUtils.copy(bufferedInput, tarOutputStream);
                } catch (Exception e) {
                    throw new ServiceException("文件["+resourceFile+"]压缩执行异常, 嵌套异常: \n" + e.toString());
                } finally {
                    tarOutputStream.closeEntry();
                }
            }
        } catch (Exception e) {
            Files.delete(Paths.get(outPath+".tmp"));
            throw new ServiceException("文件压缩至["+outPath+"]执行异常, 嵌套异常: \n" + e.toString());
        }

        //3. 读取打包好的Tar临时文件文件, 使用GZIP方式压缩
        try (FileInputStream fileInputStream = new FileInputStream(outPath+".tmp");
             BufferedInputStream bufferedInput = new BufferedInputStream(fileInputStream);
             FileOutputStream fileOutputStream = new FileOutputStream(outPath);
             GZIPOutputStream gzipOutputStream = new GZIPOutputStream(fileOutputStream);
             BufferedOutputStream bufferedOutput = new BufferedOutputStream(gzipOutputStream);
        ) {
            byte[] cache = new byte[1024];
            for (int index = bufferedInput.read(cache); index != -1; index = bufferedInput.read(cache)) {
                bufferedOutput.write(cache,0,index);
            }
            long endTime = System.currentTimeMillis();
            log.info("文件["+outPath+"]压缩执行完毕, 耗时:" + (endTime - startTime) + "ms");
        } catch (Exception e) {
            throw new ServiceException("文件压缩至["+outPath+"]执行异常, 嵌套异常: \n" + e.toString());
        }finally {
            Files.delete(Paths.get(outPath+".tmp"));
        }
    }

    /**
     * @功能描述 解压tar.gz文件
     * @param targzFile tar.gz压缩文件
     * @param outPath 存放解压后文件的目录
     * @return 返回结果
     * @throws ServiceException
     */
    public static void unpack(File targzFile, String outPath) throws ServiceException {
        //1. 验证参数, 初始化输出路径
        if(targzFile == null || !targzFile.isFile() || StringUtils.isEmpty(outPath)){
            throw new ServiceException("文件解压缩执行异常, 非法参数!");
        }
        long startTime = System.currentTimeMillis();
        //2. 读取tar.gz文件转换为tar文件
        try (FileInputStream fileInputStream = new FileInputStream(targzFile);
             BufferedInputStream bins = new BufferedInputStream(fileInputStream);
             GZIPInputStream gzipInputStream = new GZIPInputStream(bins);
             TarInputStream tarIn = new TarInputStream(gzipInputStream, 1024 * 2)) {
            //3. 迭代tar文件集合, 解压文件
            for (TarEntry entry = tarIn.getNextEntry(); entry != null; entry = tarIn.getNextEntry()){
                File targetFileName = new File(outPath + "/" + entry.getName());
                IOUtils.copy(tarIn, new FileOutputStream(targetFileName));
            }
            long endTime = System.currentTimeMillis();
            log.info("文件["+targzFile+"]解压执行完毕, 耗时:" + (endTime - startTime) + "ms");
        } catch (Exception e) {
            throw new ServiceException("[" + targzFile + "] 解压执行异常, " + e.toString());
        }
    }


    /**
     * @功能描述 压缩tar.gz 文件
     * @param sources 源文件集合
     * @param outPath 目标文件名称 无后缀的 例子 G:\backup\logstash-2020.04.22
     * @return 返回压缩结果
     * @throws Exception
     */
    public static void packet(String[] sources, String outPath) throws Exception {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        TarArchiveOutputStream tarArchiveOutputStream = null;
        GZIPOutputStream gzipOutputStream = null;
        try {
            tarArchiveOutputStream = new TarArchiveOutputStream(byteArrayOutputStream);
            // 将所有文件打包成 tar文件
            try {
                for (String source : sources) {
                    File file = new File(source);
                    tarArchiveOutputStream.putArchiveEntry(new TarArchiveEntry(file, file.getName()));
                    IOUtils.copy(new FileInputStream(file), tarArchiveOutputStream);
                    tarArchiveOutputStream.closeArchiveEntry();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                tarArchiveOutputStream.flush();
                tarArchiveOutputStream.close();
            }
            gzipOutputStream = new GZIPOutputStream(new FileOutputStream(outPath));
            gzipOutputStream.write(byteArrayOutputStream.toByteArray());
        } finally {
            byteArrayOutputStream.close();
            if(gzipOutputStream != null) {
                gzipOutputStream.flush();
                gzipOutputStream.close();
            }
        }
    }


    public static void main(String[] args) throws Exception {
        String[] files = new String[]{"/root/test111.csv", "/root/下载/uiso9_cn.exe"};
        String out = "/root/test.tar.gz";
        packet(files, out);
    }

    public static byte[] getFileBytes(String reportPath) throws IOException {
        return Files.readAllBytes(Paths.get(reportPath));
    }
}
