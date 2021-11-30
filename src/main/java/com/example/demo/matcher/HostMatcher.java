package com.example.demo.matcher;

import com.example.demo.enums.HostMatcherEnum;
import org.springframework.beans.factory.InitializingBean;

import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author zhuhf
 */
public interface HostMatcher extends InitializingBean {
    ConcurrentMap<HostMatcherEnum, HostMatcher> map = new ConcurrentHashMap<>();
    /**
     * 是否匹配
     *
     * @param matchData 匹配使用数据
     * @param targetHost 目的地址
     * @return 匹配 true，否则 false
     */
    boolean match(String targetHost, String matchData);

    HostMatcherEnum[] supports();

    @Override
    default void afterPropertiesSet() throws Exception {
        Arrays.stream(supports())
                .forEach(hostMatcherEnum -> map.put(hostMatcherEnum, this));
    }
}
