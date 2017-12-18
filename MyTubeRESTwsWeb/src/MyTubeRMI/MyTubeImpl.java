package MyTubeRMI;

import java.rmi.*;
import java.rmi.server.*;
import java.util.*;
import java.io.*;

public class MyTubeImpl extends UnicastRemoteObject implements MyTubeInterface {

    private static Vector callbackObjects; //Vector for all the clients we need to do a callback
    private static Vector MyTubeServers; //Vector for all the servers currently connected

    public MyTubeImpl() throws RemoteException {
        super();
        callbackObjects = new Vector();
        MyTubeServers = new Vector();
    }

    //Adds client to callback vector
    @Override
    public void addCallback(CallbackInterface CallbackObject) {
        System.out.println("Server got an 'addCallback' call.");
        callbackObjects.addElement(CallbackObject);
    }

    //Adds servers to vector
    @Override
    public Vector addServerAll(MyTubeInterface MyTubeServer) {
        System.out.println("Server got a server request");
        for (int i = 0; i < MyTubeServers.size(); i++) {
            MyTubeInterface server
                    = (MyTubeInterface) MyTubeServers.elementAt(i);
            addServer(server);
        }
        MyTubeServers.addElement(MyTubeServer);
        return MyTubeServers;
    }

    //Auxiliary file to add servers
    public void addServer(MyTubeInterface MyTubeServer) {
        System.out.println("Server got a server request: " + MyTubeServer);
        MyTubeServers.addElement(MyTubeServer);
    }

    //Auxiliary file to update new server
    public void copyServers(Vector MyTubeServer) {
        MyTubeServers = MyTubeServer;
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
    public byte[] download(String name, boolean repeat) throws RemoteException {
        File folder = new File("Database");
        String path = "Database";
        File[] directory = folder.listFiles();
        File userFile;
        path = search(directory, path, name); //Server searches for the file

        if (path != null) {
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
        } else {
            if (repeat == true) {
                for (int i = 0; i < MyTubeServers.size(); i++) {
                    MyTubeInterface server
                            = (MyTubeInterface) MyTubeServers.elementAt(i);
                    byte buffer[] = server.download(name, false);

                    if (buffer.length != 0) {
                        return buffer;
                    }
                }
                return new byte[0];
            } else {
                return new byte[0];
            }
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
    public String upload(byte[] file, String name
    ) {
        String ID = UUID.randomUUID().toString(); //Server generates a random unique key for the file
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
            //Server does a callback to all clients anouncing that a new file has been uploaded
            return ID;
        } catch (IOException e) {
            System.out.println("Error!" + e.getMessage());
            return null;
        }
    }

    //Auxiliary method for server find so it can be a recursive method
    @Override
    public String find(String name, boolean repeat
    ) {
        File folder = new File("Database");
        String path = "Database";
        File[] directory = folder.listFiles();
        String result = "";
        ArrayList<String> StringArray = new ArrayList<String>();

        StringArray = serverFind(directory, path, name);

        for (int i = 0; i < StringArray.size(); i++) {
            if (i != StringArray.size() - 1) {
                result += StringArray.get(i) + " | ";
            } else {
                result += StringArray.get(i);
            }
        }
        result += " ";

        if (repeat == true) {
            try {
                for (int i = 0; i < MyTubeServers.size(); i++) {
                    MyTubeInterface server
                            = (MyTubeInterface) MyTubeServers.elementAt(i);
                    result += server.find(name, false);
                }
            } catch (Exception e) {
                System.out.println("Error! " + e);
            }
            return result;
        }

        return result;
    }

    //Searches for files that contain the given name
    public ArrayList<String> serverFind(File[] Files, String path, String name) {
        ArrayList<String> StringArray = new ArrayList<String>();

        for (File e : Files) {
            if (e.isDirectory()) {
                File folder = new File(path + "/" + e.getName());
                ArrayList<String> temp = serverFind(folder.listFiles(), path + "/" + folder.getName(), name);
                //If it's a directory the method calls itself again for that directory
                for (String s : temp) {
                    StringArray.add(s);
                }
            } else {
                if (e.getName().toLowerCase().contains(name.toLowerCase())) {
                    StringArray.add(e.getName());
                }
            }
        }

        return StringArray;
    }

    public void delete(String key) {
        File folder = new File("Database");
        String path = "Database";
        File[] directory = folder.listFiles();
        searchAndDelete(directory, path, key);

        try {
            for (int i = 0; i < MyTubeServers.size(); i++) {
                MyTubeInterface server
                        = (MyTubeInterface) MyTubeServers.elementAt(i);
                server.searchAndDeleteInit(key);
            }
        } catch (Exception e) {
            System.out.println("Error! " + e);
        }
    }

    //It anounces to all clients that a new file has been uploaded
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
}
