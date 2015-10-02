package ix.idg.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import ix.ncats.controllers.App;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import play.Play;
import play.libs.XML;
import play.libs.XPath;
import play.mvc.Result;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileInputStream;
import java.io.StringWriter;
import java.util.concurrent.Callable;

public class ExpressionApp extends App {

    static ObjectMapper mapper = new ObjectMapper();

    public static Result error(int code, String mesg) {
        return ok(ix.idg.views.html.error.render(code, mesg));
    }

    public static Result _notFound(String mesg) {
        return notFound(ix.idg.views.html.error.render(404, mesg));
    }

    public static Result _badRequest(String mesg) {
        return badRequest(ix.idg.views.html.error.render(400, mesg));
    }

    public static Result _internalServerError(Throwable t) {
        t.printStackTrace();
        return internalServerError
                (ix.idg.views.html.error.render
                        (500, "Internal server error: " + t.getMessage()));
    }

    static String xml2str(Node node) {
        try {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            StreamResult result = new StreamResult(new StringWriter());
            DOMSource source = new DOMSource(node);
            transformer.transform(source, result);
            return result.getWriter().toString();
        } catch (TransformerException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static Result homunculus(final String organs, final String confs) throws Exception {
        if (organs == null || confs == null)
            return _badRequest("Must specify an organ name and a valid confidence value");
        final String key = "expression/homunculus/" + organs + "/" + confs;
        response().setContentType("image/svg+xml");
        return getOrElse(key, new Callable<Result>() {
            public Result call() throws Exception {
                String[] confidenceColors = new String[]{
                        "#ffffff", "#EDF8E9", "#BAE4B3", "#74C476", "#31A354", "#006D2C"
                };
                File svg = Play.application().getFile("app/assets/tissues_body_human.svg");
                FileInputStream fis = new FileInputStream(svg);
                Document doc = XML.fromInputStream(fis, "UTF-8");

                String[] organlist = organs.split(",");
                String[] tmp = confs.split(",");
                if (organlist.length != tmp.length) {
                    response().setContentType("text/html");
                    return _badRequest("If specifying multiple organs, must specify equal number of confidences and vice versa");
                }
                Integer[] conflist = new Integer[tmp.length];
                for (int i = 0; i < tmp.length; i++) conflist[i] = Integer.valueOf(tmp[i]);

                for (int i = 0; i < organlist.length; i++) {
                    if (conflist[i] < 0 || conflist[i] > 5) {
                        response().setContentType("text/html");
                        return _badRequest("Invalid confidence value was specified: " + conflist[i]);
                    }
                    Node node = XPath.selectNode("//*[@id='" + organlist[i] + "']", doc);
                    NamedNodeMap attributes = node.getAttributes();
                    Node attrNode = attributes.getNamedItem("fill");
                    attrNode.setNodeValue(confidenceColors[conflist[i]]);
                    attributes.setNamedItem(attrNode);
                }

                return ok(xml2str(doc));
            }
        });
    }

}
