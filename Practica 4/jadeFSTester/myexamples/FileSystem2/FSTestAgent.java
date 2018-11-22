import java.util.List;
import jade.core.*;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import java.io.IOException;
import jade.content.onto.basic.Action;
import jade.lang.acl.MessageTemplate;
import jade.content.*;
import jade.domain.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import java.io.*;
import java.nio.charset.StandardCharsets;

public class FSTestAgent extends Agent{
    String fileName = "file";
    int ammountReceived = 0;
    AID fsID;
    int fileSize;

    // Ejecutado por unica vez en la creacion
    public void setup(){
        Location origen = here();
        System.out.println("\n\nHola, agente con nombre local " + getLocalName());
        System.out.println("Y nombre completo... " + getName());
        System.out.println("Y en location " + origen.getID() + "\n\n");

        // Update the list of fs agents
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType("file-system");
        sd.setName("file-system-agent");
        template.addServices(sd);
        DFAgentDescription[] result = null;
        try {
            result = DFService.search(this, template);
        }catch (FIPAException fe) {
            fe.printStackTrace();
        }
        if (result != null && result.length > 0) {
            System.out.println("\n\nLeyendo tamaño" + fileName + ".pdf");
            // Request file to fs agent
            ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
            fsID = result[0].getName();
            msg.addReceiver(fsID);
            msg.setContent("size\n"+ fileName + ".pdf");
            send(msg);
            addBehaviour(new ReceiveSizeBehaviour(this));
        }else{
            System.out.println("\n\nNo se encontró agente de fs en paginas amarillas");
            doDelete();
        }
    }

    public class ReceiveSizeBehaviour extends Behaviour {
        private ACLMessage msg = null;
        private boolean received = false;

        public ReceiveSizeBehaviour(Agent a){
            super(a);
        }

        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
            msg = myAgent.receive(mt);
            if (msg != null) {
                received = true;
                fileSize = Integer.parseInt(msg.getContent());
                System.out.println("\n\nLeyendo " + fileName + ".pdf" + " completo: " + 0);
                ACLMessage receiveMessage = new ACLMessage(ACLMessage.REQUEST);
                receiveMessage.addReceiver(fsID);
                receiveMessage.setContent("read\n"+ fileName + ".pdf\n"+
                    String.valueOf(fileSize)+"\n0");
                send(receiveMessage);
                myAgent.addBehaviour(new CopycatBehaviour(myAgent));
            }else block();
        }
        public boolean done() {
            return received;
        }
    }

    public class CopycatBehaviour extends Behaviour {
        private boolean finished = false;
        private ACLMessage msg = null;

        public CopycatBehaviour(Agent a){
            super(a);
        }

        public void action() {
            MessageTemplate mt = MessageTemplate.and(
                MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                MessageTemplate.MatchConversationId("read"));
            msg = myAgent.receive(mt);
            if (msg != null) {
                System.out.println("\n\nRecibida respuesta");
                finished = true;

                String[] splitData = msg.getContent().split("\n", 2);
                int ammountRead = Integer.valueOf(splitData[0]);
                String dataRead = splitData[1];
                ammountReceived += ammountRead;
                try {
                    // Create local copy
                    System.out.println("\n\nCreando copia local de " + fileName + ".pdf");
                    Files.write(Paths.get(fileName + ".pdf"),
                        dataRead.getBytes(StandardCharsets.ISO_8859_1),
                        StandardOpenOption.APPEND,
                        StandardOpenOption.CREATE);

                    // Write copy in server
                    System.out.println("\n\nEscribiendo copia en otro agente ");
                    ACLMessage reply = new ACLMessage(ACLMessage.REQUEST);
                    reply.addReceiver(msg.getSender());
                    reply.setContent("write\n"+ 
                        fileName + "(copy).pdf\n" + 
                        dataRead.length() + "\n" +
                        dataRead);
                    send(reply);                                            
                } catch (IOException e) {
                    System.out.println("\n\nNo se pudo crear una copia");
                    myAgent.doDelete();
                }
                if(ammountReceived < fileSize){
                    System.out.println("\n\nLeyendo " + fileName + ".pdf" + " completo: " + ammountReceived);
                    ACLMessage receiveMessage = new ACLMessage(ACLMessage.REQUEST);
                    receiveMessage.addReceiver(fsID);
                    receiveMessage.setContent("read\n"+ fileName + ".pdf\n"+
                        String.valueOf(fileSize-ammountReceived)+"\n"+
                        String.valueOf(ammountReceived));
                    send(receiveMessage);
                    myAgent.addBehaviour(new CopycatBehaviour(myAgent));    
                }
            }else block();
        }
        public boolean done() {
            return finished;
        }
    }
}