package m0.rmitaste.rmi.serialization;

import m0.rmitaste.rmi.RmiObject;
import m0.rmitaste.utils.SimpleLogger;

import java.util.ArrayList;
import java.util.logging.Logger;

import static java.io.ObjectStreamConstants.*;

/**
 * Written by Marcin Ogorzelski (mzero - @_mzer0)
 */
public class RmiObjectParser {
    // Logger
    private static Logger logger = SimpleLogger.getLog();

    private RmiObject rmiObject;
    // Serialized object stream
    private ArrayList<Byte> stream;
    // Current index
    private int index = 0;
    // Stream size
    private int streamSize = 0;

    public RmiObjectParser(String name){
        this.rmiObject = new RmiObject(name);
        this.stream = new ArrayList<Byte>();
    }

    public void loadStream(ArrayList<Byte> stream){
        this.stream.clear();
        this.stream.addAll(stream);
        this.index = 0;
        this.streamSize = this.stream.size();
    }

    private Byte getByte(){
        if(this.index < this.streamSize){
            Byte b = this.stream.get(this.index);
            this.index++;
            return b;
        }
        this.logger.severe("No more bytes to read. Limit has been reached!");
        return -1;
    }

    private byte[] getBytes(int num){
        byte[] r = new byte[num];
        for(int i = 0; i < num; i++){
            r[i] = this.getByte();
        }
        return r;
    }

    private Short getShort(byte[] data){
        Short r = (short) (((data[0] << 8) & 0xFF00 ) | (data[1] &0xFF));
        return r;
    }

    private Short getShort(){
        Short r = (short) (((this.getByte() << 8) & 0xFF00 ) | (this.getByte() &0xFF));
        return r;
    }

    private Integer getInteger(byte[] data){
        Integer r = (int) (((data[0] << 24) & 0xFF000000) |
                ((data[1] << 16) & 0xFF0000) |
                ((data[2] << 8) & 0xFF00) | (data[3] &0xFF));
        return r;
    }

    private Integer getInteger(){
        Integer r = (int) (((this.getByte() << 24) & 0xFF000000) |
                ((this.getByte() << 16) & 0xFF0000) |
                ((this.getByte() << 8) & 0xFF00) | (this.getByte() &0xFF));
        return r;
    }

    private Long getLong(){
        Long r = (long) (((this.getByte() << 56) & 0xFF00000000000000L) |
                ((this.getByte() << 48) & 0xFF000000000000L) |
                ((this.getByte() << 40) & 0xFF0000000000L) |
                ((this.getByte() << 32) & 0xFF00000000L) |
                ((this.getByte() << 24) & 0xFF000000) |
                ((this.getByte() << 16) & 0xFF0000) |
                ((this.getByte() << 8) & 0xFF00) | (this.getByte() &0xFF));
        return r;
    }

    private String getUtfShort(byte[] data){

        int len = this.getShort(data);
        if(data.length - 2 < len){
            this.logger.warning("Could not extract UTF value. Stream is too short it has "+data.length+" byes. Required length: "+len);
            return null;
        }

        byte[] bytes = new byte[len];



        for(int i = 0; i < len; i++){
            bytes[i] = data[i + 2];
        }

        return new String(bytes);
    }

    private String getUtfShort(){
        int len = this.getShort();
        byte[] bytes = this.getBytes(len);
        return new String(bytes);
    }

    private String getUtfLong(){
        int len = this.getLong().intValue();
        byte[] bytes = this.getBytes(len);
        return new String(bytes);
    }

    private boolean checkRmi(){
        return this.getByte() == 0x51;
    }

    private boolean checkMagic(){
        return  this.getShort() == STREAM_MAGIC;
    }

    private boolean checkVersion(){
        return this.getShort() == STREAM_VERSION;
    }

    private String parseNewString(){
        byte b = this.getByte();
        if(b == TC_STRING){
            return this.getUtfShort();
        }
        else if(b == TC_LONGSTRING){
            return this.getUtfLong();
        }else if(b == TC_REFERENCE){
            this.getInteger();
        }
        return null;
    }

