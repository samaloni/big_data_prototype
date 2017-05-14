package hello;

import javax.servlet.http.HttpServletRequest;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import redis.clients.jedis.Jedis;

public class MessageUploader {
	
	Jedis jedis = new Jedis("127.0.0.1");
	JSONParser parser = new JSONParser();
	
	
	public void uploadMsg(HttpServletRequest request){
		
		JSONObject jsonObject = (JSONObject) request;
		String msgDetails = (String) jsonObject.get("msg"); 	 // Consider Json {"msg":"my complete msg","user":"1"}
			System.out.println("msgDetails have : " +msgDetails);
		String jsonPayload = jsonObject.toJSONString();
			System.out.println("jsonPayload have:" +jsonPayload);
		jedis.rpush("queue", jsonPayload);
		
		String stored = jedis.get("queue"); 
			System.out.println("stored queue has :" +stored);
	}

}
