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

import java.util.Calendar;
import java.util.Date;

public class Spacecraft extends Agent {
	
	private static final long serialVersionUID =1L;
	public final static String REGISTRATION = "Registration";
	public final static String SPACECRAFT = "Spacecraft";
	public final static String REQUEST = "REQUEST";
	public final static String COMPANY_X = "Company6";
	public final static String AGREE = "AGREE";
	public final static String NOT_UNDERSTOOD = "NOT_UNDERSTOOD";
	public final static String REFUSE = "REFUSE";
	public final static String FAILURE = "FAILURE";
	public final static String REGISTER = "REGISTER";
	//AID[] registeredCompanies = new AID[20];
	
	protected void setup()
	{
		final Calendar registerTime_Begin = Calendar.getInstance();
		final Calendar registerTime_End = Calendar.getInstance();
		final Calendar Currentime = Calendar.getInstance();
		
		registerTime_Begin.set(2016, 3, 19);
		registerTime_End.set(2016, 3, 27);
		Currentime.getTime();
		
		System.out.println(getLocalName()+": has entered");
		System.out.println(getLocalName()+": Period of Registration: "+ registerTime_Begin.getTime() + " - " + registerTime_End.getTime());
		System.out.println(getLocalName()+": Current: "+ Currentime.getTime());
		
		try
		{
			// Creates its own description
			DFAgentDescription dfd = new DFAgentDescription();
			ServiceDescription sd = new ServiceDescription();
			sd.setName(this.getName());
			sd.setType(SPACECRAFT);
			dfd.addServices(sd);
			
			// Registers its description in the DF
			DFService.register(this, dfd);
			System.out.println(getLocalName()+": registered in the DF");
			dfd = null;
			sd = null;
			doWait(10000);
		} 
		catch (FIPAException e)
		{
			e.printStackTrace();
		}
		
		addBehaviour(new SimpleBehaviour(this)
		{
			boolean end = false;
			private static final long serialVersionUID =1L;
			
			public void action()
			{
				// Waits for answer to the request
				ACLMessage msg = receive();
				
				try {
				
					if (msg != null)
					{
						//Between the Period of Registration (Boundary included)
						if ( inThePeriod(registerTime_Begin, registerTime_End, Currentime) )
						{
							ACLMessage reply = msg.createReply();
							reply.setContent(REFUSE);
							myAgent.send(reply);
							System.out.println(myAgent.getLocalName()+": answer sent -> "+reply.getContent());
							end = true;
						} else { 
						
							if ((msg.getContent()).contains(COMPANY_X))
							{
								// If an Request arrives, it answers with the ....
								System.out.println(myAgent.getLocalName()+": received a request from "+(msg.getSender()).getLocalName()); 
								ACLMessage reply = msg.createReply();		
								reply.setContent(AGREE);
								myAgent.send(reply);
								System.out.println(myAgent.getLocalName()+": answer sent -> "+reply.getContent());
								
								doWait(5000);
								
								// Send the answer
			                    System.out.println(getLocalName() +": Send answer");
			                    ACLMessage respond = new ACLMessage( ACLMessage.INFORM );
			                    respond.setContent( REGISTER );
			                    respond.addReceiver( msg.getSender() );
			                    send(respond);
			                    System.out.println(getLocalName() +": Sent Register to Company");
			                    System.out.println(respond.toString());
			                    end = true;
								
							} else {
								ACLMessage reply = msg.createReply();
								reply.setContent(NOT_UNDERSTOOD);
								myAgent.send(reply);
								
								System.out.println(myAgent.getLocalName()+": answer sent -> "+reply.getContent());
								end = true;
							}
						}
					} 
				} catch (Exception e) 
				{
					ACLMessage reply = msg.createReply();
					reply.setContent(FAILURE);
					myAgent.send(reply);
					
					System.out.println(myAgent.getLocalName()+": answer sent -> "+reply.getContent());
					end = true;
					
					e.printStackTrace();
					
				}
			}
			public boolean done()
            {
                return end;
            }
		});
	}
	
	public boolean inThePeriod ( Calendar registerTime_Begin, Calendar registerTime_End, Calendar Currentime ) {
		
		if (Currentime.getTime().equals(registerTime_Begin.getTime()))
			return false;
		if (Currentime.getTime().equals(registerTime_End.getTime()))
			return false;
		if (Currentime.getTime().before(registerTime_Begin.getTime()) || Currentime.getTime().after(registerTime_End.getTime()))
		{
			return true;
		} else return false;
		
	}

}