package MyTubeRMI;

import java.rmi.*;
import java.io.*;
import java.util.List;
import java.util.Vector;

import classData.fileData;

//Interface of the server's implementation
public interface MyTubeInterface extends Remote {

    public byte[] downloadName(String name, CallbackInterface c, String server_id)
            throws java.rmi.RemoteException;
    
    public byte[] downloadDescription(String name, CallbackInterface c, String server_id)
            throws java.rmi.RemoteException;
    
    public byte[] downloadKey(String name, CallbackInterface c, String server_id)
            throws java.rmi.RemoteException;
    
    public byte[] downloadNameDescription(String name, String description, CallbackInterface c, String server_id)
            throws java.rmi.RemoteException;

    public String upload(byte[] file, String title, String server_id, String description)
            throws java.rmi.RemoteException;

    public fileData[] findName(String name)
            throws java.rmi.RemoteException;
    
    public fileData[] findDescription(String description)
            throws java.rmi.RemoteException;
    
    public fileData findKey(String Key)
            throws java.rmi.RemoteException;
    
    public fileData[] findNameDescription(String name, String description)
            throws java.rmi.RemoteException;
    
    public void delete(String key)
            throws java.rmi.RemoteException;
    
    public void deleteInit(String key)
            throws java.rmi.RemoteException;

    public void addCallback(
            CallbackInterface CallbackObject)
            throws java.rmi.RemoteException;

    public void unregisterForCallback(
            CallbackInterface callbackClientObject)
            throws java.rmi.RemoteException;
}
