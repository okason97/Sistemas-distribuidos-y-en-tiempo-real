import com.sun.management.OperatingSystemMXBean;
import java.util.Iterator;
import jade.util.leap.List;
import jade.core.*;
import jade.core.behaviours.*;
import jade.content.*;
import jade.content.onto.basic.Result;
import jade.content.onto.basic.Action;
import jade.content.onto.OntologyException;
import jade.content.onto.UngroundedException;
import jade.content.lang.Codec.CodecException;
import jade.content.lang.sl.SLCodec;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.UnknownHostException;
import jade.domain.JADEAgentManagement.QueryPlatformLocationsAction;
import jade.domain.JADEAgentManagement.JADEManagementOntology;
import jade.domain.*;

public class ReporterAgent extends Agent{
    long startTime;
    String originName = "origin";

    // Ejecutado por unica vez en la creacion
    public void setup(){
        Location origen = here();
        System.out.println("\n\nHola, agente con nombre local " + getLocalName());
        System.out.println("Y nombre completo... " + getName());
        System.out.println("Y en location " + origen.getID() + "\n\n");

        addBehaviour(new TickerBehaviour(this, 10000) {
            protected void onTick() {
                try{
                    queryAMS();
                    listenForAMSReply();
                }catch(CodecException | OntologyException e){
                    e.printStackTrace();
                }
            }

            private void queryAMS() throws CodecException, OntologyException {
                QueryPlatformLocationsAction query = new QueryPlatformLocationsAction();
                Action action = new Action(myAgent.getAID(), query);
            
                //Register the SL content language
                myAgent.getContentManager().registerLanguage(
                    new SLCodec(), FIPANames.ContentLanguage.FIPA_SL);

                //Register the mobility ontology
                myAgent.getContentManager().registerOntology(
                    JADEManagementOntology.getInstance());
                ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
                message.addReceiver(myAgent.getAMS());
                message.setLanguage(FIPANames.ContentLanguage.FIPA_SL);
                message.setOntology(JADEManagementOntology.getInstance().getName());
                myAgent.getContentManager().fillContent(message, action);
                myAgent.send(message);
            }
        
            private void listenForAMSReply() throws UngroundedException, CodecException, 
            OntologyException {
                ACLMessage receivedMessage = blockingReceive(MessageTemplate
                        .MatchSender(myAgent.getAMS()));
                ContentElement content = getContentManager().extractContent(
                    receivedMessage);
            
                // received message is a Result object, whose Value field is a List of
                // ContainerIDs
                Result result = (Result) content;
                List listOfPlatforms = (List) result.getValue();
            
                startTime = System.nanoTime();    
        
                // use it
                Iterator iter = listOfPlatforms.iterator();
                while (iter.hasNext()) {
                    ContainerID next = (ContainerID) iter.next();
        
                    myAgent.addBehaviour(new OneShotBehaviour(myAgent) {

                        public void action() {
                    
                            // Para migrar el agente
                            try {
                                myAgent.doMove(next);
                                System.out.println("Migrando el agente a " + next.getID());
                            } catch (Exception e) {
                                System.out.println("\n\n\nNo fue posible migrar el agente a " + next.getID() + "\n\n\n");
                            }
                            
                        }
                    
                    } );
  
                }
            }
        } );
    }

    // Ejecutado al llegar a un contenedor como resultado de una migracion
    protected void afterMove(){
        System.out.println("\n\nHola, agente migrado con nombre local " + getLocalName());
        System.out.println("Y nombre completo... " + getName());
        System.out.println("Y en location " + here().getID() + "\n\n");

        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.addReceiver(new AID(originName, AID.ISLOCALNAME));
        OperatingSystemMXBean operatingSystemMXBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

        try {
            msg.setContent(String.valueOf(startTime) + "\n" + 
            operatingSystemMXBean.getSystemCpuLoad() + "\n" +
            operatingSystemMXBean.getFreeSwapSpaceSize() + "\n" +
            InetAddress.getLocalHost().getHostName());
        } catch (UnknownHostException e) {
            msg.setContent(String.valueOf(startTime) + "\n" + 
            operatingSystemMXBean.getSystemCpuLoad() + "\n" +
            operatingSystemMXBean.getFreeSwapSpaceSize() + "\n" +
            "unknown");
        }

        send(msg);
    }


}