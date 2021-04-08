package br.health.workflow.core.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class TimeFunctionDTO implements Serializable {

    private Long time_function_id;
	private String type;
	private String operator;
	private List<String> range = new ArrayList<>();

}
