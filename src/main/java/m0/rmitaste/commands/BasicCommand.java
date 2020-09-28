package m0.rmitaste.commands;

import m0.rmitaste.utils.SimpleLogger;
import picocli.CommandLine;

import java.util.concurrent.Callable;
import java.util.logging.Logger;

/**
 * Base command class
 *
 * @author Marcin Ogorzelski (mzero - @_mzer0)
 */

public class BasicCommand implements Callable<Integer> {
    // Logger
    protected static Logger logger;
    @CommandLine.Option(names={"-t", "--target"}, description = "host for the remote registry", required = true)
    protected String target = "127.0.0.1";
    @CommandLine.Option(names={"-p", "--port"}, description = "port on which the registry accepts requests", required = true)
    protected int port = 1099;
    @CommandLine.Option(names = {"-h", "--help"}, usageHelp = true, description = "display this help message")
    boolean usageHelpRequested;

    protected void initLogger(){

        // Setup logger
        try {
            SimpleLogger.init("logfile.txt");
            logger = SimpleLogger.getLog();
            logger.info("Logger activated.");
        } catch (Exception ex){
            ex.printStackTrace();
            System.exit(1);
        }
    }

    @Override
    public Integer call() throws Exception {
        return null;
    }
}
