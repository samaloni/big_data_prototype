package pojo;

import java.util.HashMap;
import java.util.Map;

import javax.validation.constraints.Size;

public class Person {

	
	private String firstName;
	private String lastName;
	private String address;
	
	public Person(String firstName,String lastName,String address){
		this.firstName= firstName;
		this.lastName = lastName;
		this.address = address;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public Map<String,String> getPersonAsMap(){
		
		Map<String, String> personmap = new HashMap();
		personmap.put("firstName", this.firstName);
		personmap.put("lastName", this.lastName);
		personmap.put("address", this.address);
		return personmap;
	}
	
	@Override
	public String toString(){
		return "Person[firstName " + firstName + ",lastName " +lastName + ",address " +address+ " ]";
	}
}
