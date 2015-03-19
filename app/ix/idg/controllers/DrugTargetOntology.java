package ix.idg.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.util.ArrayList;
import java.util.List;

/**
 * A one line summary.
 *
 * @author Rajarshi Guha
 */
public class DrugTargetOntology {
    JsonNode root;
    public DtoTerm rootTerm = null;

    public class DtoTerm {
        String id, label;
        public List<DtoTerm> children = new ArrayList<>();

        public DtoTerm() {
        }

        public DtoTerm(String id, String label) {
            this.id = id;
            this.label = label;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        @Override
        public String toString() {
            return "DtoTerm{" +
                    "label='" + label + '\'' +
                    '}';
        }
    }


    public DrugTargetOntology() {

    }

    public JsonNode getRoot() {
        return root;
    }

    public void setRoot(JsonNode root) {
        this.root = root;
        constructTree();
    }

    public DtoTerm _findTerm(DtoTerm t, String s) {
        if (t.label.equals(s) || t.id.equals(s)) return t;
        DtoTerm match = null;
        for (DtoTerm child : t.children) {
            match = _findTerm(child, s);
            if (match != null) return match;
        }
        return match;
    }

    public DtoTerm findTerm(String s) {
        return _findTerm(rootTerm, s);
    }

    private String getNodeId(JsonNode node) {
        return "";
    }

    private void handleNode(JsonNode node, DtoTerm parent) {
        DtoTerm term = new DtoTerm(getNodeId(node), node.get("data").textValue());
        parent.children.add(term);
        ArrayNode children = (ArrayNode) node.get("children");
        if (children.size() == 0) return;
        for (int i = 0; i < children.size(); i++) handleNode(children.get(i), term);
    }

    private void constructTree() {
        rootTerm = new DtoTerm(getNodeId(root), root.get("data").textValue());
        ArrayNode children = (ArrayNode) root.get("children");
        for (int i = 0; i < children.size(); i++) {
            handleNode(children.get(i), rootTerm);
        }
    }

}
