package MyTubeRMI;

import java.rmi.*;
import java.rmi.server.*;
import java.util.Scanner;

import classData.fileData;

public class CallbackImpl extends UnicastRemoteObject
        implements CallbackInterface {

    public CallbackImpl() throws RemoteException {
        super();
    }

    //It sends a specific message to all clients
    @Override
    public String callMe(String message) throws RemoteException {
        String returnMessage = "Message from the server: " + message;
        System.out.println(returnMessage);
        return returnMessage;
    }
    
    public int chooseD(fileData[] af) throws RemoteException {
    	int i, j;
        String id;
        int n_id;
        Scanner reader = new Scanner(System.in);
        
    	System.out.println("These files have been found, please select the number of the one you want (type 0 to go back):");
    	
		for(i=0;i<af.length;i++){
			j = i+1;
			System.out.println("Name: " + af[i].getName() + ", description: " + af[i].getDescription() + ", server id: " + af[i].getServerId() + ", file number: " + j);
			
		}
		//Range list
        id = reader.nextLine();
        
        if(id.equals("0")){
        	return -1;
        }
        
        n_id = Integer.parseInt(id);
        n_id = n_id - 1;
        
        while(n_id < 0 || n_id > af.length-1){
        	System.out.println("Please select one of the available numbers");
            id = reader.nextLine();
            n_id = Integer.parseInt(id);
            n_id = n_id - 1;
        }

    	return n_id;
    }
}
