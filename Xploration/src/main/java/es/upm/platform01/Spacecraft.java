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

import org.joda.time.DateTime;

import es.upm.ontology.Location;
import es.upm.ontology.RegistrationRequest;
import es.upm.ontology.ReleaseCapsule;
import es.upm.ontology.XplorationOntology;

public class Spacecraft extends Agent {

	// Codec for the SL language used and instance of the ontology
	private Codec codec = new SLCodec();

	// Declare Ontology
	public XplorationOntology ontology = (XplorationOntology) XplorationOntology.getInstance();

	private static final long serialVersionUID = 1L;
	public final static String SPACECRAFT = "Spacecraft";
	
	ArrayList<AID> companiesRegister = new ArrayList();
	//ArrayList<String> companiesRegister = new ArrayList();
	DateTime registerTime_Begin; 
	DateTime registerTime_End;
	DateTime Currentime;
/*
	public ArrayList<String> getCompaniesRegister(){
		return this.companiesRegister;
	}
	
	public void setCompanyRegister (String agent){
		this.companiesRegister.add(agent);
	}
*/
	protected void setup() {
		
		//this.companiesRegister = new ArrayList();

		// Register of the codec and the ontology to be used in the
		getContentManager().registerLanguage(codec);
		getContentManager().registerOntology(ontology);

		/*registerTime_Begin.set(2016, 3, 19);
		registerTime_End.set(2016, 12, 27);
		Currentime.getTime();*/
		
		registerTime_Begin = DateTime.now();
		registerTime_End = DateTime.now().plusSeconds(50);

		System.out.println(getLocalName() + ": has entered");
		System.out.println(getLocalName() + ": Period of Registration: " + registerTime_Begin + " - "
				+ registerTime_End);
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
			doWait(1000);
			
			RegistrationBehaviour registerCompany = new RegistrationBehaviour(this);
			addBehaviour(registerCompany); 
			
			//doWait(5000);
			
			ReleaseCapsuleBehavoiur releaseCapsuleCompany = new ReleaseCapsuleBehavoiur(this);
			addBehaviour(releaseCapsuleCompany);
			
		} catch (FIPAException e) {
			e.printStackTrace();
		}
	}
	
	class ReleaseCapsuleBehavoiur extends SimpleBehaviour{
		private boolean endRelease = false;
		ArrayList<String> companies;
		
		public ReleaseCapsuleBehavoiur(Agent a) {
			 super(a);
		}
		
		public void action() {
			
			if ( DateTime.now().isAfter(registerTime_End)) {

				//endRelease = true;

				//for(String companyReg : companiesRegister)
				for(AID companyReg : companiesRegister)
				{
					System.out.println("Company ----->" + companyReg);
					
					// Asks request to the Spacecraft
					ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
					msg.setSender(getAID());
					msg.setProtocol(ontology.PROTOCOL_RELEASE_CAPSULE);
					msg.setOntology(ontology.getName()); 
					msg.setLanguage(codec.getName());
					msg.addReceiver(companyReg);
					
					//Location for Companies
					ReleaseCapsule objReleaseCapsule = new ReleaseCapsule();
					Location objLocation = new Location();
					
					objLocation.setX(1);
					objLocation.setY(2);
					
					objReleaseCapsule.setLocation(objLocation);

		
					//Package the message
					try {
						getContentManager().fillContent(msg, new Action(getAID(), objReleaseCapsule));
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
				
				endRelease = true;
			}
			
		}
		
		public boolean done() {
			return endRelease;
		}
	}

	class RegistrationBehaviour extends CyclicBehaviour {
		private static final long serialVersionUID = 1L;
		
		public RegistrationBehaviour(Agent a) {
			 super(a);
		} 

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
						//AID agentSender = agAction.getActor();
						
						// If the action is RegistrationRequest...
						if (concRegistration instanceof RegistrationRequest) {
							RegistrationRequest request = (RegistrationRequest) concRegistration;
							
							ACLMessage reply = msgRequest.createReply();
							reply.setSender(getAID());
							reply.setProtocol(ontology.PROTOCOL_REGISTRATION);
							reply.setOntology(ontology.getName()); 
							reply.setLanguage(codec.getName());
							reply.addReceiver(msgRequest.getSender());
							
							Currentime = DateTime.now();
							
							// Between the Period of Registration (Boundary included)
							if (inThePeriod(registerTime_Begin, registerTime_End, Currentime)) {
								
									//Get Company
									String companyName = request.getCompany();
									
									// If an Request arrives
									System.out.println(myAgent.getLocalName() + ": received a request from "+ companyName);
									
									reply.setPerformative(ACLMessage.AGREE);
									
									//Send the message
									send(reply);
									
									//Company exists
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
	}

	public boolean inThePeriod(DateTime registerTime_Begin, DateTime registerTime_End, DateTime Currentime) {

		if (Currentime.equals(registerTime_Begin))
			return true;
		if (Currentime.equals(registerTime_End))
			return true;
		if (Currentime.isBefore((registerTime_Begin))
				|| Currentime.isAfter(registerTime_End)) {
			return false;
		} else
			return true;

	}

}
