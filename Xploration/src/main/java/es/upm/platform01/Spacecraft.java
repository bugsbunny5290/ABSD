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
import java.util.Calendar;
import java.util.Date;

import es.upm.ontology.RegistrationRequest;
import es.upm.ontology.XplorationOntology;

public class Spacecraft extends Agent {

	// Codec for the SL language used and instance of the ontology
	private Codec codec = new SLCodec();

	// Declare Ontology
	public XplorationOntology ontology = (XplorationOntology) XplorationOntology.getInstance();

	private static final long serialVersionUID = 1L;
	public final static String SPACECRAFT = "Spacecraft";
	
	ArrayList<String> companiesRegister;
	final Calendar registerTime_Begin = Calendar.getInstance();
	final Calendar registerTime_End = Calendar.getInstance();
	final Calendar Currentime = Calendar.getInstance();

	// AID[] registeredCompanies = new AID[20];

	protected void setup() {
		
		companiesRegister = new ArrayList();

		// Register of the codec and the ontology to be used in the
		getContentManager().registerLanguage(codec);
		getContentManager().registerOntology(ontology);

		registerTime_Begin.set(2016, 3, 19);
		registerTime_End.set(2016, 12, 27);
		Currentime.getTime();

		System.out.println(getLocalName() + ": has entered");
		System.out.println(getLocalName() + ": Period of Registration: " + registerTime_Begin.getTime() + " - "
				+ registerTime_End.getTime());
		System.out.println(getLocalName() + ": Current: " + Currentime.getTime());

		try {
			// Creates its own description
			DFAgentDescription dfd = new DFAgentDescription();
			ServiceDescription sd = new ServiceDescription();
			sd.setName(this.getName());
			sd.setType(SPACECRAFT);
			dfd.addServices(sd);

			// Registers its description in the DF
			DFService.register(this, dfd);
			System.out.println(getLocalName() + ": registered in the DF");
			dfd = null;
			sd = null;
			doWait(10000);
		} catch (FIPAException e) {
			e.printStackTrace();
		}

		addBehaviour(new CyclicBehaviour(this) {
			private static final long serialVersionUID = 1L;

			public void action() {
				MessageTemplate mt = MessageTemplate.and(
						MessageTemplate.and(MessageTemplate.MatchLanguage(codec.getName()),
								MessageTemplate.MatchOntology(ontology.getName())),
						MessageTemplate.and(MessageTemplate.MatchProtocol(ontology.PROTOCOL_REGISTRATION),
								MessageTemplate.MatchPerformative(ACLMessage.REQUEST)));
				
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
							Concept concRegistration = agAction.getAction();
							
							// If the action is RegistrationRequest...
							if (concRegistration instanceof RegistrationRequest) {
								RegistrationRequest request = (RegistrationRequest) concRegistration;
								
								ACLMessage reply = msgRequest.createReply();
								reply.setSender(getAID());
								reply.setProtocol(ontology.PROTOCOL_REGISTRATION);
								reply.setOntology(ontology.getName()); 
								reply.setLanguage(codec.getName());
								reply.addReceiver(msgRequest.getSender());
								
								// Between the Period of Registration (Boundary included)
								if (inThePeriod(registerTime_Begin, registerTime_End, Currentime)) {
									
										//Get Company
										String companyName = request.getCompany();
										
										// If an Request arrives
										System.out.println(myAgent.getLocalName() + ": received a request from "+ (msgRequest.getSender()).getLocalName());
										
										reply.setPerformative(ACLMessage.AGREE);
										
										//Send the message
										send(reply);
										
										doWait(5000);
										
										//Company exists
										if (!companiesRegister.contains(companyName)){
											
											System.out.println(myAgent.getLocalName() + ": register "+ (msgRequest.getSender()).getLocalName() );
											
											companiesRegister.add(companyName);											
											reply.setPerformative(ACLMessage.INFORM);
											
											//Send the message
											send(reply);
											
											doWait(5000);
										}else{
											
											System.out.println(myAgent.getLocalName() + ": respond "+ (msgRequest.getSender()).getLocalName() + " is already registered"  );
											
											reply.setPerformative(ACLMessage.FAILURE);
											
											//Send the message
											send(reply);
											
											doWait(5000);
										}
										
								} else {
									reply.setPerformative(ACLMessage.REFUSE);
									
									//Send the message
									send(reply);
									
									doWait(5000);

								}
							}

						} else {
							ACLMessage reply = msgRequest.createReply();
							reply.setSender(getAID());
							reply.setProtocol(ontology.PROTOCOL_REGISTRATION);
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
		});
	}

	public boolean inThePeriod(Calendar registerTime_Begin, Calendar registerTime_End, Calendar Currentime) {

		if (Currentime.getTime().equals(registerTime_Begin.getTime()))
			return true;
		if (Currentime.getTime().equals(registerTime_End.getTime()))
			return true;
		if (Currentime.getTime().before(registerTime_Begin.getTime())
				|| Currentime.getTime().after(registerTime_End.getTime())) {
			return false;
		} else
			return true;

	}

}
