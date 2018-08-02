package io.thingsofvalue.edu.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class SwaggerConfig {
	//
	  ApiInfo apiInfo() {
	        return new ApiInfoBuilder()
	            .title("ThingsOfValue")
	            .description("LDCC ThingsOfValue")
	            .license("MIT LICENSE")
	            .licenseUrl("http://www.ldcc.co.kr")
	            .termsOfServiceUrl("")
	            .version("1.0")
	            .contact(new Contact("","", ""))
	            .build();
	    }
	
	@Bean
	public Docket api() {
		return new Docket(DocumentationType.SWAGGER_2)
				.select()
				.apis(RequestHandlerSelectors.basePackage("io.thingsofvalue.edu.controller"))
				.paths(PathSelectors.any()) //swagger에 노출할 api 경로의 패턴 설정 ex) PathSelectors.ant("/api/*")
				.build()
				.apiInfo(apiInfo());
	}
	
	
	
	
	
	
	
}