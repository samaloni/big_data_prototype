package hello;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jackson.JsonLoader;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import com.github.fge.jsonschema.main.JsonValidator;

import pojo.HealthPlan;
import redis.clients.jedis.Jedis;
import service.HealthPlanService;

@RestController
public class UseCaseController {

	HealthPlanService planService= new HealthPlanService();
	
	
	@RequestMapping(value = "/HealthPlans", method=RequestMethod.GET,consumes="application/json")
	public List<HealthPlan> getAllPlans(){
		List<HealthPlan> listAllPlans = planService.getAllHealthPlans();
		Jedis jedis = new Jedis("127.0.0.1");
		System.out.println("All Keys: " +jedis.keys("*"));
		System.out.println("All Keys: " +jedis.get("postNewPlan"));
		return listAllPlans;
	}
	
	@RequestMapping(value="/HealthPlans/{id}", method=RequestMethod.GET, consumes="application/json")
	public String getPlanById(@PathVariable("id") String id){
		
		JSONParser parser = new JSONParser();
    	Jedis jedis = new Jedis("127.0.0.1");
    	
    	String stored = jedis.get(id);	// getting data as a blob
    	System.out.println("stored is " +stored);
    	    	
		return stored;
	}
	
	@RequestMapping(value="/HealthPlans", method=RequestMethod.POST, consumes="application/json")
	public String addPlan(@RequestBody String healthPlan) throws Exception{
		
			
		JSONParser parser = new JSONParser();
		Object obj = parser.parse(new FileReader("C:\\Users\\sampada\\Documents\\planJsonSchema.json")); // json schema
    	
    	JSONObject schemaObj = (JSONObject) obj;
		
    	try {
    		Jedis jedis = new Jedis("127.0.0.1");
    		validateJsonData(schemaObj.toJSONString(), healthPlan);			// string validation
			JSONObject planObj = (JSONObject) parser.parse(healthPlan);
			
			String uuid = "PLAN" + "__"+ planObj.get("UUID")+"__" +planObj.get("planTitle");
					
			Long id = (Long) planObj.get("UUID");
			
																//jedis.set(id.toString(), mapper.writeValueAsString(healthPlan));
			//jedis.set(id.toString(), healthPlan);			   // id.toString() is the key stored in Jedis for value as JSON body
			
			jedis.set(uuid, healthPlan);		
			System.out.println(jedis.get(uuid));
			
			System.out.println("new uuid is : " +uuid);
			
		} catch (Exception e) {
			
			System.out.println(e.getMessage());
			return "BadRequest.JsonSchemaNotValidated";
		}
    			
		return "Plan added successfully";
	}
	
	
    private void validateJsonData(String jsonSchema, String jsonData) throws Exception {
        try {
            final JsonNode d = JsonLoader.fromString(jsonData);
            final JsonNode s = JsonLoader.fromString(jsonSchema);

            final JsonSchemaFactory factory = JsonSchemaFactory.byDefault();
            JsonValidator v = factory.getValidator();

            ProcessingReport report = v.validate(s, d);
            System.out.println(report);
            if (!report.toString().contains("success")) {
                System.out.println("Handle Errors over here");
                throw new Exception();
            }
        } catch (IOException e) {
            throw new Exception(
                    "Failed to validate json data", e);
        } catch (ProcessingException e) {
            throw new Exception(
                    "Failed to validate json data", e);
        }
    }
    
	
	@RequestMapping(value="/HealthPlans/{id}", method=RequestMethod.PUT, consumes="application/json")
	public String updatePlan(@PathVariable("id") String id, @RequestBody String healthPlan) throws Exception{
		
		JSONParser parser = new JSONParser();
    	Jedis jedis = new Jedis("127.0.0.1");
    	
    	String stored = jedis.get(id);	// getting data as a blob
    	System.out.println("stored is " +stored);
    	
    	
    	//---------------- validator to modify object ---
		
    			
    	Object obj = parser.parse(new FileReader("C:\\Users\\sampada\\Documents\\planJsonSchema.json")); // json schema
    	   	
    	JSONObject schemaObj = (JSONObject) obj;
    			
    	try {
    	    		
    	   		validateJsonData(schemaObj.toJSONString(), healthPlan); 
    			System.out.println("data validated ");
    			
    			JSONObject storedObj = (JSONObject) parser.parse(stored);
    			Long id_stored = (Long) storedObj.get("UUID");
    			
    			JSONObject requestObj = (JSONObject) parser.parse(healthPlan);		
    			Long id_request =(Long) requestObj.get("UUID");
    			
    			if(id_request.equals(id_stored)){
    				System.out.println("entered if loop to check id's: req: " +id_request + " stored :" +id_stored);
    				jedis.set(id_stored.toString(),healthPlan);			
    			}
    			else
    			{
    				System.out.println("ID's do not Match");
    				return "ID's do not match";
    			}
    			
    			System.out.println(jedis.get(id_request.toString()));
    				
    			} catch (Exception e) {
    				// TODO Auto-generated catch block
    				//e.printStackTrace();
    				System.out.println(e.getMessage());
    				return "BadRequest.JsonSchemaNotValidated";
    			}
    	    	
    			
    		return "Plan modified successfully";
    		
    	
    	
		
		//return planService.updatePlan(healthPlan);
	}
	
	
		
	
	
	
	
