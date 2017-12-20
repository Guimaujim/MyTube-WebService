package classData;

public class serverData {
	public String server_id;
	public String ip;
	public String port;
	
	public serverData(){}
	
	public serverData(String id, String ip, String port){
		this.server_id = id;
		this.ip = ip;
		this.port = port;
	}
	
	public void setId(String id){
		this.server_id = id;
	}
	
	public String getId(){
		return this.server_id;
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
		return "{\"server_id\":\""+this.getId()+"\",\"ip\":\""+this.getIp()+"\", \"port\":\""+this.getPort()+"\"}";
	}
}