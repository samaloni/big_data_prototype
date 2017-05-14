/*package hello;

import java.util.List;

import redis.clients.jedis.Jedis;

public class MessageConsumer {
	
	public static void main(String[] args){
		
		Jedis jedis = new Jedis("localhost");
		List<String> messages = null;
		
		while(true){
			System.out.println("Waiting for the message in the queue..");
			messages = jedis.blpop(0, "queue");
			System.out.println("Got the message");
			System.out.println("KEY: " +messages.get(0)+ "VALUE : " +messages.get(1));
			String payload = messages.get(1);
			System.out.println("Payload is : " +payload);
		}
	}

}
*/