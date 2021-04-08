package br.health.workflow.core.security;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class Key {
	
	/**
	 * 
	 * Keys used by Json Web Token of according with user profile
	 * 
	 */
	
	private static final String HEALTH_PROFESSIONAL = "key1-dfdsf344r34";
	
	private static final String PATIENT = "key2-75665gdsdf3s";
	
	private static final String ADMIN = "key3-frf8764bkpo";
	
	public static String getKeyByProfile(int profile){
		
		switch(profile){
			case 1:
				return HEALTH_PROFESSIONAL;
			case 2:
				return PATIENT;
			case 3:
				return ADMIN;
			default:
				return null;
		}
	}
	
	/**
	 * 
	 * Filter: <'method:service'><'List profiles'>
	 * 
	 */
	
	private static final HashMap<String, List<String>> filter = new HashMap<String, List<String>>() {
		
		private static final long serialVersionUID = 1L;

	{
		// workflows
		put("POST:worker/register", new ArrayList<>(Arrays.asList(HEALTH_PROFESSIONAL)));
		put("POST:worker/run", 		new ArrayList<>(Arrays.asList(HEALTH_PROFESSIONAL, PATIENT)));
		put("POST:worker/publish", 	new ArrayList<>(Arrays.asList(HEALTH_PROFESSIONAL, PATIENT)));
		put("POST:worker/subscribe", new ArrayList<>(Arrays.asList(HEALTH_PROFESSIONAL, PATIENT)));
		put("POST:worker/test", new ArrayList<>(Arrays.asList(HEALTH_PROFESSIONAL, PATIENT)));
		
		// microservices
		put("GET:microservice",		new ArrayList<>(Arrays.asList(ADMIN)));
		put("POST:microservice", 	new ArrayList<>(Arrays.asList(ADMIN)));
		put("PUT:microservice", 	new ArrayList<>(Arrays.asList(ADMIN)));
		put("DELETE:microservice", 	new ArrayList<>(Arrays.asList()));
		
		// amqp tests
		put("POST:amqp/publish", 	new ArrayList<>(Arrays.asList(ADMIN)));
		put("POST:amqp/subscribe", 	new ArrayList<>(Arrays.asList(ADMIN)));
		put("POST:amqp/exchange", 	new ArrayList<>(Arrays.asList(ADMIN)));
		put("POST:amqp/queue", 		new ArrayList<>(Arrays.asList(ADMIN)));
		put("DELETE:amqp/queue", 	new ArrayList<>(Arrays.asList(ADMIN)));
	}};
	
	public static boolean isAllowed(String method, String service, String key){
		if(key.equals(ADMIN)) return true;
		
		List<String> profiles = filter.get(method+":"+service);
		
		for (String value : profiles) {
			if(value.equals(key))
				return true;
		}
		return false;
	}
}