package ix.ginas.exporters;

import gov.nih.ncgc.chemical.Chemical;
import ix.core.GinasProcessingMessage;
import ix.ginas.models.v1.Code;
import ix.ginas.models.v1.Name;
import ix.ginas.models.v1.Substance;

import java.io.IOException;
import java.io.OutputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by katzelda on 8/23/16.
 */
public class SdfExporterFactory implements SubstanceExporterFactory {

    private static final Set<OutputFormat> formats = Collections.singleton( new OutputFormat("sdf", "SD (sdf) File"));
    @Override
    public boolean supports(Parameters params) {
        return "sdf".equals(params.getFormat().getExtension());
    }

    @Override
    public Set<OutputFormat> getSupportedFormats() {
        return formats;
    }

    @Override
    public Exporter<Substance> createNewExporter(OutputStream out, Parameters params) throws IOException {
        return new SdfExporter(out, BasicGsrsPropertyModifier.INSTANCE);
    }

    public static void addProperties(Chemical c, Substance parentSubstance, List<GinasProcessingMessage> messages){
        BasicGsrsPropertyModifier.INSTANCE.modify(c, parentSubstance, messages);
    }

    private enum BasicGsrsPropertyModifier implements SdfExporter.ChemicalModifier {

        INSTANCE;

        private void addNames(List<Name> srcNames, Substance parentSubstance, Set<String> destNames) {
            for (Name n : srcNames) {
                String name = n.name;
                destNames.add(n.name);

                for (String loc : n.getLocators(parentSubstance)) {
                    destNames.add(name + " [" + loc + "]");
                }

            }
        }

        @Override
        public void modify(Chemical c, Substance parentSubstance, List<GinasProcessingMessage> messages) {

            if (parentSubstance.approvalID != null) {
                c.setProperty("APPROVAL_ID", parentSubstance.approvalID);
            }
            c.setProperty("NAME", parentSubstance.getName());
            c.setName(parentSubstance.getName());
            StringBuilder sb = new StringBuilder();

            Set<String> names = new LinkedHashSet<>();
            names.add(parentSubstance.getName());

            addNames(parentSubstance.getOfficialNames(), parentSubstance, names);
            addNames(parentSubstance.getNonOfficialNames(), parentSubstance, names);

            c.setProperty("NAMES", names.stream().collect(Collectors.joining("\n")));


            //there's probably a way to make this into one giant collector...
            Map<String, List<Code>> codes = parentSubstance.codes.stream()
                    .collect(Collectors.groupingBy(cd -> cd.codeSystem));

            for (Map.Entry<String, List<Code>> entry : codes.entrySet()) {
                c.setProperty(entry.getKey(), entry.getValue().stream()
                        .map(cd -> {
                            StringBuilder codeBuilder = new StringBuilder(cd.code);
                            if (!"PRIMARY".equals(cd.type)) {
                                codeBuilder.append(" [").append(cd.type).append("]");
                            }
                            return codeBuilder.toString();
                        })
                        .distinct()
                        .collect(Collectors.joining("\n")));
            }

            if (!messages.isEmpty()) {
                c.setProperty("EXPORT-WARNINGS", messages.stream()
                        .map(GinasProcessingMessage::getMessage)
                        .collect(Collectors.joining("\n")));

            }


        }

    }


    }
