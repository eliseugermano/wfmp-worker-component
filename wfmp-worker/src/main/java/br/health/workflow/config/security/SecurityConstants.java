package br.health.workflow.config.security;

public class SecurityConstants {
    static final String JWT_SECRET = "wfmp-jwt-secret";
    static final String JWT_PREFIX = "Bearer ";
    static final String JWT_HEADER = "Authorization";
    static final long JWT_EXPIRATION_TIME = 7200000;

    private SecurityConstants() {
    }

}
