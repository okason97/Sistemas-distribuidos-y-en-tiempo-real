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
import java.nio.charset.StandardCharsets;
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

            // size, offset, name
            String[] splitData = data.split("\n", 3);

            System.out.println("Reading from " + splitData[2] + " " +
                    splitData[0] + " bytes with " +
                    splitData[1] + " offset");

            // Read from file
            RandomAccessFile fileToRead = new RandomAccessFile(new File(splitData[2]), "r");
            byte[] buffer = new byte[Integer.valueOf(splitData[0])];
            fileToRead.seek(Long.valueOf(splitData[1]));
            int amountRead = fileToRead.read(buffer);

            String result = String.valueOf(amountRead) + "\n" + new String(buffer, StandardCharsets.ISO_8859_1);
            return result;
        } catch (IOException e) {
            return ("0" + "\n" + "");
        }
    }
    /* Write data from local file system (server) */
    public String write(String data) throws RemoteException{
        ObjectMapper mapper = new ObjectMapper();
        try {
            // name, size, data
            String[] splitData = data.split("\n", 3);

            System.out.println("Writing to " + splitData[0] + " " +
                    splitData[1] + " bytes");

            // Append to the end of the file
            Files.write(Paths.get(splitData[0]),
                    splitData[2].getBytes(StandardCharsets.ISO_8859_1), StandardOpenOption.CREATE,  StandardOpenOption.APPEND);

            return String.valueOf(splitData[2].getBytes(StandardCharsets.ISO_8859_1).length);
        } catch (IOException e) {
            return "0";
        }
    }
    /* Read data from local file system (server) */
    public String sizeOf(String data) throws RemoteException{
        System.out.println("Reading size from " + data);
        return String.valueOf(new File(data).length());
    }
}