/*Implemente un agente que copie un archivo de otro sitio del sistema distribuido en el
sistema de archivos local y genere una copia del mismo archivo en el sitio donde esta ́
originalmente. Compare esta solución con la de los sistemas cliente/servidor de las
prácticas anteriores.*/

import com.sun.management.OperatingSystemMXBean;
import java.util.Iterator;
import java.util.List;
import jade.core.*;
import jade.core.behaviours.*;
import jade.content.onto.OntologyException;
import jade.lang.acl.ACLMessage;
import jade.content.onto.UngroundedException;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import jade.domain.JADEAgentManagement.QueryPlatformLocationsAction;
import jade.content.onto.basic.Action;
import jade.domain.JADEAgentManagement.JADEManagementOntology;
import jade.lang.acl.MessageTemplate;
import jade.content.*;
import jade.content.onto.basic.Result;
import jade.domain.*;
import jade.content.lang.Codec.CodecException;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;
import jade.content.lang.sl.SLCodec;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import java.io.*;

public class FSTestAgent extends Agent{
    String fileName = "file";

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
            System.out.println("\n\nLeyendo " + fileName + ".txt");
            // Request file to fs agent
            ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
            msg.addReceiver(result[0].getName());
            msg.setContent("read\n"+ fileName + ".txt\n10\n0");
            send(msg);
            addBehaviour(new CopycatBehaviour(this));
        }else{
            System.out.println("\n\nNo se encontró agente de fs en paginas amarillas");
            doDelete();
        }
    }

    public class CopycatBehaviour extends Behaviour {
        private boolean received = false;
        private ACLMessage msg = null;

        public CopycatBehaviour(Agent a){
            super(a);
        }

        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
            msg = myAgent.receive(mt);
            if (msg != null) {
                received = true;

                String dataRead = msg.getContent().split("\n", 2)[1];
                try {
                    // Create local copy
                    System.out.println("\n\nCreando copia local de " + fileName + ".txt");
                    Files.write(Paths.get(fileName + ".txt"),
                        dataRead.getBytes(),
                        StandardOpenOption.CREATE);

                    // Write copy in server
                    System.out.println("\n\nEscribiendo copia en otro agente ");
                    ACLMessage reply = new ACLMessage(ACLMessage.REQUEST);
                    reply.addReceiver(msg.getSender());
                    reply.setContent("write\n"+ 
                        fileName + "(copy).txt\n" + 
                        dataRead.length() + "\n" +
                        dataRead);
                    send(reply);                                            
                } catch (IOException e) {
                    System.out.println("\n\nNo se pudo crear una copia");
                    myAgent.doDelete();
                }
            }else block();
        }
        public boolean done() {
            return received;
        }
    }
}