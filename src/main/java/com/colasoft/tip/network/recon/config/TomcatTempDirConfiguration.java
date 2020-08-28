package com.colasoft.tip.network.recon.config;

import com.colasoft.tip.network.recon.utils.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.MultipartConfigElement;
import java.io.File;

/*
  tomcat临时目录问题处理，解决长时间运行后的临时目录被清理的问题
 */
@Configuration
public class TomcatTempDirConfiguration {

    @Value("${server.tomcat.basedir:/tmp}")
    private String tempPath;

    @Bean
    MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        if (StringUtils.isEmpty(tempPath) || "/tmp".equals(tempPath)) {
            String location = System.getProperty("user.dir") + "/temp";
            File tmpFile = new File(location);
            if (!tmpFile.exists()) {
                tmpFile.mkdirs();
            }
            factory.setLocation(location);
        } else {
            factory.setLocation(tempPath);
        }
        return factory.createMultipartConfig();
    }

}
