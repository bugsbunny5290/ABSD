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
import jade.content.lang.Codec;
import jade.content.lang.Codec.*;
import jade.content.lang.sl.*;

import jade.content.*;
import jade.content.onto.*;
import jade.content.onto.basic.*;

import java.util.Date;

import es.upm.ontology.RegistrationRequest;
import es.upm.ontology.ReleaseCapsule;
import es.upm.ontology.XplorationOntology;

public class Company extends Agent {

	// Codec for the SL language used and instance of the ontology
	private Codec codec = new SLCodec();

	// Declare Ontology
	public XplorationOntology ontology = (XplorationOntology) XplorationOntology.getInstance();
	
	// Name of Spacecraft
	public final static String SPACECRAFT = "Spacecraft";
	//public final static String SPACECRAFT = "PROTOCOL_RELEASE_CAPSULE";
	
	//Name of Capsule
	public final static String CAPSULE = "Capsule01";


	protected void setup() {
		System.out.println(getLocalName() + ": has entered");

		// Register of the codec and the ontology to be used in the ContentManager
		getContentManager().registerLanguage(codec);
		getContentManager().registerOntology(ontology);
		
		RequestRegistrationBehaviour reqRegister = new RequestRegistrationBehaviour(this);
		addBehaviour(reqRegister); 
		
		//Release CapsuleSimpleBehaviour
		ReleaseBehaviour releaseCapsule = new ReleaseBehaviour();
		addBehaviour(releaseCapsule); 
		
	}
	
	class ReleaseBehaviour extends SimpleBehaviour { 
		private boolean endRelease = false;
		
		public void action() {
			
			MessageTemplate mt = MessageTemplate.and(
					MessageTemplate.and(MessageTemplate.MatchLanguage(codec.getName()),
							MessageTemplate.MatchOntology(ontology.getName())),
					MessageTemplate.and(MessageTemplate.MatchProtocol(ontology.PROTOCOL_RELEASE_CAPSULE),
							MessageTemplate.MatchPerformative(ACLMessage.INFORM)));
			
			
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
						Concept concRelease = agAction.getAction();
						
						// If the action is ReleaseCapsule...
						if (concRelease instanceof ReleaseCapsule) {
							ReleaseCapsule releaseObj = (ReleaseCapsule) concRelease;
							
							//createCapsule(releaseObj.getLocation().getX(), releaseObj.getLocation().getY());
							createCapsule(releaseObj);
						}
						
					}
				}
			} catch (Exception e) {					
				e.printStackTrace();
			}
			
			
					
		}
		
		public void createCapsule(ReleaseCapsule releaseObj){
			ContainerController cc = getContainerController();
			AgentController ac;
			try {
				//ac = cc.createNewAgent(CAPSULE, "es.upm.company01.Capsule", new Object[] {new String(CAPSULE), new Integer(x), new Integer(y)});
				ac = cc.createNewAgent(CAPSULE, "es.upm.company01.Capsule", new Object[] {new String(CAPSULE), releaseObj.getLocation()});
				ac.start();
				endRelease = true;
			} catch (StaleProxyException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
					
		public boolean done() {
			return endRelease;
		}
		
	}
	
	//Behaviour REQUEST REGISTRATION 
	class RequestRegistrationBehaviour extends SimpleBehaviour { 
			AID agSpacecraft;
			private boolean end = false;
			private String stateRegistration = "BEGIN";
			
			public RequestRegistrationBehaviour(Agent a) {
				 super(a);
			} 

			public void action() {
				switch (stateRegistration){
					case "BEGIN":						
						sendRegistrationRequest();
						myAgent.doWait(500);
						break;
					case "REQUEST":
						receiveMessageForRequest();
						//myAgent.doWait(500);
						break;
					case "END":
						this.end = true;
						break;
				}
				
			}

			public boolean done() {
				return this.end;
			}
			
			public void sendRegistrationRequest(){
				//Creates the description for the type of agent to be searched "Spacecraft"
				DFAgentDescription dfd = new DFAgentDescription();
				ServiceDescription sd = new ServiceDescription();
				
				sd.setType(SPACECRAFT);
				dfd.addServices(sd);
				
				this.stateRegistration = "REQUEST";
				
				myAgent.doWait(1000);
				try {

					// It finds agents of the required type
					DFAgentDescription[] res = new DFAgentDescription[20];
					res = DFService.search(myAgent, dfd);

					// Gets the first occurrence, if there was success
					if (res.length > 0) {
						
						System.out.println(getLocalName() + ": stablished communication with Spacecraft");
						
						for(DFAgentDescription foundAgent : res) {
							
							agSpacecraft = (AID)foundAgent.getName();
							
							// Asks request to the Spacecraft
							ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
							msg.setSender(getAID());
							msg.setProtocol(ontology.PROTOCOL_REGISTRATION);
							msg.setOntology(ontology.getName()); 
							msg.setLanguage(codec.getName());
							msg.addReceiver(agSpacecraft);
							
							//Register the Company01
							RegistrationRequest objRegCompany = new RegistrationRequest();
							objRegCompany.setCompany("Company01");
							
							//Package the message
							getContentManager().fillContent(msg, new Action(getAID(), objRegCompany));
							
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
			
			public void receiveMessageForRequest(){
				// Waiting the answer
				MessageTemplate mt = MessageTemplate.and(
						MessageTemplate.and(MessageTemplate.MatchLanguage(codec.getName()),
								MessageTemplate.MatchOntology(ontology.getName())),
						MessageTemplate.MatchProtocol(ontology.PROTOCOL_REGISTRATION));
				
				ACLMessage msgReceive = myAgent.receive(mt);
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
							System.out.println(getLocalName() + ": is waiting for Registering - Spacecraft answers - AGREE");
							break;
						case ACLMessage.FAILURE:
							System.out.println(getLocalName() + ": is already Registered - Spacecraft answers - FAILURE");
							break;
						case ACLMessage.INFORM:
							System.out.println(getLocalName() + ": was Registered - Spacecraft answers - INFORM");
							//this.stateRegistration = "END";
							//end = true;
							break;
						default:
							//NOT_UNDERSTOOD
							System.out.println(getLocalName() + ": Spacecraft answers - NOT UNDERSTOOD");
							this.stateRegistration = "END";
							//end = true;
							break;
			        }
				} else {
					this.end = true;
				}
			}
	}

}
