package ix.ginas.utils.validation.validators;

import ix.core.validator.GinasProcessingMessage;
import ix.core.validator.ValidatorCallback;
import ix.ginas.models.v1.GinasChemicalStructure;
import ix.ginas.models.v1.PolymerSubstance;
import ix.ginas.models.v1.Substance;
import ix.ginas.models.v1.Unit;
import ix.ginas.utils.validation.ValidationUtils;

import java.util.*;

/**
 * Created by katzelda on 5/14/18.
 */
public class PolymerValidator extends AbstractValidatorPlugin<Substance>{
    @Override
    public void validate(Substance s, Substance objold, ValidatorCallback callback) {
        PolymerSubstance cs = (PolymerSubstance)s;
        
        if (cs.polymer == null) {
            callback.addMessage(GinasProcessingMessage
                    .ERROR_MESSAGE("Polymer substance must have a polymer element"));
        } else {

            boolean withDisplay = !isNull(cs.polymer.displayStructure);
            boolean withIdealized = !isNull(cs.polymer.idealizedStructure);

            if(!withDisplay || withIdealized){
                cs.polymer.displayStructure=null;
                withDisplay=false;
            }

            if (!withDisplay && !withIdealized) {
                GinasProcessingMessage gpmwarn = GinasProcessingMessage
                        .ERROR_MESSAGE("No Display Structure or Idealized Structure found");
                callback.addMessage(gpmwarn);
            } else if (!withDisplay && withIdealized) {
                GinasProcessingMessage gpmwarn = GinasProcessingMessage
                        .WARNING_MESSAGE(
                                "No Display Structure found, default to using Idealized Structure")
                        .appliableChange(true);
                callback.addMessage(gpmwarn, ()-> {
                    try {
                        cs.polymer.displayStructure = cs.polymer.idealizedStructure
                                .copy();
                    } catch (Exception e) {
                        callback.addMessage(GinasProcessingMessage.ERROR_MESSAGE(e
                                .getMessage()));
                    }
                });

            } else if (withDisplay && !withIdealized) {
                GinasProcessingMessage gpmwarn = GinasProcessingMessage
                        .INFO_MESSAGE(
                                "No Idealized Structure found, default to using Display Structure")
                        .appliableChange(true);
                callback.addMessage(gpmwarn, ()-> {
                            try {
                                cs.polymer.idealizedStructure = cs.polymer.displayStructure
                                        .copy();
                            } catch (Exception e) {
                                callback.addMessage(GinasProcessingMessage.ERROR_MESSAGE(e
                                        .getMessage()));
                            }
                        }
                    );


            }

            if (cs.polymer.structuralUnits == null || cs.polymer.structuralUnits.isEmpty()) {
                callback.addMessage(GinasProcessingMessage
                        .WARNING_MESSAGE("Polymer substance should have structural units"));
            } else {
                List<Unit> srus = cs.polymer.structuralUnits;
                // ensure that all mappings make sense
                // first of all, any mapping should be found as a key somewhere
                Set<String> rgroupsWithMappings = new HashSet<String>();
                Set<String> rgroupsActual = new HashSet<String>();
                Set<String> rgroupMentions = new HashSet<String>();
                Set<String> connections = new HashSet<String>();

                for (Unit u : srus) {
                    List<String> contained = u.getContainedConnections();
                    List<String> mentioned = u.getMentionedConnections();
                    if (mentioned != null) {
                        if (!contained.containsAll(mentioned)) {
                            callback.addMessage(GinasProcessingMessage
                                    .ERROR_MESSAGE("Mentioned attachment points '"
                                            + mentioned.toString()
                                            + "' in unit '"
                                            + u.label
                                            + "' are not all found in actual connecitons '"
                                            + contained.toString() + "'. "));
                        }
                    }
                    Map<String, LinkedHashSet<String>> mymap = u
                            .getAttachmentMap();
                    if (mymap != null) {
                        for (String k : mymap.keySet()) {
                            rgroupsWithMappings.add(k);
                            for (String m : mymap.get(k)) {
                                rgroupMentions.add(m);
                                connections.add(k + "-" + m);
                            }
                        }
                    }
                }
                if (!rgroupsWithMappings.containsAll(rgroupMentions)) {
                    Set<String> leftovers = new HashSet<String>(rgroupMentions);
                    leftovers.removeAll(rgroupsWithMappings);
                    callback.addMessage(GinasProcessingMessage
                            .ERROR_MESSAGE("Mentioned attachment point(s) '"
                                    + leftovers.toString()
                                    + "' cannot be found "));
                }

                Map<String, String> newConnections = new HashMap<String, String>();
                // symmetry detection
                for (String con : connections) {
                    String[] c = con.split("-");
                    if (!connections.contains(c[1] + "-" + c[0])) {
                        GinasProcessingMessage gp = GinasProcessingMessage
                                .WARNING_MESSAGE(
                                        "Connection '"
                                                + con
                                                + "' does not have inverse connection. This can be created.")
                                .appliableChange(true);

                        callback.addMessage(gp, ()-> {

                            String old = newConnections.get(c[1]);
                            if (old == null)
                                old = "";
                            newConnections.put(c[1], old + c[0] + ";");
                        });
                    }
                }
                for (Unit u : srus) {
                    for (String c : u.getContainedConnections()) {
                        String additions = newConnections.get(c);
                        if (additions != null) {
                            for (String add : additions.split(";")) {
                                if (!add.equals("")) {
                                    u.addConnection(c, add);
                                }
                            }
                        }
                    }
                }

            }
            if (cs.polymer.monomers == null || cs.polymer.monomers.size() <= 0) {
                callback.addMessage(GinasProcessingMessage
                        .WARNING_MESSAGE("Polymer substance should have monomers"));
            }
            if (cs.properties == null || cs.properties.size() <= 0) {
                callback.addMessage(GinasProcessingMessage
                        .WARNING_MESSAGE("Polymer substance has no properties, typically expected at least a molecular weight"));
            }

            ValidationUtils.validateReference(cs, cs.polymer, callback, ValidationUtils.ReferenceAction.FAIL);
        }
        
    }

    private static boolean isNull(GinasChemicalStructure gcs) {
        if (gcs == null || gcs.molfile == null)
            return true;
        return false;
    }
}
