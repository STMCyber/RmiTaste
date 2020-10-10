package m0.rmitaste.rmi;

import m0.rmitaste.rmi.serialization.RmiObjectParser;
import m0.rmitaste.utils.SimpleLogger;
import sun.rmi.registry.RegistryImpl_Stub;
import sun.rmi.transport.StreamRemoteCall;

import java.io.*;
import java.lang.reflect.Field;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.server.RemoteRef;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * Class allows to:
 * - lookup Remote Object;
 * - get a list of remote object names;
 * - get reference to remote object.
 * RmiRegistry operates on java.rmi.registry.Registry object and stores RmiRemoteObject objects which represent remote objects.
 *
 * @author Marcin Ogorzelski (mzero - @_mzer0)
 */

public class RmiRegistry implements Serializable {
    // Logger
    private static Logger logger = SimpleLogger.getLog();
    // Objects in registry
    private ArrayList<RmiObject> objects;
    private String host;
    private int port;
    // Name is concatenation of host:port
    private String name;
    // javaVersion is set if Registry is JMX registry
    private String javaVersion;
    private Registry reference;

    /**
     *
     * @param host host for the remote registry
     * @param port port on which the registry accepts requests
     */
    public RmiRegistry(String host, int port){
        this.objects = new ArrayList<RmiObject>();
        this.name = host+":"+port;
        this.host = host;
        this.port = port;
        this.javaVersion = "";
        this.reference = null;
    }

    /**
     * Sets registry reference
     * @param ref reference to remote Registry
     */
    public void setReference(Registry ref){
        this.reference = ref;
    }

    /**
     * Returns RmiRegistry host:port
     * @return string host:port
     */
    public String getName() {
        return this.name;
    }

    /**
     * Returns host
     * @return remote registry host
     */
    public String getHost() { return this.host; }

    /**
     * Returns port number
     * @return port number
     */
    public int getPort() {
        return this.port;
    }

    /**
     * Returns Java Version if JMX registry is detected
     * @return returns java version
     */
    public String getJavaVersion() {
        return this.javaVersion;
    }

    /**
     * Adds RmiObject instance RmiRegistry
     * @param rmiObject instance of RmiObject representing remote object
     */
    public void addObject(RmiObject rmiObject){
        this.objects.add(rmiObject);
    }


    /**
     * Returns list of RmiObject instances
     * @return returns RmiObject list
     */
    public ArrayList<RmiObject> getObjetcts() {
        return this.objects;
    }

    /**
     * Returns specific RmiObject instance
     * @param objName remote object name
     * @return instance of RmiObject
     */
    public RmiObject getObject(String objName){
        for(RmiObject ro: this.getObjetcts()){
            if(ro.getName().equalsIgnoreCase(objName)){
                return ro;
            }
        }
        return null;
    }

    /**
     * Returns an array of remote object names in registry
     * @return an array of remote object names in registry
     * @throws RemoteException
     */
    public String[] list() throws RemoteException {
        return this.reference.list();
    }

