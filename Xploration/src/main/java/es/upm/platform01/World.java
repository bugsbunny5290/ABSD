package es.upm.platform01;

import jade.core.Agent;
import jade.core.AID;
import jade.domain.FIPAAgentManagement.*;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.content.lang.Codec;
import jade.content.lang.Codec.*;
import jade.content.lang.sl.*;

import jade.content.*;
import jade.content.onto.*;
import jade.content.onto.basic.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;

import es.upm.ontology.Location;
import es.upm.ontology.Mineral;
import es.upm.ontology.MineralResult;
import es.upm.ontology.RegistrationRequest;
import es.upm.ontology.ReleaseCapsule;
import es.upm.ontology.RequestRoverMovement;
import es.upm.ontology.XplorationOntology;
import es.upm.platform01.Spacecraft.RegistrationBehaviour;
import es.upm.platform01.Spacecraft.ReleaseCapsuleBehavoiur;

public class World extends Agent{

	// Codec for the SL language used and instance of the ontology
	private Codec codec = new SLCodec();

	// Declare Ontology
	public XplorationOntology ontology = (XplorationOntology) XplorationOntology.getInstance();

	private static final long serialVersionUID = 1L;
	public final static String WORLD = "World";
	
	public Map<Integer, AID> directionRover = new HashMap<Integer, AID>(); 
	ArrayList<AID> crashRovers = new ArrayList<AID>();
	
	int numRequest = 0;

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
			sd.setType(WORLD);
			dfd.addServices(sd);

			// Registers its description in the DF
			DFService.register(this, dfd);
			System.out.println(getLocalName() + ": registered in the DF");
			dfd = null;
			sd = null;
			doWait(500);
			
			MovementBehaviour moveRover = new MovementBehaviour(this);
			addBehaviour(moveRover); 
			
