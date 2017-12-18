package classData;

public class serverData {
	public String id;
	public String ip;
	public String port;
	
	public serverData(){}
	
	public serverData(String id, String ip, String port){
		this.id = id;
		this.ip = ip;
		this.port = port;
	}
	
	public void setId(String id){
		this.id = id;
	}
	
	public String getId(){
		return this.id;
	}
	
	public void setIp(String ip){
		this.ip = ip;
	}
	
	public String getIp(){
		return this.ip;
	}
	
	public void setPort(String port){
		this.port = port;
	}
	
	public String getPort(){
		return this.port;
	}
	
	public String getJson(){
		return "{\"id\":\""+this.getId()+"\",\"ip\":\""+this.getIp()+"\", \"port\":\""+this.getPort()+"\"}";
	}
}