package br.health.workflow.service;

import br.health.workflow.controller.dto.EndpointDTO;
import br.health.workflow.controller.dto.MicroserviceDTO;
import br.health.workflow.controller.dto.WorkflowDTO;
import br.health.workflow.core.communication.AsyncCommunication;
import br.health.workflow.core.communication.SyncCommunication;
import br.health.workflow.core.communication.config.RestTemplateConfig;
import br.health.workflow.core.data.Buffer;
import br.health.workflow.core.data.Typing;
import br.health.workflow.core.dto.*;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.TaskExecutor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

@Service
@Log4j2
@NoArgsConstructor
public class PetriNetLoaderService {
	
	private Typing typing = new Typing();
	private Buffer buffer = new Buffer();
	private boolean typingProblem;
	private WorkflowDTO workflowRequest;
	
	@Value("${google.host.manager}")
	private String hostManager;
	
	@Autowired
	private TaskExecutor taskExecutor;
	
	@Autowired
	private ManagerService managerService;

	@Autowired
	private SyncCommunication syncCommunication;

	@Autowired
	private AsyncCommunication asyncCommunication;

	@Value("${spring.rabbitmq.default-exchange}")
	private String topicExchange;
	
	/**
	 * Loads the workflow (runs as an asynchronous task)
	 *
	 */
	public void loadSemanticModel(PetriNetDTO workflow, String authorization) {
		taskExecutor.execute(() -> {
			Date date= new Date();
			Timestamp timestamp = new Timestamp(date.getTime());
			String queueName = "workflow_"+workflow.getAmqpQueueName()+"_["+timestamp+"]";

			/* AMQP create queue */
			asyncCommunication.createExchange(topicExchange);
			asyncCommunication.createQueue(queueName, topicExchange, workflow.getAmqpRoute());
			log.info("[WfMP] ->" + " [Petri Net] {}", workflow.getName());

			walkerPetriNet(workflow.getStart(), queueName, workflow.getAmqpRoute(), workflow.getName(), 0, authorization);
			log.info("[WfMP] \t\t-> [FINISH] workflow ["+workflow.getName()+"]");

			try {
				Thread.sleep(30000);

				/* AMQP stop listener queue */
				asyncCommunication.stopListenerQueue(queueName);

				/* AMQP delete queue */
				asyncCommunication.deleteQueue(queueName);

				/* Clear the buffer */
				buffer.clean();
			} catch (InterruptedException e) {
				log.error(e);
			}
		});
	}
	
	/**
	 * Loads a branch from a workflow
	 *
	 */
	public void loadSemanticModel(PlaceDTO place, String queueNameWf, String amqpRoute, String workflowName, int branch, String authorization){
		taskExecutor.execute(() -> {
			Date date= new Date();
			Timestamp timestamp = new Timestamp(date.getTime());
			String queueName = queueNameWf+"_split_["+timestamp+"]";

			/* AMQP create queue */
			asyncCommunication.createQueue(queueName, "service_data", amqpRoute);
			log.info("[WfMP] ->" + " [Petri Net][AND-SPLIT] {}", workflowName);

			walkerPetriNet(place, queueName, amqpRoute, workflowName, branch, authorization);
			log.info("[WfMP] \t\t-> [FINISH] workflow ["+workflowName+"]");

			try {
				Thread.sleep(30000);

				/* AMQP stop listener queue */
				asyncCommunication.stopListenerQueue(queueName);

				/* AMQP delete queue */
				asyncCommunication.deleteQueue(queueName);

				/* Clear the buffer */
				buffer.clean();
			} catch (InterruptedException e) {
				log.error(e);
			}
		});
	}
	
