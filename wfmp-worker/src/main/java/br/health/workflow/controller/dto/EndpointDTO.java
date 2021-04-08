package br.health.workflow.controller.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class EndpointDTO implements Serializable {
    private Long endpoint_id;
	private String reference; // identifier that is used by DSL users
	private String resource; // Web Service Resource
	private String method; // HTTP Method
	private String acceptType; // Input type
	private String contentType; // Output type
	private String dataStructure; // Data type
	private String protocol; // Communication Protocol

	public EndpointDTO(String resource, String method, String acceptType, String contentType) {
		this.resource = resource;
		this.method = method;
		this.acceptType = acceptType;
		this.contentType = contentType;
	}

}
