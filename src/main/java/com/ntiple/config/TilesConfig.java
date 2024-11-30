/**
 * @File        : TilesConfig.java
 * @Author      : 정재백
 * @Since       : 2024-10-26 
 * @Description : Tiles 설정
 * @Site        : https://devlog.ntiple.com
 **/
package com.ntiple.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.view.tiles3.TilesConfigurer;
import org.springframework.web.servlet.view.tiles3.TilesView;
import org.springframework.web.servlet.view.tiles3.TilesViewResolver;

@Configuration public class TilesConfig {
  @Bean TilesConfigurer tilesConfigurer() {
    final TilesConfigurer configurer = new TilesConfigurer();
    /** 해당 경로에 tiles.xml 파일을 넣음 */
    configurer.setDefinitions(new String[] { "/WEB-INF/tiles.xml" });
    configurer.setCheckRefresh(true);
    return configurer;
  }

  @Bean TilesViewResolver tilesViewResolver() {
    final TilesViewResolver tilesViewResolver = new TilesViewResolver();
    tilesViewResolver.setViewClass(TilesView.class);
    // tilesViewResolver.setOrder(0);
    return tilesViewResolver;
  }
}