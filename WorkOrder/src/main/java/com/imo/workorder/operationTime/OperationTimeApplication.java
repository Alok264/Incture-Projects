package com.imo.workorder.operationTime;

import javax.sql.DataSource;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Primary;

@SpringBootApplication
@ComponentScan(basePackages = "com.imo.*")
public class OperationTimeApplication {

	@Value("${spring.datasource.url}")
	private String dataSourceUrl;

	@Value("${spring.datasource.username}")
	private String dataSourceUsername;

	@Value("${spring.datasource.password}")
	private String dataSourcePassword;

	@Value("${spring.datasource.driver-class-name}")
	private String dataSourceDriver;

	public static void main(String[] args) {
		SpringApplication.run(OperationTimeApplication.class, args);
	}

	@Bean
	@Primary
	public DataSource getDataSource() {

		DataSourceBuilder dataSourceBuilder = DataSourceBuilder.create();
		dataSourceBuilder.driverClassName(dataSourceDriver);
		dataSourceBuilder.url(dataSourceUrl);
		dataSourceBuilder.username(dataSourceUsername);
		dataSourceBuilder.password(dataSourcePassword);
		return dataSourceBuilder.build();
	}

	// model mapper is used to map the 2 similar dto/model
	@Bean
	public ModelMapper modelMapper() {
		return new ModelMapper();
	}

}
