
import java.rmi.*;
import java.rmi.registry.*;
import java.io.*;
import java.net.MalformedURLException;
import java.util.Scanner;
import java.util.Vector;

public class MyTubeServer {

    public static void main(String args[]) {
        File directory = new File("Database"); //Directory where the server will save its files
        directory.mkdir();
        Scanner reader = new Scanner(System.in);
        String IP;
        String port;

        System.out.println("Enter the IP of the server:");
        IP = reader.nextLine();
        System.out.println("Enter the port of the server:");
        port = reader.nextLine();
        System.setProperty("java.rmi.server.hostname", IP); //Set so the clients can connect properly

        //Same coding as the one done in class
        try {
            MyTubeImpl exportedObj = new MyTubeImpl();
            startRegistry(Integer.parseInt(port));
            String registryURL = "rmi://" + IP + ":" + port + "/mytube";
            Naming.rebind(registryURL, exportedObj);
            joinServer(exportedObj);
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

    //Let's servers join together
    public static void joinServer(MyTubeImpl self)
            throws RemoteException, NotBoundException, MalformedURLException {
        String port;
        String IP;
        Vector list = new Vector();
        Scanner reader = new Scanner(System.in);

        System.out.println("Do you want this server to connect to another?");
        String s = reader.nextLine();
        if ("y".equals(s) || "yes".equals(s)) {
            System.out.println("Enter the IP of the server:");
            IP = reader.nextLine();

            System.out.println("Enter the port of the server:");
            port = reader.nextLine();

            String registryURL = "rmi://" + IP + ":" + port + "/mytube";
            MyTubeInterface i = (MyTubeInterface) Naming.lookup(registryURL);
            list = i.addServerAll(self);
            self.copyServers(list);
        }
    }
}
