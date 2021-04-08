package br.health.workflow.core.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class TokenDTO implements Serializable {

    private Long token_id;
	private String id;

	public TokenDTO(String id) {
		this.id = id;
	}

}
