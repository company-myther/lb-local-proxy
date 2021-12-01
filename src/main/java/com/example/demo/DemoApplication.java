package com.example.demo;

import org.apache.commons.net.util.SubnetUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.nativex.hint.TypeHint;

import java.util.Arrays;

@SpringBootApplication
@TypeHint(typeNames = "org.springframework.boot.context.properties.ConfigurationPropertiesBinder$Factory")
public class DemoApplication {

    public static void main(String[] args) {
        System.out.println(Arrays.toString(args));
        SpringApplication.run(DemoApplication.class, args);
//        System.out.println(new SubnetUtils("10.43.0.0/16").getInfo().isInRange("10.43.245.164"));
    }

}
