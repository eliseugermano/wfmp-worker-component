package br.health.workflow.core.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class ConditionDTO implements Serializable {

    private Long condition_id;
	private BinaryOperationDTO binaryOperation = null;
	private TimeFunctionDTO timeFunction = null;
	
	public boolean statementEval(String booleanCondition, boolean value){
		if(booleanCondition.equals("isTrue"))
			return value;
		else
			return !value;
	}

}
