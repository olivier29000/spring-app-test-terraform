package com.example.demo.controllers;


import com.example.demo.models.DTO.ChangementMotDePasseDTO;
import com.example.demo.models.DTO.EmailSupportDTO;
import com.example.demo.models.DTO.UserAppCreationDTO;
import com.example.demo.models.DTO.UserConnectedDTO;
import com.example.demo.models.UserApp;
import com.example.demo.services.JwtAuthentificationService;
import com.example.demo.services.UserAppService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/back/user")
public class UserAppController {
    @Value("${redirect.url}")
    private String redirectUrl;

    @Autowired
    private UserAppService userAppService;
    @Autowired
    JwtAuthentificationService jwtAuthentificationService;


    @GetMapping("/verifAuthenticate")
    public ResponseEntity<UserConnectedDTO> verifAuthenticate(HttpServletRequest request) throws Exception {
        UserConnectedDTO userConnected = userAppService.getUserConnectedDTO(request);
        return ResponseEntity.ok(userConnected);
    }

    @PostMapping("/register")
    public ResponseEntity<?> creationCompte(@RequestBody UserAppCreationDTO userCreationDTO) throws Exception {

        userAppService.registerProf(userCreationDTO);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserAppCreationDTO userCreationDTO) throws Exception {

        return userAppService.login(userCreationDTO);
    }

    @GetMapping("/oubli-mot-de-passe/{email}")
    public ResponseEntity<?> oubliMotDePasse(@PathVariable String email) throws Exception {

        return userAppService.mailOubliMotDePasse(email);
    }

    @PostMapping("/changement-mot-de-passe")
    public ResponseEntity<?> changementMotDePasse(@RequestBody ChangementMotDePasseDTO changementMotDePasseDTO) throws Exception {

        return userAppService.changementMotDePasse(changementMotDePasseDTO);
    }

    @GetMapping("/confirm-email/{tokenConfirmation}")
    public RedirectView confirmEmail(@PathVariable String tokenConfirmation, HttpServletResponse response) throws Exception {
        Optional<UserApp> optionalUsers =  userAppService.findUserByTokenEnabled(tokenConfirmation);
        if(optionalUsers.isPresent()) {
            UserApp userApp = optionalUsers.get();
            userApp = userAppService.enableTokenConfirmation(userApp);

            ResponseCookie responseCookie =  jwtAuthentificationService.getAuthenticationToken(userApp);
            response.addHeader(HttpHeaders.SET_COOKIE, responseCookie.toString());
            return new RedirectView(redirectUrl);
        }else{
            return new RedirectView(redirectUrl);
        }
    }


}
