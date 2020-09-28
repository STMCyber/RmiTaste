package m0.rmitaste.rmi.payload;

/**
 * PayloadGenerator provides list of methods that should be implemented in class that generates payloads
 *
 * @author Marcin Ogorzelski (mzero - @_mzer0)
 */
public interface PayloadGenerator {
    /**
     * Returns current payload object
     * @return current payload object
     */
    Object getPayload();

    /**
     * Generates next payload
     */
    void generatePayload();

    /**
     * Returns true if there is next payload and false if not
     * @return true if there is next payload and false if not
     */
    boolean isNextPayload();

    /**
     * Returns true if payload generation is end and false if not
     * @return true if payload generation is end and false if not
     */
    boolean isEnd();

    /**
     * Start method is called before payload generation process start
     */
    void start();

    /**
     * End method is called when payload generation process end.
     */
    void end();

    /**
     * Returns current payload name
     * @return current payload name
     */
    String getPayloadName();

    /**
     * Returns current payload command
     * @return current payload command
     */
    String getCmd();
}
