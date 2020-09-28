package m0.rmitaste.commands;

import m0.helpers.CacheWriter;
import m0.rmitaste.rmi.RmiRegistry;
import m0.rmitaste.rmi.exploit.Enumerate;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

/**
 * Interprets commandline parameters in enum mode and enumerates specific RMI registry
 * @author Marcin Ogorzelski (mzero - @_mzer0)
 */
@CommandLine.Command(
        name="enum",
        description = "enumerates RMI registry"
)
public class EnumerateCommand extends BasicCommand{
    @CommandLine.Option(names={"-o", "--out"}, description = "path to output file", required = false)
    private String outFileName = null;

    public static void main(String[] args) {
        int exitCode = new CommandLine(new EnumerateCommand()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() throws Exception {
        // Setup logger
        this.initLogger();
        try {
            this.logger.info("Starting registry "+this.target + ":"+this.port+" enumeration...");
            Enumerate enumerate = new Enumerate(this.target, this.port);
            enumerate.enumerate();
            RmiRegistry rmiRegistry = enumerate.getRegistry();

            this.logger.info("Enumeration of "+this.target+":"+this.port+" has been completed. Information about "+rmiRegistry.getObjetcts().size()+" remote objects retrieved.");
            System.out.println(rmiRegistry);

            try{
                // Dump results to file
                if (this.outFileName != null){
                    CacheWriter cacheWriter = new CacheWriter(new File(this.outFileName));
                    cacheWriter.open();
                    cacheWriter.write(rmiRegistry.dumpRawOutput());
                    cacheWriter.close();
                }
            }catch (IOException ex) {
                logger.log(Level.SEVERE, "Could not dump information about registry. Check file permissions.  Exception: ", ex);
            }
        }catch(Throwable ex){
            System.out.println("Unknown error");

            logger.log(Level.SEVERE, "Unknown error: ", ex);
            System.exit(1);
        }
        return 0;
    }
}
