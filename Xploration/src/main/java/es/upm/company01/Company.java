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

public class Company extends Agent {
	
	public final static String REGISTRATION = "Registration";
	public final static String SPACECRAFT = "SPACECRAFT";
	public final static String COMPANY_X = "CompanyX";
	
	protected void setup()
	{
		System.out.println(getLocalName()+": has entered");
		
		addBehaviour(new SimpleBehaviour(this)
		{
			AID ag;
			boolean end = false;
			
			public void action()
			{
				// Creates the description for the type of agent to be searched
				DFAgentDescription dfd = new DFAgentDescription();
				ServiceDescription sd = new ServiceDescription();
				sd.setType(SPACECRAFT);
				dfd.addServices(sd);
				
				try {
				
					// It finds agents of the required type
					DFAgentDescription[] res = new DFAgentDescription[20];
					res = DFService.search(myAgent, dfd);
					
					// Gets the first occurrence, if there was success
					if (res.length > 0)
					{
						System.out.println(getLocalName()+": found Spacecraft");
						
						ag = (AID)res[0].getName();
						
						// Asks request to the spacecraft
						ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
						msg.setSender(getAID());
						msg.addReceiver(ag);
						msg.setProtocol(REGISTRATION);
						
						msg.setContent(COMPANY_X);
						
						send(msg);
						System.out.println(getLocalName()+": asks for Request");
						
						//Waiting the answer
			            ACLMessage msg2 = blockingReceive();
			            if (msg2!= null)
			            {
			                System.out.println(getLocalName() + ": Received the following message: ");
			                System.out.println(msg2.toString());
			                end = true;
			            }
						
						//end = true;
						
						doWait(5000);
					}
				}catch (Exception e) 
				{
					e.printStackTrace();
				}
			}
			
			public boolean done ()
			{
				return end;
			}
		});
		
	}
	
}
