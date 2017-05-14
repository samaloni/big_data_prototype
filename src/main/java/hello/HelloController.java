package hello;

import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jackson.JsonLoader;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import com.github.fge.jsonschema.main.JsonValidator;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import pojo.Person;
import redis.clients.jedis.Jedis;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.security.Key;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.Path;
import javax.xml.bind.DatatypeConverter;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.http.HttpHeaders;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
public class HelloController {
    
    @RequestMapping("/")
    public String index() {
        return "Greetings from Spring Boot!";
    }
   //----------------------------TOKENS------------------------------------------------------------- 
     
    private static final String secret ="SECRET";
    private static final String subject = "ACCESS_TOKEN";
    private static final String issuer= "SUPER_ADMIN";
    
    public static final String uri = "http://localhost:9200/";
    
    @RequestMapping(value="/token", method= RequestMethod.POST)
    public String createAccessToken(@RequestParam("id") String id, @RequestParam("user")String user)
    {
    	SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;

        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);

       // String secret = "SECRET";
        //We will sign our JWT with our ApiKey secret
        byte[] apiKeySecretBytes = DatatypeConverter.parseBase64Binary(secret);
        Key signingKey = new SecretKeySpec(apiKeySecretBytes, signatureAlgorithm.getJcaName());

        //String id= "plan_0215";        
        
		//Let's set the JWT Claims
        JwtBuilder builder = Jwts.builder().setId(id)
                                    .setIssuedAt(now)
                                    .setSubject(subject)
                                    .claim("user", "unique-id-of-user")
                                    .setIssuer(issuer)
                                    .signWith(signatureAlgorithm, signingKey);

        // builder.setExpiration(getNextYearDate());
//        int ttlMillis=0;
//		//if it has been specified, let's add the expiration
//        if (ttlMillis >= 0) {
//        long expMillis = nowMillis + ttlMillis;
//            Date exp = new Date(expMillis);
//            builder.setExpiration(exp);
//        } 
        System.out.println("builder " + builder.compact());

