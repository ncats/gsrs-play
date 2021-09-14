package ix.ginas.processors;

import gov.nih.ncats.molwitch.Atom;
import gov.nih.ncats.molwitch.Chemical;
import ix.core.EntityProcessor;
import ix.ginas.models.v1.Amount;
import ix.ginas.models.v1.ChemicalSubstance;
import ix.ginas.models.v1.Property;
import ix.ginas.models.v1.QualifiedAtom;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.DoubleAccumulator;
import java.util.stream.Collectors;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import play.Logger;

/**
 *
 * @author mitch
 */
public class ConfigurableMolweightProcessor implements EntityProcessor<ChemicalSubstance> {

    private Map<QualifiedAtom, Double> atomicWeights = new HashMap<>();
    private String atomWeightFilePath;
    private String persistanceMode;
    private String propertyName;
    private Integer decimalDigits = 0;
    private String oldPropertyName;

    private final String PROPERTY_TYPE = "PHYSICAL";

    public ConfigurableMolweightProcessor(Map initialValues) {
        Logger.trace("starting in ConfigurableMolweightProcessor");
        atomWeightFilePath = (String) initialValues.get("atomWeightFilePath");
        if (!atomWeightFilePath.isEmpty() && Files.exists(Paths.get(atomWeightFilePath))) {
            initAtomicWeights(atomWeightFilePath);
        }
        persistanceMode = (String) initialValues.get("persistanceMode");
        propertyName = (String) initialValues.get("propertyName");
        if (initialValues.get("decimalDigits") != null) {
            decimalDigits = (Integer) initialValues.get("decimalDigits");
        }
        if (initialValues.get("oldPropertyName") != null) {
            oldPropertyName = (String) initialValues.get("oldPropertyName");
        }
    }

    public String getAtomWeightFilePath() {
        return atomWeightFilePath;
    }

    public void setAtomWeightFilePath(String atomWeightFilePath) {
        this.atomWeightFilePath = atomWeightFilePath;
        if (Files.exists(Paths.get(atomWeightFilePath))) {
            initAtomicWeights(atomWeightFilePath);
        }
    }

    public String getPersistanceMode() {
        return persistanceMode;
    }

