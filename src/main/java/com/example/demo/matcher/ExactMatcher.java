package com.example.demo.matcher;

import com.example.demo.enums.HostMatcherEnum;
import com.example.demo.properties.ServerConfigProperties;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * @author zhuhf
 */
@Component
public class ExactMatcher implements HostMatcher {
    private final ServerConfigProperties serverConfigProperties;

    public ExactMatcher(ServerConfigProperties properties) {
        serverConfigProperties = properties;
    }

    @Override
    public boolean match(String targetHost, String matchData) {
        return Objects.equals(targetHost, matchData);
    }

    @Override
    public HostMatcherEnum[] supports() {
        return new HostMatcherEnum[]{HostMatcherEnum.EXACT_MATCH};
    }
}
