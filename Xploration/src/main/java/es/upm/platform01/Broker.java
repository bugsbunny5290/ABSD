package es.upm.platform01;

import jade.core.Agent;
import jade.core.behaviours.*;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.content.lang.Codec;
import jade.content.lang.sl.*;

import jade.content.*;
import jade.content.onto.basic.*;

import es.upm.ontology.MoveInformation;
import es.upm.ontology.XplorationOntology;

public class Broker extends Agent{
	
	// Codec for the SL language used and instance of the ontology
		private Codec codec = new SLCodec();

		// Declare Ontology
		public XplorationOntology ontology = (XplorationOntology) XplorationOntology.getInstance();

		private static final long serialVersionUID = 1L;
		public final static String BROKER = "Broker";

		protected void setup() {
			
			// Register of the codec and the ontology to be used in the
			getContentManager().registerLanguage(codec);
			getContentManager().registerOntology(ontology);
			
			System.out.println(getLocalName() + ": has entered");
			
			try {
				// Creates its own description
				DFAgentDescription dfd = new DFAgentDescription();
				ServiceDescription sd = new ServiceDescription();
				sd.setName(this.getName());
				sd.setType(BROKER);
				dfd.addServices(sd);

				// Registers its description in the DF
				DFService.register(this, dfd);
				System.out.println(getLocalName() + ": registered in the DF");
				dfd = null;
				sd = null;
				doWait(500);
				
				ReceiveRoverPosition receiveRoverPosition = new ReceiveRoverPosition(this);
				addBehaviour(receiveRoverPosition); 
				
			} catch (FIPAException e) {
				e.printStackTrace();
			}
			
		}
		
		//Inform the position to Broker 
		class ReceiveRoverPosition extends CyclicBehaviour {
			private static final long serialVersionUID = 1L;

			public ReceiveRoverPosition(Agent a) {
				super(a);
			} 

			public void action() {
				MessageTemplate mt = MessageTemplate.and(
						MessageTemplate.and(MessageTemplate.MatchLanguage(codec.getName()),
								MessageTemplate.MatchOntology(ontology.getName())),
						MessageTemplate.and(MessageTemplate.MatchProtocol(ontology.PROTOCOL_MOVE_INFO),
								MessageTemplate.MatchPerformative(ACLMessage.INFORM)
								));

				// The ContentManager transforms the message content (string) in java objects
				ContentElement cElementSpace = null;

				// Waits for request
				ACLMessage msgRequest = myAgent.receive(mt);

				try {

					if (msgRequest != null) {
						// Unpacking the content
						cElementSpace = getContentManager().extractContent(msgRequest);

						// We expect an action inside the message
						if (cElementSpace instanceof Action) {

							Action agAction = (Action)cElementSpace;
							Concept concMoveInform = agAction.getAction();
							//AID agentSender = agAction.getActor();

							// If the action is MoveInformation...
							if (concMoveInform instanceof MoveInformation) {
								MoveInformation moveInfo = (MoveInformation) concMoveInform;

								System.out.println( "Broker: " + moveInfo.getRover().getName() + " informs " + 
													"Location: x. " + moveInfo.getLocation().getX() + " y." + moveInfo.getLocation().getY() +
													" Direction: " + moveInfo.getDirection().getX());
							}

						} 
					} else {
						block();
					}
				} catch (Exception e) {					
					e.printStackTrace();
				}
			}
		}
}
