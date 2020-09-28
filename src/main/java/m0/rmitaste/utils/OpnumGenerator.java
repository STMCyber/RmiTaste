package m0.rmitaste.utils;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author Marcin Ogorzelski (mzero - @_mzer0)
 */
public class OpnumGenerator {

    public static String getDescriptorForClass(final Class c){
        if(c.isPrimitive())
        {
            if(c==byte.class)
                return "B";
            if(c==char.class)
                return "C";
            if(c==double.class)
                return "D";
            if(c==float.class)
                return "F";
            if(c==int.class)
                return "I";
            if(c==long.class)
                return "J";
            if(c==short.class)
                return "S";
            if(c==boolean.class)
                return "Z";
            if(c==void.class)
                return "V";
            throw new RuntimeException("Unrecognized primitive "+c);
        }
        if(c.isArray()) return c.getName().replace('.', '/');
        return ('L'+c.getName()+';').replace('.', '/');
    }

    public static String getMethodDescriptor(Method m)
    {
        String s=m.getName()+"(";
        for(final Class c:(m.getParameterTypes())){
            s+=getDescriptorForClass(c);
        }
        s+=')';
        return s+getDescriptorForClass(m.getReturnType());
    }

    public static long computeMethodHash(String methodString) {
        long hash = 0;
        ByteArrayOutputStream sink = new ByteArrayOutputStream(512);
        try {
            MessageDigest md = MessageDigest.getInstance("SHA");
            DataOutputStream out = new DataOutputStream(new DigestOutputStream(sink, md));
            out.writeUTF(methodString);

            // use only the first 64 bits of the digest for the hash
            out.flush();
            byte hashArray[] = md.digest();
            for (int i = 0; i < Math.min(8, hashArray.length); i++) {
                hash += ((long) (hashArray[i] & 0xFF)) << (i * 8);
            }
        } catch (IOException e) {
            throw new Error(
                    "unexpected exception computing intetrface hash: " + e);
        } catch (NoSuchAlgorithmException e) {
            throw new Error(
                    "unexpected exception computing intetrface hash: " + e);
        }

        return hash;
    }

    public static long computeMethodHash(Method method){
        String methodString = OpnumGenerator.getMethodDescriptor(method);
        return OpnumGenerator.computeMethodHash(methodString);
    }
}