    private ArrayList<ObjectClassField> parseClassDescInfoFields(){
        ArrayList<ObjectClassField> fields = new ArrayList<ObjectClassField>();
        // Read fields
        // (short)<count>  fieldDesc[count]
        short count = this.getShort();
        for(int i = 0; i < count; i++){
            byte primTypeCode = this.getByte();

            ObjectClassField objectClassField = new ObjectClassField(primTypeCode);

            switch (primTypeCode){
                case 'B':
                case 'C':
                case 'D':
                case 'F':
                case 'I':
                case 'J':
                case 'S':
                case 'Z':
                    objectClassField.setName(this.getUtfShort());
                    break;
                case '[':
                case 'L':
                    this.getUtfShort();
                    this.parseNewString();
                    break;
            }

            fields.add(objectClassField);
        }
        return fields;
    }

    private ObjectClassDesc parseClassDescInfo(){
        ObjectClassDesc objectClassDesc = new ObjectClassDesc();

        // Read flags
        byte flags = this.getByte();
        objectClassDesc.setFlag(flags);
        this.logger.info("Class flag: "+(int)flags);
        objectClassDesc.setFields(this.parseClassDescInfoFields());

        this.parseClassAnnotation();

        ObjectClassDesc superClass = this.parseClassDesc();
        if(superClass != null){
            objectClassDesc.addSuperClass(superClass);
        }

        return objectClassDesc;
    }

    private ObjectClassDesc parseClassDesc(){
        // 1812  private ObjectStreamClass readClassDesc(boolean unshared)
        ObjectClassDesc objectClassDesc = null;
        Byte b = this.getByte();
        // TC_CLASSDESC className serialVersionUID newHandle classDescInfo
        if(b == TC_CLASSDESC){
            this.logger.info("ClassDesc detected");
            String className = this.getUtfShort();
            this.rmiObject.addClass(className, false);
            this.logger.info("Reading description of class: "+className);
            // SerialVersionUID
            this.getLong();
            objectClassDesc = this.parseClassDescInfo();
        }
        // TC_PROXYCLASSDESC newHandle proxyClassDescInfo
        else if(b == TC_PROXYCLASSDESC){
            this.logger.info("ProxyClassDesc detected");
            this.parseProxyClassDesc();
        }

        return objectClassDesc;
    }

    private void parseClassAnnotation(){
        // Skip annotation element
        Byte b = 0x00;
        while (true){
            b = this.getByte();
            if(b == TC_ENDBLOCKDATA){
                break;
            }
            else if(b == TC_REFERENCE){
                this.getInteger();
            }
            else if(b == TC_STRING){
                this.getUtfShort();
            }
        }
    }


    private void parseProxyClassDesc(){
        // (int)<count> proxyInterfaceName[count] classAnnotation
        //      superClassDesc

        int count = this.getInteger();
        this.logger.info("Detected "+count+" proxyInterfaceNames");
        // proxyInterfaceName[count]
        for(int i = 0; i < count; i++){
            String name = this.getUtfShort();
            this.logger.info("Interface name read: "+name);
            // Add interface name to RmiObject
            this.rmiObject.addClass(name, true);
        }
        // classAnnotation
        this.parseClassAnnotation();
        // superClassDesc
        this.parseClassDesc();

    }