	/**
	 * Iterative Processing of the Petri Net
	 *
	 */
	private void walkerPetriNet(PlaceDTO place, String queueName, String amqpRoute, String workflowName, int branch, String authorization) {
		String exportData = null;
		
		// Running places
		while(true){
			log.info("[WfMP] \t-> [Place] {}", place.getName());
			
			if(place.getName().equals("end.workflow")){
				break; // Finish
			} else if(place.getName().equals("end.workflow.export")){
				log.info("[WfMP] \t\t-> [Publish] "+exportData);
				
				// Publish the output of workflow in a RabbitMQ broker
				asyncCommunication.sendMessage(buffer.getWorkflowMemory().get(exportData).toString(), exportData, "service_data", "user003.workflow", "");
				break; // Finish
			}

			for (TransitionDTO transition : place.getTransitions()) {
				EventDTO event = transition.getEvent();
				ConditionDTO condition = transition.getCondition();
				ActionDTO action = transition.getAction();
				JSONObject objectData = null;
				
				List<ServiceCallDTO> calls = action.getCalls();
				
				// AND-SPLIT
				
				if(calls.size()>1 && !calls.get(branch).isAndSplit()){
					for (int j = 0; j < calls.size(); j++) {
						calls.get(j).setAndSplit(true);
						log.info("[WfMP] \t\t-> [call] "+calls.get(j).getEndPoint());
						loadSemanticModel(place, queueName, amqpRoute, workflowName, j, authorization);
					}
					return;
				}
				
				// EVENT
				
				// Retrieves queue data
				
				log.debug("\t\t-> serviceCall {}", calls.toString());
				int indice = calls.size()==1? 0 : branch;
				
				ServiceCallDTO serviceCall = calls.get(indice);
				PlaceDTO transitionTarget = transition.getTargets().get(indice);
				
				if("start task".equals(place.getName())){
					if(serviceCall.getParameter()!=null && !serviceCall.getParameter().equals(""))
						objectData = buffer.getWorkflowMemory().get(serviceCall.getParameter());
					
					/* AMQP listener queue */
					asyncCommunication.listenerQueue(queueName, buffer);
				} else {
					log.debug("\t\t-> GET Object {}", event.getObjectKind());
					objectData = buffer.getWorkflowMemory().get(event.getObjectKind());
				}
				log.debug("\t\t-> MEMORY {}", buffer.getWorkflowMemory());
				
				// Checks data retrieved from queue
				
				if(objectData!=null){
					if("start task".equals(place.getName())){
						
						// Check if the data has any typing problem
						typingProblem = typing.checkTypingProblem(objectData.toString(), serviceCall.getParameter());
						if(!typingProblem){
							buffer.getWorkflowMemory().put(serviceCall.getParameter(), objectData);
							
							log.info("[WfMP] \t\t-> [Trasition] {} to {}", transition.getSourcePlaceName(), transitionTarget.getName());
						} else {
							return;
						}
					} else {
						
						// Check if the data has any typing problem
						typingProblem = typing.checkTypingProblem(objectData.toString(), event.getObjectKind());
						if(!typingProblem){
							buffer.getWorkflowMemory().put(event.getObjectKind(), objectData);
							
							log.info("[WfMP] \t\t-> [Event] " + event.getEventType() + " '{\"object\":"+event.getObjectKind()+" \"typing_problem\":"+ typingProblem +"}'", place.getName());
							
							// CONDITION
							
							if(condition.getBinaryOperation()!=null){
								log.info("[WfMP] \t\t-> [Condition] {} {}", condition.getBinaryOperation().getBooleanCondition(), condition.getBinaryOperation().getObjectCondition() + "." +condition.getBinaryOperation().getAttrCondition());
								
								boolean data = objectData.getBoolean(condition.getBinaryOperation().getAttrCondition());
								if(!condition.statementEval(condition.getBinaryOperation().getBooleanCondition(), data))
									continue;
							}
							log.info("[WfMP] \t\t-> [Trasition] {} to {} ", transition.getSourcePlaceName(), transitionTarget.getName());
						} else {
							return;
						}
					}
				}
				
				// ACTION
				
				log.info("[WfMP] \t\t-> [Action] call {}", serviceCall.getEndPoint());
				if(transitionTarget.getName().equals("end.workflow")){
					place = transitionTarget;
					break;
				} else if(transitionTarget.getName().equals("end.workflow.export")){
					place = transitionTarget;
					exportData = serviceCall.getExport();
					break;
				}
				
				if(serviceCall.isCallWorkflow()){
					
					// Calls the subworkflow in WfMP
					
					// Prepares HTTP request and data to execute subworkflow in WfMP
					
					log.info("[WfMP] \t\t-> [WorkflowRequest] {}", transitionTarget.getName());
					PetriNetDTO subworkflow = managerService.getPetriNetData(transitionTarget.getName(), workflowRequest.getAccessToken(), hostManager);

					workflowRequest.setReference(subworkflow.getName());
					workflowRequest.setWorkflowMemory(buffer.getWorkflowMemory());

					HttpHeaders headers = new HttpHeaders();
					headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
					headers.add("Authorization", "Bearer "+workflowRequest.getAccessToken());

					RestTemplateConfig restTemplateAttributes = new RestTemplateConfig(
							headers,
							workflowRequest,
							hostManager,
							"/api/workflow/run",
							HttpMethod.POST,
							Boolean.TRUE
					);
					
					JSONObject json = new JSONObject(workflowRequest);
					log.info("[WfMP] \t\t-> [workflowRequest] {}", json.toString());
					
					// Make a call to WfMP
					syncCommunication.restRequest(restTemplateAttributes);
					
					// Waiting for the return of processed data in Subworkflow (getReturnTypeWorkflow)
					
					String workflowReturnType = getReturnTypeWorkflow(subworkflow, serviceCall.getEndPoint());
					buffer.getWorkflowMemory().put(workflowReturnType, buffer.getDataWorkflow(workflowReturnType));
				} else {
					
					// Retrieves the information to call the microservice
					MicroserviceDTO microservice = managerService.getMicroserviceData(transitionTarget.getName(), authorization, hostManager);
					buffer.setMicroservice(microservice);
					EndpointDTO endpoint = microservice.getEndPointByReference(serviceCall.getEndPoint());
					
					log.debug("\t\t-> Endpoint {}", endpoint);
					
					// Make the call to the endpoint or subscribe a queue
					
					if(endpoint!=null && "http".equalsIgnoreCase(endpoint.getProtocol())){ /* HTTP Communication */
						String data = (objectData != null ? objectData.toString() : "");
						RestTemplateConfig microserviceClientConfig = syncCommunication.getMicroserviceClientConfig(
								microservice, serviceCall.getEndPoint(), authorization, data);

						log.debug("\t\t-> [serviceCall.getParameter()] {}", serviceCall.getParameter());
						log.debug("\t\t-> [objectData] {}", data);
						log.debug("\t\t-> [httpClient] {}", microserviceClientConfig.getBody().toString());

						// TODO: serviceCall.getParameter() treatment
						ResponseEntity responseEntity = syncCommunication.restRequest(microserviceClientConfig);

						if (responseEntity.getBody() == null)
							throw new RuntimeException("Error communicating with external service");

						String responseData = responseEntity.getBody().toString();
						JSONObject httpResponse = new JSONObject(responseData);
						log.debug("\t\t-> [HTTPRequest] {} data {} response {}", serviceCall.getParameter(), data, httpResponse);
						log.info("[WfMP] \t\t-> [HTTPRequest] {}", serviceCall.getParameter());

						if(endpoint.getDataStructure()!=null && !"".equals(endpoint.getDataStructure()))
							buffer.getWorkflowMemory().put(endpoint.getDataStructure(), httpResponse);
					} else if(endpoint!=null && "amqp".equalsIgnoreCase(endpoint.getProtocol())){ /* AMQP Communication */
						buffer.getWorkflowMemory().put(endpoint.getDataStructure(), requestObject(endpoint.getDataStructure()));
					}
				}
				// go to next place
				place = transitionTarget;
				break;
			}
		}
	}
	
