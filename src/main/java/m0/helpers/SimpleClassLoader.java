package m0.helpers;

import m0.rmitaste.utils.SimpleLogger;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * Extends ClassLoader
 * @see java.lang.ClassLoader
 * @author Marcin Ogorzelski (mzero - @_mzer0)
 */
public class SimpleClassLoader extends ClassLoader{
    // Logger
    private static Logger logger = SimpleLogger.getLog();

    /**
     * Returns class reference
     * @param class_name class name
     * @return class reference
     * @throws ClassNotFoundException if class is not available in classpath
     */
    public Class getClass(String class_name) throws ClassNotFoundException{
        Class loadedMyClass = null;
        ClassLoader classLoader = this.getClass().getClassLoader();
        loadedMyClass = classLoader.loadClass(class_name);
        return loadedMyClass;
    }

    /**
     * Returns list of methods available in specific class
     * @param className class name
     * @return list of methods available in specific class
     */
    public Method[] getClassMethods(String className){
        //List<Method> methods = new ArrayList<Method>;
        Method[] methods = {};
        try{
            Class cls = this.getClass(className);
            methods = cls.getDeclaredMethods();
            //return methods;
        }catch(Exception e){
            e.printStackTrace();
        }
        return methods;
    }
    /**
     * Returns list of methods available in specific class
     * @param className class name
     * @return list of methods available in specific class
     */
    public ArrayList<String> listMethods(String className){
        ArrayList<String> result = new ArrayList<String>();
        try{
            Method[] methods = this.getClassMethods(className);

            for(Method m : methods){
                result.add(m.getName());
                //System.out.println(m.getName());
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Returns list of method parameters types
     * @param method method name
     * @return list of method parameters types
     */
    public Class[] getMethodParameters(Method method){
        return method.getParameterTypes();
    }
}
