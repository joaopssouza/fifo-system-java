package com.joaopssouza.fifosystem.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.joaopssouza.fifosystem.domain.entity.User; // Importe sua entidade User
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long jwtExpirationDate;

    // Gera a chave criptográfica segura a partir da string
    private SecretKey key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    // Gerar Token JWT
// Mude a assinatura para receber o USER, não apenas Authentication
    public String generateToken(User user) {
        Date currentDate = new Date();
        Date expireDate = new Date(currentDate.getTime() + jwtExpirationDate);

        // Prepara as permissões em formato de lista de strings
        var permissions = user.getRole().getPermissions().stream()
                .map(p -> p.getName())
                .collect(Collectors.toList());

        // Claims personalizadas (para o React ler)
        Map<String, Object> claims = new HashMap<>();
        claims.put("user", user.getUsername());
        claims.put("fullName", user.getFullName());
        claims.put("role", user.getRole().getName());
        claims.put("permissions", permissions);
        claims.put("sub", String.valueOf(user.getId())); // O React usa 'sub' como ID numérico às vezes

        return Jwts.builder()
                .claims(claims) // Adiciona o mapa
                .subject(user.getUsername())
                .issuedAt(new Date())
                .expiration(expireDate)
                .signWith(key())
                .compact();
    }

    // Obter username do Token
    public String getUsername(String token) {
        return Jwts.parser()
                .verifyWith(key())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    // Validar Token
    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(key()).build().parse(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            // Em um projeto real, logaríamos o erro aqui
            return false;
        }
    }
}