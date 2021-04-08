package br.health.workflow.core.dto;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@JsonIdentityInfo(
		generator = ObjectIdGenerators.PropertyGenerator.class,
		property = "transition_id"
)
public class TransitionDTO implements Serializable {

    private Long transition_id;
	private EventDTO event;
	private ConditionDTO condition;
	private ActionDTO action = new ActionDTO();
	private String sourcePlaceName; // size > 1 => and join

//	@JsonManagedReference
	private List<PlaceDTO> targets; // size > 1 => and split

}
