package br.health.workflow.controller;

import br.health.workflow.config.security.UserAuthorization;
import lombok.extern.log4j.Log4j2;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import br.health.workflow.core.communication.AsyncCommunication;
import br.health.workflow.core.data.Buffer;

@RestController
@RequestMapping("/api/amqp")
@Log4j2
public class MessageQueueController {
	
	@Autowired
	private AsyncCommunication asyncCommunication;

	@Autowired
	private UserAuthorization userAuthorization;
	
	/**
	 * Publish a message in a RabbitMQ topic
	 * 
	 * @param data message
	 * @param authorization JWT Token
	 * @param dataType message type
	 * @param exchange RabbitMQ exchange
	 * @param route RabbitMQ route
	 * @param token JTW
	 * @return HTTP Status
	 */
	@RequestMapping(value = "/publish", method = RequestMethod.POST)
	public ResponseEntity<?> userPatient(
			@RequestBody String data,
			@RequestHeader("Authorization") String authorization,
			@RequestHeader("Data-Type") String dataType,
			@RequestHeader("Exchange") String exchange,
			@RequestHeader("Route") String route,
			@RequestHeader("Access-Token") String token
	) {
		Boolean authorized = userAuthorization.checkAuthorization(authorization, "POST:/api/amqp/publish");
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
	
	/**
	 * Listening a queue in a RabbitMQ node
	 *
	 * @param authorization JWT Token
	 * @param queue name
	 */
	@RequestMapping(value = "/subscribe", method = RequestMethod.POST)
	public ResponseEntity<?> listenerQueue(
			@RequestHeader("Authorization") String authorization,
			@RequestHeader("Queue") String queue
	) {
		Boolean authorized = userAuthorization.checkAuthorization(authorization, "POST:/api/amqp/subscribe");
		if (authorized.equals(Boolean.FALSE))
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);

		try {
			asyncCommunication.listenerQueue(queue, new Buffer());
		} catch (Exception e) {
			log.error(e);
			return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
		}
  		return new ResponseEntity(HttpStatus.OK);
    }
	
	/**
	 * Create a exchange in a RabbitMQ node
	 * 
	 * @param data message queue structure
	 * @param authorization JWT Token
	 */
  	@RequestMapping(value = "/exchange", method = RequestMethod.POST)
	public ResponseEntity<?> createExchange(
			@RequestBody String data,
			@RequestHeader("Authorization") String authorization
	) {
		Boolean authorized = userAuthorization.checkAuthorization(authorization, "POST:/api/amqp/exchange");
		if (authorized.equals(Boolean.FALSE))
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);

  		JSONObject json = new JSONObject(data);
  		asyncCommunication.createExchange(json.getString("name"));
  		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  	}
  	
  	/**
	 * Create a queue in a RabbitMQ node
	 * 
	 * @param data message queue structure
	 * @param authorization JWT Token
	 */
  	@RequestMapping(value = "/queue", method = RequestMethod.POST)
	public ResponseEntity<?> createQueue(
			@RequestBody String data,
			@RequestHeader("Authorization") String authorization
	) {
		Boolean authorized = userAuthorization.checkAuthorization(authorization, "POST:/api/amqp/queue");
		if (authorized.equals(Boolean.FALSE))
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);

  		JSONObject json = new JSONObject(data);
  		asyncCommunication.createQueue(json.getString("name"), json.getString("exchange"), json.getString("route"));
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  	}
  	
  	/**
	 * Delete a queue in a RabbitMQ node
	 * 
	 * @param data message queue structure
	 * @param authorization JWT Token
	 */
  	@RequestMapping(value = "/queue", method = RequestMethod.DELETE)
	public ResponseEntity<?> deleteQueue(
			@RequestBody String data,
			@RequestHeader("Authorization") String authorization
	) {
		Boolean authorized = userAuthorization.checkAuthorization(authorization, "DELETE:/api/amqp/queue");
		if (authorized.equals(Boolean.FALSE))
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);

  		JSONObject json = new JSONObject(data);
  		asyncCommunication.deleteQueue(json.getString("name"));
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  	}

}