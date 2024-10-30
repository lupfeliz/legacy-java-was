/**
 * @File        : OpenAPIConfig.java
 * @Author      : 정재백
 * @Since       : 2024-04-16 
 * @Description : swagger 설정파일
 * @Site        : https://devlog.ntiple.com
 **/
package com.ntiple.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
// import io.swagger.v3.oas.models.security.SecurityRequirement;
// import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;

@OpenAPIDefinition
@Configuration public class OpenAPIConfig {
  @Value("${springdoc.server.url:/}") private String svurl;
  @Value("${springdoc.server.description:기본URL}") private String description;
  @Bean OpenAPI customOpenAPI() {
    // final String secureScheme = "인증전송자(Bearer)";
    List<Server> servers = new ArrayList<>();
    Server server = new Server();
    server.setUrl(svurl);
    server.setDescription(description);
    servers.add(server);
    return new OpenAPI()
      .servers(servers)
      // .addSecurityItem(new SecurityRequirement().addList(secureScheme))
      .components(new Components()
        // .addSecuritySchemes(secureScheme, new SecurityScheme()
        //   .name(secureScheme)
        //   .type(SecurityScheme.Type.HTTP)
        //   .scheme("bearer")
        //   .bearerFormat("JWT"))
        )
      .info(new Info().title("데모 프로그램"));
  }
}