package service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.print.attribute.HashAttributeSet;

import io.searchbox.cluster.Health;
import pojo.HealthPlan;

public class HealthPlanService {
	
	static HashMap<Integer,HealthPlan> healthPlanServiceMap = getHealthPlanIDMap();
	
	public HealthPlanService(){
		
		super();
		if(healthPlanServiceMap== null){
			
			healthPlanServiceMap = new HashMap<>();
			
			HealthPlan plan1 = new HealthPlan(1, "Internal Medicine", "Physical", "20");
			HealthPlan plan2 = new HealthPlan(2, "PhysioTherapy" , "Physio", "40");
			
			healthPlanServiceMap.put(1, plan1);
		    healthPlanServiceMap.put(2, plan2);
					System.out.println("max id: "+ getMaxId());	
		}
		
	}
	
	public List<HealthPlan> getAllHealthPlans(){
		List<HealthPlan> plans = new ArrayList<HealthPlan>(healthPlanServiceMap.values());
		System.out.println("max id: "+ getMaxId());	
		return plans;
	}
	
	public HealthPlan getPlan(int id){
		HealthPlan plan = healthPlanServiceMap.get(id);
		return plan;
	}
	
	public HealthPlan addHealthPlan(HealthPlan healthPlan){
		healthPlan.setUUID(getMaxId()+1);
		
		healthPlanServiceMap.put(healthPlan.getUUID(), healthPlan);
		return healthPlan;
		
	}
	
	public static int getMaxId(){
		
		int max = 0;
		for(int id:healthPlanServiceMap.keySet()){
			if(max<= id){
				max = id;
			}
		}
		return max;
		
	}
	
	public HealthPlan updatePlan(HealthPlan healthPlan){
		if(healthPlan.getUUID()<=0)
			return null;
		healthPlanServiceMap.put(healthPlan.getUUID(), healthPlan);
		return healthPlan;
	}
	
	public void deletePlan(int id){
		healthPlanServiceMap.remove(id);
	}
	
	public static HashMap<Integer, HealthPlan> getHealthPlanIDMap() {
		
		return healthPlanServiceMap;
	}
	
		
	

}
