import jade.core.*;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import java.io.IOException;
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

public class FSAgent extends Agent{
    
    // Ejecutado por unica vez en la creacion
    public void setup(){
        Location origen = here();
        System.out.println("\n\nHola, agente con nombre local " + getLocalName());
        System.out.println("Y nombre completo... " + getName());
        System.out.println("Y en location " + origen.getID() + "\n\n");

        // Register the book-selling service in the yellow pages
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("file-system");
        sd.setName("file-system-agent");
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        }catch (FIPAException fe) {
            fe.printStackTrace();
        }

        addBehaviour(new ReceiveQuery(this));
    }

    protected void takeDown() {
        // Deregister from the yellow pages
        try {
            DFService.deregister(this);
        }catch (FIPAException fe) {
            fe.printStackTrace();
        }
        // Printout a dismissal message
        System.out.println("Seller-agent "+getAID().getName()+" terminating.");
    }

    private class ReceiveQuery extends CyclicBehaviour {
        public ReceiveQuery(Agent a){
            super(a);
        }

        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
                String type = msg.getContent().split("\n", 2)[0];
                System.out.println(type);
                if (type.equals("read")) {
                    String[] lines = msg.getContent().split("\n");
                    System.out.println("Reading from " + lines[1] + " " +
                                        Integer.parseInt(lines[2]) + " bytes with " +
                                        Integer.parseInt(lines[3]) + " offset");
                    try {
                        // Read from file
                        RandomAccessFile fileToRead = new RandomAccessFile(
                        new File(lines[1]), "r");
                        fileToRead.seek(Long.parseLong(lines[3]));
                        byte[] buffer = new byte[Integer.parseInt(lines[2])];
                        int amountRead = fileToRead.read(buffer);

                        // Return message
                        ACLMessage reply = msg.createReply();
                        reply.setPerformative(ACLMessage.INFORM);
                        reply.setConversationId("read");
                        reply.setContent(
                            String.valueOf(amountRead) + "\n" +
                            new String(buffer, StandardCharsets.ISO_8859_1));
                        send(reply);
                    } catch (IOException e) {
                        System.out.println(e.getMessage());
                        // Return message
                        ACLMessage reply = msg.createReply();
                        reply.setPerformative(ACLMessage.INFORM);
                        reply.setContent("0\n");
                        send(reply);
                    }       
                }else if (type.equals("write")) {
                    String[] lines = msg.getContent().split("\n", 4);
                    try {
                        String data = lines[3];
                        System.out.println("Writing to " + lines[1] + " " +
                                            lines[2] + " bytes");
                        // Append to the end of the file
                        Files.write(Paths.get(lines[1]),
                                    data.getBytes(StandardCharsets.ISO_8859_1), 
                                    StandardOpenOption.CREATE,
                                    StandardOpenOption.APPEND);

                        // Return message
                        ACLMessage reply = msg.createReply();
                        reply.setPerformative(ACLMessage.INFORM);
                        reply.setContent(lines[2]);
                        send(reply);
                    } catch (IOException e) {
                        System.out.println(e.getMessage());
                        // Return message
                        ACLMessage reply = msg.createReply();
                        reply.setPerformative(ACLMessage.INFORM);
                        reply.setContent("0");
                        send(reply);
                    }
                }else if (type.equals("size")){
                    String[] lines = msg.getContent().split("\n");
                    System.out.println("Size of " + lines[1]);

                    // Return message
                    ACLMessage reply = msg.createReply();
                    reply.setPerformative(ACLMessage.INFORM);
                    reply.setContent(String.valueOf(new File(lines[1]).length()));
                    send(reply);
                }
            }else block();
        }
    }
}