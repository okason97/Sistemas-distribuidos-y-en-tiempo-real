import jade.core.behaviours.CyclicBehaviour;
import jade.core.*;
import jade.lang.acl.ACLMessage;
import java.io.*;

public class OriginAgent extends Agent{

    // Ejecutado por unica vez en la creacion
    public void setup(){
        Location origen = here();
        System.out.println("\n\nHola, agente con nombre local " + getLocalName());
        System.out.println("Y nombre completo... " + getName());
        System.out.println("Y en location " + origen.getID() + "\n\n");

        // add a Behaviour to handle messages from the reporter
        addBehaviour( new CyclicBehaviour( this ) {
            public void action() {
                ACLMessage msg = receive();

                if (msg != null) {
                    BufferedReader reader = new BufferedReader(new StringReader(msg.getContent()));
                    try {
                        System.out.println("\n\nTiempo de recorrido: " + (System.nanoTime() - Long.parseLong(reader.readLine())) + "\n");
                        System.out.println("Carga de procesamiento: " + reader.readLine() + "\n");
                        System.out.println("Memoria total libre: " + reader.readLine() + "\n");
                        System.out.println("Nombre de computadora: " + reader.readLine() + "\n\n");                            
                    } catch (IOException e) {
                        //TODO: handle exception
                    }
                }
                else {
                    // if no message is arrived, block the behaviour
                    block();
                }
            }
        } );
    }
}