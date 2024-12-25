/**
 * @File        : SecurityConfig.java
 * @Author      : 정재백
 * @Since       : 2024-10-26 
 * @Description : Spring-Security 설정
 * @Site        : https://devlog.ntiple.com
 **/
package com.ntiple.config;

import static com.ntiple.commons.ConvertUtil.asList;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSessionEvent;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.filter.GenericFilterBean;

import lombok.extern.slf4j.Slf4j;

@Slf4j @Configuration @EnableWebSecurity
public class SecurityConfig {

  @Autowired private CorsFilter corsFilter;
  @Autowired private AuthFilter authFilter;
  @Autowired private JwtAuthenticationEntryPoint authPoint;
  @Autowired private JwtAccessDeniedHandler authHandler;

  /** URL 패턴매칭 */
  private static AntPathRequestMatcher matcher(HttpMethod m, String path) {
    if (m != null) {
      return new AntPathRequestMatcher(path, m.name());
    } else {
      return new AntPathRequestMatcher(path);
    }
  }

  @Bean SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    /** 전체공개 (인증없이 접근 가능한 목록) */
    List<RequestMatcher> reqPubLst = new ArrayList<>();
    /** 회원 사용자만 허용 */
    List<RequestMatcher> reqMbrLst = new ArrayList<>();
    /** 웹리소스 */
    List<RequestMatcher> reqWebLst = new ArrayList<>();

    reqPubLst.addAll(asList(
      matcher(GET, "/"),
      matcher(GET, "/smp/**"),
      matcher(GET, "/pbl/**"),
      matcher(POST, "/smp/**"),
      matcher(GET, "/assets/**"),
      /** GET /api/cmn/* (공용API) */
      matcher(GET, "/api/cmn/**"),
      /** GET /api/usr/usr01001a01/** (마이페이지) */
      matcher(GET, "/api/usr/usr01001/**"),
      /** PUT /api/usr/** (회원가입) */
      matcher(PUT, "/api/usr/**"),
      /** POST /api/lgn/ (로그인) */
      matcher(POST, "/api/lgn/**"),
      /** POST /api/atc/atc01001/ (게시물 검색) */
      matcher(POST, "/api/atc/atc01001"),
      /** POST /api/atc/atc01001/ (게시물 상세조회) */
      matcher(GET, "/api/atc/atc01001/**"),
      /** H2DB웹콘솔 */
      matcher(null, "/h2-console/**"),
      /** 스웨거(OPENAPI) */
      matcher(null, "/swagger/**")
      // matcher(null, "/swagger/swagger-ui/**"),
      // matcher(null, "/swagger/swagger-resources/**"),
      // matcher(null, "/swagger/v3/api-docs/**")
      // matcher(null, "/swagger-ui/**"),
      // matcher(null, "/swagger-resources/**"),
      // matcher(null, "/v3/api-docs/**")
    ));

    /** 샘플 API 추가 */
    reqPubLst.add(
      /** /api/smp/* (샘플API) */
      matcher(null, "/api/smp/**")
    );

    /** 기타 /api 로 시작되는 모든 리퀘스트 들은 권한 필요 */
    reqMbrLst.addAll(asList(
      matcher(null, "/api/**")
    ));

    // reqWebLst.addAll(asList(
    //   /** 기타 GET 메소드로 접근하는 모든 웹 리소스 URL */
    //   matcher(GET, "/**")
    // ));