    private void parseClassData(ObjectClassDesc objectClassDesc){
        this.logger.info("Parsing class data...");
        byte classDescFlag = objectClassDesc.getFlag();
        ArrayList<ObjectClassField> fields = objectClassDesc.getFields();

        if((SC_SERIALIZABLE & classDescFlag) == SC_SERIALIZABLE){
            for(ObjectClassField objectClassField: fields){
                switch(objectClassField.getPrimTypeCode()){
                    case 'J':	//Long
                    case 'D':	//Double
                        this.getInteger();
                    case 'I':	//Integer
                    case 'F':	//Float
                        this.getBytes(2);
                    case 'S':	//Short
                    case 'B':	//Byte
                    case 'C':	//Char
                    case 'Z':	//Boolean
                        this.getByte();;
                        break;
                    case 'L':
                        byte type = this.getByte();
                        switch(type) {
                            case TC_OBJECT:
                                this.parseObject();
                                break;

                            //Handle strings
                            case TC_STRING:
                                this.getUtfShort();
                                break;

                            case TC_REFERENCE:
                                this.getInteger();
                                break;
                            case TC_NULL:
                            default:
                                break;
                        }
                        break;
                    case '[':
                    default:
                        break;
                }
            }

            if((classDescFlag & SC_WRITE_METHOD) == SC_WRITE_METHOD) {
                this.parseObjectAnnotation();
            }
        }
    }

    private void parseObjectAnnotation() {
        this.logger.info("Parsing object annotation...");
        byte b = 0x00;
        while((b = this.getByte()) != TC_ENDBLOCKDATA){
            if(b == TC_OBJECT){
                this.parseObject();
            }else if(b == TC_BLOCKDATA){
                this.logger.info("ELO ELO");
                this.extractEndpoint(this.parseBlockData());
            }
        }
    }

    private byte[] substring(byte[] data, int begin){
        byte[] r = new byte[data.length - begin];
        for(int i = begin; i < data.length; i++){
            r[i - begin] = data[i];
        }
        return r;
    }

    private void extractEndpoint(byte[] blockData) {
        this.logger.info("Trying to extract information about endpoint...");
        String className = this.getUtfShort(blockData);
        if(className != null && className.contains("UnicastRef")){
            int offset = 12;
            this.logger.info("Endpoint detected");
            if(className.contains("UnicastRef2")){
                offset = 13;
            }
            String host = this.getUtfShort(this.substring(blockData, offset));
            int port = this.getInteger(this.substring(blockData, offset + 2 + host.length()));
            this.logger.info("Endpoint detected "+host + ":"+port);
            this.rmiObject.setEndpoint(host, port);
        }

    }

    /**
     * Parses object element in stream rule
     */
    private void parseObject(){
        ObjectClassDesc objectClassDesc = this.parseClassDesc();
        if(objectClassDesc == null){
            this.logger.severe("Could not parse class description or serialized stream does not have class description.");
            return;
        }

        this.parseClassData(objectClassDesc);
    }

    /**
     * Parses blockdata element in stream rule
     */
    private byte[] parseBlockData(){
        Byte b = this.getByte();
        int len = b.intValue();
        this.logger.info("BlockData length "+len);
        byte[] result =  this.getBytes(b);
        return result;
    }

    /**
     * Parses RMI response packet and extracts information about remote object.
     */
    private void parse(){
        this.index = 0;

        this.logger.info("Trying to parse serialized remote object...");
        if(!this.checkRmi()){
            // NOT RMI
            this.logger.severe("Could not parse RMI response. Provided stream is not RMI packet!");
            return;
        }

        if(!this.checkMagic()){
            // NOT SERIALIZED OBJECT
            this.logger.severe("Could not parse RMI response. Stream magic is not correct!");
            return;
        }

        if(!this.checkVersion()){
            // NOT SUPPORTED VERSION
            this.logger.severe("Could not parse RMI response. Not supported serialization version!");
            return;
        }
        this.logger.info("Stream is correct. Parsing serialized "+this.rmiObject.getName()+" remote object...");
        Byte b = 0x00;
        // Parse content
        while((b = this.getByte()) != -1){
            // Parse object
            if(b == TC_OBJECT){
                this.logger.info("Object element detected");
                this.parseObject();
            }
            // Parse blockdata
            else if(b == TC_BLOCKDATA){
                this.logger.info("BlockData element detected");
                this.extractEndpoint(this.parseBlockData());
            }else{
                // Byte not match stream rule. Exception...
                return;
            }
        }
        this.logger.info("Parsing of "+this.rmiObject.getName()+" remote object finished.");

    }



    public RmiObject getRmiObject(){
        this.parse();
        return this.rmiObject;
    }
}
