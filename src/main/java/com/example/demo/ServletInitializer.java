package com.example.demo;

import java.io.File;
import java.io.IOException;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

public class ServletInitializer extends SpringBootServletInitializer {

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(DemoApplication.class);
	}

	public static void main(String[] args) throws IOException {
		String fileName = "C:\\Users\\Marko_Biljuric\\Downloads\\Java_mid_level_backend_exercise\\prices\\";

		File cryptoFolder = new File(fileName);
        for(File cryptoFile : cryptoFolder.listFiles()) {
            System.out.println(cryptoFile);
        }
	}

}
