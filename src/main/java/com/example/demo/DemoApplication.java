package com.example.demo;

import org.apache.commons.net.util.SubnetUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
//        System.out.println(new SubnetUtils("10.43.0.0/16").getInfo().isInRange("10.43.245.164"));
    }

}
