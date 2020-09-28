package m0.rmitaste.commands;

import m0.rmitaste.rmi.exploit.Enumerate;
import m0.rmitaste.utils.SimpleLogger;
import picocli.CommandLine;

import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Interprets commandline parameters in conn mode and checks connection to RMI service
 * @author Marcin Ogorzelski (mzero - @_mzer0)
 */
@CommandLine.Command(
        name="conn",
        description = "checks connection to RMI registry"
)
public class ConnectionCommand extends BasicCommand {

    public static void main(String[] args) {
        int exitCode = new CommandLine(new ConnectionCommand()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() throws Exception {
        // Setup logger
        this.initLogger();
        try {
            // Connect
            logger.info("Trying to connect to " + this.target + ":" + this.port);
            Enumerate.connect(this.target, this.port);
        }catch(Throwable ex){
            System.out.println("Unknown error");
            logger.log(Level.SEVERE, "Unknown error: ", ex);
            System.exit(1);
        }
        return 0;
    }
}
