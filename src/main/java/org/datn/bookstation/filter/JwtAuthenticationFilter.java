package org.datn.bookstation.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.datn.bookstation.configuration.JwtUtil;

import java.io.IOException;

/**
 * JWT Authentication Filter
 */
public class JwtAuthenticationFilter implements Filter {
    
    private final JwtUtil jwtUtil;
    
    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        // TODO: Implement JWT authentication logic
        chain.doFilter(request, response);
    }
}
