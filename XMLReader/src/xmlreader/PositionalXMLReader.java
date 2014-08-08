/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package xmlreader;


import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class PositionalXMLReader {
    final static String LINE_NUMBER_KEY_NAME = "lineNumber";
    final static String MEDIATOR_POSITION_KEY_NAME = "mediatorPosition";
    final static String MEDIATION_COMPONENT_KEY_NAME = "mediationComponent";
    static Map<String, Element>  lineToNodeMap;

    public static Document readXML(final InputStream is) throws IOException, SAXException {
        final Document doc;
        SAXParser parser;
        try {
            final SAXParserFactory factory = SAXParserFactory.newInstance();
            parser = factory.newSAXParser();
            final DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            final DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            doc = docBuilder.newDocument();
        } catch (final ParserConfigurationException e) {
            throw new RuntimeException("Can't create SAX parser / DOM builder.", e);
        }

        final Stack<Element> elementStack = new Stack<Element>();
        final StringBuilder textBuffer = new StringBuilder();
        final DefaultHandler handler = new DefaultHandler() {
            Map<String, Element>  lineToNodeMap = new HashMap<String, Element>();
            
            private Locator locator;
            @Override
            public void setDocumentLocator(final Locator locator) {
                this.locator = locator; // Save the locator, so that it can be used later for line tracking when traversing nodes.
            }
         
            @Override
            public void startElement(final String uri, final String localName, final String qName, final Attributes attributes)
                    throws SAXException {
                addTextIfNeeded();
                PositionalXMLReader.setLineToNodeMap(lineToNodeMap);
                final Element el = doc.createElement(qName);
                for (int i = 0; i < attributes.getLength(); i++) {
                    el.setAttribute(attributes.getQName(i), attributes.getValue(i));
                }
                el.setUserData(LINE_NUMBER_KEY_NAME, String.valueOf(this.locator.getLineNumber()), null);
                elementStack.push(el);
            }

            @Override
            public void endElement(final String uri, final String localName, final String qName) {
                addTextIfNeeded();
                final Element closedEl = elementStack.pop();
                if (elementStack.isEmpty()) { // Is this the root element?
                    doc.appendChild(closedEl);
                    closedEl.setUserData(MEDIATOR_POSITION_KEY_NAME, doc.getChildNodes().getLength()-1, null);
                    lineToNodeMap.put((String)closedEl.getUserData(LINE_NUMBER_KEY_NAME), closedEl);
                    if(closedEl.getNodeName().equals(SynapseConstants.SYNAPSE_API)){
                         doc.setUserData(MEDIATION_COMPONENT_KEY_NAME, SynapseConstants.SYNAPSE_API, null);
                    }else if (closedEl.getNodeName().equals(SynapseConstants.SYNAPSE_PROXY)){
                         doc.setUserData(MEDIATION_COMPONENT_KEY_NAME, SynapseConstants.SYNAPSE_PROXY, null);
                    }else if (closedEl.getNodeName().equals(SynapseConstants.SYNAPSE_SEQUENCE)){
                         doc.setUserData(MEDIATION_COMPONENT_KEY_NAME, SynapseConstants.SYNAPSE_SEQUENCE, null);
                    }else if (closedEl.getNodeName().equals(SynapseConstants.SYNAPSE_TEMPLATE)){
                         doc.setUserData(MEDIATION_COMPONENT_KEY_NAME, SynapseConstants.SYNAPSE_TEMPLATE, null);                       
                    }                   
                 /*   System.out.println(doc.getNodeName()+"      "+doc.getChildNodes().getLength()+"         "+closedEl.getNodeName());
                    for(int i=0;i<doc.getChildNodes().getLength();i++){
                        System.out.println( doc.getChildNodes().item(i).getNodeName());
                        
                    } */
                } else {
                    final Element parentEl = elementStack.peek();                    
                    parentEl.appendChild(closedEl);
                    lineToNodeMap.put((String)closedEl.getUserData(LINE_NUMBER_KEY_NAME), closedEl);
                    closedEl.setUserData(MEDIATOR_POSITION_KEY_NAME, parentEl.getChildNodes().getLength()/2-1, null);
                 /* System.out.println(parentEl.getNodeName()+"       "+parentEl.getChildNodes().getLength()/2+"       "+closedEl.getNodeName());
                    for(int i=0;i<parentEl.getChildNodes().getLength();i++){
                        System.out.println( parentEl.getChildNodes().item(i).getNodeName());
                        
                    }  */
                }
            }

            @Override
            public void characters(final char ch[], final int start, final int length) throws SAXException {
                textBuffer.append(ch, start, length);
            }

            // Outputs text accumulated under the current node
            private void addTextIfNeeded() {
                if (textBuffer.length() > 0) {
                    final Element el = elementStack.peek();
                    final Node textNode = doc.createTextNode(textBuffer.toString());
                    el.appendChild(textNode);
                    textBuffer.delete(0, textBuffer.length());
                }
            }
        };
        parser.parse(is, handler);

        return doc;
    }
    public static void setLineToNodeMap(Map<String, Element>  lineToNodeMapArg){
        lineToNodeMap=lineToNodeMapArg;
    }
    public static Map<String, Element> getLineToNodeMap(){
        return lineToNodeMap;
    }
    
}
