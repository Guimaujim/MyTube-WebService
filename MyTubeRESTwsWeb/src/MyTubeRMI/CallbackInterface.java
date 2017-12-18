import java.rmi.*;

//Interface of the callback's implementation
public interface CallbackInterface extends Remote{

    public String callMe(String message) throws
            java.rmi.RemoteException;	
}
