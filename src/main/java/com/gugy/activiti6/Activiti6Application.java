package com.gugy.activiti6;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication(exclude ={org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
		org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration.class
})
@ComponentScan({"com.gugy","com.org"})
public class Activiti6Application {

	public static void main(String[] args) {
		SpringApplication.run(Activiti6Application.class, args);
	}

}
