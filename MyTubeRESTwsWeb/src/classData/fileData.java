package classData;

import java.io.Serializable;

public class fileData implements Serializable{
	public String key;
	public String name;
	public String serverId;
	public String description;
	
	public fileData(){}
	
	public fileData(String name, String serverId, String key, String description){
		this.key = key;
		this.name = name;
		this.serverId = serverId;
		this.description = description;
	}
	
	public void setKey(String key){
		this.key = key;
	}
	
	public String getKey(){
		return this.key;
	}
	
	public void setname(String name){
		this.name = name;
	}
	
	public String getName(){
		return this.name;
	}
	
	public void setServerId(String serverId){
		this.serverId = serverId;
	}
	
	public String getServerId(){
		return this.serverId;
	}
	
	public void setdescription(String description){
		this.description = description;
	}
	
	public String getDescription(){
		return this.description;
	}
	
	public String getJson(){
		return "{\"key\":\""+this.getKey()+"\",\"name\":\""+this.getName()+"\", \"serverId\":\""+this.getServerId()+"\", \"description\":\""+this.getDescription()+"\"}";
	}
	
}