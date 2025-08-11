package com.example.demo.repositories;

import com.example.demo.models.TokenModifMotDePasse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TokenModifMotDePasseRepository extends JpaRepository<TokenModifMotDePasse, Integer> {
    Optional<TokenModifMotDePasse> findByToken(String token);
}
