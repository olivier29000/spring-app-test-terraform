package com.example.demo;


import com.example.demo.services.JwtUserDetailsService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {
    @Value("${jwt.cookie}")
    private String TOKEN_COOKIE;
    @Value("${jwt.secret}")
    private String SECRET;
    @Autowired
    private JwtUserDetailsService jwtUserDetailsService;
    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {

        if (req.getCookies() != null) {
            Stream.of(req.getCookies()).filter(cookie -> cookie.getName().equals(TOKEN_COOKIE)).map(Cookie::getValue)
                    .forEach(token -> {
                        try {
                            Claims body = Jwts.parser().setSigningKey(SECRET).parseClaimsJws(token).getBody();
                            List<SimpleGrantedAuthority> authorities = Arrays.stream(body.get("roles", String.class).split(","))
                                    .map(SimpleGrantedAuthority::new).toList();
                            UserDetails userDetails = jwtUserDetailsService.loadUserByUsername(body.getSubject());

                            if (jwtTokenUtil.validateToken(token, userDetails)) {
                                UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                                        userDetails, null, userDetails.getAuthorities());
                                usernamePasswordAuthenticationToken
                                        .setDetails(new WebAuthenticationDetailsSource().buildDetails(req));
                                SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
                            }
                        } catch (ExpiredJwtException e) {
                            // Log the exception
                            System.out.println("JWT expired: " + e.getMessage());
                            // Remove the cookie
                            Cookie expiredCookie = new Cookie(TOKEN_COOKIE, null);
                            expiredCookie.setPath("/");
                            expiredCookie.setHttpOnly(true);
                            expiredCookie.setMaxAge(0); // Set the cookie's max age to 0 to delete it
                            res.addCookie(expiredCookie);
                        }
                    });
        }
        chain.doFilter(req, res);
    }

}
