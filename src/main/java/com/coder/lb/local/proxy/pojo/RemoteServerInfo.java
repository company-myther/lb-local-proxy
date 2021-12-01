package com.coder.lb.local.proxy.pojo;

import com.coder.lb.local.proxy.enums.HostMatcherEnum;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

/**
 * @author zhuhf
 */

@Getter
@Setter
@ToString
public class RemoteServerInfo {

    @NotNull
    private String host;

    @NotNull
    private Integer port;

    @NotNull
    @NotEmpty
    private List<HostMatcherEnum> hostMatcher = new ArrayList<>();

    @NotNull
    private String matchData;

    private String username;
    private String password;
}
