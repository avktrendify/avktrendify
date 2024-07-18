package com.github.hkzorman.avakinitemdb.configuration;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;
import java.util.Base64;

public class CustomFilter extends GenericFilterBean {
    @Autowired
    private AuthenticationManager authManager;

    public CustomFilter(AuthenticationManager authManager) {
        //super(authManager);
        this.authManager = authManager;
    }
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;

        String auth = httpRequest.getHeader("Authorization");
        if (auth != null) {
            // Parse the Basic Auth username and password
            String[] credentials = new String(Base64.getDecoder().decode(auth.substring(6))).split(":");
            if (credentials[1].equals("undefined")) credentials[1] = "dummy";
            UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(credentials[0], credentials[1]);

            // Authenticate the user
            Authentication authentication = authManager.authenticate(token);
            // Set the authentication in the SecurityContextHolder
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        chain.doFilter(request, response);
    }
}