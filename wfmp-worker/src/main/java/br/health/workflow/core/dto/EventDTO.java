package br.health.workflow.core.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class EventDTO implements Serializable {

    private Long event_id;
	private String eventType; 	// onReceive
	private String objectKind; 	// BloodPressure
	private boolean multipleKinds=false;

}
