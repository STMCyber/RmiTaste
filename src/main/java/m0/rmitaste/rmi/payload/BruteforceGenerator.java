package m0.rmitaste.rmi.payload;

/**
 * @author Marcin Ogorzelski (mzero - @_mzer0)
 */
public class BruteforceGenerator extends BasicGenerator {

    private String[] payloads = {"CommonsCollections1", "CommonsCollections2", "CommonsCollections3", "CommonsCollections4",
            "CommonsCollections5", "CommonsCollections6", "CommonsCollections7", "Groovy1", "JBossInterceptors1",
            "JavassistWeld1", "Jdk7u21", "MozillaRhino1", "Spring1", "Spring2"};

    private int payloadsLen;
    private int currentPayloadNum;

    public BruteforceGenerator(String cmd){
        super("", cmd);
        this.payloadsLen = this.payloads.length;
        this.currentPayloadNum = -1;
    }

    public BruteforceGenerator(String[] payloads, String cmd){
        super("", cmd);
        this.payloads = payloads;
        this.payloadsLen = this.payloads.length;
        this.currentPayloadNum = -1;
    }

    @Override
    protected void setNextPayload(){
        if(!this.isNext){
            return;
        }
        this.currentPayloadNum++;
        this.payloadName = this.payloads[this.currentPayloadNum];

    }

    @Override
    protected void setIsNext(){
        if(this.currentPayloadNum < this.payloadsLen - 1){
            this.isNext = true;
        }else{
            this.isNext = false;
        }
    }

    private void resetCounter(){
        this.currentPayloadNum = -1;
        this.setIsNext();
    }

    @Override
    public boolean isEnd() {
        return false;
    }

    @Override
    public void end() {
        this.resetCounter();
    }
}