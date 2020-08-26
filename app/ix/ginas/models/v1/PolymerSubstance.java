package ix.ginas.models.v1;

import gov.nih.ncats.molwitch.Chemical;
import ix.core.models.Indexable;
import ix.core.validator.GinasProcessingMessage;
import ix.ginas.models.GinasAccessReferenceControlled;
import ix.ginas.models.GinasSubstanceDefinitionAccess;

import com.fasterxml.jackson.annotation.JsonIgnore;
import ix.core.controllers.StructureFactory;
import ix.core.models.DefinitionalElement;
import ix.core.models.Structure;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;

import javax.persistence.*;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import play.Logger;

@Entity
@Inheritance
@DiscriminatorValue("POL")
public class PolymerSubstance extends Substance implements GinasSubstanceDefinitionAccess
{

    @OneToOne(cascade=CascadeType.ALL)
    public Polymer polymer;

    public PolymerSubstance () {
    	super(SubstanceClass.polymer);
    }

    @Override
    protected Chemical getChemicalImpl(List<GinasProcessingMessage> messages) {
        messages.add(GinasProcessingMessage
                .WARNING_MESSAGE("Polymer substance structure is for display, and is not complete in definition"));

        return polymer.displayStructure.toChemical(messages);
    }

    @JsonIgnore
    public GinasAccessReferenceControlled getDefinitionElement(){
        return polymer;
    }

    @Override
   	@JsonIgnore
   	public List<GinasAccessReferenceControlled> getAllChildrenCapableOfHavingReferences(){
   		List<GinasAccessReferenceControlled> temp = super.getAllChildrenCapableOfHavingReferences();
   		if(this.polymer!=null){
   			temp.addAll(this.polymer.getAllChildrenAndSelfCapableOfHavingReferences());
   		}
   		return temp;
   	}

    @JsonIgnore
    @Indexable(indexed=false, structure=true)
	public String getStructureMolfile(){
    	return polymer.displayStructure.molfile;
	}

	@Override
	protected void additionalDefinitionalElements(Consumer<DefinitionalElement> consumer) {
		Logger.debug("in polymer additionalDefinitionalElements");
		performAddition(this.polymer, consumer, Collections.newSetFromMap(new IdentityHashMap<>()));
	}

	private void performAddition(Polymer polymer, Consumer<DefinitionalElement> consumer, Set<Polymer> visited) {
		Logger.debug("in polymer performAddition");
		if (polymer != null) {
			visited.add(polymer);
			List<DefinitionalElement> definitionalElements = additionalElementsFor();
			for (DefinitionalElement de : definitionalElements) {
				consumer.accept(de);
			}
			Logger.debug("DE processing complete");
		}
	}

	public List<DefinitionalElement> additionalElementsFor() {
		Logger.debug("in PolymerSubstance additionalElementsFor");
		List<DefinitionalElement> definitionalElements = new ArrayList<>();

		for (Material monomer : this.polymer.monomers) {
			if( monomer.monomerSubstance != null) {
				DefinitionalElement monomerElement = DefinitionalElement.of("polymer.monomer.monomerSubstance.refuuid",
								monomer.monomerSubstance.refuuid, 1);
				definitionalElements.add(monomerElement);
				Logger.debug("adding monomer refuuid to the def hash: " + monomer.monomerSubstance.refuuid);
				if (monomer.amount != null) {
					DefinitionalElement monomerAmountElement = DefinitionalElement.of("polymer.monomer.monomerSubstance.amount",
									monomer.amount.toString(), 2);
					definitionalElements.add(monomerAmountElement);
				}
			} else {
				Logger.debug("monomer does not have a substance attached.");
			}
		}

		if (this.polymer.structuralUnits != null && !this.polymer.structuralUnits.isEmpty()) {
			//todo: consider canonicalizing
			List<Unit> canonicalizedUnits = this.polymer.structuralUnits;
			for (Unit unit : canonicalizedUnits) {
				if( unit.type == null) {
					Logger.debug("skipping null unit");
					continue;
				}
				//Logger.debug("about to process unit structure " + unit.structure);
				String molfile = unit.structure;//prepend newline to avoid issue later on
				Structure structure = null;
				try	{
					structure= StructureFactory.getStructureFrom(molfile, false);
				}
				catch(Exception ex) {
					Logger.warn("Unable to parse structure from polymer unit molfile: " + molfile);
					continue;
				}

				Logger.debug("created structure OK. looking at unit type: " + unit.type);
				int layer = 1;
				/* all units are part of layer 1 as of 13 March 2020 based on https://cnigsllc.atlassian.net/browse/GSRS-1361
				if( unit.type.contains("SRU")) {
					layer=1;
				}*/

				String currentHash = structure.getExactHash();
				DefinitionalElement structUnitElement = DefinitionalElement.of("polymer.structuralUnit.structure.l4",
								currentHash, layer);
				definitionalElements.add(structUnitElement);

				if (unit.amount != null) {
					DefinitionalElement structUnitAmountElement = DefinitionalElement.of("polymer.structuralUnit["
									+ currentHash +"].amount", unit.amount.toString(), 2);
					definitionalElements.add(structUnitAmountElement);
				}
				Logger.debug("adding structural unit def element: " + structUnitElement);
			}
		}

		//todo: add additional items to the definitional element list
		if (this.modifications != null) {
				definitionalElements.addAll(this.modifications.getDefinitionalElements().getElements());
		}

		if( this.properties != null ) {
			for(Property property : this.properties) {
				if(property.isDefining() && property.getValue() != null) {
					String defElementName = String.format("properties.%s.value",
									property.getName());
					DefinitionalElement propertyValueDefElement =
									DefinitionalElement.of(defElementName, property.getValue().toString(), 2);
					definitionalElements.add(propertyValueDefElement);
					Logger.debug("added def element for property " + defElementName);
					for(Parameter parameter : property.getParameters()) {
						defElementName = String.format("properties.%s.parameters.%s.value",
									property.getName(), parameter.getName());
						if( parameter.getValue() != null) {
							DefinitionalElement propertyParamValueDefElement =
											DefinitionalElement.of(defElementName,
															parameter.getValue().toString(), 2);
							definitionalElements.add(propertyParamValueDefElement);
							Logger.debug("added def element for property parameter " + defElementName);
						}
					}
				}
			}
		}

		return definitionalElements;
	}

}
