package com.example.demo.services;


import com.example.demo.JwtTokenUtil;
import com.example.demo.models.UserApp;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.stream.Stream;

@Service
public class JwtAuthentificationService {


    @Value("${jwt.expires_in}")
    private Integer EXPIRES_IN;

    @Value("${jwt.cookie}")
    private String TOKEN_COOKIE;

    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    @Autowired
    private JwtUserDetailsService UserAppDetailsService;

    public ResponseCookie createAuthenticationToken(String UserAppname, String password ) throws Exception {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(UserAppname,  password));
            final UserDetails userDetails = UserAppDetailsService.loadUserByUsername(UserAppname);
            final String token = jwtTokenUtil.generateToken(userDetails);
            return ResponseCookie.from(TOKEN_COOKIE, token).httpOnly(true)
                    .maxAge(EXPIRES_IN).path("/").build();



        }catch(DisabledException e) {
            throw new Exception();
        }

    }

    public ResponseCookie getAuthenticationToken(UserApp UserApp ) throws Exception {
        try {
            final UserDetails UserAppDetails = UserAppDetailsService.loadUserByUsername(UserApp.getUsername());
            final String token = jwtTokenUtil.generateToken(UserAppDetails);
            return ResponseCookie.from(TOKEN_COOKIE, token).httpOnly(true)
                    .maxAge(EXPIRES_IN).path("/").build();
        }catch(DisabledException e) {
            throw new Exception("UserApp_DISABLED", e);
        }

    }

    public String getEmailFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {

            String token = Stream.of(cookies)
                    .filter(cookie -> cookie.getName().equals(TOKEN_COOKIE))
                    .map(Cookie::getValue)
                    .findFirst()
                    .orElse(null);

            if (token != null) {
                String email = jwtTokenUtil.getUsernameFromToken(token);
                UserDetails userDetails = UserAppDetailsService.loadUserByUsername(email);
                if ( userDetails != null && jwtTokenUtil.validateToken(token, userDetails)) {
                    return email;
                }
            }
        }
        return null;
    }
}