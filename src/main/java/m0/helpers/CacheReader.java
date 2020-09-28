package m0.helpers;

import java.io.*;
import java.util.ArrayList;
import java.util.Base64;

/**
 * @author Marcin Ogorzelski (mzero - @_mzer0)
 */
public class CacheReader {
    File fileHandler;
    private FileInputStream fileInputStream;
    BufferedReader  bufferedReader;

    public CacheReader(File file){
        fileHandler = file;
        fileInputStream = null;
        bufferedReader = null;
    }

    public void open() throws FileNotFoundException{
        this.fileInputStream = new FileInputStream(fileHandler);
        this.bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream));
    }

    public void close() throws IOException{
        // Close handler
        this.bufferedReader.close();
        this.fileInputStream.close();
    }

    /*
        Returns line
     */
    public String read() throws IOException {
        return this.bufferedReader.readLine();
    }

    /*
        Returns object
     */
    private Object readObject() throws IOException, ClassNotFoundException{
        Object object = null;
        String encodedObject = "";
        byte[] decodedObject = {};
        // Read line
        encodedObject = this.read();
        if(encodedObject == null){
            return null;
        }
        // Decode
        decodedObject = this.decode(encodedObject);
        // Deserialize
        ObjectInputStream ois = new ObjectInputStream(
                new ByteArrayInputStream(decodedObject) );
        object = ois.readObject();
        return object;
    }

    /*
    Returns object
 */
    public Object[] readObjects() throws IOException, ClassNotFoundException{
        this.open();
        // Check if cache exists
        ArrayList<Object> objects = new ArrayList<Object>();
        String encodedObject = "";
        String decodedObject = "";

        Object object = this.readObject();
        while(object != null){
            // Read object and add to list
            objects.add(object);
            object = this.readObject();
        }

        this.close();
        return objects.toArray();
    }

    private byte[] decode(String encodedData){
        byte [] data = Base64.getDecoder().decode(encodedData);
        return data;
    }
}
