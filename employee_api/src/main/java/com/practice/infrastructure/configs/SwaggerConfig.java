package com.practice.infrastructure.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.base.Optional;

import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.OperationBuilderPlugin;
import springfox.documentation.spi.service.contexts.OperationContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class SwaggerConfig {

	@Bean
	public Docket api() {
		return new Docket(DocumentationType.SWAGGER_2)
					.useDefaultResponseMessages(false)
					.select()
					.apis(RequestHandlerSelectors.withClassAnnotation(RestController.class))
					.paths(PathSelectors.any())
					.build()
					.apiInfo(apiEndPointsInfo());
	}

	private ApiInfo apiEndPointsInfo() {
		return new ApiInfoBuilder()
					.title("Employee API")
					.description("Handling CRUD operation of employees data")
					.contact(new Contact("Mohamed Hassan", "www.example-via.net", "example-via@via.com"))
					.license("Sample License")
					.licenseUrl("http://www.example-license.org/LICENSE.html")
					.version("1.0")
					.build();
	}

	@Component // workaround to remove the default 200 message if it is not needed
	public class OkStatusCodeFilteringPlugin implements OperationBuilderPlugin {

		@Override
		public void apply(OperationContext operationContext) {
			Optional<ApiResponses> apiResponsesOptional = operationContext.findAnnotation(ApiResponses.class);
			if (apiResponsesOptional.isPresent()) {
				ApiResponses apiResponses = apiResponsesOptional.get();
				ApiResponse[] expectedResponses = apiResponses.value();
				boolean has200 = false;
				for (ApiResponse apiResponse : expectedResponses) {
					if (apiResponse.code() == HttpStatus.OK.value()) {
						has200 = true;
						break;
					}
				}
				if (!has200) {
					operationContext
						.operationBuilder()
						.build()
						.getResponseMessages()
						.removeIf(responseMessage -> responseMessage.getCode() == HttpStatus.OK.value());
				}
			}
		}

		@Override
		public boolean supports(DocumentationType documentationType) {
			return true;
		}

	}

}
