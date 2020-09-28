package m0.rmitaste.rmi;

import m0.helpers.SimpleClassLoader;
import m0.rmitaste.utils.OpnumGenerator;
import m0.rmitaste.utils.SimpleLogger;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.logging.Logger;

/**
 * RmiObjectMethod class represents remote object method and holds reference to it
 *
 * @author Marcin Ogorzelski (mzero - @_mzer0)
 */

public class RmiObjectMethod implements Serializable {
    // Logger
    private static Logger logger = SimpleLogger.getLog();
    private String name;
    private HashMap<String, Class> parameters;
    private ArrayList<Class> exceptionTypes;
    private String paramNamePrefix;
    private SimpleClassLoader rmiClassLoader;
    private Method reference;
    private Long opnum;
    private boolean isRemote;

    public RmiObjectMethod(String nameL){
        name = nameL;
        paramNamePrefix = "param";
        rmiClassLoader = new SimpleClassLoader();
        parameters = new HashMap<String, Class>();
        this.reference = null;
        this.exceptionTypes = new ArrayList<Class>();
        this.isRemote = false;
    }

    public RmiObjectMethod(String nameL, Method referenceL){
        name = nameL;
        paramNamePrefix = "param";
        rmiClassLoader = new SimpleClassLoader();
        parameters = new HashMap<String, Class>();
        this.exceptionTypes = new ArrayList<Class>();
        this.reference = referenceL;
        this.computeOpnum();
        this.loadExceptionTypes();
    }

    /**
     * Returns method name
     * @return method name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns method parameters
     * @return method parameters
     */
    public HashMap<String, Class> getParameters() {
        return parameters;
    }

    /**
     * Returns an array of method parameters
     * @return an array of method parameters
     */
    public Class[] getParametersArray() {
        Collection<Class> c = this.parameters.values();
        Class[] result = c.toArray(new Class[c.size()]);
        return result;
    }

    /**
     * Sets parameters
     * @param parameters method parameters
     */
    public void setParameters(HashMap<String, Class> parameters) {
        this.parameters = parameters;
    }

    /**
     * Adds parameter to array of method parameters
     * @param type parameter type
     */
    public void addParameter(Class type){
        String paramName = this.paramNamePrefix + this.parameters.size();
        this.parameters.put(paramName, type);
    }

    /**
     * Adds parameters to array of method parameters
     * @param params parameters types
     */
    public void addParameters(Class[] params){
        for(Class type: params){
            this.addParameter(type);
        }
    }

    public String getText(){
        String result = this.name+"(";
        int i = 1;
        for(HashMap.Entry<String, Class> entry: this.parameters.entrySet()){
            result = result + entry.getValue().getName() +" "+entry.getKey();
            if(i != this.parameters.size()){
                result = result + ", ";
            }
            i++;
        }
        result = result + ");";
        return result;
    }

    public String getVulnerableParamsText(){
        String exploitable_params = "Parameters: ";
        boolean flag = false;
        for(HashMap.Entry<String, Class> entry: this.parameters.entrySet()){
            // Check if parameter is of primitive type. If not it could be exploitable via deserialization
            // Check unmarshalValue function from sun.rmi.server.UnicastRef.
            // If we don’t deal with a primitive type like an Integer readObject() is called in unmarshalValue function.
            if(!entry.getValue().isPrimitive()){
                exploitable_params = exploitable_params + entry.getKey() +"; ";
                flag = true;
            }
        }
        exploitable_params = exploitable_params + " may be vulnerable to Java Deserialization!";
        if(!flag){
            exploitable_params = "";
        }
        return exploitable_params;
    }

    /**
     * Returns reference to method
     * @return reference to method
     */
    public Method getReference() {
        return reference;
    }

    private void setReference(Method methodRef) {
        this.reference = methodRef;
        this.computeOpnum();
        this.loadExceptionTypes();
    }

    /**
     * Returns method operation number
     * @return operation number
     */
    public long getOpNum(){
        return this.opnum;
    }

    /**
     * Computes method operation number
     */
    private void computeOpnum(){
        if(this.reference != null) {
            this.opnum = OpnumGenerator.computeMethodHash(this.reference);
        }
    }

    /**
     * Loads exceptions types that are thrown by remote method
     * If method throws RemoteException then isRemote flag will be set to true
     */
    private void loadExceptionTypes(){
        if(this.reference != null) {
            Class[] exceptionTypes = this.reference.getExceptionTypes();
            for (Class exception: exceptionTypes){
                if(exception.getName().equals("java.rmi.RemoteException")){
                    this.isRemote = true;
                }
                this.exceptionTypes.add(exception);
            }
        }
    }

    /**
     *
     * Returns true if method is remote and false if not
     * @return true if method is remote and false if not
     */
    public boolean isRemote() {
        return isRemote;
    }

}
