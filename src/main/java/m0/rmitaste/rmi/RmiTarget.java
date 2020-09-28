package m0.rmitaste.rmi;

import m0.rmitaste.utils.SimpleLogger;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.logging.Logger;

/**
 * The RmiTarget class provides methods to connect to RMI Registry
 *
 * @author Marcin Ogorzelski (mzero - @_mzer0)
 */

public class RmiTarget {
    // Logger
    private static Logger logger = SimpleLogger.getLog();


    /**
     * Connects to registry on the specified host:port and calls list method on it.
     *
     * @param host host for the remote registry
     * @param port port on which the registry accepts requests
     * @return reference to the remote object registry
     * @throws RemoteException in case if the reference could not be created
     */
    public static Registry getRegistryUnencrypted(String host, int port) throws RemoteException {
        Registry reg = LocateRegistry.getRegistry(host, port);
        reg.list();
        return reg;
    }

    /**
     * Connects to SSL registry on the specified host:port and calls list method on it.
     *
     * @param host host for the remote registry
     * @param port port on which the registry accepts requests
     * @return reference to the remote object registry
     * @throws RemoteException in case if the reference could not be created
     */
    public static Registry getRegistryEncrypted(String host, int port) throws RemoteException {
        Registry reg = LocateRegistry.getRegistry(host, port, new m0.rmitaste.rmi.RMISSLClientSocketFactory());
        reg.list();
        return reg;
    }

    /**
     * Connects to RMI registry by using getRegistryUnencrypted and getRegistryEncrypted methods
     *
     * @param host host for the remote registry
     * @param port port on which the registry accepts requests
     * @see #getRegistryUnencrypted(String, int)
     * @see #getRegistryEncrypted(String, int)
     * @return reference to the remote object registry
     * @throws RemoteException in case if the reference could not be created
     */
    public static Registry getRegistry(String host, int port) throws RemoteException {
        try{
            return RmiTarget.getRegistryUnencrypted(host, port);
        }catch(java.rmi.ConnectIOException ex){
            // If unencrypted fail try to use encrypted channel. If fail then probably this is not rmi service.
            return RmiTarget.getRegistryEncrypted(host, port);
        }
    }

    /*
     * Returns RmiRegistry object that has reference to remote registry.
     */

    /**
     * Connects to RMI registry by using getRegistry method
     *
     * @param host host for the remote registry
     * @param port port on which the registry accepts requests
     * @see #getRegistry(String, int)
     * @return RmiRegistry object that has reference to remote registry.
     * @throws RemoteException in case if the reference could not be created
     */
    public static RmiRegistry connect(String host, int port) throws RemoteException {
        Registry reg = RmiTarget.getRegistry(host, port);
        RmiRegistry rmiRegistry = new RmiRegistry(host, port);
        rmiRegistry.setReference(reg);
        return rmiRegistry;
    }

    /*
     * Returns RmiRegistry object that has reference to remote registry.
     */

    /**
     * Connects to RMI registry by using getRegistry method
     *
     * @param rmiRegistry RmiRegistry object that has host and port attributes set
     * @see #getRegistry(String, int)
     * @return RmiRegistry object that has reference to remote registry.
     * @throws RemoteException in case if the reference could not be created
     */
    public static RmiRegistry connect(RmiRegistry rmiRegistry) throws RemoteException{
        Registry reg = RmiTarget.getRegistry(rmiRegistry.getHost(), rmiRegistry.getPort());
        rmiRegistry.setReference(reg);
        return rmiRegistry;
    }

}
