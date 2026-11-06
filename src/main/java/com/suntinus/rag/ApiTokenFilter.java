package com.suntinus.rag;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;
@Component
public class ApiTokenFilter extends OncePerRequestFilter {
    private final byte[] expected;
    public ApiTokenFilter(@Value("${lab.api-token}") String token){
        if(token.length()<32)throw new IllegalArgumentException("RAG_API_TOKEN must have at least 32 characters");
        expected=("Bearer "+token).getBytes(StandardCharsets.UTF_8);
    }
    @Override protected void doFilterInternal(HttpServletRequest request,HttpServletResponse response,FilterChain chain) throws ServletException,IOException {
        response.setHeader("Cache-Control","no-store");
        response.setHeader("X-Content-Type-Options","nosniff");
        if(request.getMethod().equals("GET") && request.getRequestURI().equals("/actuator/health")){chain.doFilter(request,response);return;}
        String supplied=request.getHeader("Authorization");
        if(supplied==null || !MessageDigest.isEqual(expected,supplied.getBytes(StandardCharsets.UTF_8))){response.sendError(401);return;}
        if(request.getContentLengthLong()>1_000_000){response.sendError(413);return;}
        chain.doFilter(request,response);
    }
}
