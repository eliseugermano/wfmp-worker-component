package br.health.workflow.core.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class ActionDTO implements Serializable {

    private Long action_id;
	private List<ServiceCallDTO> calls = new ArrayList<>();

}