			AnalyzeMineralBehaviour analyzeMineral = new AnalyzeMineralBehaviour(this);
			addBehaviour(analyzeMineral);
			
		} catch (FIPAException e) {
			e.printStackTrace();
		}

	}
	
	class MovementBehaviour extends CyclicBehaviour {
		private static final long serialVersionUID = 1L;

		public MovementBehaviour(Agent a) {
			super(a);
		} 

		public void action() {
			MessageTemplate mt = MessageTemplate.and(
					MessageTemplate.and(MessageTemplate.MatchLanguage(codec.getName()),
							MessageTemplate.MatchOntology(ontology.getName())),
					MessageTemplate.and(MessageTemplate.MatchProtocol(ontology.PROTOCOL_ROVER_MOVEMENT),
							MessageTemplate.or(
									MessageTemplate.MatchPerformative(ACLMessage.REQUEST), 
									MessageTemplate.MatchPerformative(ACLMessage.CANCEL))
							));

			// The ContentManager transforms the message content (string) in java objects
			ContentElement cElementSpace = null;

			// Waits for request
			ACLMessage msgRequest = myAgent.receive(mt);

			try {

				if (msgRequest != null) {

					numRequest++;

					// Unpacking the content
					cElementSpace = getContentManager().extractContent(msgRequest);

					// We expect an action inside the message
					if (cElementSpace instanceof Action) {

						Action agAction = (Action)cElementSpace;
						Concept concReqMovement = agAction.getAction();
						//AID agentSender = agAction.getActor();

						// If the action is RegistrationRequest...
						if (concReqMovement instanceof RequestRoverMovement) {
							RequestRoverMovement request = (RequestRoverMovement) concReqMovement;

							int direction = request.getDirection().getX();

							ACLMessage reply = msgRequest.createReply();
							reply.setSender(getAID());
							reply.setProtocol(ontology.PROTOCOL_ROVER_MOVEMENT);
							reply.setOntology(ontology.getName()); 
							reply.setLanguage(codec.getName());
							reply.addReceiver(msgRequest.getSender());


							if (msgRequest.getPerformative() == ACLMessage.REQUEST) {

								// If doesn't exist the position
								if(!directionRover.containsKey(direction)) {
									if (numRequest == 1){
										//Get Company
										String roverName = msgRequest.getSender().getLocalName();

										// If an Request arrives
										System.out.println(myAgent.getLocalName() + ": answer agree to "+ roverName);

										reply.setPerformative(ACLMessage.AGREE);

										//Send the message
										send(reply);

										//Add direction and agent
										directionRover.put(direction, msgRequest.getSender());

										// Inform the ROVER is moving
										reply.setPerformative(ACLMessage.INFORM);

										//Send the message
										send(reply);

									} else
									{
										//over has already send one request to the world
										reply.setPerformative(ACLMessage.REFUSE);

										//Send the message
										send(reply);
									}

								} else {
									//Rover crash with another rover
									reply.setPerformative(ACLMessage.FAILURE);

									//Send the message
									send(reply);

									doWait(1000);
								}

							} else if (msgRequest.getPerformative() == ACLMessage.CANCEL) {

								System.out.println(msgRequest.getSender() + " is asking for CANCEL");

								//Remove direction of an Agent
								directionRover.remove(direction, msgRequest.getSender());

							}
						}

					} else {
						ACLMessage reply = msgRequest.createReply();
						reply.setSender(getAID());
						reply.setProtocol(ontology.PROTOCOL_ROVER_MOVEMENT);
						reply.setOntology(ontology.getName()); 
						reply.setLanguage(codec.getName());
						reply.setPerformative(ACLMessage.NOT_UNDERSTOOD);
						reply.addReceiver(msgRequest.getSender());

						//Send the message
						send(reply);

					}


				} else {
					block();
				}
			} catch (Exception e) {					
				e.printStackTrace();
			}
		}
	}
	
	class AnalyzeMineralBehaviour extends CyclicBehaviour {
		private static final long serialVersionUID = 2L;
				
		public AnalyzeMineralBehaviour(Agent a) {
			 super(a);
		} 

		public void action() {
			MessageTemplate mt = MessageTemplate.and(
					MessageTemplate.and(MessageTemplate.MatchLanguage(codec.getName()),
							MessageTemplate.MatchOntology(ontology.getName())),
					MessageTemplate.and(MessageTemplate.MatchProtocol(ontology.PROTOCOL_ANALYZE_MINERAL),
							MessageTemplate.MatchPerformative(ACLMessage.REQUEST)));
			
			// The ContentManager transforms the message content (string) in java objects
			ContentElement cElementSpace = null;
			
			// Waits for request
			ACLMessage msgRequest = myAgent.receive(mt);
			
			try {
				
				if (msgRequest != null) {
					
					ACLMessage reply = msgRequest.createReply();
					reply.setSender(getAID());
					reply.setProtocol(ontology.PROTOCOL_ANALYZE_MINERAL);
					reply.setOntology(ontology.getName()); 
					reply.setLanguage(codec.getName());
					reply.addReceiver(msgRequest.getSender());
					
					
					if (isAliveRover(msgRequest.getSender())) { 
						
						reply.setPerformative(ACLMessage.AGREE);
						send(reply);
						System.out.println(myAgent.getLocalName() + ": is responding AGREE");
						
						myAgent.doWait(1000);
						
						reply.setPerformative(ACLMessage.INFORM);

						//Information about Mineral
						MineralResult objMineralFound = new MineralResult();
						Mineral objMineral = new Mineral();
						
						//According to the position of a Rover, send the mineral information
						objMineral.setType("A"); 
						
						objMineralFound.setMineral(objMineral);
						
						//Package the message
						try {
							getContentManager().fillContent(reply, new Action(getAID(), objMineralFound));
							//Send the message
							send(reply);
							
							System.out.println(myAgent.getLocalName() + ": is responding INFORM");
							
						} catch (CodecException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (OntologyException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					} else {
						reply.setPerformative(ACLMessage.REFUSE);
						send(reply);
					}
					
				} else {
					block();
					/*ACLMessage reply = msgRequest.createReply();
					reply.setSender(getAID());
					reply.setProtocol(ontology.PROTOCOL_ANALYZE_MINERAL);
					reply.setOntology(ontology.getName()); 
					reply.setLanguage(codec.getName());
					reply.addReceiver(msgRequest.getSender());
					
					reply.setPerformative(ACLMessage.NOT_UNDERSTOOD);
					
					//Send the message
					send(reply);*/
				}
			} catch (Exception e) {					
				e.printStackTrace();
			}
		}
		
		public boolean isAliveRover (AID rover) {
			//Check if the Rover is Alive
			
			return true;
		}
	}	
}
