package com.example.demo.matcher;

import com.example.demo.enums.HostMatcherEnum;
import org.apache.commons.net.util.SubnetUtils;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * @author zhuhf
 */
@Component
public class SubnetMatcher implements HostMatcher {

    private static final String IP_DOMAIN = "[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}";
    /**
     * IP v6 regex taken from http://stackoverflow.com/questions/53497/regular-expression-that-matches-valid-ipv6-addresses
     */
    private static final String IP_V6_DOMAIN = "(([0-9a-fA-F]{1,4}:){7,7}[0-9a-fA-F]{1,4}|([0-9a-fA-F]{1,4}:){1,7}:|([0-9a-fA-F]{1,4}:){1,6}:[0-9a-fA-F]{1,4}|([0-9a-fA-F]{1,4}:){1,5}(:[0-9a-fA-F]{1,4}){1,2}|([0-9a-fA-F]{1,4}:){1,4}(:[0-9a-fA-F]{1,4}){1,3}|([0-9a-fA-F]{1,4}:){1,3}(:[0-9a-fA-F]{1,4}){1,4}|([0-9a-fA-F]{1,4}:){1,2}(:[0-9a-fA-F]{1,4}){1,5}|[0-9a-fA-F]{1,4}:((:[0-9a-fA-F]{1,4}){1,6})|:((:[0-9a-fA-F]{1,4}){1,7}|:)|fe80:(:[0-9a-fA-F]{0,4}){0,4}%[0-9a-zA-Z]{1,}|::(ffff(:0{1,4}){0,1}:){0,1}((25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])\\.){3,3}(25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])|([0-9a-fA-F]{1,4}:){1,4}:((25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])\\.){3,3}(25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9]))";
    private static final Pattern IP_PATTERN = Pattern.compile("(" + IP_DOMAIN + ")|(" + IP_DOMAIN + ")");
    private final ConcurrentMap<String, SubnetUtils.SubnetInfo> cache = new ConcurrentHashMap<>();

    @Override
    public boolean match(String targetHost, String matchData) {
        if (!IP_PATTERN.matcher(targetHost).find()) {
            return false;
        }
        return cache.computeIfAbsent(matchData, new Function<String, SubnetUtils.SubnetInfo>() {
            @Override
            public SubnetUtils.SubnetInfo apply(String s) {
                return new SubnetUtils(s).getInfo();
            }
        }).isInRange(targetHost);
    }

    @Override
    public HostMatcherEnum[] supports() {
        return new HostMatcherEnum[]{HostMatcherEnum.SUBNET_MATCH};
    }
}
