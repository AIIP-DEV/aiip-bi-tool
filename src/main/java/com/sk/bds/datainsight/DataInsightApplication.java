package com.sk.bds.datainsight;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.security.Security;

@SpringBootApplication(
		scanBasePackages={
				"com.sk.bds.datainsight.config",
				"com.sk.bds.datainsight.controller",
				"com.sk.bds.datainsight.exception",
				"com.sk.bds.datainsight.interceptor",
				"com.sk.bds.datainsight.service",
				"com.sk.bds.datainsight.database.dao"
		}
)
@EnableTransactionManagement
public class DataInsightApplication {

	public static void main(String[] args) {
		Security.setProperty("crypto.policy", "unlimited");
		SpringApplication.run(DataInsightApplication.class, args);
	}
}
