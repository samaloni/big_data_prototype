package pojo;

import java.util.HashMap;
import java.util.Map;

public class HealthPlan {
	
	public int UUID;
	public String planTitle;
	public String planServices;
	public String cost;
	
		public HealthPlan(){
			
		}
	public HealthPlan(int uUID, String planTitle, String planServices, String cost) {
		super();
		this.UUID = uUID;
		this.planTitle = planTitle;
		this.planServices = planServices;
		this.cost = cost;
	}
	
	
	public int getUUID() {
		return UUID;
	}
	public void setUUID(int uUID) {
		this.UUID = uUID;
	}
	public String getPlanTitle() {
		return planTitle;
	}
	public void setPlanTitle(String planTitle) {
		this.planTitle = planTitle;
	}
	public String getPlanServices() {
		return planServices;
	}
	public void setPlanServices(String planServices) {
		this.planServices = planServices;
	}
	public String getCost() {
		return cost;
	}
	public void setCost(String cost) {
		this.cost = cost;
	}
	
	/*public HashMap<String,String> getPlanAsMap(){
		
		HashMap<String,String> planMap = new HashMap<>();
		
		planMap.put(planTitle, this.planTitle);
		planMap.put(planServices, this.planServices);
		planMap.put(cost, this.cost);
		return planMap;
		
	}
	*/
	
	
	@Override
	public String toString(){
		return "HealthPlan [UUID =" +UUID+ "planTitle= " +planTitle + ",planServices= " +planServices+ ",cost =" +cost+"]";
	}
	
	

}
