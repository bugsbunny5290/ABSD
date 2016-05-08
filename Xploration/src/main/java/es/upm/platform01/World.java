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
	
	ArrayList<AID> companiesRegister = new ArrayList();
	
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
							MessageTemplate.MatchPerformative(ACLMessage.REQUEST)));
			
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
									} else
									{
										//Rover crash with another rover
										reply.setPerformative(ACLMessage.REFUSE);
										
										//Send the message
										send(reply);
									}
									
									
									
									/*
									
									//If Rover sends CANCEL
									if (!companiesRegister.contains(msgRequest.getSender())){
										
										System.out.println(myAgent.getLocalName() + ": register "+ companyName );
										
										//companiesRegister.add( companyName );
										companiesRegister.add( msgRequest.getSender() );
										//new Spacecraft().setCompanyRegister(companyName);
										
										reply.setPerformative(ACLMessage.INFORM);
										
										//Send the message
										send(reply);
										
										//doWait(5000);
									}else{
										
										System.out.println(myAgent.getLocalName() + ": respond "+ (msgRequest.getSender()).getLocalName() + " is already registered"  );
										
										reply.setPerformative(ACLMessage.FAILURE);
										
										//Send the message
										send(reply);
										
										doWait(5000);
									}*/
									
							} else {
								//Rover crash with another rover
								reply.setPerformative(ACLMessage.FAILURE);
								
								//Send the message
								send(reply);
								
								doWait(1000);

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
}
