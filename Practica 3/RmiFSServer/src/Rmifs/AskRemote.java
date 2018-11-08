package Rmifs;/*
* AskRemote.java
* a) Looks up for the remote object
* b) "Makes" the RMI
*/
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

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

            String fileName = "text";
            int fileSize = 10;

            // Encode JSON
            ObjectMapper mapper = new ObjectMapper();
            JsonNode readJson = mapper.createObjectNode();
            ((ObjectNode) readJson).put("length", fileSize);
            ((ObjectNode) readJson).put("offset", 0);
            ((ObjectNode) readJson).put("name", fileName);

            // Read from server
            String readResult = remote.read(mapper.writeValueAsString(readJson));

            // Decode JSON
            JsonNode readResultJson = mapper.readTree(readResult);

            // Create local copy
            Files.write(Paths.get(fileName),
                    readResultJson.get("data").asText().getBytes(), StandardOpenOption.CREATE);

            // Encode JSON
            JsonNode writeCopyJson = mapper.createObjectNode();
            ((ObjectNode) writeCopyJson).put("name", fileName + "(copy)");
            ((ObjectNode) writeCopyJson).put("data", readResultJson.get("data").asText());
            ((ObjectNode) writeCopyJson).put("length", readResultJson.get("length").asInt());

            // Write copy in server
            remote.write(mapper.writeValueAsString(writeCopyJson));

            System.out.println("Done");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}