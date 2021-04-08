package br.health.workflow.core.data;

import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

@NoArgsConstructor
@Log4j2
public class Typing {

	public boolean checkTypingProblem(String objectData, String schemaName){
		InputStream inputStream;
		try {
			inputStream = new FileInputStream("./src/main/resources/inputRegistry/" + schemaName + ".json");
			JSONObject jsonSchema = new JSONObject(new JSONTokener(inputStream));
			JSONObject jsonSubject = new JSONObject(objectData);
			
			Schema schema = SchemaLoader.load(jsonSchema);
			schema.validate(jsonSubject);
		} catch (FileNotFoundException e) {
			log.error("\t->" + " [FileNotFoundException] " +e.getMessage());
			return true;
		} catch (ValidationException e) {
			log.error("\t->" + " [ValidationException] " +e.getMessage());
			return true;
		}
		return false;
	}

}
