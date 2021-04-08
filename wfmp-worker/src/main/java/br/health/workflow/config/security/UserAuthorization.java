package br.health.workflow.config.security;

import io.jsonwebtoken.*;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

@Log4j2
@Component
public class UserAuthorization {

    // ROLES
    private static final String HEALTH_PROFESSIONAL = "HEALTH_PROFESSIONAL";
    private static final String PATIENT = "PATIENT";
    private static final String ADMIN = "ADMIN";

    private HashMap<String, List<String>> filter = new HashMap<>();

    @PostConstruct
    public void init() {
        // workflows
        filter.put("POST:/api/worker/register", new ArrayList<>(Arrays.asList(ADMIN, HEALTH_PROFESSIONAL)));
        filter.put("POST:/api/worker/run", 		new ArrayList<>(Arrays.asList(ADMIN, HEALTH_PROFESSIONAL, PATIENT)));
        filter.put("POST:/api/worker/publish", 	new ArrayList<>(Arrays.asList(ADMIN, HEALTH_PROFESSIONAL, PATIENT)));
        filter.put("POST:/api/worker/subscribe",new ArrayList<>(Arrays.asList(ADMIN, HEALTH_PROFESSIONAL, PATIENT)));
        filter.put("POST:/api/worker/test",     new ArrayList<>(Arrays.asList(ADMIN, HEALTH_PROFESSIONAL, PATIENT)));

        // microservices
        filter.put("GET:/api/microservice",		new ArrayList<>(Arrays.asList(ADMIN)));
        filter.put("POST:/api/microservice", 	new ArrayList<>(Arrays.asList(ADMIN)));
        filter.put("PUT:/api/microservice", 	new ArrayList<>(Arrays.asList(ADMIN)));
        filter.put("DELETE:/api/microservice", 	new ArrayList<>(Arrays.asList(ADMIN)));

        // amqp tests
        filter.put("POST:/api/amqp/publish", 	new ArrayList<>(Arrays.asList(ADMIN)));
        filter.put("POST:/api/amqp/subscribe", 	new ArrayList<>(Arrays.asList(ADMIN)));
        filter.put("POST:/api/amqp/exchange", 	new ArrayList<>(Arrays.asList(ADMIN)));
        filter.put("POST:/api/amqp/queue", 		new ArrayList<>(Arrays.asList(ADMIN)));
        filter.put("DELETE:/api/amqp/queue", 	new ArrayList<>(Arrays.asList(ADMIN)));
    }

    public Boolean checkAuthorization(String token, String endpointPath) {
        if (!token.isEmpty() && token.startsWith(SecurityConstants.JWT_PREFIX)) {
            try {
                // token decode
                Claims parsedToken = Jwts.parser()
                        .setSigningKey(SecurityConstants.JWT_SECRET)
                        .parseClaimsJws(token.replace(SecurityConstants.JWT_PREFIX, ""))
                        .getBody();
                if (filter.containsKey(endpointPath)) {
                    List authorities = (List) parsedToken.get("authorities");

                    List<String> authorityList = new ArrayList<>();
                    authorities.forEach(auth ->authorityList.add(String.valueOf(auth)));

                    List<String> roles = filter.get(endpointPath);

                    if(roles.isEmpty())
                        return Boolean.FALSE;
                    return authorityList.stream().anyMatch(authority -> roles.contains(authority.split("_")[1]));
                }
                return Boolean.FALSE;
            } catch (ExpiredJwtException exception) {
                log.warn("Request to parse expired JWT : {} failed : {}", token, exception.getMessage());
            } catch (UnsupportedJwtException exception) {
                log.warn("Request to parse unsupported JWT : {} failed : {}", token, exception.getMessage());
            } catch (MalformedJwtException exception) {
                log.warn("Request to parse invalid JWT : {} failed : {}", token, exception.getMessage());
            } catch (SignatureException exception) {
                log.warn("Request to parse JWT with invalid signature : {} failed : {}", token, exception.getMessage());
            } catch (IllegalArgumentException exception) {
                log.warn("Request to parse empty or null JWT : {} failed : {}", token, exception.getMessage());
            }
        }
        return Boolean.FALSE;
    }

}
