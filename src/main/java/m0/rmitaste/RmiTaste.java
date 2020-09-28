package m0.rmitaste;

import m0.rmitaste.commands.AttackCommand;
import m0.rmitaste.commands.CallCommand;
import m0.rmitaste.commands.ConnectionCommand;
import m0.rmitaste.commands.EnumerateCommand;
import m0.rmitaste.rmi.RmiRegistry;
import picocli.CommandLine;

import java.util.concurrent.Callable;
import java.util.logging.Logger;

/**
 * Runs RmiTaste in specific mode - conn, enum, attack or call
 *
 * @author Marcin Ogorzelski (mzero - @_mzer0)
 */

@CommandLine.Command(
        subcommands = {
                ConnectionCommand.class,
                EnumerateCommand.class,
                AttackCommand.class,
                CallCommand.class
        },
        header = "\n"+
               " __________        ._____________                __ \n"+
               " \\______   \\ _____ |__\\__    ___/____    _______/  |_  ____ \n"+
               " |       _//     \\|  | |    |  \\__  \\  /  ___/\\   __\\/ __ \\ \n"+
               " |    |   \\  Y Y  \\  | |    |   / __ \\_\\___ \\  |  | \\  ___/ \n"+
               " |____|_  /__|_|  /__| |____|  (____  /____  > |__|  \\___  > \n"+
               "       \\/      \\/                  \\/     \\/            \\/ \n"+
                " @author Marcin Ogorzelski (mzero - @_mzer0) STM Solutions\n\n" +
                "Warning: RmiTaste was written to aid security professionals in identifying the\n" +
                "         insecure use of RMI services on systems which the user has prior\n" +
                "         permission to attack. RmiTaste must be used in accordance with all\n" +
                "         relevant laws. Failure to do so could lead to your prosecution.\n" +
                "         The developers assume no liability and are not responsible for any\n" +
                "         misuse or damage caused by this program.\n"
)
public class RmiTaste implements Callable<Integer> {
    // Logger
    private static Logger logger;
    @CommandLine.Option(names = {"-h", "--help"}, usageHelp = true, description = "display this help message")
    boolean usageHelpRequested;

    public static void main(String[] args) {
        int exitCode = new CommandLine(new RmiTaste()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() throws Exception{
        return 0;
    }

}