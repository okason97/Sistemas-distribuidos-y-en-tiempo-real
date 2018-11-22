package Rmifs;/*
* AskRemote.java
* a) Looks up for the remote object
* b) "Makes" the RMI
*/
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.rmi.Naming; /* lookup */
import java.rmi.registry.Registry; /* REGISTRY_PORT */
public class AskRemote{
    public static void main(String[] args){
        /* Look for hostname and msg length in the command line */
        if (args.length != 1){
            System.out.println("1 argument needed: (remote) hostname");
            System.exit(1);
        }
        try {
            String rname = "//" + "localhost" + ":" + Registry.REGISTRY_PORT + "/remote";
            IfaceRemoteClass remote = (IfaceRemoteClass) Naming.lookup(rname);

            String fileName = "file.pdf";
            String copyName = "file2.pdf";

            int fileSize = Integer.valueOf(remote.sizeOf(fileName));

            int received = 0;

            String data = String.valueOf(fileSize) + "\n" +
                    "0" + "\n" + fileName;

            // Read from server
            String readResult = remote.read(data);

            String[] splitReadResult = readResult.split("\n", 2);

            received += Integer.valueOf(splitReadResult[0]);
            System.out.println("received: " + Integer.valueOf(splitReadResult[0]));
            System.out.println("real received: " + splitReadResult[1].getBytes(StandardCharsets.ISO_8859_1).length);

            // Create local copy
            Files.write(Paths.get(fileName),
                    splitReadResult[1].getBytes(StandardCharsets.ISO_8859_1), StandardOpenOption.CREATE);

            data = copyName + "\n" + splitReadResult[0] + "\n" + splitReadResult[1];

            // Write copy in server
            remote.write(data);
            while (received < fileSize){
                data = String.valueOf(fileSize-received) + "\n" +
                        String.valueOf(received) + "\n" + fileName;

                // Read from server
                readResult = remote.read(data);

                splitReadResult = readResult.split("\n", 2);

                received += Integer.valueOf(splitReadResult[0]);

                // Create local copy
                Files.write(Paths.get(fileName),
                        splitReadResult[1].getBytes(StandardCharsets.ISO_8859_1), StandardOpenOption.APPEND);

                data = copyName + "\n" + splitReadResult[0] + "\n" + splitReadResult[1];

                // Write copy in server
                remote.write(data);
            }

            System.out.println("Done");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}