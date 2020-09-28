package m0.rmitaste.rmi.serialization;

/**
 * Written by Marcin Ogorzelski (mzero - @_mzer0)
 */
public class ObjectClassField {
    private String name;
    private byte primTypeCode;

    public ObjectClassField(byte primTypeCode){
        this.name = null;
        this.primTypeCode = primTypeCode;
    }

    public void setName(String name){
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public byte getPrimTypeCode() {
        return primTypeCode;
    }
}
