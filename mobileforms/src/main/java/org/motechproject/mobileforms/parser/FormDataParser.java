package org.motechproject.mobileforms.parser;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FormDataParser {
    private final Logger log = LoggerFactory.getLogger(FormDataParser.class);

    public Map<String, String> parse(String xml) {
        try {
            Map<String, String> dataMap = new HashMap<String, String>();
            Document doc = new SAXBuilder().build(new ByteArrayInputStream(xml.getBytes()));
            List children = doc.getRootElement().getChildren();

            for (Object o : children) {
                Element child = (Element) o;
                dataMap.put(child.getName(), child.getText());
            }

            return dataMap;
        } catch (Exception e) {
            log.error("Error in parsing form xml", e);
        }

        return null;
    }

}


