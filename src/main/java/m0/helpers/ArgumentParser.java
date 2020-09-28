package m0.helpers;

import java.util.ArrayList;

/**
 * The ArgumentParser class provides methods for commandline argument parsing.
 *
 * @author Marcin Ogorzelski (mzero - @_mzer0)
 */
public class ArgumentParser {

    /**
     * Splits methodName into <code>ArrayList</code> based on ':' as separator.
     *
     * @param methodName name of method in format remote_object:class:method_name
     * @return an ArrayList of Strings
     */
    public static ArrayList<String> parseMethodName(String methodName){
        ArrayList<String> elements = ArgumentParser.splitParams(methodName, ':', '\\');
       /* if(elements.size() != 5){
            // THROW ERROR
            //logger.severe("Wrong format of method-name parameter!");
            System.exit(1);
        }*/
        return elements;
    }

    /**
     * Parses remote method parameters
     *
     * @param methodParams method parameters in format type1=value1;type2=value2
     * @return an array of parameters values
     * @throws Exception in case of an invalid methodParams format
     */
    public static Object[] parseMethodParams(String methodParams)throws Exception{
        char keyValueSeparator = '=';
        ArrayList<String> elements = ArgumentParser.splitParams(methodParams, ';', '\\');
        ArrayList<Object> params = new ArrayList<Object>();
        for(String param: elements){
            if(param.isEmpty()){
                continue;
            }
            ArrayList<String> p = ArgumentParser.splitParams(param, '=', '\\');
            if(p.size() == 2){
                String type = p.get(0);
                Object value = ArgumentParser.castParam(type, p.get(1));
                params.add(value);
            }else{
                // WRONG FORMAT ERROR
                throw new Exception("Wrong parameters format. Should be: paramType1=paramValue1;paramType2=paramValue2...");
            }
        }
        return params.toArray();
    }

    /**
     * Casts value to type
     *
     * @param type parameter type
     * @param value parameter value
     * @return value with correct type
     */
    public static Object castParam(String type, String value){
        Object result = null;
        if(value == null){
            return result;
        }
        type = type.toLowerCase();
        if(type.equals("string")){
            result = value;
        }else if(type.equals("object")){
            result = value;
        }else if(type.equals("byte")){
            result = Byte.parseByte(value);
        }else if(type.equals("int")){
            result = Integer.parseInt(value);
        }else if(type.equals("long")){
            result = Long.parseLong(value);
        }else if(type.equals("short")){
            result = Short.parseShort(value);
        }else if(type.equals("float")){
            result = Float.parseFloat(value);
        }else if(type.equals("boolean")){
            result = Boolean.parseBoolean(value);
        }else if(type.equals("double")){
            result = Double.parseDouble(value);
        }
        else if(type.equals("char")){
            result = value.toCharArray()[0];
        }
        return result;
    }

    /**
     * Splits methodParams string into array based on separator and escape chars
     * @param methodParams string to split
     * @param separator separator character
     * @param escape escape character
     * @return an ArrayList of strings computed by splitting this string
     */
    public static ArrayList<String> splitParams(String methodParams, char separator, char escape){
        String temp = "";
        ArrayList<String> result = new ArrayList<String>();
        for(int i = 0, n = methodParams.length() ; i < n ; i++) {
            char c = methodParams.charAt(i);
            if(c == separator && methodParams.charAt(i-1) != escape){
                result.add(temp);
                temp = "";
                continue;
            }
            temp = temp + c;
        }
        result.add(temp);
        return result;
    }
}
