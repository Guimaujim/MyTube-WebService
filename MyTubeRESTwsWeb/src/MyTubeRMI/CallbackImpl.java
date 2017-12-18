
import java.rmi.*;
import java.rmi.server.*;

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
}
