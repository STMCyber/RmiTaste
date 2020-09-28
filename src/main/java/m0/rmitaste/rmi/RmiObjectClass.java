package m0.rmitaste.rmi;

import m0.helpers.SimpleClassLoader;
import m0.rmitaste.utils.SimpleLogger;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.logging.Logger;

/**
 * RmiObjectClass represents class or interface that remote object extends or implements
 * @author Marcin Ogorzelski (mzero - @_mzer0)
 */

public class RmiObjectClass implements Serializable {
    // Logger
    private static Logger logger = SimpleLogger.getLog();
    private String name;
    private Class reference;
    private HashMap<String, RmiObjectMethod> methods;
    private SimpleClassLoader simpleClassLoader;
    private boolean isInterface;
    private boolean isRemote;

    // List of classess that we don't want to attack
    private static String[] blackList = {"java.rmi.server.RemoteStub", "java.rmi.server.RemoteObject", "java.rmi.Remote", "java.lang.reflect.Proxy"};

    public RmiObjectClass(String nameL, boolean isInterface){
        name = nameL;
        methods = new HashMap<String, RmiObjectMethod>();
        reference = null;
        simpleClassLoader = new SimpleClassLoader();
        this.isInterface = isInterface;
        this.isRemote = false;
    }

    /**
     * Return class name
     * @return class name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns reference to class
     * @return class reference
     */
    public Class getReference() {
        return reference;
    }

    /**
     * Sets class reference
     * @param reference class reference
     */
    private void setReference(Class reference) {
        this.reference = reference;

        if(!this.checkBlackList())
            this.checkInterfaces(this.reference);
    }

    /**
     * Checks whether class implements java.rmi.Remote interface. If yes then class stored in this.reference is Remote class.
     * @param cls class reference to check
     */
    private void checkInterfaces(Class cls){
        Class[] clazz = cls.getInterfaces();
        for(Class c: clazz){
            //System.out.println(c);
            if(c.getName().equals("java.rmi.Remote")){
                this.isRemote = true;
                break;
            }
            this.checkInterfaces(c);
        }
    }

    /**
     * Checks whether class is in blacklist of classes which should not be attacked.
     * @return true if class is in blacklist and false if not
     */
    private boolean checkBlackList(){
        for(String s: RmiObjectClass.blackList){
            if(this.getName().equals(s)){
                this.isRemote = false;
                return true;
            }
        }
        return false;
    }

    /**
     * Loads reference to class
     * @throws ClassNotFoundException if class is not found in classpath
     */
    public void loadReference() throws ClassNotFoundException{
        Class clazz = this.simpleClassLoader.getClass(this.name);
        this.setReference(clazz);
    }

    /**
     * Returns methods that are available in class
     * @return methods that are available in class
     */
    public HashMap<String, RmiObjectMethod> getMethods() {
        return methods;
    }

    /**
     * Sets methods that are available in class
     * @param methods methods that are available in class
     */
    public void setMethods(HashMap<String, RmiObjectMethod> methods) {
        this.methods = methods;
    }

    /**
     * Adds method that is available in class
     * @param method method that is available in class
     */
    public void addMethod(Method method){
        RmiObjectMethod rom = new RmiObjectMethod(method.getName());
        this.methods.put(method.getName(), rom);
    }

    /**
     * Adds method that is available in class
     * @param method method that is available in class
     */
    public void addMethod(RmiObjectMethod method){
        this.methods.put(method.getName(), method);
    }

    /**
     * Returns specific method
     * @param name method name
     * @return
     */
    public RmiObjectMethod getMethod(String name){
        return this.methods.get(name);
    }

    /**
     * Loads method that are available in class by using java reflect
     */
    public void loadMethods(){
        if(this.reference == null){
            return;
        }
        Method[] m = this.simpleClassLoader.getClassMethods(this.name);
        for(Method method: m){
            RmiObjectMethod rom = new RmiObjectMethod(method.getName(), method);
            rom.addParameters(this.simpleClassLoader.getMethodParameters(method));
            this.addMethod(rom);
        }
    }

    /**
     * Returns true if class is interface and false if not
     * @return true if class is interface and false if not
     */
    public boolean isInterface() {
        return this.isInterface;
    }

    /**
     * Returns true if class is remote and false if not
     * @return true if class is remote and false if not
     */
    public boolean isRemote() {
        return isRemote;
    }
}
