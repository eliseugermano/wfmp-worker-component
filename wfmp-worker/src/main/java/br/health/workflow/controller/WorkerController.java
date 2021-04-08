package br.health.workflow.controller;

import br.health.workflow.config.security.UserAuthorization;
import br.health.workflow.core.communication.AsyncCommunication;
import br.health.workflow.core.data.Buffer;
import br.health.workflow.controller.dto.WorkflowDTO;
import br.health.workflow.core.dto.PetriNetDTO;
import br.health.workflow.service.PetriNetLoaderService;
import br.health.workflow.core.dto.TokenDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;

@RestController
@RequestMapping("/api/worker")
@Log4j2
public class WorkerController {
	
	@Autowired
	private PetriNetLoaderService loader;
  	
  	@Autowired
  	private AsyncCommunication asyncCommunication;

	@Autowired
	private UserAuthorization userAuthorization;
  	
  	@RequestMapping(value = "/run", method = RequestMethod.POST)
	public @ResponseBody ResponseEntity<?> runProcess(
			@RequestBody WorkflowDTO workflowRequest,
			@RequestHeader("Authorization") String authorization
	){
		Boolean authorized = userAuthorization.checkAuthorization(authorization, "POST:/api/worker/run");
		if (authorized.equals(Boolean.FALSE))
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);

  		log.info("[Authorization] \t\t-> "+workflowRequest);
		String serializedPetriNet = workflowRequest.getSerializedPetriNet();

		PetriNetDTO workflow = null;
		try {
			workflow = new ObjectMapper().readerFor(PetriNetDTO.class).readValue(serializedPetriNet);
		} catch (IOException e) {
			e.printStackTrace();
		}

  		if (workflow == null)
  			throw new RuntimeException("Workflow not found.");

		workflow.getStart().getTokens().add(new TokenDTO(workflowRequest.getAccessToken()));
		workflow.setAmqpQueueName(workflow.getName());
		workflow.setAmqpRoute(workflowRequest.getUserId()+".#");

		if(workflowRequest.getWorkflowMemory()!=null)
			loader.getBuffer().setWorkflowMemory(workflowRequest.getWorkflowMemory());

  		loader.loadSemanticModel(workflow, authorization);
  		loader.setWorkflowRequest(workflowRequest);
		
		// response message
		HashMap<String, Object> response = new LinkedHashMap<>();
		response.put("id", workflow.getPetrinet_id());
		response.put("name", workflow.getName());
		response.put("status", "running");
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	
	@RequestMapping(value = "/publish", method = RequestMethod.POST)
	public ResponseEntity userPatient(
			@RequestBody String data,
			@RequestHeader("Authorization") String authorization,
			@RequestHeader("Data-Type") String dataType,
			@RequestHeader("Exchange") String exchange,
			@RequestHeader("Route") String route,
			@RequestHeader("Access-Token") String token) {
		Boolean authorized = userAuthorization.checkAuthorization(authorization, "POST:/api/worker/publish");
		if (authorized.equals(Boolean.FALSE))
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);

		try {
			asyncCommunication.sendMessage(data, dataType, exchange, route, token);
		} catch (Exception e) {
			log.error(e);
			return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity(HttpStatus.OK);
	}
	
	@RequestMapping(value = "/subscribe", method = RequestMethod.POST)
	public ResponseEntity receiveMessage(
			@RequestHeader("Authorization") String authorization,
			@RequestHeader("Queue") String queue
	) {
		Boolean authorized = userAuthorization.checkAuthorization(authorization, "POST:/api/worker/subscribe");
		if (authorized.equals(Boolean.FALSE))
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);

		try {
			Buffer buffer = new Buffer();
			asyncCommunication.listenerQueue(queue, buffer);
		} catch (Exception e) {
			log.error(e);
			return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
		}
  		return new ResponseEntity(HttpStatus.OK);
    }

}
