package com.coder.lb.local.proxy.matcher;

import com.coder.lb.local.proxy.enums.HostMatcherEnum;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * @author zhuhf
 */
@Component
public class ExactMatcher implements HostMatcher {

    @Override
    public boolean match(String targetHost, String matchData) {
        return Objects.equals(targetHost, matchData);
    }

    @Override
    public HostMatcherEnum[] supports() {
        return new HostMatcherEnum[]{HostMatcherEnum.EXACT_MATCH};
    }
}
