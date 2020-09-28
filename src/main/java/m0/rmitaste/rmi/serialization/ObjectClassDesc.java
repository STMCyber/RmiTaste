package m0.rmitaste.rmi.serialization;

import java.util.ArrayList;

/**
 * Written by Marcin Ogorzelski (mzero - @_mzer0)
 */
public class ObjectClassDesc {

    private byte flag;
    private ArrayList<ObjectClassField> fields;
    private ArrayList<ObjectClassDesc> superClasses;

    public ObjectClassDesc(){
        this.flag = 0x00;
        this.fields = new ArrayList<ObjectClassField>();
        this.superClasses = new ArrayList<ObjectClassDesc>();
    }

    public void setFlag(byte flag){
        this.flag = flag;
    }

    public byte getFlag() {
        return flag;
    }

    public ArrayList<ObjectClassField> getFields() {
        return fields;
    }

    public void setFields(ArrayList<ObjectClassField> fields) {
        this.fields = fields;
    }

    public void addField(ObjectClassField field){
        this.fields.add(field);
    }

    public ObjectClassField getField(String name){

        for(ObjectClassField field: this.fields){
            if(field.getName().equals(name)){
                return field;
            }
        }

        return null;
    }

    public ArrayList<ObjectClassDesc> getSuperClasses() {
        return superClasses;
    }

    public void setSuperClasses(ArrayList<ObjectClassDesc> superClasses) {
        this.superClasses = superClasses;
    }

    public void addSuperClass(ObjectClassDesc objectClassDesc){
        this.superClasses.add(objectClassDesc);
    }
}
