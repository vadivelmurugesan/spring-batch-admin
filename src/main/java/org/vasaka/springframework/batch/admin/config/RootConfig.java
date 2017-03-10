package org.vasaka.springframework.batch.admin.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = "org.vasaka.springframework.batch.admin.service")
public class RootConfig {

}
