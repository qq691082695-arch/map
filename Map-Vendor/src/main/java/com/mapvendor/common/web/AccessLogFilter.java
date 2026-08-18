package com.mapvendor.common.web;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class AccessLogFilter extends OncePerRequestFilter {
    private static final Logger ACCESS_LOG = LoggerFactory.getLogger("ACCESS");

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        long startedAt = System.nanoTime();
        try {
            chain.doFilter(request, response);
        } finally {
            long elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedAt);
            // Deliberately exclude query strings, headers, and bodies because they may contain openid, phones, or tokens.
            ACCESS_LOG.info("http_request method={} path={} status={} durationMs={}",
                    request.getMethod(), request.getRequestURI(), response.getStatus(), elapsedMs);
        }
    }
}
