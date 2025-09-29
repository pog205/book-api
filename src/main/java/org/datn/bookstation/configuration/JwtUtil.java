package org.datn.bookstation.configuration;

import io.jsonwebtoken.*;
import io.jsonwebtoken.Claims;

import org.datn.bookstation.entity.User;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtUtil {
    private final JwtProperties props;

    public JwtUtil(JwtProperties props) {
        this.props = props;
    }

    public String generateToken(User user) {
        return Jwts.builder()
                .setSubject(user.getEmail())
                .claim("id", user.getId())
                .claim("email", user.getEmail())
                .claim("fullName", user.getFullName())
                .claim("role", user.getRole().getRoleName().name())
                .claim("status", user.getStatus())
                .claim("emailVerified", user.getEmailVerified())
                .claim("phone", user.getPhoneNumber())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + props.getExpiration()))
                .signWith(SignatureAlgorithm.HS256, props.getSecret())
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(props.getSecret()).parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public String extractEmail(String token) {
        return Jwts.parser().setSigningKey(props.getSecret()).parseClaimsJws(token).getBody().getSubject();
    }

    public String extractRole(String token) {
        return (String) Jwts.parser().setSigningKey(props.getSecret()).parseClaimsJws(token).getBody().get("role");
    }

    public Byte extractStatus(String token) {
        try {
            Object statusObj = Jwts.parser().setSigningKey(props.getSecret()).parseClaimsJws(token).getBody().get("status");
            if (statusObj instanceof Byte) {
                return (Byte) statusObj;
            }
            if (statusObj instanceof Number) {
                return ((Number) statusObj).byteValue();
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    public Byte extractEmailVerified(String token) {
        try {
            Object emailVerifiedObj = Jwts.parser().setSigningKey(props.getSecret()).parseClaimsJws(token).getBody().get("emailVerified");
            if (emailVerifiedObj instanceof Byte) {
                return (Byte) emailVerifiedObj;
            }
            if (emailVerifiedObj instanceof Number) {
                return ((Number) emailVerifiedObj).byteValue();
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    // ====== RESET PASSWORD SUPPORT ======

    /** Sinh token RESET, hết hạn sau 15 phút */
    public String generateResetToken(User user) {
        long expirationMillis = 15 * 60 * 1000; // 15 phút
        return Jwts.builder()
                .setSubject(user.getEmail())
                .claim("id", user.getId())
                .claim("type", "RESET")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationMillis))
                .signWith(SignatureAlgorithm.HS256, props.getSecret())
                .compact();
    }

    // ====== EMAIL VERIFICATION SUPPORT ======

    /** Sinh token VERIFICATION, không bao giờ hết hạn */
    public String generateVerificationToken(User user) {
        return Jwts.builder()
                .setSubject(user.getEmail())
                .claim("id", user.getId())
                .claim("type", "VERIFICATION")
                .setIssuedAt(new Date())
                .signWith(SignatureAlgorithm.HS256, props.getSecret())
                .compact();
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser().setSigningKey(props.getSecret()).parseClaimsJws(token).getBody();
    }

    public boolean isResetToken(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return "RESET".equals(claims.get("type"));
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isVerificationToken(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return "VERIFICATION".equals(claims.get("type"));
        } catch (Exception e) {
            return false;
        }
    }

    public Integer extractUserId(String token) {
        try {
            Claims claims = extractAllClaims(token);
            Object idObj = claims.get("id");
            if (idObj instanceof Integer) {
                return (Integer) idObj;
            }
            if (idObj instanceof Number) {
                return ((Number) idObj).intValue();
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }
} 