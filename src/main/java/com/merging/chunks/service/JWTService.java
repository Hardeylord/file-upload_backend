package com.merging.chunks.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class JWTService {
    private final long EXPIRATION_DATE=1000*60*15;
    private final String SECRET=System.getenv("JWT_KEY");

    public String generateToken(String username, String role) {
        Map<String, String> claim = new HashMap<>();
        claim.put("Role", role);
       return Jwts
                .builder()
                .subject(username)
                .claims(claim)
                .signWith(key())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis()+EXPIRATION_DATE))
                .compact();
    }

    private Key key() {
//        return Keys.hmacShaKeyFor(Decoders.BASE64URL.decode(SECRET));
        byte[] keyByte = Decoders.BASE64URL.decode(SECRET);
        return Keys.hmacShaKeyFor(keyByte);
    }

    public String extractUsername(String token) {
       return verifyExtractClaims(token).getSubject();
    }

    public boolean isTokenExpired(String token) {
        return expirationDate(token).before(new Date());
    }

    private Date expirationDate(String token) {
        return verifyExtractClaims(token).getExpiration();
    }

    public Claims verifyExtractClaims(String token) {
       return Jwts.parser()
                .verifyWith((SecretKey) key())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
