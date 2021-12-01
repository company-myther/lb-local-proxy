package com.example.demo.properties;

import com.example.demo.pojo.RemoteServerInfo;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

/**
 * @author zhuhf
 */
@ConfigurationProperties(prefix = "lb-proxy")
@Data
@Component
public class ServerConfigProperties {
    @NestedConfigurationProperty
    private List<RemoteServerInfo> remoteServerInfoList = new ArrayList<>(0);
    private String bindHost;
    @NotNull
    private Integer port;
    @NotNull
    @NestedConfigurationProperty
    private RemoteServerInfo defaultRemoteServer;
}
