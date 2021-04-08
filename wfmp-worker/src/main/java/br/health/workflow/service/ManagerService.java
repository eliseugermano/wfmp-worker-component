package br.health.workflow.service;

import br.health.workflow.core.communication.SyncCommunication;
import br.health.workflow.core.communication.config.RestTemplateConfig;
import br.health.workflow.controller.dto.MicroserviceDTO;
import br.health.workflow.core.dto.PetriNetDTO;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Service
@Log4j2
public class ManagerService {

    @Autowired
    private SyncCommunication syncCommunication;

    @Autowired
    private Gson gson;

    public MicroserviceDTO getMicroserviceData(String reference, String accessToken, String managerHost) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
        headers.add("Authorization", accessToken);
        RestTemplateConfig restTemplateAttributes = new RestTemplateConfig(
                headers,
                null,
                managerHost,
                "/api/microservice/reference/{reference}",
                HttpMethod.GET,
                Boolean.TRUE
        );

        HashMap<String, String> pathVariables = new HashMap<>();
        pathVariables.put("{reference}", String.valueOf(reference));
        restTemplateAttributes.setPathVariables(pathVariables);

        ResponseEntity responseEntity = syncCommunication.restRequest(restTemplateAttributes);

        if (responseEntity.getBody() == null)
            throw new RuntimeException("Microservice not found.");
        try {
            return gson.fromJson(responseEntity.getBody().toString(), MicroserviceDTO.class);
        } catch (Exception e) {
            throw new JsonParseException("Microservice not found.");
        }
    }

    public PetriNetDTO getPetriNetData(String reference, String accessToken, String managerHost) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
        headers.add("Authorization", "Bearer "+accessToken);

        RestTemplateConfig restTemplateAttributes = new RestTemplateConfig(
                headers,
                null,
                managerHost,
                "/api/workflow/{reference}",
                HttpMethod.GET,
                Boolean.TRUE
        );

        HashMap<String, String> pathVariables = new HashMap<>();
        pathVariables.put("{reference}", String.valueOf(reference));
        restTemplateAttributes.setPathVariables(pathVariables);

        ResponseEntity responseEntity = syncCommunication.restRequest(restTemplateAttributes);

        if (responseEntity.getBody() == null)
            throw new RuntimeException("PetriNet not found.");

        try {
            return gson.fromJson(responseEntity.getBody().toString(), PetriNetDTO.class);
        } catch (Exception e) {
            throw new JsonParseException("Microservice not found.");
        }
    }

}
