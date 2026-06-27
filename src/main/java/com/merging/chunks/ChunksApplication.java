package com.merging.chunks;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class ChunksApplication {

	public static void main(String[] args) {
		SpringApplication.run(ChunksApplication.class, args);
	}

}
