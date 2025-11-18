package org.opendevstack.apiservice.core;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = { "org.opendevstack.apiservice" })
public class DevstackApiServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(DevstackApiServiceApplication.class, args);
	}

}
