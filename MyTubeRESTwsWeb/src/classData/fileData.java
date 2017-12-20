package classData;

import java.io.Serializable;

public class fileData implements Serializable{
	public String key;
	public String name;
	public String server_id;
	public String description;
	
	public fileData(){}
	
	public fileData(String name, String serverId, String key, String description){
		this.key = key;
		this.name = name;
		this.server_id = serverId;
		this.description = description;
	}
	
	public void setKey(String key){
		this.key = key;
	}
	
	public String getKey(){
		return this.key;
	}
	
	public void setName(String name){
		this.name = name;
	}
	
	public String getName(){
		return this.name;
	}
	
	public void setServerId(String serverId){
		this.server_id = serverId;
	}
	
	public String getServerId(){
		return this.server_id;
	}
	
	public void setDescription(String description){
		this.description = description;
	}
	
	public String getDescription(){
		return this.description;
	}
	
	public String getJson(){
		return "{\"key\":\""+this.getKey()+"\",\"name\":\""+this.getName()+"\", \"description\":\""+this.getDescription()+"\", \"server_id\":\""+this.getServerId()+"\"}";
	}
	
}