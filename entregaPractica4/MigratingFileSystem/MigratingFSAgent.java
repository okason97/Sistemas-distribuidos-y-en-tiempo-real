import java.util.ArrayList;
import java.util.List;
import jade.core.*;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import java.io.IOException;
import java.net.InetAddress;
import jade.content.*;
import jade.domain.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.*;
import java.nio.file.StandardOpenOption;

public class MigratingFSAgent extends Agent{
    String fileName, copyName;
    Location origen;
    long fileSize, bytesCopied;
    int bytesRead;
    int bufferSize = 1024;
    byte[] buffer;
    ContainerID destino;

    // Ejecutado por unica vez en la creacion
    public void setup(){
        origen = here();
        bytesCopied = 0;
        fileSize = Long.MAX_VALUE;
        System.out.println("\n\nHola, agente con nombre local " + getLocalName());
        System.out.println("Y nombre completo... " + getName());
        System.out.println("Y en location " + origen.getID() + "\n\n");

        // Get container name, file name to copy and it's copy name
        Object[] args = getArguments();
        System.out.println(args.length);
        if (args != null && args.length >= 3) {
            fileName = (String) args[1];
            copyName = (String) args[2];
            destino = new ContainerID((String) args[0], null);

            // Para migrar el agente
            try {
                System.out.println("Migrando el agente a " + destino.getID());
                doMove(destino);
            } catch (Exception e) {
                System.out.println("\n\n\nNo fue posible migrar el agente a " + destino.getID() + "\n\n\n");
            }
        }else {
            // Make the agent terminate immediately
            System.out.println("No file specified");
            doDelete();
        }
    }

    // Ejecutado al llegar a un contenedor como resultado de una migracion
    protected void afterMove(){
        if (fileSize > bytesCopied){
            Location location = here();
            if (!location.getID().equals(origen.getID())){
                System.out.println("\n\nHola, agente migrado con nombre local " + getLocalName());
                System.out.println("Y nombre completo... " + getName());
                System.out.println("Y en location " + location.getID() + "\n\n");
    
                if (fileSize == Long.MAX_VALUE){
                    fileSize = FilesManager.size(fileName);
                }
                ArrayList readReturn = FilesManager.read(fileName, bufferSize, bytesCopied);
                bytesRead = (int)readReturn.get(0);
                buffer = (byte[])readReturn.get(1);
                bytesCopied += bytesRead;
                if (FilesManager.write(copyName, buffer, bytesRead) == 0){
                    System.out.println("Error writing \n\n");                    
                }
                doMove(origen);
            }else{
                FilesManager.write(fileName, buffer, bytesRead);
                doMove(destino);
            }
        }else{
            FilesManager.write(fileName, buffer, bytesRead);
            // finished
            doDelete();
        }
    }

    private static class FilesManager{
        private static ArrayList read(String filename, int bytes, long off){
            System.out.println("Reading from " + filename + " " +
                                bytes + " bytes with " +
                                off + " offset");
            ArrayList answer = new ArrayList();
            try {
                // Read from file
                RandomAccessFile fileToRead = new RandomAccessFile(
                new File(filename), "r");
                fileToRead.seek(off);
                byte[] buffer = new byte[bytes];
                int amountRead = fileToRead.read(buffer);
                answer.add(amountRead);
                answer.add(buffer);
            } catch (IOException e) {
                System.out.println(e.getMessage());
                answer.add(0);
                answer.add(new byte[0]);
            }
            return answer;
        }

        private static long write(String filename, byte[] buffer, long bytes){
            try {
                System.out.println("Writing to " + filename + " " +
                                    bytes + " bytes");
                // Append to the end of the file
                Files.write(Paths.get(filename),
                            buffer, 
                            StandardOpenOption.CREATE,
                            StandardOpenOption.APPEND);

                // Return message
                return bytes;
            } catch (IOException e) {
                // error, kill it
                System.out.println(e.getMessage());
                return 0;
            }
        }
        private static long size(String filename){
            System.out.println("Size of " + filename);
            return (new File(filename).length());
        }
    }
}
