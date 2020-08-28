package com.colasoft.tip.network.recon.utils;

import org.slf4j.Logger;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.stream.LogOutputStream;
import org.zeroturnaround.exec.stream.slf4j.Slf4jStream;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.TimeoutException;

public class CommandUtils {

    /**
     * 执行指令但不获取结果
     * @param command 指令    例如 java -version
     */
    public static void justRunCommand(String... command) throws InterruptedException, TimeoutException, IOException {
        new ProcessExecutor().command(command).execute();
    }

    /**
     * 执行指令获取输出结果
     * @param command 指令    例如 java -version
     * @return  执行结果
     */
    public static String runCommandGetResult(String... command) throws InterruptedException, TimeoutException, IOException {
        return new ProcessExecutor().command(command).readOutput(true).execute().outputUTF8();
    }

    /**
     * 执行指令并将结果过程输出到日志中
     * @param logger    日志
     * @param command   指令
     */
    public static void runCommandOutputLogger(Logger logger, String... command) throws InterruptedException, TimeoutException, IOException {
        new ProcessExecutor().command(command).redirectOutput(Slf4jStream.of(logger).asInfo()).execute();
    }

    /**
     * 自定义处理输出
     * @param fos   输出函数
     * @param command   指令
     */
    public static void runCommandOutputLister(OutputStream fos, String... command) throws InterruptedException, TimeoutException, IOException {
        new ProcessExecutor().command(command).redirectOutput(fos).execute();
    }


    public static void main(String[] args) throws IOException, TimeoutException, InterruptedException {

        LogOutputStream logOutputStream = new LogOutputStream() {
            @Override
            protected void processLine(String line) {
                System.out.println(line);
            }
        };
        runCommandOutputLister(logOutputStream, "ping", "192.168.5.242");

    }

}
