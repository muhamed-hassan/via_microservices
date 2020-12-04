package com.practice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableCaching
//@EnableScheduling
@EnableCircuitBreaker
@EnableDiscoveryClient
@EnableFeignClients
@SpringBootApplication
public class EmployeeApiStarter {

	public static void main(String[] args) {
		SpringApplication.run(EmployeeApiStarter.class, args);
	}

}
