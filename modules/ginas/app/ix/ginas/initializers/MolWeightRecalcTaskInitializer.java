package ix.ginas.initializers;

import ix.core.initializers.Initializer;
import ix.core.plugins.CronExpressionBuilder;
import ix.core.plugins.SchedulerPlugin;
import ix.core.utils.executor.ProcessExecutionService;
import ix.core.utils.executor.ProcessListener;
import ix.ginas.models.v1.ChemicalSubstance;
import ix.ginas.models.v1.Substance;
import ix.ginas.processors.ConfigurableMolweightProcessor;
import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import play.Logger;

/**
 *
 * @author mitch
 */
public class MolWeightRecalcTaskInitializer extends ScheduledTaskInitializer {

    private String atomWeightFilePath;
    private String persistanceMode;
    private String propertyName;
    private String oldPropertyName;

    private Integer decimalDigits = 0;

    @Override
    public Initializer initializeWith(Map<String, ?> m) {
        super.initializeWith(m);

        atomWeightFilePath = (String) m.get("atomWeightFilePath");
        persistanceMode = (String) m.get("persistanceMode");
        propertyName = (String) m.get("propertyName");
        decimalDigits = (Integer) m.get("decimalDigits");
        if( m.containsKey("oldPropertyName") ){
            oldPropertyName = (String) m.get("oldPropertyName");
        }

        return this;
    }

    @Override
    public void run(SchedulerPlugin.TaskListener l) {
        l.message("Initializing MW calculations");
        Logger.trace("Starting in MolWeightRecalcTaskInitializer.run");
        try {
            Map<String, Object> configValues = new HashMap<>();
            configValues.put("atomWeightFilePath", atomWeightFilePath);
            configValues.put("persistanceMode", persistanceMode);
            configValues.put("propertyName", propertyName);
            configValues.put("decimalDigits", decimalDigits);
            configValues.put("oldPropertyName", oldPropertyName);

            ConfigurableMolweightProcessor processor = new ConfigurableMolweightProcessor(configValues);
            Logger.trace("instantiated processor");

            ProcessListener listen = ProcessListener.onCountChange((sofar, total)
                    -> {
                if (total != null) {
                    l.message("Recalculated: " + sofar + " of " + total);
                }
                else {
                    l.message("Recalculated: " + sofar);
                }
            });

            try {
                new ProcessExecutionService(5, 10).buildProcess(Substance.class)
                        .streamSupplier(ProcessExecutionService.CommonStreamSuppliers.allFor(Substance.class))
                        .consumer((Substance s)
                                -> {
                            if ((s instanceof ChemicalSubstance)) {
                                ChemicalSubstance chem = (ChemicalSubstance) s;
                                Logger.trace("Starting calc for " + s.uuid);
                                //System.out.println("deleted properties for structure " + s.id);

                                processor.calculateMw(chem);
                                s.save();
                                Logger.trace("completed");
                            }
                        })
                        //.before(ProcessExecutionService::nukeEverything)

                        .listener(listen)
                        .build()
                        .execute();
            } catch (IOException ex) {
                Logger.error("Error in MW recalculation task", ex);
            }
        } catch (Exception ex) {
            Logger.error("Error: " + ex.getMessage());
            ex.printStackTrace();
        }

    }

    @Override
    public String getDescription() {
        return "Generate or update a molecular weight (property) for every chemical substance in the database using configured atomic weights";
    }

    public String getAtomWeightFilePath() {
        return atomWeightFilePath;
    }

    public void setAtomWeightFilePath(String atomWeightFilePath) {
        this.atomWeightFilePath = atomWeightFilePath;
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

    public Integer getDecimalDigits() {
        return decimalDigits;
    }

    public void setDecimalDigits(Integer decimalDigits) {
        this.decimalDigits = decimalDigits;
    }

    public String getOldPropertyName() {
        return oldPropertyName;
    }

    public void setOldPropertyName(String oldPropertyName) {
        this.oldPropertyName = oldPropertyName;
    }

}
