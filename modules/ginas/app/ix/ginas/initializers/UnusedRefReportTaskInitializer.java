package ix.ginas.initializers;

import ix.ginas.models.v1.Substance;
import ix.ginas.models.v1.ChemicalSubstance;
import ix.ginas.models.v1.ProteinSubstance;
import ix.ginas.models.v1.NucleicAcidSubstance;
import ix.ginas.models.v1.PolymerSubstance;
import ix.ginas.models.v1.StructurallyDiverseSubstance;
import ix.core.models.Keyword;

import ix.core.plugins.SchedulerPlugin;
import ix.core.utils.executor.ProcessExecutionService;
import ix.core.utils.executor.ProcessListener;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Mitch Miller
 */
public class UnusedRefReportTaskInitializer extends ScheduledTaskInitializer
{

	@Override
	public void run(SchedulerPlugin.TaskListener l)
	{
		l.message("Initializing reference analysis");
		ProcessListener listen = ProcessListener.onCountChange((sofar, total) ->
		{
			if (total != null)
			{
				l.message("Examined:" + sofar + " of " + total);
			} else
			{
				l.message("Examined:" + sofar);
			}
		});

		try
		{
			new ProcessExecutionService(5, 10).buildProcess(Substance.class)
					.streamSupplier(ProcessExecutionService.CommonStreamSuppliers.allFor(Substance.class))
					.consumer((Substance s) ->
					{

						Map<String, Integer> usages = new HashMap<>();
						s.references.forEach((ref) ->
						{
							System.out.println("adding ref with ID " + ref.id + " UUID: " + ref.getUuid());
							usages.put(ref.getUuid().toString(), 0);
						});
						//go through all collections
						s.names.forEach((name) ->
						{
							name.getReferences().forEach((nameRef) ->
							{
								/*System.out.println(String.format("href: %s, id: %d; label: %s, term: %s" + nameRef.href,
										nameRef.id, nameRef.label, nameRef.term));*/
//								System.out.println(String.format("href: " + nameRef.href + "; id: " + nameRef.id
//										+ "; label: " + nameRef.label + "; term: " + nameRef.term));
								processKeyword(nameRef, usages);

								name.getNameOrgs().forEach((nameOrg) ->
								{
									nameOrg.getReferences().forEach((nameOrgRef) ->
									{
										processKeyword(nameOrgRef, usages);
									});
								});
							});
						});
						s.codes.forEach((code) ->
						{
							code.getReferences().forEach((codeRef) ->
							{
								processKeyword(codeRef, usages);
							});
						});
						s.properties.forEach((prop) ->
						{
							prop.getReferences().forEach((propRef) ->
							{
								processKeyword(propRef, usages);
							});
						});
						s.relationships.forEach((rel) ->
						{
							rel.getReferences().forEach((relRef) ->
							{
								processKeyword(relRef, usages);
							});
						});

						if (s.modifications != null && s.modifications.agentModifications != null)
						{
							s.modifications.agentModifications.forEach((agentMod) ->
							{
								agentMod.getReferences().forEach((agentModRef) ->
								{
									processKeyword(agentModRef, usages);
								});
							});
						}
						if (s.modifications != null && s.modifications.physicalModifications != null)
						{
							s.modifications.physicalModifications.forEach((physMod) ->
							{
								physMod.getReferences().forEach((physModRef) ->
								{
									processKeyword(physModRef, usages);
								});
							});
						}

						if (s.modifications != null && s.modifications.structuralModifications != null)
						{
							s.modifications.structuralModifications.forEach((strMod) ->
							{
								strMod.getReferences().forEach((strModRef) ->
								{
									processKeyword(strModRef, usages);
								});
							});
						}
						System.out.println("Substance with ID " + s.getUuid() + " is of class "
								+ s.getClass().getName());

						if (s instanceof ChemicalSubstance)
						{
							ChemicalSubstance chem = (ChemicalSubstance) s;
							chem.getStructure().getReferences().forEach((structRef) ->
							{
								processKeyword(structRef, usages);
							});

							chem.getMoieties().forEach((moiety) ->
							{
								moiety.getReferences().forEach((moiRef) ->
								{
									processKeyword(moiRef, usages);
								});
							});

						} else if (s instanceof ProteinSubstance)
						{
							ProteinSubstance prot = (ProteinSubstance) s;
							prot.protein.subunits.forEach((unit) ->
							{
								unit.getReferences().forEach((unitRef) ->
								{
									processKeyword(unitRef, usages);
								});
							});
							prot.protein.glycosylation.getReferences().forEach((glyRef) ->
							{
								processKeyword(glyRef, usages);
							});

						} else if (s instanceof NucleicAcidSubstance)
						{
							NucleicAcidSubstance nucl = (NucleicAcidSubstance) s;
							nucl.nucleicAcid.subunits.forEach((subunit) ->
							{
								subunit.getReferences().forEach((subunitRef) ->
								{
									processKeyword(subunitRef, usages);
								});
							});
							nucl.nucleicAcid.getSugars().forEach((sugar) ->
							{
								sugar.getReferences().forEach((sugRef) ->
								{
									processKeyword(sugRef, usages);
								});
							});

						} else if (s instanceof PolymerSubstance)
						{
							PolymerSubstance polymer = (PolymerSubstance) s;
							polymer.polymer.monomers.forEach((mon) ->
							{
								mon.getReferences().forEach((monRef) ->
								{
									processKeyword(monRef, usages);
								});
							});
							polymer.polymer.structuralUnits.forEach((strUnit) ->
							{
								strUnit.getReferences().forEach((strUnitRef) ->
								{
									processKeyword(strUnitRef, usages);
								});
							});
						} else if (s instanceof StructurallyDiverseSubstance)
						{
							StructurallyDiverseSubstance divSub = (StructurallyDiverseSubstance) s;
							//has no collections with refs?
						}

						for (String ref : usages.keySet())
						{
							System.out.println("ref: " + ref + " count: " + usages.get(ref));
						}
					})
					.listener(listen)
					.build()
					.execute();
		} catch (IOException ex)
		{
			Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
		}
	}

	@Override
	public String getDescription()
	{
		return "Produce a list of unused references";
	}

	private static void processKeyword(Keyword ref, Map<String, Integer> counts)
	{
		Integer count = counts.get(ref.term);
		if (count != null)
		{
			count++;
			counts.put(ref.term, count);
		} else
		{
			System.out.println("Reference  " + ref.term + " not found!");
		}
	}
}