    /**
     * Enumerates the remote reference(remote object) bound to the specified name in this registry.
     * @param name remote object name
     * @return
     * @throws java.rmi.MarshalException
     * @throws Exception
     */
    private RmiObject lookup(String name) throws java.rmi.MarshalException, Exception{
        RmiObject ro = null;
        // Cast Registry to RegistryImpl_Stub
        RegistryImpl_Stub reg = (RegistryImpl_Stub) this.reference;
        // Get RemoteRef. We need it to send RMI request
        RemoteRef ref = reg.getRef();
        final long interfaceHash = 4905912898345647071L;
        final java.rmi.server.Operation[] operations = {
                new java.rmi.server.Operation("void bind(java.lang.String, java.rmi.Remote)"),
                new java.rmi.server.Operation("java.lang.String list()[]"),
                new java.rmi.server.Operation("java.rmi.Remote lookup(java.lang.String)"),
                new java.rmi.server.Operation("void rebind(java.lang.String, java.rmi.Remote)"),
                new java.rmi.server.Operation("void unbind(java.lang.String)")
        };
        // Invoke lookup
        StreamRemoteCall call = (StreamRemoteCall)ref.newCall(reg, operations, 2, interfaceHash);
        try {
            java.io.ObjectOutput out = call.getOutputStream();
            out.writeObject(name);
        } catch (java.io.IOException e) {
            throw new java.rmi.MarshalException("error marshalling arguments", e);
        }
        ref.invoke(call);

        // Receive and parse response. We want to get some info about remote object
        try {
            // We can't deserialize object because there is possibility that we don't have remote object class
            // So we would like to read serialized object as raw bytes and parse it
            // Raw bytes reside in fields: bin->in->in->buf
            ObjectInputStream in =(ObjectInputStream) call.getInputStream();
            // Get bin field from ObjectInputStream
            Field binField = ObjectInputStream.class.getDeclaredField("bin");
            binField.setAccessible(true);
            // bin type: java.io.ObjectInputStream$BlockDataInputStream
            DataInput bin = (DataInput) binField.get(in);

            // Get in field from bin field
            Field inField = bin.getClass().getDeclaredField("in");
            inField.setAccessible(true);
            // in type: java.io.ObjectInputStream$PeekInputStream
            InputStream inputStream = (InputStream)inField.get(bin);

            // Get in field from in field
            inField = inputStream.getClass().getDeclaredField("in");
            inField.setAccessible(true);
            // second in type: java.io.BufferedInputStream
            InputStream in2 = (InputStream) inField.get(inputStream);

            // Get serialized object from java.io.BufferedInputStream. It's in buf field
            Field bufField = in2.getClass().getDeclaredField("buf");
            bufField.setAccessible(true);
            byte buf[] = (byte[]) bufField.get(in2);

            // Convert byte array to ArrayList
            ArrayList<Byte> packetBytes = new ArrayList<Byte>();
            for(byte b: buf){
                packetBytes.add(b);
            }
            // Parse serialized object

            RmiObjectParser rmiObjectParser = new RmiObjectParser(name);
            rmiObjectParser.loadStream(packetBytes);
            ro = rmiObjectParser.getRmiObject();

            in.readObject();
        }catch (ClassNotFoundException ex){
            // PASS exception
            // probably the error is caused by readObject function. That's ok because we may not have a class that is implemented by remote object.
        }
        catch (java.io.IOException e) {
            throw new java.rmi.MarshalException("error unmarshalling arguments", e);
        } finally {
            ref.done(call);
        }
        return ro;
    }

    /**
     * Initialize all RemoteObjects
     * @throws RemoteException
     * @throws Exception
     */
    public void loadObjects() throws RemoteException, Exception{
        String[] names = this.list();
        this.loadObjects(names);
    }

    /**
     * Initialize specific RemoteObjects
     * @param names array of remote object names
     */
    public void loadObjects(String[] names){
        for(String name: names){
            this.logger.info("Starting remote object enumeration: "+name+"...");
            try {
                RmiObject ro = this.lookup(name);
                this.logger.info("Information about the remote object has been retrieved successfully: "+name);
                ro.extendClasses();
                this.addObject(ro);
            }catch (Exception ex){
                this.logger.log(Level.SEVERE,"An error occurred while retrieving information about the remote object: "+name+". Omitting... Exception: ", ex);
            }
        }
    }

    /**
     * Sets remote object reference in specific RmiObject
     * @param name remote object name
     * @throws RemoteException
     * @throws NotBoundException
     */
    public void loadObjectRef(String name)throws RemoteException, NotBoundException{
        for(RmiObject ro: this.objects) {
            if(ro.getName().equals(name)){
                ro.setRemoteObject(this.reference.lookup(ro.getName()));
                break;
            }
        }
    }

    /**
     * Sets remote object reference in all RmiObject
     */
    public void loadObjectRef() {
        for(RmiObject ro: this.objects){
            try{
                ro.setRemoteObject(this.reference.lookup(ro.getName()));
                this.logger.info("Remote object reference has been set: "+ro.getName()+".");
            }catch(NotBoundException ex){
                this.logger.severe("Could not lookup remote object. Remote object has no associated binding: "+ro.getName()+".");
            }
            catch(RemoteException ex){
                this.logger.log(Level.SEVERE, "Could not lookup remote object: "+ro.getName()+". Exception:", ex);
            }
        }
    }

    public String toString(){
        String result = "";
        for(RmiObject ro: this.objects){
            result = result + "\r\n" + ro;
        }
        return result;
    }

    public String dumpRawOutput(){
        String result = "";
        String target = this.getHost() + ":"+ this.getPort();
        for(RmiObject ro: this.getObjetcts()){
            String line = "";
            for ( HashMap.Entry<String, RmiObjectClass> class_entry : ro.getClassesExtended().entrySet() ) {
                if(class_entry.getValue().isRemote()) {
                    for (HashMap.Entry<String, RmiObjectMethod> method_entry : class_entry.getValue().getMethods().entrySet()) {
                        //if(method_entry.getValue().isRemote()) {
                        line = line + target + ":" + ro.getName() + ":" + class_entry.getValue().getName() + ":" + method_entry.getValue().getName() + "\r\n";
                        //}
                    }
                }
            }
            result = result + line;
        }
        return result;
    }
}
