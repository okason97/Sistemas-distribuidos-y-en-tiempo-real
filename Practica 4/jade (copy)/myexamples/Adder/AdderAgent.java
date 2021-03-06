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

public class AdderAgent extends Agent{
    String fileName;

    // Ejecutado por unica vez en la creacion
    public void setup(){
        Location origen = here();
        System.out.println("\n\nHola, agente con nombre local " + getLocalName());
        System.out.println("Y nombre completo... " + getName());
        System.out.println("Y en location " + origen.getID() + "\n\n");

        // Get the file to add numbers from
        Object[] args = getArguments();
        System.out.println(args.length);
        if (args != null && args.length >= 2) {
            fileName = (String) args[1];
            ContainerID destino = new ContainerID((String) args[0], null);

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
        Location origen = here();
        System.out.println("\n\nHola, agente migrado con nombre local " + getLocalName());
        System.out.println("Y nombre completo... " + getName());
        System.out.println("Y en location " + origen.getID() + "\n\n");
        List<String> lines = null;
        String s = Paths.get(fileName).toAbsolutePath().toString();
        System.out.println("Trying to read: " + s);
        try {
            lines = Files.readAllLines(Paths.get(fileName));            
        } catch (IOException e) {
            System.out.println(e.getMessage());
            doDelete();
        }
        if(lines != null && lines.size() > 0){
            int result = 0;
            for (String line : lines) {
                result += Integer.parseInt(line);
            }
            System.out.println(result);    
        }else{
            doDelete();
        }
    }
}