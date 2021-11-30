package com.example.demo.pojo;

import com.example.demo.enums.HostMatcherEnum;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

/**
 * @author zhuhf
 */
@Data
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
}
