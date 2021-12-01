package com.coder.lb.local.proxy;

import com.coder.lb.local.proxy.pojo.RemoteServerInfo;
import com.coder.lb.local.proxy.properties.ServerConfigProperties;
import org.apache.commons.cli.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.io.FileSystemResource;
import org.springframework.nativex.hint.TypeHint;

import java.io.IOException;

@SpringBootApplication
@TypeHint(typeNames = "org.springframework.boot.context.properties.ConfigurationPropertiesBinder$Factory",
        types = {ServerConfigProperties.class, RemoteServerInfo.class})
public class DemoApplication {

    public static void main(String[] args) throws IOException {
        Options options = new Options();

        Option input = new Option("c", "config-file", true, "config file path");
        input.setRequired(true);
        options.addOption(input);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        //not a good practice, it serves it purpose
        CommandLine cmd = null;
        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("utility-name", options);

            System.exit(1);
        }
        String configFilePath = cmd.getOptionValue(input.getOpt());
        ServerConfigProperties.getInstance(new FileSystemResource(configFilePath).getInputStream());
        SpringApplication.run(DemoApplication.class, args);
//        System.out.println(new SubnetUtils("10.43.0.0/16").getInfo().isInRange("10.43.245.164"));
    }

}
