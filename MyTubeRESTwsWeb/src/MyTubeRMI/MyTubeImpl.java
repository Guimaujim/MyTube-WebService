package MyTubeRMI;

import java.rmi.*;
import java.rmi.server.*;
import java.util.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import classData.fileData;
import classData.serverData;

import javax.ws.rs.core.MediaType;

import com.google.gson.Gson;

public class MyTubeImpl extends UnicastRemoteObject implements MyTubeInterface {

    private static Vector callbackObjects; //Vector for all the clients we need to do a callback

    public MyTubeImpl() throws RemoteException {
        super();
        callbackObjects = new Vector();
    }

    //Adds client to callback vector
    @Override
    public void addCallback(CallbackInterface CallbackObject) {
        System.out.println("Server got an 'addCallback' call.");
        callbackObjects.addElement(CallbackObject);
    }

    //Removes client from callback vector
    @Override
    public void unregisterForCallback(
            CallbackInterface callbackClientObject)
            throws java.rmi.RemoteException {
        if (callbackObjects.removeElement(callbackClientObject)) {
            System.out.println("Unregistered client ");
        } else {
            System.out.println("Unregister: client wasn't registered.");
        }
    }

    //Sends file to client
    @Override
    public byte[] download(String name, CallbackInterface c, String server_id) throws RemoteException {  	
    	fileData[] af = getFileByNameAndServer(name, server_id);

        if (af != null) {
        	if(af.length > 1){
        		int j = c.chooseD(af);
	            String path = "Database/" + af[j].getKey() + "/" + name;
	            File userFile;
	            userFile = new File(path);
	            byte buffer[] = new byte[(int) userFile.length()]; //Server converts file into an array of bytes to be sent
	            try {
	                BufferedInputStream input = new BufferedInputStream(new FileInputStream(path));
	                input.read(buffer, 0, buffer.length);
	                input.close();
	                return (buffer); //Server sends the array of bytes
	            } catch (IOException e) {
	                System.out.println("Error!");
	                return new byte[0];
	            }
        	}else{
	            String path = "Database/" + af[0].getKey() + "/" + name;
	            File userFile;
	            userFile = new File(path);
	            byte buffer[] = new byte[(int) userFile.length()]; //Server converts file into an array of bytes to be sent
	            try {
	                BufferedInputStream input = new BufferedInputStream(new FileInputStream(path));
	                input.read(buffer, 0, buffer.length);
	                input.close();
	                return (buffer); //Server sends the array of bytes
	            } catch (IOException e) {
	                System.out.println("Error!");
	                return new byte[0];
	            }
        	}
        } else {
			af = getFileByName(name);
			int num = c.chooseD(af);
			
			if(num == -1){
				return new byte[0];
			}
			
			serverData sd = getServer(af[num].getServerId());
			
            try {
                String registryURL = "rmi://" + sd.getIp() + ":" + sd.getPort() + "/mytube/" + sd.getId();
				MyTubeInterface i = (MyTubeInterface) Naming.lookup(registryURL);
				return i.download(name, c, sd.getId());
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (NotBoundException e) {
				e.printStackTrace();
			}
        	return null;
        }
    }

    //Auxiliary method so the server can find the files
    public String search(File[] Files, String path, String name) {
        String found = null;
        for (File e : Files) {
            if (e.isDirectory()) {
                File folder = new File(path + "/" + e.getName());
                found = search(folder.listFiles(), path + "/" + folder.getName(), name);
                if (found != null) {
                    return found;
                }
            } else {
                if (e.getName().equalsIgnoreCase(name)) {
                    return path + "/" + name;
                }
            }
        }
        return found;
    }
    
    public void delete(String key) throws RemoteException {
		fileData f = getFileByKey(key);
		serverData sd = getServer(f.getServerId());
		
		try {
	        String registryURL = "rmi://" + sd.getIp() + ":" + sd.getPort() + "/mytube/" + sd.getId();
			MyTubeInterface i = (MyTubeInterface) Naming.lookup(registryURL);
			i.searchAndDeleteInit(key);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (NotBoundException e) {
			e.printStackTrace();
		}
    }

    @Override
    public void searchAndDeleteInit(String key) {
        File folder = new File("Database");
        String path = "Database";
        File[] directory = folder.listFiles();
        searchAndDelete(directory, path, key);    
    }

    public void searchAndDelete(File[] Files, String path, String key) {
        for (File e : Files) {
            if (e.isDirectory() && e.getName().equals(key)) {
                deleteDirectory(e);
                e.delete();
            } else if (e.isDirectory()) {
                File folder = new File(path + "/" + e.getName());
                searchAndDelete(folder.listFiles(), path + "/" + folder.getName(), key);
            }
        }
        deleteFile(key);
    }

    public void deleteDirectory(File path) {
        File[] files = path.listFiles();
        for (int i = 0; i < files.length; i++) {
            if (files[i].isDirectory()) {
                deleteDirectory(files[i]);
            } else {
                files[i].delete();
            }
        }
    }

    //Receives and saves file from client
    @Override
    public String upload(byte[] file, String name, String server_id, String description) {
        String ID;
        
        fileData f = new fileData();
        f.setKey("");
        f.setName(name);
        f.setServerId(server_id);
        f.setDescription(description);
        
        try {
            // Posting File on Database
            ID = postFile(f); 
            
            //get to check
            File dir = new File("Database/" + ID);
            dir.mkdir();
            String path = "Database/" + ID + "/" + name;
            
            try {
                FileOutputStream FOS = new FileOutputStream(path);
                BufferedOutputStream Output = new BufferedOutputStream(FOS);
                Output.write(file, 0, file.length);
                Output.flush();
                Output.close();
                //Server receives the byte array and flushes it into a file
                callback();
                //Server does a callback to all clients announcing that a new file has been uploaded
                return ID;
            } catch (IOException e) {
                System.out.println("Error!" + e.getMessage());
                return null;
            }
        } catch (IOException ex) {
            Logger.getLogger(MyTubeImpl.class.getName())
                    .log(Level.SEVERE, null, ex);
            return null;
        }
    }
    
    public String postFile(fileData f) 
    		throws IOException{
    	 try {
             URL url = new URL ("http://localhost:8080/MyTubeRESTwsWeb/rest/file/");
             HttpURLConnection conn = (HttpURLConnection) url.openConnection();
             conn.setDoOutput(true);
             conn.setRequestMethod("POST");
             conn.setRequestProperty("Content-Type", "application/json");
             
             OutputStream os = conn.getOutputStream();
             os.write(f.getJson().getBytes());
             os.flush();
             
             int status = conn.getResponseCode();
             if(status != HttpURLConnection.HTTP_CREATED){ 
                 throw new IOException();
             }
             BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
 			 String key = br.readLine();
             conn.disconnect();
             return key;
             
         } catch (IOException e) {
             System.out.println(e.toString());
             return null;
         }  
    }

    //Auxiliary method for server find so it can be a recursive method
    @Override
    public fileData[] findName(String name) {	
    	return getFileByName(name);
    }
    
    @Override
    public fileData[] findDescription(String description) {	
    	return getFileByDescription(description);
    }

    //It announces to all clients that a new file has been uploaded
    private void callback() {
        for (int i = 0; i < callbackObjects.size(); i++) {
            System.out.println("Now performing the " + i + "th callback\n");
            CallbackInterface client
                    = (CallbackInterface) callbackObjects.elementAt(i);
            try {
                client.callMe("New content has been uploaded!");
            } catch (Exception e) {
                System.out.println("Client has disconnected, removing from callback list");
                try {
                    unregisterForCallback(client);
                } catch (Exception e2) {
                    System.out.println("Error!");
                }
            }
        }
    }
    
    public fileData[] getFileByName(String name){
    	try {
			URL url = new URL ("http://localhost:8080/MyTubeRESTwsWeb/rest/filen/" + name);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Accept", MediaType.APPLICATION_JSON);
			if(conn.getResponseCode() != 200){
				System.out.println(conn.getResponseCode());
				return null;
			}
			int i;
			BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String output = br.readLine();
			conn.disconnect();
			
			Gson g = new Gson();
			fileData[] af = g.fromJson(output, fileData[].class);
			for(i=0;i<af.length;i++){
				af[i].setKey(af[i].getKey().trim());
				af[i].setName(af[i].getName().trim());
				af[i].setDescription(af[i].getDescription().trim());
				af[i].setServerId(af[i].getServerId().trim());
			}
			return af;		
		} catch (Exception e) { 
			System.out.println(e);
			return null;
		}
    }
    
    public fileData[] getFileByNameAndServer(String name, String server_id){
    	try {
			URL url = new URL ("http://localhost:8080/MyTubeRESTwsWeb/rest/filenas/" + name + "_" + server_id);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Accept", MediaType.APPLICATION_JSON);
			if(conn.getResponseCode() != 200){
				System.out.println(conn.getResponseCode());
				return null;
			}
			int i;
			BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String output = br.readLine();
			conn.disconnect();
			
			Gson g = new Gson();
			fileData[] af = g.fromJson(output, fileData[].class);
			for(i=0;i<af.length;i++){
				af[i].setKey(af[i].getKey().trim());
				af[i].setName(af[i].getName().trim());
				af[i].setDescription(af[i].getDescription().trim());
				af[i].setServerId(af[i].getServerId().trim());
			}
			return af;		
		} catch (Exception e) { 
			System.out.println(e);
			return null;
		}
    }
    
    public fileData[] getFileByDescription(String description){
    	try {
			URL url = new URL ("http://localhost:8080/MyTubeRESTwsWeb/rest/filed/" + description);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Accept", MediaType.APPLICATION_JSON);
			if(conn.getResponseCode() != 200){
				System.out.println(conn.getResponseCode());
				return null;
			}
			int i;
			BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String output = br.readLine();
			conn.disconnect();
			
			Gson g = new Gson();
			fileData[] af = g.fromJson(output, fileData[].class);
			for(i=0;i<af.length;i++){
				af[i].setKey(af[i].getKey().trim());
				af[i].setName(af[i].getName().trim());
				af[i].setDescription(af[i].getDescription().trim());
				af[i].setServerId(af[i].getServerId().trim());
			}
			return af;				
		} catch (Exception e) { 
			System.out.println(e);
			return null;
		}
    }
    
    public fileData getFileByKey(String key){
    	try {
			URL url = new URL ("http://localhost:8080/MyTubeRESTwsWeb/rest/filek/" + key);
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
			fileData f = g.fromJson(output, fileData.class);
			f.setKey(f.getKey().trim());
			f.setName(f.getName().trim());
			f.setDescription(f.getDescription().trim());
			f.setServerId(f.getServerId().trim());
			return f;	
		} catch (Exception e) { 
			System.out.println(e);
			return null;
		}
    }
    
    public void deleteFile(String key){
    	try {
			URL url = new URL ("http://localhost:8080/MyTubeRESTwsWeb/rest/file/" + key + "/delete");
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("DELETE");
			conn.setRequestProperty("Accept", MediaType.APPLICATION_JSON);
			if(conn.getResponseCode() != 204){
				System.out.println(conn.getResponseCode());
			}				
		} catch (Exception e) { 
			System.out.println(e);
		}
    }
    
    public serverData getServer(String id){
    	try {
			URL url = new URL ("http://localhost:8080/MyTubeRESTwsWeb/rest/server/" + id);
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
			serverData as = g.fromJson(output, serverData.class);
			as.setId(as.getId().trim());
			as.setPort(as.getPort().trim());
			as.setIp(as.getIp().trim());
			return as;			
		} catch (Exception e) { 
			System.out.println(e);
			return null;
		}
    }
}
