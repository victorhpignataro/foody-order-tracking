package com.foody.order_tracking_server.security;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.foody.order_tracking_server.service.JwtService;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

  public static final String ACCESS_COOKIE = "access_token";

  private final JwtService jwtService;

  public JwtAuthFilter(JwtService jwtService) {
    this.jwtService = jwtService;
  }

  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain) throws ServletException, IOException {

    String token = extractToken(request);

    if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
      try {
        String login = jwtService.extractLogin(token);

        if (login != null) {
          UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
              login, null, Collections.emptyList());
          authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
          SecurityContextHolder.getContext().setAuthentication(authToken);
        }
      } catch (JwtException | IllegalArgumentException e) {
        logger.warn("JWT inválido ou expirado: " + e.getMessage());
      }
    }

    filterChain.doFilter(request, response);
  }

  private String extractToken(HttpServletRequest request) {
    Cookie[] cookies = request.getCookies();
    if (cookies == null) {
      return null;
    }
    return Arrays.stream(cookies)
        .filter(c -> ACCESS_COOKIE.equals(c.getName()))
        .map(Cookie::getValue)
        .findFirst()
        .orElse(null);
  }
}