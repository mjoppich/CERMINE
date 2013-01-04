package pl.edu.icm.cermine.content.transformers;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import pl.edu.icm.cermine.exception.TransformationException;
import pl.edu.icm.cermine.tools.transformers.ModelToFormatWriter;

/**
 * Writes DocumentContentStructure model to NLM format.
 *
 * @author Dominika Tkaczyk
 */
public class NLMElementToHTMLWriter implements ModelToFormatWriter<Element> {

    @Override
    public String write(Element object, Object... hints) throws TransformationException {
        StringWriter sw = new StringWriter();
        write(sw, object, hints);
        return sw.toString();
    }

    @Override
    public void write(Writer writer, Element object, Object... hints) throws TransformationException {
        Element html = new Element("html");
        Element body = object.getChild("body");
        if (body != null) {
            List<Element> sections = body.getChildren("sec");
            for (Element section : sections) {
                for (Element el : toHTML(section, 1)) {
                    html.addContent(el);
                }
            }
        }
        XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
        try {
            outputter.output(html, writer);
        } catch (IOException ex) {
            throw new TransformationException("", ex);
        }
    }
    
    private List<Element> toHTML(Element section, int level) {
        List<Element> elements = new ArrayList<Element>();
        List<Element> children = section.getChildren();
        for (Element child : children) {
            if ("title".equals(child.getName())) {
                Element element = new Element("H"+String.valueOf(level));
                element.setText(child.getText());
                elements.add(element);
            } else if ("p".equals(child.getName())) {
                Element el = new Element("p");
                el.setText(child.getText());
                elements.add(el);
            } else if ("sec".equals(child.getName())) {
                elements.addAll(toHTML(child, level+1));
            }
        }
        return elements;
    }
    
}