    public void setPersistanceMode(String persistanceMode) {
        this.persistanceMode = persistanceMode;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    public String getOldPropertyName() {
        return oldPropertyName;
    }

    public void setOldPropertyName(String oldPropertyName) {
        this.oldPropertyName = oldPropertyName;
    }

    private void initAtomicWeights(String filePath) {
        Logger.debug("starting in initAtomicWeights. ");
        atomicWeights = new HashMap<>();
        String commentIntro = "#";
        try {
            List<String> lines = Files.readAllLines(Paths.get(filePath));
            for (int lineNum = 1; lineNum < lines.size(); lineNum++) {
                String line = lines.get(lineNum);
                if (line.trim().isEmpty() || line.trim().startsWith(commentIntro)) {
                    Logger.debug(String.format("line %d is blank", lineNum));
                    continue;
                }
                String[] lineParts = line.split(",");
                //Logger.debug("lineParts len: " + lineParts.length);
                String symbol = lineParts[1];
                String rawAw = lineParts[3];
                if (!rawAw.isEmpty()) {
                    Optional<Double> parsedWt = safelyParseDouble(rawAw);
                    if (parsedWt.isPresent()) {
                        int massIndication = 0;
                        if (lineParts.length >= 5 && !lineParts[4].isEmpty()) {
                            Optional<Integer> parsedMassIndication = safelyParseInteger(lineParts[4]);
                            if (parsedMassIndication.isPresent()) {
                                massIndication = parsedMassIndication.get();
                            }
                        }
                        Logger.debug("massIndication: " + massIndication);
                        QualifiedAtom qa = new QualifiedAtom(symbol, massIndication);
                        atomicWeights.put(qa, parsedWt.get());
                    }
                    else {
                        //Logger.debug("no double in input " + rawAw);
                    }
                }
                else {
                    Logger.debug("skipping blank line " + line);
                }
            }
        } catch (Exception ex) {
            Logger.error("Error reading atomic weights: " + ex.getMessage());
            ex.printStackTrace();
        }
        Logger.debug("initAtomicWeights completed");
    }

    @Override
    public void prePersist(final ChemicalSubstance s) {
        calculateMw(s);
    }

    public void calculateMw(ChemicalSubstance s) {
        Logger.trace("ConfigurableMolweightProcessor.calculateMw");
        Double calculatedMw = computeMolWt(s);
        if (persistanceMode.equalsIgnoreCase("inherent")) {
            Logger.debug("setting inherent value to " + calculatedMw);
            s.structure.mwt = calculatedMw;
            s.structure.forceUpdate();
        }
        else {
            setMwProperty(s, calculatedMw);
        }
    }

    @Override
    public void preUpdate(ChemicalSubstance chem) {
        //Logger.debug("ConfigurableMolweightProcessor.preUpdate");
        calculateMw(chem);
    }

    private void setMwProperty(ChemicalSubstance chem, Double mw) {
        Logger.trace("setMwProperty");
        Property mwProperty = null;
        for (Property p : chem.properties) {
            if (p.getName().equalsIgnoreCase(propertyName)) {
                mwProperty = p;
                Logger.trace("found property");
                break;
            }
        }
        if (mwProperty == null) {
            mwProperty = new Property();
            mwProperty.setName(propertyName);
            mwProperty.setPropertyType(PROPERTY_TYPE);
            chem.properties.add(mwProperty);
            Logger.trace("created property");
        }
        Amount propertyAmount = new Amount();
        propertyAmount.average = decimalDigits > 0 ? roundToDecimals(mw, decimalDigits) : mw;
        //propertyAmount.type= PROPERTY_TYPE;
        //propertyAmount.nonNumericValue = this.getClass().getName();
        mwProperty.setValue(propertyAmount);
        if (oldPropertyName != null && oldPropertyName.length() > 0) {
            //Property propertyToRemove = null;
            int indexToRemove = -1;
            for (int p = 0; p < chem.properties.size(); p++) {

                if (chem.properties.get(p).getName().equalsIgnoreCase(oldPropertyName)) {
                    indexToRemove = p;
                    chem.properties.get(p).deprecated = true;
                    break;
                }
            }
            if (indexToRemove > -1) {
                Property propertyToRemove = chem.properties.get(indexToRemove);

                Logger.trace("found and removed old property at index " + indexToRemove);
                chem.properties.remove(indexToRemove);
                propertyToRemove.delete();
            }
        }

    }

    private Double computeMolWt(ChemicalSubstance chemical) {

        java.util.concurrent.atomic.DoubleAccumulator mw = new DoubleAccumulator(Double::sum, 0L);
        Chemical chem = chemical.structure.toChemical().copy();
        if (chem.hasImplicitHs()) {
            chem.makeHydrogensExplicit();
        }
        AtomicInteger implicitHydrogenCount = new AtomicInteger(0);
        List<Atom> atoms = chemical.structure.toChemical().atoms().collect(Collectors.toList());
        atoms.forEach(a -> {
            implicitHydrogenCount.addAndGet(a.getImplicitHCount());
            //todo: reduce logging when this gets tedious:
            Logger.debug(String.format("atom; symbol: %s; mass number: %d", a.getSymbol(), a.getMassNumber()));
            QualifiedAtom qa = new QualifiedAtom(a.getSymbol(), a.getMassNumber());
            if (atomicWeights.containsKey(qa)) {
                mw.accumulate(atomicWeights.get(qa));
            }
            else {
                Logger.debug("Using internal exact mass " + a.getExactMass() + " for atom " + a.getSymbol());
                mw.accumulate(a.getExactMass());
            }
        });
        double mass = chem.getMass();
        QualifiedAtom hydrogen = new QualifiedAtom("H", 0, 1.008);
        double calculated = mw.doubleValue() + (atomicWeights.containsKey(hydrogen) ? atomicWeights.get(hydrogen) : hydrogen.getAtomicMass()) * implicitHydrogenCount.get();
        Logger.debug(String.format("getMass: %.4f; computed: %.4f.  implicitHydrogenCount.get(): %d", mass, calculated, implicitHydrogenCount.get()));
        return calculated;
    }

    private Optional<Double> safelyParseDouble(String input) {
        Optional<Double> parsed = Optional.empty();
        try {
            double d = Double.parseDouble(input);
            parsed = Optional.of(d);
        } catch (NumberFormatException ex) {

        }
        return parsed;
    }

    private Optional<Integer> safelyParseInteger(String input) {
        Optional<Integer> parsed = Optional.empty();
        try {
            int d = Integer.parseInt(input);
            parsed = Optional.of(d);
        } catch (NumberFormatException ex) {

        }
        return parsed;
    }

    public static double roundToDecimals(double num, int n) {
        String format = String.format("%s.%df", "%", n);
        String formattedNumber = String.format(format, num);
        Logger.trace("formattedNumber: " + formattedNumber);
        return Double.parseDouble(formattedNumber);
    }
}
