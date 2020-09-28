package m0.rmitaste.commands;

import m0.helpers.ArgumentParser;
import m0.rmitaste.rmi.RmiRegistry;
import m0.rmitaste.rmi.exploit.Attack;
import m0.rmitaste.rmi.exploit.Enumerate;
import picocli.CommandLine;

import java.util.ArrayList;
import java.util.logging.Level;

/**
 * Interprets commandline parameters in call mode and calls specific method on remote object
 *
 * @author Marcin Ogorzelski (mzero - @_mzer0)
 */
@CommandLine.Command(
        name="call",
        description = "calls specific method on RMI remote object"
)
public class CallCommand extends BasicCommand{
    @CommandLine.Option(names={"-m", "--method"}, description = "name of remote method to call in format: remoteObjectName:className:methodName . Use ':' as separator.", required = true)
    private String methodName = "";
    @CommandLine.Option(names={"-mp", "--params"}, description = "values of remote method parameters in format: paramType1=value1;paramTyp2=value2.... Use ';' as separator. Supported types: string, object, byte, int, long, short, float, boolean, double, char.", required = false)
    private String methodParams = null;


    public static void main(String[] args) {
        int exitCode = new CommandLine(new CallCommand()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() throws Exception {
        // Setup logger
        this.initLogger();
        try {
            // Connect and enumerate
            this.logger.info("Starting registry "+this.target + ":"+this.port+" enumeration...");
            Enumerate enumerate = new Enumerate(this.target, this.port);
            enumerate.enumerate();
            RmiRegistry rmiRegistry = enumerate.getRegistry();
            this.logger.info("Enumeration of "+this.target+":"+this.port+" has been completed. Information about "+rmiRegistry.getObjetcts().size()+" remote objects retrieved.");
            String methodToCall = this.target + ":" + this.port + ":"+ this.methodName;
            this.logger.info("Method to call: "+this.methodName);
            ArrayList<String> elements = ArgumentParser.parseMethodName(methodToCall);
            Object[] params;
            if(this.methodParams != null){
                this.logger.info("Detected method parameters. Parsing...");
                try{
                    params = ArgumentParser.parseMethodParams(this.methodParams);
                }catch(Exception ex){
                    this.logger.log(Level.SEVERE, "Could not parse method parameters. Exception: ", ex);
                    return 1;
                }

            }else{
                params = new Object[]{};
            }

            Attack attack = new Attack(rmiRegistry);
            try {
                Object result = attack.invokeMethod(elements.get(2), elements.get(3), elements.get(4), params);
                System.out.println("Method result: ");
                System.out.println(result);
                this.logger.info("Method "+methodName+" called. Result: \n"+result+"\n");
            }catch(Exception ex){
                this.logger.log(Level.SEVERE, "Could not invoke remote method: "+this.methodName+". Exception: ", ex);
                return 1;
            }
        }catch(Throwable ex){
            System.out.println("Unknown error");
            logger.log(Level.SEVERE, "Unknown error: ", ex);
            System.exit(1);
        }
        return 0;
    }
}
