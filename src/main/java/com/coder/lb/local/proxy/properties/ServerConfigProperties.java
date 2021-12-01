package com.coder.lb.local.proxy.properties;

import com.coder.lb.local.proxy.pojo.RemoteServerInfo;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.yaml.snakeyaml.Yaml;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author zhuhf
 */
@Getter
@Setter
@ToString
public class ServerConfigProperties {
    private List<RemoteServerInfo> remoteServerInfoList = new ArrayList<>(0);
    private String bindHost;
    private Integer port;
    private RemoteServerInfo defaultRemoteServer;

    private ServerConfigProperties() {
    }

    private static volatile ServerConfigProperties INSTANCE;
    private static final CountDownLatch cDL = new CountDownLatch(1);

    public static ServerConfigProperties getInstance(InputStream inputStream) throws IOException {
        if (INSTANCE == null) {
            synchronized (ServerConfigProperties.class) {
                if (INSTANCE == null) {
                    YAMLMapper yamlMapper = new YAMLMapper();
                    yamlMapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
                    INSTANCE = yamlMapper.readValue(inputStream, ServerConfigProperties.class);
                    cDL.countDown();
                }
            }
        }
        return INSTANCE;
    }

    public static ServerConfigProperties getInstance() {
        try {
            boolean await = cDL.await(10, TimeUnit.SECONDS);
            if (!await) {
                throw new RuntimeException("等待配置失败");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return INSTANCE;
    }

    @PostConstruct
    public void t() {

        String dump = new Yaml().dump(this);
        System.out.println(dump);
    }

}
