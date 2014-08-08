/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package xmlreader;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 *
 * @author kevin
 */
public class XMLReader {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException, SAXException {
       
        
        
         String xmlString = "<sequence  xmlns=\"http://ws.apache.org/ns/synapse\" name=\"main\">\n"
                         + "    <in>\n"                         
                         + "        <log></log>\n"
                         + "        <property></property>\n"                          
                         + "        <send></send>\n"                 
                         + "    </in>\n"
                         + "    \n"
                         + "    <out>\n"                         
                         + "        <log></log>\n"
                         + "    </out>\n"
                         + "</sequence>";

        InputStream is = new ByteArrayInputStream(xmlString.getBytes());
        Document doc = PositionalXMLReader.readXML(is);
        System.out.println(doc.getUserData("mediationComponent"));
        Map<String, Element>  lineToNodeMapArg=PositionalXMLReader.getLineToNodeMap();
        System.out.println(lineToNodeMapArg.size());
        System.out.println(lineToNodeMapArg.containsKey("9"));      
        is.close();        
        Node node = doc.getElementsByTagName("out").item(0);       
        System.out.println(node.hasChildNodes());
        System.out.println("Line number: " + node.getUserData("lineNumber"));
        System.out.println("Mediator Position: " + node.getUserData("mediatorPosition"));      
        
    }
    
}