	//@RequestMapping(value = "/HealthPlans/{id}", method = RequestMethod.PATCH, consumes ="application/json")
	public void patchPlan(@RequestHeader HttpHeaders headers,@PathVariable("id") String id, @RequestBody String request) throws Exception{
		
		JSONParser parser = new JSONParser();
    	Jedis jedis = new Jedis("127.0.0.1");
    	
		String stored = jedis.get(id);				// getting data as a blob from jedis
    	System.out.println("stored is " +stored);		// stored is the json object for the ID entered
    	
    	JSONObject storedObj = (JSONObject) parser.parse(stored);
    	
    	
    	Iterator iterator =  (Iterator) storedObj.entrySet();
		while(iterator.hasNext()){
			Map.Entry pairs= (Entry) iterator.next();
			String key= (String) pairs.getKey();
			Object value = pairs.getValue();
			
			if(value instanceof List){
				Object entry = ((List) value).get(0);
				System.out.println("entry");
				
				if(entry instanceof Map){
					
				}
				else if(entry instanceof List){
					
				}
				
			}
			else if(value instanceof Map){
				
			}else 
			{
				
			}
		}

    	
    	JSONObject requestObj = (JSONObject) parser.parse(request);
    	
    	
    	Object obj = parser.parse(new FileReader("C:\\Users\\sampada\\Documents\\planJsonSchema.json")); // json schema
    	JSONObject schemaObj = (JSONObject) obj;
    	 	
	}
	
	
	
	
	public String parse(HashMap storedObj,HashMap<String, HashMap> map, HashMap<String, List> map2,String map2Key ) throws Exception{
		String id = null;
		String type = null;
		HashMap temp = new HashMap();
		Iterator iterator =  (Iterator) storedObj.entrySet();
		while(iterator.hasNext()){
			Map.Entry pairs= (Entry) iterator.next();
			String key= (String) pairs.getKey();
			Object value = pairs.getValue();
			
			if(value instanceof List){
				Object entry = ((List) value).get(0);
				System.out.println("entry");
				
				if(entry instanceof Map){
					id = (String) ((Map) entry).get("UUID");
					type = (String) ((HashMap) entry).get("type");
					
					parse((HashMap) value,map,map2, "__" + type + "__" + id + "__"+key);
					
				}
				else if(entry instanceof List){
					
				}
				
			}
			else if(value instanceof Map){
				parse((HashMap) value,map,map2,"__" + type + "__" + id);
				
				
			}else 
			{
			
				if(key.contentEquals("UUID")){
					id = key;
					
				}
				temp.put(key, (String) value);
				if(key.contentEquals("type")){				// add type to the schma
					type = key;
					
				}
			}
		}
		if(!temp.isEmpty())
			map.put("__" + type + "__" + id, temp);
			
		ArrayList list;
		if(!map2Key.isEmpty()){
			 list = (ArrayList) map2.get(map2Key);
			 if(list == null){
				 list = new ArrayList();
			 }
			 list.add("__" + type + "__" + id);
			 
		}else{
			 list = new ArrayList();
			 list.add("__" + type + "__" + id);
			map2.put(map2Key, list);
		}
		
		return null;
	}
	
	@RequestMapping(value="/healthPlan/{id}", method=RequestMethod.DELETE, consumes="application/json")
	public void deletePlan(@RequestHeader HttpHeaders headers,@PathVariable("id") String id ) throws Exception{
		JSONParser parser = new JSONParser();
    	
    	Jedis jedis = new Jedis("127.0.0.1");   
    	
    	jedis.del(id);
    	jedis.flushAll();
		
		//planService.deletePlan(id);
    	
	}
	
	
	
	public String addPlanToJedis(String healthPlan) throws Exception{
		
		JSONParser parser = new JSONParser();
		
		Jedis jedis = new Jedis("127.0.0.1");  
		System.out.println(healthPlan);
		
		JSONObject planObj = (JSONObject) parser.parse(healthPlan);
		
		jedis.set("testPlanObj", planObj.toJSONString());
		
		System.out.println(jedis.get("testPlanObj"));
		return planObj.toJSONString();
		
	}
	
	
	@RequestMapping(value="/postHealthPlan", method=RequestMethod.POST, consumes ="application/json")
	public String postHealthPlan(@RequestHeader HttpHeaders httpHeaders, @RequestBody String body) throws Exception{
		
		JSONParser parser = new JSONParser();
		
		//Jedis jedis = new Jedis("127.0.0.1");  
		System.out.println(body);
		
		JSONObject planObj = (JSONObject) parser.parse(body); 
		
		
		//jedis.set("jedisPlanKey", planObj.toJSONString());
		//System.out.println(jedis.get("jedisPlanKey"));
		
		return "planObj.toJSONString()";
	}
	
	
	
	
	
	
	
	
}
