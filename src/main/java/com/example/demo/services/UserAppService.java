package com.example.demo.services;

import com.example.demo.models.AppException;
import com.example.demo.models.DTO.ChangementMotDePasseDTO;
import com.example.demo.models.DTO.UserAppCreationDTO;
import com.example.demo.models.DTO.UserConnectedDTO;
import com.example.demo.models.Role;
import com.example.demo.models.TokenModifMotDePasse;
import com.example.demo.models.UserApp;
import com.example.demo.repositories.TokenModifMotDePasseRepository;
import com.example.demo.repositories.UserAppRepository;
import com.example.demo.utils.UtilsComponent;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;


@Service
public class UserAppService {

    @Autowired
    private PasswordEncoder bcrypt;
    @Autowired
    UserAppRepository userAppRepository;
    @Autowired
    private JwtUserDetailsService userDetailsService;
    @Autowired
    private JwtAuthentificationService jwtAuthentificationService;
    @Autowired
    private TokenModifMotDePasseRepository tokenModifMotDePasseRepository;
    @Autowired
    private EmailService emailService;

    public UserApp getUserAppFromCookie(HttpServletRequest request) throws Exception {
        String username = jwtAuthentificationService.getEmailFromCookie(request);
        Optional<UserApp> optionalUser = userAppRepository.findByUsername(username);
        if(optionalUser.isPresent()){
            UserApp userApp = optionalUser.get();
            // userRepository.updateLastConnexion(user.getId(), LocalDateTime.now());
            return userApp;
        }
        throw new AppException("aucun user connu");
    }

    public ResponseEntity<?> mailOubliMotDePasse(String email){
        Optional<UserApp> optUser = userAppRepository.findByUsername(email);
        if(optUser.isEmpty()){
            return ResponseEntity.ok().build();
        }
        UserApp userApp = optUser.get();
        TokenModifMotDePasse tokenModifMotDePasse = tokenModifMotDePasseRepository.save(new TokenModifMotDePasse(
                userApp,
                UtilsComponent.generateRandomString(10),
                LocalDateTime.now().plusHours(2)
        )) ;
        try {
            emailService.sendEmailOubliMotDePasse(tokenModifMotDePasse);
        }catch (Exception e){
            System.out.println("L'email pour " + tokenModifMotDePasse.getUserApp().getUsername() + " n'a pas pu être envoyé");
        }


        return ResponseEntity.ok().build();
    }

    public ResponseEntity<UserConnectedDTO> login(UserAppCreationDTO userCreationDTO) throws Exception {
        // Charger les détails de l'utilisateur par le nom d'utilisateur
        UserDetails userDetails = userDetailsService.loadUserByUsername(userCreationDTO.getUsername());
        if(!userDetails.isEnabled()){
            throw new AppException(userCreationDTO.getUsername() + " n'a pas été validé via l\'email envoyé");
        }
        // Vérifier si le mot de passe est correct
        if (userDetails != null && bcrypt.matches(userCreationDTO.getPassword(), userDetails.getPassword())) {
            // Retourner la réponse avec le token
            ResponseCookie tokenCookie = jwtAuthentificationService.createAuthenticationToken(userDetails.getUsername(), userCreationDTO.getPassword());
            Optional<UserApp>  optUser = userAppRepository.findByUsername(userDetails.getUsername());
            if(optUser.isPresent()){
                UserConnectedDTO userConnected = new UserConnectedDTO(optUser.get().getUsername());
                return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, tokenCookie.toString()).body(userConnected);
            }else {
                throw new AppException("Erreur anormale pour " + userCreationDTO.getUsername());
            }

        } else {
            // Retourner une réponse d'erreur en cas d'échec de l'authentification
            throw new AppException("Mauvais mot de passe pour " + userCreationDTO.getUsername());
        }
    }

    public void registerProf(UserAppCreationDTO userCreationDTO) throws Exception {
        Optional<UserApp> optionalUser = userAppRepository.findByUsername(userCreationDTO.getUsername());
        if(optionalUser.isPresent()){
            if(optionalUser.get().isEnabled()){
                throw new AppException("L'adresse email " + userCreationDTO.getUsername() + " est déjà utilisée et a été confirmée");
            }else{
                throw new AppException("L'adresse email " + userCreationDTO.getUsername() + " doit être confirmée par l'email reçu, vérifiez vos spams");
            }
        }

        UserApp userApp = userAppRepository.save(new UserApp(userCreationDTO.getUsername(),bcrypt.encode(userCreationDTO.getPassword()), Role.PROF, false)) ;

        emailService.sendEmailCreationCompte(userApp.getUsername(),  "activation de votre compte", "www.ndfrais.pro/back/user/confirm-email/" +  userApp.getTokenEnabled());
        emailService.sendEmail("lasbleis.olivier@yahoo.fr",
                userApp.getUsername() + " vient de créer un compte sur ndfrais.pro",
                "Nouvelle inscription",
                userApp.getUsername() + " vient de créer un compte sur ndfrais.pro");
    }

    public UserConnectedDTO getUserConnectedDTO(HttpServletRequest request) throws Exception {
        String username = jwtAuthentificationService.getEmailFromCookie(request);
        Optional<UserApp> optionalUser = userAppRepository.findByUsername(username);
        if(optionalUser.isPresent()){
            UserApp userApp = optionalUser.get();
            return convertToUserConnected(userApp);
        }
        throw new AppException("aucun user connu");
    }

    public UserApp enableTokenConfirmation(UserApp userApp) throws Exception {
        userApp.setEnabled(true);
        userApp.setTokenEnabled(null);
        return userAppRepository.save(userApp);
    };

    public Optional<UserApp> findUserByTokenEnabled(String tokenEnabled) {
        return userAppRepository.findUserByTokenEnabled(tokenEnabled);

    };

    public ResponseEntity<?> changementMotDePasse(ChangementMotDePasseDTO changementMotDePasseDTO){
        Optional<TokenModifMotDePasse> optToken = tokenModifMotDePasseRepository.findByToken(changementMotDePasseDTO.getToken());
        if(optToken.isEmpty()){
            throw new AppException("le token de changement de mot de passe est inconnu");
        }
        TokenModifMotDePasse tokenModifMotDePasse = optToken.get();

        UserApp userApp = tokenModifMotDePasse.getUserApp();
        userApp.setPassword(bcrypt.encode(changementMotDePasseDTO.getPassword()));
        userAppRepository.save(userApp);
        return ResponseEntity.ok().build();
    }

    private UserConnectedDTO convertToUserConnected(UserApp userApp) throws Exception{
        UserConnectedDTO userConnectedDTO = new UserConnectedDTO(
                userApp.getUsername()
        );
        return userConnectedDTO;
    }

}
