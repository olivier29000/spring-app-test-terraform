package com.example.demo.services;


import com.example.demo.models.UserApp;
import com.example.demo.repositories.UserAppRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class JwtUserDetailsService implements UserDetailsService {

    @Autowired
    private UserAppRepository userAppRepository;

    @Value("${jwt.expires_in}")
    private Integer EXPIRES_IN;

    @Value("${jwt.cookie}")
    private String TOKEN_COOKIE;

    @Value("${jwt.secret}")
    private String secret;



    @Override
    public UserDetails loadUserByUsername(String username) {
        Optional<UserApp> optionalUser = userAppRepository.findByUsername(username);
        if (optionalUser.isEmpty()) {
           // throw new ChronoshareException("aucun compte avec l\'email " + email + " n\'existe");
        }
        UserApp user = optionalUser.get();
        List<GrantedAuthority> roles = new ArrayList<>();
        roles.add(new SimpleGrantedAuthority(user.getRole().toString()));
        UserDetails ud = new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(), true, true, true, true, roles);
        return ud;
    }

}