package br.health.workflow.controller.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
public class MicroserviceDTO implements Serializable {
    private Long microservice_id;
	private String reference; // identifier that is used by DSL users
	private String host; // address
	private String protocol; // communication protocol
	private String clientId; // Oauth2 protocol attribute
	private String clientSecret; // Oauth2 protocol attribute
	private String description;
	private List<EndpointDTO> endpoints;
	
	public EndpointDTO getEndPointByReference(String reference){
		EndpointDTO endpoint = null;
		for (EndpointDTO e : endpoints) {
			if(e.getReference().equals(reference)){
				endpoint = e;
				break;
			}
		}
		return endpoint;
	}

}