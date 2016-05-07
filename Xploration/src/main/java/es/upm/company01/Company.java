package es.upm.company01;

import jade.core.Agent;
import jade.core.AID;
import jade.domain.FIPAAgentManagement.*;
import jade.domain.DFService;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.content.lang.Codec;
import jade.content.lang.Codec.*;
import jade.content.lang.sl.*;

import jade.content.*;
import jade.content.onto.*;
import jade.content.onto.basic.*;

import java.util.Date;

import es.upm.ontology.RegistrationRequest;
import es.upm.ontology.XplorationOntology;

public class Company extends Agent {

	// Codec for the SL language used and instance of the ontology
	private Codec codec = new SLCodec();

	// Declare Ontology
	public XplorationOntology ontology = (XplorationOntology) XplorationOntology.getInstance();
	
	// Name of Spacecraft
	public final static String SPACECRAFT = "Spacecraft";


	protected void setup() {
		System.out.println(getLocalName() + ": has entered");

		// Register of the codec and the ontology to be used in the ContentManager
		getContentManager().registerLanguage(codec);
		getContentManager().registerOntology(ontology);
		
		
		//Behaviour REQUEST REGISTRATION 
		addBehaviour(new SimpleBehaviour(this) {
			AID agSpacecraft;
			boolean end = false;

			public void action() {
				
				//Creates the description for the type of agent to be searched "Spacecraft"
				DFAgentDescription dfd = new DFAgentDescription();
				ServiceDescription sd = new ServiceDescription();
				sd.setType(SPACECRAFT);
				dfd.addServices(sd);
				
				doWait(3000);
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
							
							//System.out.println(getLocalName() + ": asks for Request to Spacecraft");
							
						}
						
						doWait(2000);
						// Waiting the answer
						MessageTemplate mt = MessageTemplate.and(
								MessageTemplate.and(MessageTemplate.MatchLanguage(codec.getName()),
										MessageTemplate.MatchOntology(ontology.getName())),
								MessageTemplate.MatchProtocol(ontology.PROTOCOL_REGISTRATION));
						
						ACLMessage msgReceive = myAgent.receive(mt);
						if (msgReceive != null) {
							
							// Process the message
							int performative = msgReceive.getPerformative();
							
							switch (performative)
					        {
								case ACLMessage.REFUSE:
									System.out.println(getLocalName() + ": is late - Spacecraft answers - REFUSE");
									break;
								case ACLMessage.AGREE:
									System.out.println(getLocalName() + ": is waiting for Registering - Spacecraft answers - AGREE");
									doWait(5000);
									break;
								case ACLMessage.FAILURE:
									System.out.println(getLocalName() + ": is already Registered - Spacecraft answers - FAILURE");
									end = true;
									break;
								case ACLMessage.INFORM:
									System.out.println(getLocalName() + ": was Registered - Spacecraft answers - INFORM");
									end = true;
									break;
								default:
									//NOT_UNDERSTOOD
									System.out.println(getLocalName() + ": Spacecraft answers - NOT UNDERSTOOD");
									end = true;
									break;
					        }
						} else {
							end = true;
						}
						doWait(5000);
					} else {
						System.out.println(getLocalName() + ": Didn't found a Spacecraft.");
					}
						
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			public boolean done() {
				return end;
			}
		});

	}

}
