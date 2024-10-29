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
import io.swagger.v3.oas.models.servers.Server;

@OpenAPIDefinition
@Configuration public class OpenAPIConfig {
  @Value("${springdoc.server.url:/}") private String svurl;
  @Value("${springdoc.server.description:기본URL}") private String description;
  @Bean OpenAPI customOpenAPI() {
    List<Server> servers = new ArrayList<>();
    Server server = new Server();
    server.setUrl(svurl);
    server.setDescription(description);
    servers.add(server);
    return new OpenAPI()
      .servers(servers)
      .components(new Components())
      .info(new Info().title("데모 프로그램"));
  }
}