package br.health.workflow.core.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class ServiceCallDTO implements Serializable {

    private Long service_call_id;
	private String endPoint;
	private String parameter;
	private boolean callWorkflow=false; // is workflow to true value and is service to false value
	private boolean syncCallService=false;
	private String export;
	private boolean andSplit = false;

}
