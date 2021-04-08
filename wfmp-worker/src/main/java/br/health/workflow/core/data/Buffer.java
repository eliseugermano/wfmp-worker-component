package br.health.workflow.core.data;

import br.health.workflow.controller.dto.EndpointDTO;
import br.health.workflow.controller.dto.MicroserviceDTO;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

@Log4j2
@Data
@NoArgsConstructor
public class Buffer {
	
	/**
	 * Groupings in lists the data received from the RabbitMQ queue according to the data 
	 * types accepted in the workflow of this buffer.
	 * 
	 */
    private Hashtable<String, List<JSONObject>> workflowInput = new Hashtable<>();
    
    /**
     * Variables and parameters using in the workflows
     * 
     */
    private Hashtable<String, JSONObject> workflowMemory = new Hashtable<>();

	private MicroserviceDTO microservice;

	/**
	 * Registered method in a MessageListenerContainer of the RabbitMQ for a specific queue of a workflow.
	 * This method receive a message of a RabbitMQ, distributes and forwards the data to workflow lists.
	 * 
	 * @param message: serialized message consumed from the RabbitMQ queue for this workflow buffer
	 */
	public void listeningRabbitMQ(String message) {
		log.info("Received Message of the RabbitMQ: <" + message + ">");
		
		try {
			synchronized (this){
				JSONObject json = new JSONObject(message);
				JSONObject jsonHeader = new JSONObject(json.getString("header"));
				JSONObject jsonBody = new JSONObject(json.getString("body"));

				Header header = new Header(jsonHeader.getString("type"), jsonHeader.getString("token"));

				if(!workflowInput.containsKey(header.getType()))
					workflowInput.put(header.getType(), new ArrayList<>());
				
				workflowInput.get(header.getType()).add(jsonBody);
				
				// notifies the consumer that now it can start consuming
				notify();
			}
		} catch (JSONException e) {
			log.error(e);
		}
	}
	
	/**
	 * Listener the workflow lists and use the Java synchronized keyword for control of the exclusive accessing
	 * @param objectName: name of the object of interest
	 * @return serialized object in JSON format 
	 * @throws InterruptedException Listener error
	 */
	private JSONObject listenerWorkflowLists(String objectName) throws InterruptedException {
		JSONObject value;
		synchronized (this) {
			log.info("[WfMP] \t\t-> [AMQPListener] Wait [{}]", objectName);

			while (!(workflowInput.containsKey(objectName) && workflowInput.get(objectName).size() != 0))
				wait();

			log.info("[WfMP] \t\t-> Receive [{}]", objectName);

			value = workflowInput.get(objectName).remove(0);
			notify();
		}
		return value;
	}

	public JSONObject getMessageQueueData(String type) throws InterruptedException {
		if(microservice==null) return null;
		
		for (EndpointDTO endpoint : microservice.getEndpoints()) {
			if(!"".equals(endpoint.getDataStructure()) && endpoint.getDataStructure().equals(type))
				return listenerWorkflowLists(endpoint.getDataStructure());
		}
		return null;
	}
	
	public JSONObject getDataWorkflow(String type){
		try {
			return listenerWorkflowLists(type);
		} catch (InterruptedException e) {
			log.error(e);
			return null;
		}
	}

	public void clean(){
		workflowInput.clear();
		workflowMemory.clear();
	}
	
	public void setWorkflowMemory(Hashtable<String, JSONObject> workflowMemory) {
		this.workflowMemory = workflowMemory;
	}
}