        return builder.compact();
    }
    
    @RequestMapping(value="/token/validate", method= RequestMethod.GET)
    public Boolean isTokenValid(@RequestHeader String token,@RequestHeader String uuid)
    {
    	try{
    	Claims claims = Jwts.parser()         
    		       .setSigningKey(DatatypeConverter.parseBase64Binary(secret))
    		       .parseClaimsJws(token).getBody();
    		    System.out.println("ID: " + claims.getId());
    		    System.out.println("Subject: " + claims.getSubject());
    		    System.out.println("Issuer: " + claims.getIssuer());
    		    //System.out.println("Expiration: " + claims.getExpiration());
    		    System.out.println("User:" + claims.get("user"));
    		  
    		    if(claims.getId().equals(uuid)){
    		    	System.out.println("uuid is valid : "+uuid);
    		    	return Boolean.TRUE;
    		    }
    	}catch(Exception e){
    		e.printStackTrace();
    		return Boolean.FALSE;
    	}
    		    
    	return Boolean.FALSE;	    
    	
    	
    }
    
    
  //------------------------------------------------------------------------------------------  
    @RequestMapping(value="/Plan/{id}", method=RequestMethod.GET, consumes="application/json")
	public String getPlanById(@PathVariable("id") String id, HttpServletRequest httpRequest,HttpServletResponse httpResponse,
			@RequestHeader String token) throws Exception{
		
		JSONParser parser = new JSONParser();
    	Jedis jedis = new Jedis("127.0.0.1");
    	
    	Boolean result = isTokenValid(token, id);
    	
    	if(result == true){
    		String stored = jedis.get(id);	// getting data as a blob
        	System.out.println("stored is " +stored);
        	 
        	String eTag = httpRequest.getHeader("If-None-Match");
        	if(eTag != null){
        		if (!eTag.isEmpty() && stored.contains(eTag))
        		{
        			httpResponse.sendError(304, "Object is not changed");
        		}
        		}else {
        			return stored;
        		}
    	}else{
    		return "Token Invalid";
    	}
    	
    	
    	return null;
	}
    
    
    @RequestMapping(value="/Plan/schema", method=RequestMethod.POST, consumes="application/json")
    public String postSchema(@RequestBody String schemaBody) throws Exception{
    	JSONParser parser = new JSONParser();
    	Jedis jedis = new Jedis("127.0.0.1");
    	
    	JSONObject schemaObj = (JSONObject) parser.parse(schemaBody);
    	
    	jedis.set("schema", schemaBody);
    	
    	return schemaBody;
    }
    
    @RequestMapping(value="/Plan/schema", method=RequestMethod.GET, consumes="application/json")
    public String getSchema( HttpServletRequest httpRequest,HttpServletResponse httpResponse) throws Exception{
    	JSONParser parser = new JSONParser();
    	Jedis jedis = new Jedis("127.0.0.1");
    	
    	String storedSchema = jedis.get("schema");
    	System.out.println("Schema is : " +storedSchema);
    	
    	return storedSchema;
    }
    
    @RequestMapping(value="/Plan/schema", method=RequestMethod.PATCH, consumes="application/json")
    public String patchSchema(@RequestBody String patchSchema,@RequestParam String paramType) throws Exception, IOException{
    	
    	JSONParser parser = new JSONParser();
    	Jedis jedis = new Jedis("127.0.0.1");
    	Object obj = parser.parse(new FileReader("C:\\Users\\sampada\\Documents\\JsonSchema.json")); // json schema
    	JSONObject schemaObj = (JSONObject) obj;
    	
    	JSONObject properties = (JSONObject) schemaObj.get("properties");
    	
    	JSONObject patchObj = (JSONObject)parser.parse(patchSchema);
    	
    	System.out.println("patch obj : "+patchObj.toJSONString());
    	properties.put(paramType, patchObj);
    	
    	System.out.println("schema : " +schemaObj.toJSONString());
    	try{
    		FileWriter schemaFile = new FileWriter("C:\\Users\\sampada\\Documents\\JsonSchema.json");
    		schemaFile.write(schemaObj.toJSONString());
    		schemaFile.flush();
    		schemaFile.close();
    		
    	}catch(Exception e){
    		e.printStackTrace();
    	}
    	
    	return "";
    }
    
    
    @RequestMapping(value="/Plan", method=RequestMethod.POST, consumes="application/json")
    public String postPlan(@RequestHeader HttpHeaders httpHeaders, @RequestBody String healthPlan, HttpServletResponse response) throws Exception{
    	
    	JSONParser parser = new JSONParser();
    	Jedis jedis = new Jedis("127.0.0.1");
    	RestClient restClient = new RestClient();
    	    	
    	Object obj = parser.parse(new FileReader("C:\\Users\\sampada\\Documents\\JsonSchema.json")); // json schema
    	
    	JSONObject schemaObj = (JSONObject) obj;
    	   	
    	try {
    		validateJsonData(schemaObj.toJSONString(), healthPlan);			// string validation
    		JSONObject jsonObj = (JSONObject) parser.parse(healthPlan);
    		
    		String uuid = jsonObj.get("_type") + "__" +jsonObj.get("_id") + "__" + jsonObj.get("firstname");
    		System.out.println("UUID is : " +uuid);
    	//---Jwt----
    		String token = createAccessToken(uuid, jsonObj.get("firstname").toString());
    		System.out.println("token created: " +token);
    		
    		jsonObj.put("token", token);
    		
    	//---ETAG------------	
    		 MessageDigest messageDigest = MessageDigest.getInstance("MD5");
    	        byte[] bytesOfMessage = jsonObj.toJSONString().getBytes("UTF-8");
    	        byte[] thedigest = messageDigest.digest(bytesOfMessage);
    	        String eTag = thedigest.toString();
    		jsonObj.put("ETag", eTag);
    		
    		jedis.set(uuid, jsonObj.toJSONString());   
    		
    		
    		//ElasticSearch
    		String newuri = uri+"insurance.index/plan/" +jsonObj.get("_id");
    		jsonObj.put("id", jsonObj.get("_id"));
    		jsonObj.remove("_id");
    		jsonObj.put("type", jsonObj.get("_type"));
    		jsonObj.remove("_type");
    		
    		jedis.rpush("queue",jsonObj.toJSONString());
    		
    		// send to the indexer -- RestClient rc = new RestClient();
    		//List<String> range = jedis.lrange("queue", 0, -1);
    		//System.out.println("range is : " +range);
    		
    		restClient.post(newuri, jsonObj.toJSONString());
    		
    		
    		//System.out.println("rest client newuri get : " +restClient.get(newuri)); 
    		    		
    		response.addHeader("ETag", eTag);			
    		//System.out.println(jedis.get(uuid));
    		
		} catch (Exception e) {

			System.out.println(e.getMessage());
			System.out.println("entered catch");
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
    
    
    @RequestMapping(value ="/Plan/{id}", method = RequestMethod.PUT, consumes="application/json")
    public String updatePlan(@PathVariable("id") String id,@RequestBody String healthPlan) throws Exception{
    	
    	JSONParser parser = new JSONParser();
    	Jedis jedis = new Jedis("127.0.0.1");
    	
    	String stored = jedis.get(id);						// getting data as a blob
    	System.out.println("stored is " +stored);
    	
    	Object obj = parser.parse(new FileReader("C:\\Users\\sampada\\Documents\\JsonSchema.json"));	 // json schema
    	JSONObject schemaObj = (JSONObject) obj;
    	
    	try{
    		validateJsonData(schemaObj.toJSONString(), healthPlan);
    		
    		JSONObject storedObj = (JSONObject) parser.parse(stored);
    		String id_stored = (String) storedObj.get("_id");
    		
    		JSONObject requestObj = (JSONObject) parser.parse(healthPlan);
    		String id_request =(String) requestObj.get("_id");
    		
    		if(id_request.equals(id_stored)){
				System.out.println("entered if loop to check id's: req: " +id_request + " --stored :" +id_stored);
				
//				String uuid = "PLAN" + "__" +requestObj.get("_id") + "__" + requestObj.get("firstname");
				String uuid = requestObj.get("_type") + "__" +requestObj.get("_id") + "__" + requestObj.get("firstname");
	    		System.out.println("UUID is : " +uuid);
	    		
	    		jedis.set(uuid, healthPlan);
				
	    		System.out.println(jedis.get(uuid));
						
			}
			else
			{
				System.out.println("ID's do not Match");
				return "ID's do not match";
			}
    		
    		System.out.println(jedis.get(id_stored));
    		
    	}catch(Exception e){
    		
    		System.out.println(e.getMessage());
			return "BadRequest.JsonSchemaNotValidated";
    	}
    	
    	return "Plan modified successfully";
    }

    
    @SuppressWarnings("unchecked")
	@RequestMapping(value = "/Plan/{id}", method = RequestMethod.PATCH, consumes ="application/json")
	public String patchHealthPlan(@RequestHeader HttpHeaders headers,@PathVariable("id") String id, @RequestBody String request) 
			throws Exception{
		
		JSONParser parser = new JSONParser();
    	Jedis jedis = new Jedis("127.0.0.1");
    	
    	JSONObject storedObj = (JSONObject) parser.parse(jedis.get(id));
    	String stored = jedis.get(id);	// getting data as a blob
    	System.out.println("stored is " +stored);
    	
    	if(!stored.isEmpty()) {
    		System.out.println(" ID Found " +stored);
    	}else
    		System.out.println("ID not found");
    	
    	
    	Object obj = parser.parse(new FileReader("C:\\Users\\sampada\\Documents\\JsonSchema.json")); // json schema
    	JSONObject schemaObj = (JSONObject) obj;
    	JSONObject properties = (JSONObject) schemaObj.get("properties");
    	  
    	JSONObject jsonMap = new JSONObject();		//used for storing final output
    	
    	HashMap<String,Object> requestBody = (HashMap<String, Object>) parser.parse(request);	
		
		HashMap<String,List> map2 = new HashMap<>();
		HashMap map = new HashMap<>();
    	try{
    		    					
    		parse(storedObj, map, map2);
    		
    		String[] ids= id.split("__");
    	
    		String add_key = "__" +ids[0]+"__" +ids[1];
    		
    		System.out.println("Map2 completed parse : " +map2);
    		
    		HashMap<String,List> patchMap2 = new HashMap<>();
    		
    		HashMap<String,HashMap> patchMap = new HashMap<>();
    		requestBody.put("_id", ids[1]);
    					
    		requestBody.put("_type", ids[0]);
    		parse(requestBody, patchMap, patchMap2);
    		
    	/*for(String key:patchMap.keySet()){
    		if(patchMap2.keySet().isEmpty()){
    			if(patchMap.containsKey(key))
    				output.put(key, requestBody);		// string property
    		}
    	}*/
    		
    		for(String key: patchMap2.keySet()){
    			if(map2.containsKey(key)){
    				ArrayList<String> list = (ArrayList) patchMap2.get(key);
    				ArrayList maplist = (ArrayList) map2.get(key);
    				
    				for(String listKey: list){
    					if(map.containsKey(listKey)){
    						HashMap<String,Object> temp = (HashMap) map.get(listKey);
    						temp.putAll(patchMap.get(listKey));
    						map.put(listKey,temp);
    					}else{
    						map.put(listKey, patchMap.get(listKey));
    						System.out.println("new key " +listKey);
    					}
    				}
    				maplist.addAll(list);
    			//	map2.put(key, maplist);  //merge patchmap into map then add output for string properties
    				
    			}else{
    				map2.put(key, patchMap2.get(key));
    			}
    		}
    		
    		
    	   		    		   		
    		HashMap<String,Object> output = new HashMap<>(); 
    		output.putAll((Map<? extends String, ? extends Object>) map.get("__"+ids[0]+"__"+ids[1]));
    		
    		for(String key: map2.keySet()){
    			
    			ArrayList<HashMap> listMap = new ArrayList<>();
    			ArrayList<String> value = (ArrayList) map2.get(key);
    			System.out.println("patchMap2 key is: " +key);
    			
    			String[] sp = key.split("__");
				String addr = sp[3];	
    			
				for(String mapKey: value) {
					
					listMap.add((HashMap) map.get(mapKey));
				}
    			output.put(addr, listMap);
    			
    			
    		}
				/*
				if(map2.containsKey(key)){
    				ArrayList map2list = (ArrayList) map2.get(key);
    				ArrayList patchMap2list = (ArrayList) patchMap2.get(key);
    				
    				map2list.addAll(patchMap2list);
    				
    				map2.put(key,map2list);
    				System.out.println("map2 in if condition: " +map2);
    				
    				
    			//	map2.put(addr, map2list);
    				
    			}
    			else{
    				ArrayList patchMap2list = (ArrayList) patchMap2.get(key);
    				map2.put(key,patchMap2list);
    				System.out.println("map2 in else : " +map2);
    			}    			*/
    	
    		
    	/*	for(String key: patchMap.keySet()){
    				
    			Object value = patchMap.get(key);
    			System.out.println("patchMap key is: " +key);
    			
    			if(map.containsKey(key)){
    				HashMap mapHash = (HashMap) map.get(key);
    				HashMap patchMapHash = (HashMap) patchMap.get(key);
    				
    				mapHash.putAll(patchMapHash);
    				System.out.println("maphash if condition :" +mapHash);
    				   				
    			}
    			else{
    				
    				HashMap patchMapHash = (HashMap) patchMap.get(key);
    				
    				map.put(key,patchMapHash);
    				System.out.println("map in else condition: " +map);  	
    				*/
    				
    				
    				
    				jsonMap.putAll(output);
    				System.out.println("JSON Object map : " +jsonMap.toJSONString());
    				
    				jedis.set(id, jsonMap.toJSONString());
    			
    		System.out.println(map.toString());
    	    		
    		
    	}catch(Exception e){
    		e.printStackTrace();
    		return "BadRequest.PatchRequestNotValidated";    		   		
    	}
    
		
		return "Patch request successful" +jsonMap.toJSONString();
	}
    
    
    @SuppressWarnings("rawtypes")
	public String parse(HashMap<String, Object> storedObj,HashMap<String, HashMap> map, HashMap<String, List> map2) throws Exception{
		String id = (String) ((Map) storedObj).get("_id");;
		String type = (String) ((Map) storedObj).get("_type");
		HashMap temp = new HashMap();
		Iterator iterator = storedObj.keySet().iterator();
		
		for(String key: storedObj.keySet()){
			/*Map.Entry pairs= (Entry) iterator.next();
			String key= (String) pairs.getKey();*/		
			Object value = storedObj.get(key);
			System.out.println("key" +key);
			
			if(value instanceof Map){
				//recurse call the method again
				ArrayList list = new ArrayList<>();
				String value_id = (String) ((Map) value).get("_id");  
				String value_type = (String) ((Map) value).get("_type");
				String relation_key = "__" + type + "__" + id + "__" +key;
				
				String relation_value = "__"+value_type + "__" +value_id;
				
				list.add(relation_value);
				
						System.out.println(" relation_key" + relation_key);
				
				if(map2.containsKey(relation_key))
					list = (ArrayList) map2.get(relation_key);
					
				
				map2.put(relation_key, list);
				System.out.println(" Map2 contains: " +map2.toString());
				parse((HashMap)value, map, map2);	
					
				
			}else if(value instanceof List){
				// test the first entry
				
				Object entry = ((List) value).get(0);
				System.out.println("entry" +entry);
				
				if(entry instanceof Map){ // copy above
					
					
					for(int i=0; i <((List)value).size();i++){
						HashMap entityMap = (HashMap) ((List)value).get(i);
						
						ArrayList list = new ArrayList<>();
						String value_id = (String) ((Map) entityMap).get("_id");  
						String value_type = (String) ((Map) entityMap).get("_type");
						String relation_key = "__" + type + "__" + id + "__" +key;
						String relation_value = "__"+value_type + "__" +value_id;
						
						if(map2.containsKey(relation_key))
							list = (ArrayList) map2.get(relation_key);
						
						list.add(relation_value);
						map2.put(relation_key, list);
						System.out.println(" Map2 contains: " +map2.toString());
						parse((HashMap)entityMap, map, map2);
					}
				}
				
			}else{
					temp.put(key, (String) value);
					map.put("__" + type + "__" + id, temp);
					System.out.println("map contains " +map.toString());
					
				}
			
		}
		return null;
    }
}

//----------------------------------------------------------------------------
		/*String toChange = (String) patchObj.get("toChange");
	Object value = patchObj.get("value");
	
	
	
	System.out.println(" change " + toChange + " value " +value);
	if(properties.containsKey(toChange)){
		if (value instanceof String) {
			String new_value = (String) value;
			System.out.println(" changedvalue is : " +new_value);	
			
		}else if(value instanceof Map){
			Map new_mapValue = (Map) value;
			System.out.println(" changedvalue is : " +new_mapValue);	
		}
		else if(value instanceof List){
			List new_listValue = (List) value;
			System.out.println(" changedvalue is : " +new_listValue);	
		}
	
	storedObj.put(toChange,value);
	System.out.println("new object modified: " +storedObj.toJSONString());
	
	jedis.set(id,storedObj.toJSONString());
	
	}else{
		return "Property Not Found in JSON Schema";
	}
	
// for each key in map , store the key value as a map in redis. 

	    */	
       
    

