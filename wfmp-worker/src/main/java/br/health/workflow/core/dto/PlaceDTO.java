package br.health.workflow.core.dto;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@JsonIdentityInfo(
		generator = ObjectIdGenerators.PropertyGenerator.class,
		property = "place_id"
)
public class PlaceDTO implements Serializable {

	private Long place_id;
	private String name;
	private List<TokenDTO> tokens = new ArrayList<>();
	private List<TransitionDTO> transitions = new ArrayList<>();

	public void addToken(TokenDTO token){
		this.tokens.add(token);
	}

	public TokenDTO removeToken(String tokenId) {
		for (TokenDTO token: tokens) {
			if (token.getId().equals(tokenId)) {
				tokens.remove(token);
				return token;
			}
		}
		return null;
	}
	
}
