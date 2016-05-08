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

import es.upm.company01.Company.RequestRegistrationBehaviour;
import es.upm.ontology.RegistrationRequest;
import es.upm.ontology.XplorationOntology;

public class Capsule extends Agent {
	// Codec for the SL language used and instance of the ontology
	private Codec codec = new SLCodec();
	// Declare Ontology
	public XplorationOntology ontology = (XplorationOntology) XplorationOntology.getInstance();
	//Name of Rover
	public final static String ROVER = "Rover01";

	protected void setup() {
		try{
			System.out.println(getLocalName() + ": has entered");
	
			// Register of the codec and the ontology to be used in the ContentManager
			getContentManager().registerLanguage(codec);
			getContentManager().registerOntology(ontology);
			
			Object[] capsuleInfo = getArguments() ;
			
			ContainerController cc = getContainerController();
			AgentController ac;
			try {
				ac = cc.createNewAgent(ROVER, "es.upm.company01.Rover", new Object[] {new String(ROVER), capsuleInfo[1]});
				ac.start();
			} catch (StaleProxyException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
}