    final RequestMatcher[] reqPub = reqPubLst.toArray(new RequestMatcher[]{ });
    final RequestMatcher[] reqMbr = reqMbrLst.toArray(new RequestMatcher[]{ });
    final RequestMatcher[] reqWeb = reqWebLst.toArray(new RequestMatcher[]{ });
    log.debug("PUBLIC-ALLOWED:{}{}", "", reqPub);
    http
      /** token을 사용하는 방식이므로 csrf disable */ 
      .csrf(csrf -> csrf.disable())
      .addFilterBefore(corsFilter, UsernamePasswordAuthenticationFilter.class)
      .addFilterBefore(authFilter, UsernamePasswordAuthenticationFilter.class)
      .exceptionHandling(exh -> exh
        /** 인증 실패 핸들링 */
        .authenticationEntryPoint(authPoint)
        /** 권한인가실패 핸들링 */
        .accessDeniedHandler(authHandler)
      )
      .headers(hdr ->
        hdr.frameOptions(frm -> frm.sameOrigin())
          /** 동일 사이트 referer */
          .referrerPolicy(ref -> ref.policy(ReferrerPolicy.SAME_ORIGIN))
          /** xss 보호 */
          .xssProtection(xss -> xss.disable())
      )
      /** 세션 사용 */
      .sessionManagement(mng -> mng
        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
        .maximumSessions(1)
        .maxSessionsPreventsLogin(true)
        .sessionRegistry(sessionRegistry())
      )
      /** URI별 인가설정 */
      .authorizeHttpRequests(req -> req
        // .anyRequest().permitAll()
        /** 전체공개 (인증없이 접근 가능한 목록) */
        .requestMatchers(reqPub).permitAll()
        /** 회원 사용자만 허용 */
        .requestMatchers(reqMbr).hasAnyAuthority("ROLE_USER")
        /** 웹리소스 */
        .requestMatchers(reqWeb).permitAll()
        .anyRequest()
          .hasAnyAuthority("ROLE_USER", "ROLE_ADMIN")
      )
      /* 관리자, 예비사용자, 사용자, 협력사 */
      // /** 폼 로그인 불가 */
      // .formLogin(login -> login.disable())
      // /** 폼 로그아웃 불가 */
      // .logout(logout -> logout.disable())
      /** 임의유저 불가 */
      .anonymous(anon -> anon.disable())
      ;
    SecurityFilterChain ret = http.build();
    return ret;
  }

  @Component public static class AuthFilter extends GenericFilterBean {
    /** 토큰발급기 */
    @Override public void doFilter(ServletRequest sreq, ServletResponse sres, FilterChain chain)
      throws IOException, ServletException {
      HttpServletRequest req = (HttpServletRequest) sreq;
      req.setAttribute(HttpServletResponse.class.getName(), sres);
      chain.doFilter(sreq, sres);
    }
  }

  /** 인증 실패 핸들링 */
  @Component public static class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
    @Override public void commence(HttpServletRequest req, HttpServletResponse res,
      AuthenticationException e) throws IOException, ServletException {
      /** TODO: 페이지 리퀘스트인 경우 인증오류 또는 로그인 페이지로 인도 */
      log.debug("AUTH-ERR:{} / {} / {}", req.getRequestURI(), req.getContentType(),  e.getMessage());
      res.sendError(HttpStatus.UNAUTHORIZED.value(), HttpStatus.UNAUTHORIZED.name());
    }
  }

  /** 권한인가실패 핸들링 */
  @Component public static class JwtAccessDeniedHandler implements AccessDeniedHandler {
    @Override public void handle(HttpServletRequest req, HttpServletResponse res,
      AccessDeniedException e) throws IOException, ServletException {
      log.debug("ACCESS-DENIED:{}", e.getMessage());
      res.sendError(HttpStatus.FORBIDDEN.value(), HttpStatus.FORBIDDEN.name());
    }
  }

  /** Cross Origin Resource Sharing 필터링 (일단은 전부허용)  */
  @Configuration public static class CorsFilterConfig {
    @Bean CorsFilter corsFilter() {
      UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
      CorsConfiguration cfg = new CorsConfiguration();
      cfg.setAllowCredentials(true);
      cfg.addAllowedOriginPattern("*");
      cfg.addAllowedHeader("*");
      cfg.addAllowedMethod("*");
      source.registerCorsConfiguration("/api/**", cfg);
      return new CorsFilter(source);
    }
  }

  @Bean WebSecurityCustomizer webSecurityCustomizer() {
    return (web) -> web.ignoring()
      .requestMatchers(PathRequest.toStaticResources().atCommonLocations())
      .antMatchers("/assets");
  }

  @Bean
  SessionRegistry sessionRegistry() {
    return new SessionRegistryImpl() {
      @Override
      public void registerNewSession(String sessionId, Object principal) {
        log.debug("================================================================================");
        log.debug("SESSION-CREATED!!!");
        super.registerNewSession(sessionId, principal);
      }
    };
  }

  @Bean
  static ServletListenerRegistrationBean<HttpSessionEventPublisher> httpSessionEventPublisher() {
    return new ServletListenerRegistrationBean<>(new HttpSessionEventPublisher() {
      @Override public void sessionCreated(HttpSessionEvent event) {
        log.debug("================================================================================");
        log.debug("SESSION-CREATED!!!");
        super.sessionCreated(event);
      }
    });
  }
}