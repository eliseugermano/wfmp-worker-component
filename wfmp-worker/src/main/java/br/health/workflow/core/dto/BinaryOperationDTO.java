package br.health.workflow.core.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class BinaryOperationDTO implements Serializable {

    private Long binary_operation_id;
	private String booleanCondition;
	private String objectCondition;
	private String attrCondition;
	private String operator;

	public boolean booleanOperationValue(boolean booleanValue1, boolean booleanValue2, String operator) {
		switch (operator) {
		case "==":
			return (booleanValue1 == booleanValue2);
		case "and":
			return (booleanValue1 && booleanValue2);
		case "or":
			return (booleanValue1 || booleanValue2);
		default:
			return false;
		}
	}
	
	public boolean relationalOperation(int aritmeticValue1, int aritmeticValue2, String operator) {
		switch (operator) {
		case "==":
			return (aritmeticValue1 == aritmeticValue2);
		case "!=":
			return (aritmeticValue1 != aritmeticValue2);
		case "<":
			return (aritmeticValue1 < aritmeticValue2);
		case ">":
			return (aritmeticValue1 > aritmeticValue2);
		case "<=":
			return (aritmeticValue1 <= aritmeticValue2);
		case ">=":
			return (aritmeticValue1 >= aritmeticValue2);
		default:
			return false;
		}
	}

}
