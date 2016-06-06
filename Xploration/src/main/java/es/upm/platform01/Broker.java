package es.upm.platform01;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.*;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.content.lang.Codec;
import jade.content.lang.Codec.CodecException;
import jade.content.lang.sl.*;

import jade.content.*;
import jade.content.onto.OntologyException;
import jade.content.onto.basic.*;

import java.util.ArrayList;

import es.upm.ontology.FindingsMessage;
import es.upm.ontology.Location;
import es.upm.ontology.MoveInformation;
import es.upm.ontology.ReleaseCapsule;
import es.upm.ontology.XplorationOntology;

public class Broker extends Agent{
	
	// Codec for the SL language used and instance of the ontology
		private Codec codec = new SLCodec();

		// Declare Ontology
		public XplorationOntology ontology = (XplorationOntology) XplorationOntology.getInstance();
		
		//Rover in Range
		ArrayList<AID> roverInRange = new ArrayList();

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
				
				RoverPosition roverPosition = new RoverPosition(this);
				addBehaviour(roverPosition); 
				
				doWait(500);
				
				RoverFindings roverFindings = new RoverFindings(this);
				addBehaviour(roverFindings); 
				
			} catch (FIPAException e) {
				e.printStackTrace();
			}
			
		}
		
		//Inform the position to Broker 
		class RoverPosition extends CyclicBehaviour {
			private static final long serialVersionUID = 1L;

			public RoverPosition(Agent a) {
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
							Concept concRoverPosition = agAction.getAction();
							//AID agentSender = agAction.getActor();

							// If the action is MoveInformation...
							if (concRoverPosition instanceof MoveInformation) {
								MoveInformation roverPositionInfo = (MoveInformation) concRoverPosition;
								
								//Store information about Capsule, Rover and position
								World.locationAgents.put(roverPositionInfo.getRover().getRover_agent(), roverPositionInfo.getLocation());

								System.out.println( "Broker: " + roverPositionInfo.getRover().getName() + " informs " + 
													"Location: x. " + roverPositionInfo.getLocation().getX() + " y." + roverPositionInfo.getLocation().getY() +
													" Direction: " + roverPositionInfo.getDirection().getX());
								
								for(AID roverReg : roverInRange)
								{
									//Send information to all ROVERs in the range
									ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
									msg.setSender(getAID());
									msg.setProtocol(ontology.PROTOCOL_MOVE_INFO);
									msg.setOntology(ontology.getName()); 
									msg.setLanguage(codec.getName());
									msg.addReceiver(roverReg);

									//Package the message
									try {
										getContentManager().fillContent(msg, new Action(getAID(), roverPositionInfo));
									} catch (CodecException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									} catch (OntologyException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
						
									//Send the message
									send(msg);
								}
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
		
		//Inform the position to Broker 
		class RoverFindings extends CyclicBehaviour {
			private static final long serialVersionUID = 1L;

			public RoverFindings(Agent a) {
				super(a);
			} 

			public void action() {
				MessageTemplate mt = MessageTemplate.and(
						MessageTemplate.and(MessageTemplate.MatchLanguage(codec.getName()),
								MessageTemplate.MatchOntology(ontology.getName())),
						MessageTemplate.and(MessageTemplate.MatchProtocol(ontology.PROTOCOL_SEND_FINDINGS),
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
							Concept concFindingsMessage = agAction.getAction();
							//AID agentSender = agAction.getActor();

							// If the action is MoveInformation...
							if (concFindingsMessage instanceof FindingsMessage) {
								FindingsMessage roverFindingsInfo = (FindingsMessage) concFindingsMessage;
								
								//if(isReachableCapsule())
								{
									//Send information to all Capsule in the range
									ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
									msg.setSender(getAID());
									msg.setProtocol(ontology.PROTOCOL_SEND_FINDINGS);
									msg.setOntology(ontology.getName()); 
									msg.setLanguage(codec.getName());
									
									msg.addReceiver(getCapsuleByRover(msgRequest.getSender()));
									
									doWait(500);
									//System.out.println( "Broker: informs Mineral to Capsule" + getCapsuleByRover(msgRequest.getSender()));
									System.out.println( "Broker: informs Mineral to Capsule" );
									
									//Package the message
									try {
										getContentManager().fillContent(msg, new Action(getAID(), roverFindingsInfo));
									} catch (CodecException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									} catch (OntologyException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
						
									//Send the message
									send(msg);
								}
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
		
		public AID getCapsuleByRover(AID varRover){
			AID varCapsule = null;
			
			for (int i=0; i< Spacecraft.registerAgents.size(); i++) {
				if (Spacecraft.registerAgents.get(i).getRover().getRover_agent() == varRover){
					varCapsule = Spacecraft.registerAgents.get(i).getCapsule().getCapsule_agent();
					break;
				}
			}
			
			return varCapsule;
		}
}
