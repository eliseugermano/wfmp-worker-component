package br.health.workflow.core.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class PetriNetDTO implements Serializable {

    private Long petrinet_id;
	private String name;
	private PlaceDTO start;
	private String amqpQueueName;
	private String amqpRoute;

}
