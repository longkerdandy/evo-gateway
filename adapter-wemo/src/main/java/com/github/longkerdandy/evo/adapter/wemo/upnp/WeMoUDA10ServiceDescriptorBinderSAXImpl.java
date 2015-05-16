package com.github.longkerdandy.evo.adapter.wemo.upnp;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.fourthline.cling.binding.xml.DescriptorBindingException;
import org.fourthline.cling.binding.xml.UDA10ServiceDescriptorBinderSAXImpl;
import org.fourthline.cling.model.ValidationException;
import org.fourthline.cling.model.meta.RemoteService;
import org.fourthline.cling.model.meta.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashSet;

/**
 * WeMo Fix
 */
public class WeMoUDA10ServiceDescriptorBinderSAXImpl extends UDA10ServiceDescriptorBinderSAXImpl {

    @Override
    public <S extends Service> S describe(S undescribedService, String descriptorXml) throws DescriptorBindingException, ValidationException {
        if (descriptorXml == null || descriptorXml.length() == 0) {
            throw new DescriptorBindingException("Null or empty descriptor");
        }

        try {
            RemoteService remoteService = (RemoteService) undescribedService;
            if (remoteService.getDescriptorURI().toString().equals("/deviceinfoservice.xml")) {
                System.out.print("miao");
            }
            // Fix WeMo descriptor xml error
            descriptorXml = fixDescriptorXml(descriptorXml);

            return super.describe(undescribedService, descriptorXml);
        } catch (ParserConfigurationException | IOException | SAXException | TransformerException e) {
            throw new DescriptorBindingException("Error when trying to fix WeMo descriptor xml:" + ExceptionUtils.getMessage(e));
        }
    }

    protected String fixDescriptorXml(String descriptorXml) throws ParserConfigurationException, IOException, SAXException, TransformerException {
        // remove empty argument
        descriptorXml = descriptorXml.replaceAll("<argument>\\s+</argument>", "");
        // remove <retval/>
        descriptorXml = descriptorXml.replaceAll("<retval\\s*/>", "");

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = factory.newDocumentBuilder();
        Document document = documentBuilder.parse(new InputSource(new StringReader(descriptorXml.trim())));
        Element root = document.getDocumentElement();

        Node serviceStateTable = root.getElementsByTagName("serviceStateTable").item(0);

        HashSet<String> varSet = new HashSet<>();
        NodeList stateVariables = document.getElementsByTagName("stateVariable");
        for (int i = 0; i < stateVariables.getLength(); i++) {
            Element stateVariable = (Element) stateVariables.item(i);
            varSet.add(stateVariable.getFirstChild().getNodeValue());
        }

        NodeList actions = document.getElementsByTagName("action");
        for (int i = 0; i < actions.getLength(); i++) {
            Element action = (Element) actions.item(i);
            NodeList args = action.getElementsByTagName("argument");
            for (int j = 0; j < args.getLength(); j++) {
                Element arg = (Element) args.item(j);
                // add non-present stateVariable
                Node relatedStateVariable = arg.getElementsByTagName("relatedStateVariable").item(0);
                if (relatedStateVariable != null && !varSet.contains(relatedStateVariable.getNodeValue())) {
                    Element stateVariable = document.createElement("stateVariable");
                    Element name = document.createElement("name");
                    name.appendChild(document.createTextNode(relatedStateVariable.getNodeValue()));
                    stateVariable.appendChild(name);
                    Element dataType = document.createElement("dataType");
                    dataType.appendChild(document.createTextNode("string"));
                    stateVariable.appendChild(dataType);
                    Element defaultValue = document.createElement("defaultValue");
                    defaultValue.appendChild(document.createTextNode("0"));
                    stateVariable.appendChild(defaultValue);
                    serviceStateTable.appendChild(stateVariable);
                }
            }
        }

        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(document), new StreamResult(writer));
        return writer.getBuffer().toString().replaceAll("\n|\r", "");
    }
}
