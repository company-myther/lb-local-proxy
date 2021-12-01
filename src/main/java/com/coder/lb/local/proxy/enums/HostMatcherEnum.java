package com.coder.lb.local.proxy.enums;

/**
 * @author zhuhf
 */
public enum HostMatcherEnum {
    /**
     * 精确匹配
     */
    EXACT_MATCH,
    /**
     * 正则匹配
     */
    REGEX_MATCH,
    /**
     * 子网匹配
     */
    SUBNET_MATCH;
}
