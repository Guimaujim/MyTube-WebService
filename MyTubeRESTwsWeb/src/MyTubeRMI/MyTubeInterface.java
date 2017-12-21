package MyTubeRMI;

import java.rmi.*;
import java.io.*;
import java.util.List;
import java.util.Vector;

import classData.fileData;

//Interface of the server's implementation
public interface MyTubeInterface extends Remote {

    public byte[] download(String name, CallbackInterface c)
            throws java.rmi.RemoteException;

    public String upload(byte[] file, String title, String server_id, String description)
            throws java.rmi.RemoteException;

    public fileData[] find(String name)
            throws java.rmi.RemoteException;
    
    public void delete(String key)
            throws java.rmi.RemoteException;
    
    public void searchAndDeleteInit(String key)
            throws java.rmi.RemoteException;

    public void addCallback(
            CallbackInterface CallbackObject)
            throws java.rmi.RemoteException;

    public void unregisterForCallback(
            CallbackInterface callbackClientObject)
            throws java.rmi.RemoteException;
}
