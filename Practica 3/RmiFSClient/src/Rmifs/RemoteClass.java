package Rmifs;/*
* RemoteClass.java
* Just implements the RemoteMethod interface as an extension to
* UnicastRemoteObject
*
*/
/* Needed for implementing remote method/s */
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/* This class implements the interface with remote methods */
public class RemoteClass extends UnicastRemoteObject implements IfaceRemoteClass{
    public RemoteClass() throws RemoteException{
        super();
    }
    /* Read data from local file system (server) */
    public String read(String data) throws RemoteException{
        ObjectMapper mapper = new ObjectMapper();
        try {
            // Decode JSON
            JsonNode jsonNode = mapper.readTree(data);

            System.out.println("Reading from " + jsonNode.get("name").asText() + " " +
                    jsonNode.get("length").asInt() + " bytes with " +
                    jsonNode.get("offset").asInt() + " offset");

            // Read from file
            RandomAccessFile fileToRead = new RandomAccessFile(new File(jsonNode.get("name").asText()), "r");
            byte[] buffer = new byte[jsonNode.get("length").asInt()];
            int amountRead = fileToRead.read(buffer, jsonNode.get("offset").asInt(), jsonNode.get("length").asInt());

            // Encode JSON
            JsonNode result = mapper.createObjectNode();
            ((ObjectNode) result).put("length", amountRead);
            ((ObjectNode) result).put("data", new String(buffer));
            return mapper.writeValueAsString(result);
        } catch (IOException e) {
            // Encode JSON
            JsonNode result = mapper.createObjectNode();
            ((ObjectNode) result).put("length", 0);
            ((ObjectNode) result).put("data", "");
            try {
                return mapper.writeValueAsString(result);
            } catch (JsonProcessingException e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();
            return  null;
        }
    }
    /* Write data from local file system (server) */
    public String write(String data) throws RemoteException{
        ObjectMapper mapper = new ObjectMapper();
        try {
            // Decode JSON
            JsonNode jsonNode = mapper.readTree(data);

            System.out.println("Writing to " + jsonNode.get("name").asText() + " " +
                    jsonNode.get("data").asText().getBytes().length + " bytes");

            // Append to the end of the file
            Files.write(Paths.get(jsonNode.get("name").asText()),
                    jsonNode.get("data").asText().getBytes(), StandardOpenOption.CREATE,  StandardOpenOption.APPEND);

            // Encode JSON
            JsonNode result = mapper.createObjectNode();
            ((ObjectNode) result).put("length", jsonNode.get("data").asText().getBytes().length);
            return mapper.writeValueAsString(result);
        } catch (IOException e) {
            // Encode JSON
            JsonNode result = mapper.createObjectNode();
            ((ObjectNode) result).put("length", 0);
            try {
                return mapper.writeValueAsString(result);
            } catch (JsonProcessingException e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();
            return  null;
        }
    }
}