	/**
	 * 
	 *
	 */
	private JSONObject requestObject(String objectName){
		JSONObject value = null;
		try {
			value = buffer.getMessageQueueData(objectName);
		} catch (InterruptedException e) {
			log.error(e);
		}
		return value;
	}
	
	/**
	 * 
	 *
	 */
	public String getReturnTypeWorkflow(PetriNetDTO petriNet, String placeName){
		Queue<PlaceDTO> nextPlaces = new LinkedList<>();
		nextPlaces.add(petriNet.getStart()); // add first place
		String exportName = null;
		boolean placeFound = false;
		
		while(nextPlaces.size()!=0) {
			PlaceDTO place = nextPlaces.remove(); // remove a head place of the queue
			
			for (TransitionDTO transition : place.getTransitions()) {
				ActionDTO action = transition.getAction();
				List<ServiceCallDTO> calls = action.getCalls();
				
				if(place.getName().equals(placeName)) {
					placeFound = true;

					for (ServiceCallDTO serviceCall : calls) {
						if (serviceCall.getEndPoint().equals("end.workflow.export"))
							exportName = serviceCall.getExport();
					}
				}
				for(int i=0; i<calls.size(); i++) {
					nextPlaces.add(transition.getTargets().get(i));
				}
			}
			// if the place was found, the type is returned
			if(placeFound) return exportName;
		}
		return exportName;
	}
	
	public Buffer getBuffer() {
		return buffer;
	}
	
	public void setWorkflowRequest(WorkflowDTO workflowRequest) {
		this.workflowRequest = workflowRequest;
	}
}