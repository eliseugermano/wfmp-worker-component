package br.health.workflow.core.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class ServiceDTO implements Serializable {

    private Long service_id;
	private String name;

}
