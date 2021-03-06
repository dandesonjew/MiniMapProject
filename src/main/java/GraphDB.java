import org.xml.sax.SAXException;

import java.io.File;
import java.util.ArrayList;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * Wraps the parsing functionality of the MapDBHandler as an example.
 * You may choose to add to the functionality of this class if you wish.
 * @author Alan Yao
 */
public class GraphDB {
    /**
     * Example constructor shows how to create and start an XML parser.
     * @param db_path Path to the XML file to be parsed.
     */
    public GraphDB(String db_path) {
        try {
            // InputStream in = getClass().getClassLoader().getResourceAsStream(dbpath);
            // sax.parse(in, maphandler);
            File inputFile = new File(db_path);
            hashMap = new HashMap<Long, GraphNode>();
            hashSet = new HashSet<GraphNode>();
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            MapDBHandler maphandler = new MapDBHandler(this);
            saxParser.parse(inputFile, maphandler);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
        clean();
    }

    private GraphNode closestNode(double startLat, double startLon) {
        GraphNode closest = new GraphNode("100000000", "0", "0");
        double minEuclidDist = 100;
        for(GraphNode gnTemp: hashSet){
            double latDist = Math.abs(startLat - gnTemp.lat);
            double lonDist = Math.abs(startLon - gnTemp.lon);
            double tempEuclidDist = Math.sqrt(latDist * latDist + lonDist * lonDist);
            if(minEuclidDist > tempEuclidDist) {
                closest = gnTemp;
                minEuclidDist = tempEuclidDist;
            }

        }
        return closest;
    }

    public LinkedList<Long> shortestPath(double startLat, double startLon, double endLat, double endLon) {
        GraphNode start = closestNode(startLat, startLon);
        GraphNode end = closestNode(endLat, endLon);
        Connector connector = new Connector(start, end, hashMap);


        LinkedList<Long> shortestPath = connector.aStar();
        return shortestPath;
    }

    public HashMap<Long, GraphNode> hashMap;
    public HashSet<GraphNode> hashSet;

    public void connectWay(ArrayList<Long> nodes){
        if(nodes.size() >= 2) {
            GraphNode prev = hashMap.get(nodes.get(0));
            for (int i = 1; i < nodes.size(); i++) {
                GraphNode curr = hashMap.get(nodes.get(i));
                curr.connect(prev);
                prev = curr;
            }
        }
    }

    /**
     * Helper to process strings into their "cleaned" form, ignoring punctuation and capitalization.
     * @param s Input string.
     * @return Cleaned string.
     */
    static String cleanString(String s) {
        return s.replaceAll("[^a-zA-Z ]", "").toLowerCase();
    }

    /**
     *  Remove nodes with no connections from the graph.
     *  While this does not guarantee that any two nodes in the remaining graph are connected,
     *  we can reasonably assume this since typically roads are connected.
     */
    private void clean() {
        ArrayList<GraphNode> disconnectedNodes = new ArrayList<GraphNode>();
        for(GraphNode gn : hashSet) {
            if(gn.edges.isEmpty()){
                disconnectedNodes.add(gn);
            }
        }
        for(GraphNode gn1: disconnectedNodes) {
            if(!(hashMap.remove(gn1.id,gn1) && hashSet.remove(gn1))) {
                System.out.println("unable to remove");
            }
        }
    }
}
