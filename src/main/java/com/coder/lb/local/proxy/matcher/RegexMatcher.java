package com.coder.lb.local.proxy.matcher;

import com.coder.lb.local.proxy.enums.HostMatcherEnum;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * @author zhuhf
 */
@Component
public class RegexMatcher implements HostMatcher {
    private final ConcurrentMap<String, Pattern> cache = new ConcurrentHashMap<>();

    @Override
    public boolean match(String targetHost, String matchData) {
        Pattern pattern = cache.computeIfAbsent(matchData, new Function<String, Pattern>() {
            @Override
            public Pattern apply(String s) {
                return Pattern.compile(s);
            }
        });
        return pattern.matcher(targetHost).find();
    }

    @Override
    public HostMatcherEnum[] supports() {
        return new HostMatcherEnum[]{HostMatcherEnum.REGEX_MATCH};
    }
}
