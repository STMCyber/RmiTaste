package m0.helpers;

import m0.rmitaste.utils.SimpleLogger;

import java.io.*;
import java.util.Base64;
import java.util.logging.Logger;

public class CacheWriter {
    // Logger
    private static Logger logger = SimpleLogger.getLog();

    private File fileHandler;
    private FileOutputStream fileOutputStream;
    private BufferedWriter bufferedWriter;

    public CacheWriter(File file){
        this.fileHandler = file;
        this.fileOutputStream = null;
        this.bufferedWriter = null;
    }


    public void open() throws FileNotFoundException{
        this.fileOutputStream = new FileOutputStream(fileHandler);
        this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(fileOutputStream));
    }

    public void close() throws IOException{
        // Close handler
        this.bufferedWriter.close();
        this.fileOutputStream.close();
    }

    public void write(String data) throws IOException{
        this.bufferedWriter.write(data);
    }

    private void writeObject(Object object)throws UnsupportedEncodingException, IOException {
        String data = "";
        // Serialize
        ByteArrayOutputStream serializedData = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(serializedData);
        objectOutputStream.writeObject(object);
        objectOutputStream.close();
        // Encode
        data = this.encode(serializedData.toByteArray());
        this.write(data);
    }

    public void writeObjects(Object[] objects) throws UnsupportedEncodingException, IOException {
        this.open();
        // Dump all objects
        for(Object object: objects){
            this.writeObject(object);
            this.bufferedWriter.newLine();
        }
        this.close();

    }

    private String encode(byte[] bytes) throws UnsupportedEncodingException{
        return Base64.getEncoder().encodeToString(bytes);
    }
}
