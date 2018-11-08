/*
* AskRemote.java
* a) Looks up for the remote object
* b) "Makes" the RMI
*/
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.rmi.Naming; /* lookup */
import java.rmi.registry.Registry; /* REGISTRY_PORT */
import java.rmi.server.RMISocketFactory;

public class AskRemote{
    public static void main(String[] args){
        /* Look for hostname and msg length in the command line */
        try {
            String rname = "//" + "localhost" + ":" + Registry.REGISTRY_PORT + "/remote";
            IfaceRemoteClass remote = (IfaceRemoteClass) Naming.lookup(rname);
            long start = System.nanoTime();
            remote.doNothing();
            long elapsedTime = System.nanoTime() - start;
            Files.write(Paths.get("time.csv"), (String.valueOf(elapsedTime) + "\n").getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            System.out.println("Time elapsed: "+ elapsedTime);
            System.out.println("Done");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}