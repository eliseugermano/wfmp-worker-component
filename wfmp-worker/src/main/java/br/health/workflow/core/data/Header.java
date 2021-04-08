package br.health.workflow.core.data;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Header {
	
	private String type;
	private String token;
	private String route;
	
	public Header(String type, String token) {
		super();
		this.type = type;
		this.token = token;
	}

}
