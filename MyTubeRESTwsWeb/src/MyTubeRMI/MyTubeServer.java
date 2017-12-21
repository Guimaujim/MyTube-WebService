package MyTubeRMI;

import java.rmi.*;
import java.rmi.registry.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.core.MediaType;

import com.google.gson.Gson;

import classData.fileData;
import classData.serverData;

public class MyTubeServer {

    public static void main(String args[]) {
        File directory = new File("Database"); //Directory where the server will save its files
        directory.mkdir();
        Scanner reader = new Scanner(System.in);
        String IP;
        String port;
        String server_id;

        System.out.println("Enter the IP of the server:");
        IP = reader.nextLine();
        System.out.println("Enter the port of the server:");
        port = reader.nextLine();
        System.out.println("Enter the id for the server:");
        //get to check
        //check no spaces or delete spaces
        server_id = reader.nextLine();
        System.setProperty("java.rmi.server.hostname", IP); //Set so the clients can connect properly

        
        serverData sd = new serverData();
        sd.setIp(IP);
        sd.setPort(port);
        sd.setId(server_id);
        
        try {
            // Posting server on Database
            postServer(sd); 
        } catch (IOException ex) {
            Logger.getLogger(MyTubeImpl.class.getName())
                    .log(Level.SEVERE, null, ex);
        }

        //Same coding as the one done in class
        try {
            MyTubeImpl exportedObj = new MyTubeImpl();
            startRegistry(Integer.parseInt(port));
            String registryURL = "rmi://" + IP + ":" + port + "/mytube/" + server_id;
            Naming.rebind(registryURL, exportedObj);
            System.out.println("MyTube Server ready.");
        } catch (Exception ex) {
            System.out.println("Error!");
        }
    }

    //Same coding as the one done in class
    private static void startRegistry(int RMIPortNum)
            throws RemoteException {
        try {
            Registry registry = LocateRegistry.getRegistry(RMIPortNum);
            registry.list();
        } catch (RemoteException ex) {
            System.out.println(
                    "RMI registry cannot be located at port " + RMIPortNum);
            Registry registry = LocateRegistry.createRegistry(RMIPortNum);
            System.out.println(
                    "RMI registry created at port " + RMIPortNum);
        }
    }
    
    public static void postServer(serverData s) 
    		throws IOException{
    	 try {
             URL url = new URL ("http://localhost:8080/MyTubeRESTwsWeb/rest/server/");
             HttpURLConnection conn = (HttpURLConnection) url.openConnection();
             conn.setDoOutput(true);
             conn.setRequestMethod("POST");
             conn.setRequestProperty("Content-Type", "application/json");
             
             OutputStream os = conn.getOutputStream();
             os.write(s.getJson().getBytes());
             os.flush();
             
             int status = conn.getResponseCode();
             if(status != HttpURLConnection.HTTP_CREATED){ 
                 throw new IOException();
             }
             BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
 			 String id = br.readLine();
             conn.disconnect();
             
             
         } catch (IOException e) {
             System.out.println(e.toString());
         }  
    }
    
    public static serverData getServer(String server_Id){
    	try {
			URL url = new URL ("http://localhost:8080/MyTubeRESTwsWeb/rest/server/" + server_Id);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Accept", MediaType.APPLICATION_JSON);
			if(conn.getResponseCode() != 200){
				System.out.println(conn.getResponseCode());
				return null;
			}
			
			BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String output = br.readLine();
			conn.disconnect();
			
			Gson g = new Gson();
			serverData f = g.fromJson(output, serverData.class);
			f.setId(f.getId().trim());
			f.setIp(f.getIp().trim());
			f.setPort(f.getPort().trim());
			return f;
					
		} catch (Exception e) { 
			System.out.println(e);
			return null;
		}
    }
}
