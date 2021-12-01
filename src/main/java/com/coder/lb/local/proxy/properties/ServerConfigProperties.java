package com.coder.lb.local.proxy.properties;

import com.coder.lb.local.proxy.pojo.RemoteServerInfo;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

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

    private String username;
    private String password;
    private RemoteServerInfo defaultRemoteServer;

    private ServerConfigProperties() {
    }

    private static volatile ServerConfigProperties INSTANCE;

    public static void init(InputStream inputStream) throws IOException {
        if (INSTANCE == null) {
            synchronized (ServerConfigProperties.class) {
                if (INSTANCE == null) {
                    YAMLMapper yamlMapper = new YAMLMapper();
                    yamlMapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
                    INSTANCE = yamlMapper.readValue(inputStream, ServerConfigProperties.class);
                }
            }
        }
    }

    public static ServerConfigProperties init() {
        return INSTANCE;
    }

}
