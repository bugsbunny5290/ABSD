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

import org.joda.time.DateTime;

import es.upm.ontology.Location;
import es.upm.ontology.XplorationOntology;

public class Broker extends Agent{
	
	// Codec for the SL language used and instance of the ontology
		private Codec codec = new SLCodec();

		// Declare Ontology
		public XplorationOntology ontology = (XplorationOntology) XplorationOntology.getInstance();

		private static final long serialVersionUID = 1L;
		public final static String BROKER = "Broker";

		protected void setup() {
			
		}
		
		

}
