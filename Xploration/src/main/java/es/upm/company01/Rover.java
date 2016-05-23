package es.upm.company01;

import jade.core.Agent;
import jade.core.AID;
import jade.domain.FIPAAgentManagement.*;
import jade.domain.DFService;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import jade.content.Concept;
import jade.content.ContentElement;
import jade.content.lang.Codec;
import jade.content.lang.sl.*;

import jade.content.onto.basic.*;

import es.upm.ontology.Direction;
import es.upm.ontology.Location;
import es.upm.ontology.MineralResult;
import es.upm.ontology.RegistrationRequest;
import es.upm.ontology.RequestRoverMovement;
import es.upm.ontology.XplorationOntology;

public class Rover extends Agent {
	// Codec for the SL language used and instance of the ontology
	private Codec codec = new SLCodec();
	// Declare Ontology
	public XplorationOntology ontology = (XplorationOntology) XplorationOntology.getInstance();
	//Name of Rover
	public final static String ROVER = "Rover01";
	
	//Name of World
	public final static String WORLD = "World";

	protected void setup() {
		try{
			System.out.println(getLocalName() + ": has entered");
	
			// Register of the codec and the ontology to be used in the ContentManager
			getContentManager().registerLanguage(codec);
			getContentManager().registerOntology(ontology);
			Location initialLocation = new Location();
			
			Object[] capsuleInfo = getArguments() ;
			initialLocation = (Location) capsuleInfo[1];
			
			System.out.println(getLocalName() + ": "+ capsuleInfo[0] + " position X: " + initialLocation.getX() + " Y: " + initialLocation.getY());
			
			RequestMovementBehaviuor reqMovRover = new RequestMovementBehaviuor(this);
			addBehaviour(reqMovRover); 
			
			doWait(1000);
			
			//RequestAnalyzeMineralBehaviuor reqMineral = new RequestAnalyzeMineralBehaviuor(this);
			//addBehaviour(reqMineral); 
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	class RequestMovementBehaviuor extends CyclicBehaviour{
		private static final long serialVersionUID = 1L;
		AID agWorld;
		private boolean endRequest = false;
		private String stateRegistration = "BEGIN";
		
		public RequestMovementBehaviuor(Agent a) {
			 super(a);
		}
		
		public void action() {
			switch (stateRegistration){
				case "BEGIN":						
					sendMovementRequest();
					//myAgent.doWait(500);
					break;
				case "REQUEST":
					receiveMessageForRequest();
					myAgent.doWait(500);
					break;
				case "END":
					//this.endRequest = true;
					block();
					break;
			}
			
		}
		
		public void sendMovementRequest(){
			
			//Creates the description for the type of agent to be searched "World"
			DFAgentDescription dfd_r = new DFAgentDescription();
			ServiceDescription sd_r = new ServiceDescription();
			
			sd_r.setType(WORLD);
			dfd_r.addServices(sd_r);
			
			this.stateRegistration = "REQUEST";
			
			myAgent.doWait(1000);
			try {

				// It finds agents of the required type
				DFAgentDescription[] res = new DFAgentDescription[20];
				res = DFService.search(myAgent, dfd_r);

				// Gets the first occurrence, if there was success
				if (res.length > 0) {
					
					System.out.println(getLocalName() + ": stablished communication with World");
					
					for(DFAgentDescription foundAgent : res) {
						
						agWorld = (AID)foundAgent.getName();
						
						// Asks request to the World
						ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
						msg.setSender(getAID());
						msg.setProtocol(ontology.PROTOCOL_ROVER_MOVEMENT);
						msg.setOntology(ontology.getName()); 
						msg.setLanguage(codec.getName());
						msg.addReceiver(agWorld);
						
						//Send Direction
						RequestRoverMovement objReqRoverMov = new RequestRoverMovement();
						Direction objDirection = new Direction();
						objDirection.setX(1);
						
						objReqRoverMov.setDirection(objDirection);
						
						//Package the message
						getContentManager().fillContent(msg, new Action(getAID(), objReqRoverMov));
						
						//Send the message
						send(msg);
						
						return;
					}

				} else {
					System.out.println(getLocalName() + ": Didn't found World.");
				}
					
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		public void receiveMessageForRequest(){
			// Waiting the answer
			MessageTemplate mt = MessageTemplate.and(
					MessageTemplate.and(MessageTemplate.MatchLanguage(codec.getName()),
							MessageTemplate.MatchOntology(ontology.getName())),
					MessageTemplate.MatchProtocol(ontology.PROTOCOL_ROVER_MOVEMENT));
			
			ACLMessage msgReceive = myAgent.blockingReceive(mt);
			if (msgReceive != null) {
				
				// Process the message
				int performative = msgReceive.getPerformative();
				
				myAgent.doWait(1000);
				
				switch (performative)
		        {
					case ACLMessage.REFUSE:
						System.out.println(getLocalName() + ": is late - Spacecraft answers - REFUSE");
						break;
					case ACLMessage.AGREE:
						System.out.println(getLocalName() + ": Rover is moving - AGREE");
						break;
					case ACLMessage.FAILURE:
						System.out.println(getLocalName() + ": is CRASH - FAILURE");
						break;
					case ACLMessage.INFORM:
						System.out.println(getLocalName() + ": movement was Successful - INFORM");
						
						RequestAnalyzeMineralBehaviuor reqMineral = new RequestAnalyzeMineralBehaviuor(myAgent);
						addBehaviour(reqMineral); 
						
						this.stateRegistration = "END";
						
						break;
					default:
						//NOT_UNDERSTOOD
						System.out.println(getLocalName() + ": Spacecraft answers - NOT UNDERSTOOD");
						this.stateRegistration = "END";
						//end = true;
						break;
		        }
			} else {
				this.endRequest = true;
			}
		}
		/*
		public boolean done() {
			return endRequest;
		}*/
	}
	
	//Behaviour Request Analyze Mineral Behaviuor
	class RequestAnalyzeMineralBehaviuor extends Behaviour { 
				private static final long serialVersionUID = 1L;
				AID agWorld;
				private boolean endAnalyze = false;
				private String stateAnalyze = "BEGIN";
				
				public RequestAnalyzeMineralBehaviuor(Agent a) {
					 super(a);
				} 

				public void action() {
					switch (stateAnalyze){
						case "BEGIN":						
							sendAnalyzeMineralRequest();
							doWait(500);
							break;
						case "REQUEST":
							receiveMessageForAnalyze();
							doWait(500);
							break;
						case "END":
							this.endAnalyze = true;
							break;
					}
					
				}

				public boolean done() {
					return this.endAnalyze;
				}
				
				public void sendAnalyzeMineralRequest(){
					//Creates the description for the type of agent to be searched "World"
					DFAgentDescription dfd = new DFAgentDescription();
					ServiceDescription sd = new ServiceDescription();
					
					sd.setType(WORLD);
					dfd.addServices(sd);
					
					try {
						this.stateAnalyze = "REQUEST";
						
						// It finds agents of the required type
						DFAgentDescription[] res = new DFAgentDescription[20];
						res = DFService.search(myAgent, dfd);

						// Gets the first occurrence, if there was success
						if (res.length > 0) {
							
							System.out.println(getLocalName() + ": stablished communication with World");
							
							for(DFAgentDescription foundAgent : res) {
								
								agWorld = (AID)foundAgent.getName();
								
								// Asks request to the World
								ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
								msg.setSender(getAID());
								msg.setProtocol(ontology.PROTOCOL_ANALYZE_MINERAL);
								msg.setOntology(ontology.getName()); 
								msg.setLanguage(codec.getName());
								msg.addReceiver(agWorld);
								
								//Send the message
								send(msg);
								
								return;
							}

						} else {
							System.out.println(getLocalName() + ": Didn't found a Spacecraft.");
						}
							
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				
				public void receiveMessageForAnalyze(){
					// Waiting the answer
					MessageTemplate mt = MessageTemplate.and(
							MessageTemplate.and(MessageTemplate.MatchLanguage(codec.getName()),
									MessageTemplate.MatchOntology(ontology.getName())),
							MessageTemplate.MatchProtocol(ontology.PROTOCOL_ANALYZE_MINERAL));
					
					// The ContentManager transforms the message content (string) in java objects
					ContentElement cElementSpace = null;
					
					ACLMessage msgReceive = myAgent.receive(mt);
					
					if (msgReceive != null) {
						
						// Process the message
						int performative = msgReceive.getPerformative();
						
						myAgent.doWait(500);
						
						switch (performative)
				        {
						
					        case ACLMessage.REFUSE:
								System.out.println(getLocalName() + ": is crash - Mineral Information - REFUSE");
								break;
							case ACLMessage.AGREE:
								System.out.println(getLocalName() + ": World is sending Mineral Information - AGREE");
								break;
							case ACLMessage.INFORM:
								
								System.out.println(getLocalName() + ": World is sending - INFORM");
								
								try {
									// Unpacking the content
									cElementSpace = getContentManager().extractContent(msgReceive);
									
									// We expect an action inside the message
									if (cElementSpace instanceof Action) {
										
										Action agAction = (Action)cElementSpace;
										Concept concMineral = agAction.getAction();
										//AID agentSender = agAction.getActor();
										
										// If the action is RegistrationRequest...
										if (concMineral instanceof MineralResult) {
											MineralResult request = (MineralResult) concMineral;
											
											//Get Company
											String mineralFound = request.getMineral().getType();
											
											// If an Request arrives
											System.out.println(myAgent.getLocalName() + ": found is "+ mineralFound);
										}
									}
									
									this.stateAnalyze = "END";
									
								} catch (Exception e) {					
									e.printStackTrace();
								}
									
								break;
							default:
								//NOT_UNDERSTOOD
								System.out.println(getLocalName() + ": Spacecraft answers - NOT UNDERSTOOD");
								this.stateAnalyze = "END";
								break;
				        }
					} else {
						//this.endAnalyze = true;
					}
				}
		}
}