package MyTubeRMI;

import java.rmi.*;

import classData.fileData;

//Interface of the callback's implementation
public interface CallbackInterface extends Remote{

    public String callMe(String message) throws
            java.rmi.RemoteException;	
    
    public int chooseD(fileData[] af) throws RemoteException;
}
