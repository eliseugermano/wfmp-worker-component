package br.health.workflow.core.communication;

import br.health.workflow.core.data.Buffer;
import br.health.workflow.core.data.Header;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;

import java.util.Hashtable;

@Data
@Log4j2
public class AsyncCommunication {
	
	// Mapping between WfMP listeners and RabbitMQ queues
    private Hashtable<String, SimpleMessageListenerContainer> workflowListeners = new Hashtable<>();
	
    private AmqpAdmin amqpAdmin;
    
    private RabbitAdmin rabbitAdmin;
    
    private RabbitTemplate rabbitTemplate;
    
	public AsyncCommunication(AmqpAdmin amqpAdmin, RabbitAdmin rabbitAdmin, RabbitTemplate rabbitTemplate) {
		this.amqpAdmin = amqpAdmin;
		this.rabbitAdmin = rabbitAdmin;
		this.rabbitTemplate = rabbitTemplate;
	}
	
	/**
	 * Listener a specific queue
	 * 
	 * @param queue: queue name
	 */
    
	public void listenerQueue(String queue, Buffer workflowBuffer) {
  		if(!workflowListeners.containsKey(queue)){
			SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
			container.setConnectionFactory(rabbitAdmin.getRabbitTemplate().getConnectionFactory());
			container.setQueueNames(queue);
			container.setMessageListener(new MessageListenerAdapter(workflowBuffer, "listeningRabbitMQ"));
			container.start();
			
			workflowListeners.put(queue, container);
			log.info("Subscribe [" + queue + "]");
			workflowListeners.put(queue, container);
  		}
  		
    }
	
	/**
	 * Submit a message to specific RabbitMQ Topic
	 * 
	 * @param data message
	 * @param dataType message type
	 * @param exchange RabbitMQ exchange
	 * @param route RabbitMQ route
	 * @param token JWT
	 */
	
	public void sendMessage(String data, String dataType, String exchange, String route, String token){
		log.info("Receive => " + data);
		Gson gson = new Gson();
		JsonObject jsonObj = new JsonObject();
		jsonObj.add("body", gson.toJsonTree(data));
		jsonObj.add("header", gson.toJsonTree(gson.toJson((new Header(dataType, token)))));

		log.info("Publish => " + jsonObj);
		rabbitTemplate.convertAndSend(exchange, route, jsonObj.toString());
	}
	
	
	/**
	 * Stop listening a specific queue
	 * 
	 * @param queue: queue name
	 */
	
	public void stopListenerQueue(String queue) {
  		if(workflowListeners.containsKey(queue)){
  			SimpleMessageListenerContainer container = workflowListeners.remove(queue);
  			container.stop();
			log.info("Unsubscribe ["+ queue +"]");
  		}
  	}
  	
	/**
	 * Create a exchange in a RabbitMQ node
	 * 
	 * @param name: exchange name
	 */
	public void createExchange(String name) {
  		Exchange exchange = ExchangeBuilder.topicExchange(name).durable(true).build();
  		amqpAdmin.declareExchange(exchange);
  	}
  	
	/**
	 * Create a queue in a RabbitMQ node
	 * 
	 * @param name: queue name
	 * @param exchange: exchange in the RabbitMQ
	 * @param route: route in the RabbitMQ
	 */
	
	public void createQueue(String name, String exchange, String route) {
		Queue queue = QueueBuilder.nonDurable(name).build();
		amqpAdmin.declareQueue(queue);
		
		Binding binding = BindingBuilder.bind(queue).to(new TopicExchange(exchange)).with(route);
		amqpAdmin.declareBinding(binding);
  	}
  	
	/**
	 * Delete a queues in a RabbitMQ node
	 * 
	 * @param name: queue name
	 */
  	
	public void deleteQueue(String name) {
		amqpAdmin.deleteQueue(name);
  	}

}
