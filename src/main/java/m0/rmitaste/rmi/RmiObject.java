package m0.rmitaste.rmi;

import m0.rmitaste.utils.SimpleLogger;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.RemoteRef;
import java.rmi.server.RemoteStub;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

/**
 * RmiObject holds reference to remote object and allows to call methods on this object
 *
 * @author Marcin Ogorzelski (mzero - @_mzer0)
 */

public class RmiObject implements Serializable {
    // Logger
    private static Logger logger = SimpleLogger.getLog();
    // Remote object name
    private String name;
    // Address and port of registry in which remote object is stored
    private String endpoint;
    // HashMap of classess that remote object implements or extends
    private HashMap<String, RmiObjectClass> classes;
    // True - JMX registry; False - not JMX
    private boolean isJMX;
    // Reference to remote object
    private Remote remoteObject;
    // True - if remote object has dynamic stub - False if remote object has static stub
    private boolean isDynamicStub;


    public RmiObject(String nameL){
        name = nameL;
        classes = new HashMap<String, RmiObjectClass>();
        isJMX = false;
        isDynamicStub = true;
    }

    public String getName() { return name; }
    public boolean isJMX() {
        return isJMX;
    }

    public void setJMXFlag() {
        isJMX = true;
    }

    public void clearJMXFlag() {
        isJMX = false;
    }

    /**
     * Adds Class that remote object implements or extends
     * @param classname class name
     * @param isInterface true if class represents interface and false if not
     */
    public void addClass(String classname, boolean isInterface) {
        if(classname.equals("java.rmi.server.RemoteStub")){
            this.isDynamicStub = false;
            logger.info("Static stub has been detected in "+this.getName() +" remote object.");
        }

        RmiObjectClass rmiObjectClass = new RmiObjectClass(classname, isInterface);
        this.classes.put(classname, rmiObjectClass);
    }

    /**
     * Returns list of classes that remote object implements or extends
     * @return list of classes that remote object implements or extends
     */
    public HashMap<String, RmiObjectClass> getClassesExtended() {
        return classes;
    }

    /**
     * Returns reference to remote object
     * @return reference to remote object
     */
    public Object getRemoteObject() {
        return remoteObject;
    }


    /**
     * Returns specific RmiObjectClass object
     * @param className class name
     * @return RmiObjectClass instance
     */
    public RmiObjectClass getRmiObjectClass(String className){
        for(HashMap.Entry<String, RmiObjectClass> entry: this.classes.entrySet()){
            if(entry.getKey().equalsIgnoreCase(className)){
                return entry.getValue();
            }
        }
        return null;
    }

    /**
     * Returns endpoint string
     * @return endpoint string
     */
    public String getEndpoint() {
        return this.endpoint;
    }

    /**
     * Sets endpoint address and port
     * @param address - ip address
     * @param port - port number
     */
    public void setEndpoint(String address ,int port) {
        this.endpoint = address+":"+port;
    }

    /**
    * Returns extended object details as string
    */
    public String toString(){
        String result = "";
        result = this.getName()+" [object] ["+this.getEndpoint()+"] ";
        if(this.isJMX()){
            result = result + "[JMX Registry]";
        }
        result = result + "\r\n";
        for ( HashMap.Entry<String, RmiObjectClass> entry : this.classes.entrySet() ){
            if(entry.getValue().isInterface()){
                result=result+"\t implements "+entry.getKey()+" [interface]\r\n";
            }else{
                result=result+"\t extends "+entry.getKey()+" [class]\r\n";
            }

            if(entry.getValue().getReference() == null){
                result=result+"\t\t"+"No methods found. I don't have remote object interface. Give it to me!\r\n";
            }else if(entry.getValue().isRemote()){

                for (HashMap.Entry<String, RmiObjectMethod> methodEntry : entry.getValue().getMethods().entrySet()) {
                    RmiObjectMethod method = methodEntry.getValue();
                    result = result + "\t\t" + method.getText() + " [method]\r\n";
                    String vultext = method.getVulnerableParamsText();
                    if (!vultext.isEmpty()) {
                        result = result + "\t\t\t" + vultext + " [info]\r\n";
                    }
                }
            }
        }
        return result;
    }


    /**
     * Reads the methods of remote object classes based on object interface.
     * If interface is not available, then empty RmiObjectClass is created.
     */
    public void extendClasses(){
        for(String className: this.classes.keySet()){
            RmiObjectClass roc = this.classes.get(className);
            try {
                // Try to load class
                roc.loadReference();
                logger.info("Class has been loaded successfully: "+className+".");
                // Try to load class methods
                roc.loadMethods();
                logger.info("Class methods have been loaded successfully: "+className+".");
            }catch (ClassNotFoundException ex){
                // Skip class not found exception, when we dont have interface
                logger.warning("Could not load class "+className+". You have to find this class and import it via -classpath/-cp.");
            }
        }
    }

    /**
     * Invokes specific method on remote object and returns result.
     * This method detects if the remote object has static or dynamic stub.
     * @param className class in which method is available
     * @param methodName method name
     * @param params remote method parameters
     * @return result of remote method
     * @throws Throwable
     */
    public Object invokeMethod(String className, String methodName, Object[] params) throws Throwable{
        // Try to bind
        if(this.remoteObject == null){
            return null;
        }
        Object result = null;
        // Locate class
        RmiObjectClass roc = this.getRmiObjectClass(className);
        // Locate method
        RmiObjectMethod rom = roc.getMethod(methodName);
        result = this.invokeMethod(rom, params);
        return result;
    }

    /**
     * Invokes specific method on remote object and returns result.
     * This method detects if the remote object has static or dynamic stub.
     * @param className class in which method is available
     * @param methodName method name
     * @return result of remote method
     * @throws Throwable
     */
    public Object invokeMethod(String className, String methodName) throws Throwable {
        return this.invokeMethod(className, methodName, new Object[]{});
    }


    /**
     * Invokes specific method on remote object and returns result.
     * This method detects if the remote object has static or dynamic stub.
     * @param method remote method to call
     * @param params remote method parameters
     * @return result of remote method
     * @throws Throwable
     */
    public Object invokeMethod(RmiObjectMethod method, Object[] params) throws Throwable{
        Object result = null;
        if(this.isDynamicStub){
            result = this.invokeDynamicStub(method, params);
        }else{
            result = this.invokeStaticStub(method, params);
        }
        return result;
    }

    /**
     * Invokes specific method on remote object with dynamic stub.
     * This method allows us to call remote method with parameters of other types than those required by the remote method.
     * @param method remote method to call
     * @param params remote method parameters
     * @return result of remote method
     * @throws Throwable
     */
    public Object invokeDynamicStub(RmiObjectMethod method, Object[] params) throws Throwable{
        Proxy remote = (Proxy) this.remoteObject;
        InvocationHandler inv = Proxy.getInvocationHandler(remote);
        return inv.invoke(remote, method.getReference(), params);
    }

    /**
     * Invokes specific method on remote object with static stub.
     * This method allows us to call remote method with parameters of other types than those required by the remote method.
     * @param method remote method to call
     * @param params remote method parameters
     * @return result of remote method
     * @throws Exception
     */
    public Object invokeStaticStub(RmiObjectMethod method, Object[] params) throws Exception {
        RemoteStub remote = (RemoteStub) this.remoteObject;
        RemoteRef ref = remote.getRef();
        return ref.invoke(this.remoteObject, method.getReference(), params, method.getOpNum());
    }

    /**
     * Sets remote object reference
     * @param remote remote object reference
     */
    public void setRemoteObject(Remote remote){
        this.remoteObject = remote;
    }




}
