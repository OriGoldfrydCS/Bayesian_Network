import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.*;
import java.util.*;

/**
 * The `XMLParser` class is responsible for parsing an XML file representing a Bayesian network
 * and creating a `BayesianNetwork` object from the parsed data.
 */
public class XMLParser {

    /**
     * Parses the given XML file and creates a `BayesianNetwork` object.
     *
     * @param xmlFile the path to the XML file containing the Bayesian network data
     * @return a `BayesianNetwork` object representing the parsed network
     * @throws Exception if any errors occur during the parsing process
     */
    public static BayesianNetwork parse(String xmlFile) throws Exception {
        BayesianNetwork network = new BayesianNetwork();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new File(xmlFile));
        doc.getDocumentElement().normalize();

        // Parse the VARIABLE elements and add them to the network
        NodeList nodeList = doc.getElementsByTagName("VARIABLE");
        for (int i = 0; i < nodeList.getLength(); i++) {
            Element element = (Element) nodeList.item(i);
            String name = null;
            NodeList nameList = element.getElementsByTagName("NAME");
            if (nameList.getLength() > 0) {
                name = nameList.item(0).getTextContent();
            }
            ArrayList<String> outcomes = new ArrayList<>();
            NodeList outcomeList = element.getElementsByTagName("OUTCOME");
            for (int j = 0; j < outcomeList.getLength(); j++) {
                outcomes.add(outcomeList.item(j).getTextContent());
            }
            Node node = new Node(name);
            node.addPossibleStates(outcomes);
            network.addNode(node);
        }

        // Parse the DEFINITION elements and create or retrieve the corresponding nodes
        NodeList definitionList = doc.getElementsByTagName("DEFINITION");
        int factorIndex = 1;
        for (int i = 0; i < definitionList.getLength(); i++) {
            Element element = (Element) definitionList.item(i);
            String forNode = element.getElementsByTagName("FOR").item(0).getTextContent();
            Node node = network.getNodeByName(forNode);
            if (node == null) {
                node = new Node(forNode);
                network.addNode(node);
            }

            // Parse the GIVEN elements and set the parents of the node
            NodeList givenList = element.getElementsByTagName("GIVEN");
            List<String> parents = new ArrayList<>();
            for (int j = 0; j < givenList.getLength(); j++) {
                String parentName = givenList.item(j).getTextContent();
                parents.add(parentName);
            }
            network.setParents(node, parents);

            // Parse the CPT table and set the CPT for the node
            String table = element.getElementsByTagName("TABLE").item(0).getTextContent();
            String[] probabilities = table.split(" ");
            node.buildCPT(probabilities);

            // Create and assign a Factor to the node
            Factor factor = node.createFactor();
            node.setFactor(factor);
        }

        // Print the initial factors
        System.out.println("\nInitial Factors:");
        for (Node node : network.getNodes()) {
            Factor factor = node.getFactor();
            if (factor != null) {
                System.out.println(factor);
            }
        }

        System.out.println("Nodes:");
        for (Node node : network.getNodes()) { // Modify this line
            System.out.println(node);
        }

        network.printNetwork();

        return network;
    }

    public static void main(String[] args) throws Exception {
        BayesianNetwork network = XMLParser.parse("big_net.xml");

        // Accessing a specific node's factor and applying a filter
        Node nodeB1 = network.getNodeByName("B1");
        Node nodeC2 = network.getNodeByName("C2");
        Node nodeD1 = network.getNodeByName("D1");

        if (nodeB1 == null || nodeC2 == null || nodeD1 == null) {
            System.out.println("One or both nodes not found.");
            return;
        }

        Factor factorB1 = nodeB1.getFactor();
        Factor factorC2 = nodeC2.getFactor();
        Factor factorD1 = nodeD1.getFactor();

        if (factorB1 == null || factorC2 == null) {
            System.out.println("One or both factors not found.");
            return;
        }
        System.out.println((factorD1.getFactorLabel()) + " " + (factorB1.getFactorLabel()) + " " + (factorC2.getFactorLabel()));
        // Print original factors
        System.out.println("Original Factor J:");
        System.out.println(factorB1);
        System.out.println("Original Factor M:");
        System.out.println(nodeC2);
        System.out.println("Original Factor A:");
        System.out.println(factorD1);

        // Filtering based on evidence
        Map<String, String> evidencesJ = new HashMap<>();
        evidencesJ.put("J", "T");
        factorB1.filterRows(evidencesJ);

        Map<String, String> evidencesM = new HashMap<>();
        evidencesM.put("M", "T");
        factorC2.filterRows(evidencesM);

        // Filtering based on combined evidence for A (E=T and B=T)
        Map<String, String> evidencesA = new HashMap<>();
        evidencesA.put("E", "T");
        evidencesA.put("B", "T");
        factorD1.filterRows(evidencesA);

        // Print filtered factors
        System.out.println("Filtered Factor J (J=T):");
        System.out.println(factorB1);
        System.out.println("Filtered Factor M (M=T):");
        System.out.println(factorC2);
        System.out.println("Filtered Factor A (E=T, B=T):");
        System.out.println(factorD1);

        // Join the filtered factors
//        Factor joinedFactor = Factor.joinFactors(factorB1, factorC2);
        System.out.println("Joined Factor after filtering:");
//        System.out.println(joinedFactor);
    }
}