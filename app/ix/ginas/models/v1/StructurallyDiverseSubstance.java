package ix.ginas.models.v1;

import ix.core.models.Group;
import ix.ginas.models.GinasAccessReferenceControlled;
import ix.ginas.models.GinasSubstanceDefinitionAccess;
import ix.ginas.models.v1.Substance.SubstanceClass;

import com.fasterxml.jackson.annotation.JsonIgnore;
import ix.core.models.DefinitionalElement;
import ix.core.models.Keyword;
import static ix.ginas.models.v1.MixtureSubstance.MixtureDefinitionPrefix;
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
@DiscriminatorValue("DIV")
public class StructurallyDiverseSubstance extends Substance implements GinasSubstanceDefinitionAccess{
    @OneToOne(cascade=CascadeType.ALL)
    public StructurallyDiverse structurallyDiverse;

    public StructurallyDiverseSubstance () {
    	super(SubstanceClass.structurallyDiverse);
        
    }
    
    @Override
    public void delete(){
    	super.delete();
    }

    @JsonIgnore
    public GinasAccessReferenceControlled getDefinitionElement(){
        return structurallyDiverse;
    }

    @Override
	@JsonIgnore
	public List<GinasAccessReferenceControlled> getAllChildrenCapableOfHavingReferences(){
		List<GinasAccessReferenceControlled> temp = super.getAllChildrenCapableOfHavingReferences();
		if(this.structurallyDiverse!=null){
			temp.addAll(this.structurallyDiverse.getAllChildrenAndSelfCapableOfHavingReferences());
		}
		return temp;
	}

	@Override
	protected void additionalDefinitionalElements(Consumer<DefinitionalElement> consumer)
	{
		Logger.debug("in additionalDefinitionalElements");
		performAddition(structurallyDiverse, consumer, Collections.newSetFromMap(new IdentityHashMap<>()));
	}

	private void performAddition(StructurallyDiverse structDiv, Consumer<DefinitionalElement> consumer, Set<StructurallyDiverse> visited) {
		Logger.debug("in StructurallyDiverse.performAddition");
		if (structDiv != null)
		{
			visited.add(structDiv);
			Logger.debug("main part");
			List<DefinitionalElement>	definitionalElements = additionalElementsFor();
			for(DefinitionalElement de : definitionalElements)
			{
				consumer.accept(de);
			}
			Logger.debug("DE processing complete");
		}
	}

	public List<DefinitionalElement> additionalElementsFor()
	{
		/*
		Factors in this type of substance:
		1) Parent layer 1
    2) Taxonomy (if applicable) layer 1 (Except for author which is layer 2)
    3) Part layer 1
    4) Part location layer 2
    5) Fraction (both name and materialtype) layer 1
    6) Source material class layer 1
    7) Source material type layer 1
		8) Modifications (3 types) layer 2
		9) Properties (values and parameter values) layer 2
		10) adding developmentalStage to layer 2 on 28 December 2020
		*/
		Logger.debug("in StructurallyDiverse additionalElementsFor");
		List<DefinitionalElement> definitionalElements = new ArrayList<>();
		if( this.structurallyDiverse.parentSubstance != null) {
			DefinitionalElement parentElement = DefinitionalElement.of("structurallyDiverse.parentSubstance.refuuid",
							structurallyDiverse.parentSubstance.refuuid, 1);
			definitionalElements.add(parentElement);
			Logger.trace("adding parent to the def hash: " + this.structurallyDiverse.parentSubstance.refuuid);
		}

		if( this.structurallyDiverse.organismFamily != null && this.structurallyDiverse.organismFamily.length() > 0) {
			DefinitionalElement familyElement = DefinitionalElement.of("structurallyDiverse.organismFamily",
                                                                this.structurallyDiverse.organismFamily.toUpperCase(), 1);
			definitionalElements.add(familyElement);
			Logger.trace("adding family to the def hash: " + this.structurallyDiverse.organismFamily.toUpperCase());
		}

		if( this.structurallyDiverse.organismGenus != null && this.structurallyDiverse.organismGenus.length() > 0) {
			DefinitionalElement genusElement = DefinitionalElement.of("structurallyDiverse.organismGenus",
                                                                this.structurallyDiverse.organismGenus.toUpperCase(), 1);
			definitionalElements.add(genusElement);
			Logger.trace("adding genus to the def hash: " + this.structurallyDiverse.organismGenus.toUpperCase());
		}
		if( this.structurallyDiverse.organismSpecies != null && this.structurallyDiverse.organismSpecies.length() > 0) {
			DefinitionalElement speciesElement = DefinitionalElement.of("structurallyDiverse.organismSpecies",
                                                                this.structurallyDiverse.organismSpecies.toUpperCase(), 1);
			definitionalElements.add(speciesElement);
			Logger.trace("adding species to the def hash: " + this.structurallyDiverse.organismSpecies);
		}
		if( this.structurallyDiverse.organismAuthor != null &&  this.structurallyDiverse.organismAuthor.length() > 0) {
			DefinitionalElement authorElement = DefinitionalElement.of("structurallyDiverse.organismAuthor",
							structurallyDiverse.organismAuthor.toUpperCase(), 2);
			definitionalElements.add(authorElement);
			Logger.trace("adding author to the def hash: " + authorElement);
		}

		if( this.structurallyDiverse.part != null && this.structurallyDiverse.part.size() >0) {
			for(Keyword p : this.structurallyDiverse.part){
				//Logger.debug(String.format("part href: %s; id: %s; label: %s; term: %s",	p.href, p.id, p.label, p.term));
				DefinitionalElement partElement = DefinitionalElement.of("structurallyDiverse.part",
							(p.term !=null && p.term.length() >0) ? p.term.toUpperCase() : p.term, 1);
				definitionalElements.add(partElement);
				Logger.trace("adding part to the def hash: " + partElement);
			}
		}

		if( this.structurallyDiverse.partLocation != null && this.structurallyDiverse.partLocation.length() > 0) {
			DefinitionalElement partLocationElement = DefinitionalElement.of("structurallyDiverse.partLocation",
							this.structurallyDiverse.partLocation.toUpperCase(), 2);
			definitionalElements.add(partLocationElement);
		}
		if( this.structurallyDiverse.sourceMaterialClass != null && this.structurallyDiverse.sourceMaterialClass.length() > 0)
		{
		DefinitionalElement sourceMaterialClassElement = DefinitionalElement.of("structurallyDiverse.sourceMaterialClass",
						this.structurallyDiverse.sourceMaterialClass.toUpperCase(), 1);
		definitionalElements.add(sourceMaterialClassElement);
			Logger.trace("adding sourceMaterialClass to the def hash: " + this.structurallyDiverse.sourceMaterialClass.toUpperCase());
		}
		if( this.structurallyDiverse.sourceMaterialType != null && this.structurallyDiverse.sourceMaterialType.length() > 0)
		{
		DefinitionalElement sourceMaterialTypeElement = DefinitionalElement.of("structurallyDiverse.",
						this.structurallyDiverse.sourceMaterialType.toUpperCase(), 1);
		definitionalElements.add(sourceMaterialTypeElement);
			Logger.trace("adding sourceMaterialType to the def hash: " + this.structurallyDiverse.sourceMaterialType);
		}
		if( this.structurallyDiverse.fractionName != null && this.structurallyDiverse.fractionName.length() >0) {
			DefinitionalElement fractionNameElement = DefinitionalElement.of("structurallyDiverse.fractionName",
							structurallyDiverse.fractionName.toUpperCase().trim(), 1);
			definitionalElements.add(fractionNameElement);
			Logger.trace("adding fractionName to the def hash: " + this.structurallyDiverse.fractionName.toUpperCase().trim());
		}

		if( this.structurallyDiverse.fractionMaterialType != null && this.structurallyDiverse.fractionMaterialType.length() >0) {
			DefinitionalElement fractionTypeElement = DefinitionalElement.of("structurallyDiverse.fractionMaterialType",
							structurallyDiverse.fractionMaterialType.toUpperCase().trim(), 1);
			definitionalElements.add(fractionTypeElement);
			Logger.trace("adding fractionMaterialType to the def hash: " + this.structurallyDiverse.fractionMaterialType.toUpperCase().trim());
		}

		if( this.structurallyDiverse.infraSpecificType != null && structurallyDiverse.infraSpecificType.length() >0 ) {
			DefinitionalElement infraSpecTypeElement = DefinitionalElement.of("structurallyDiverse.infraSpecificType",
							this.structurallyDiverse.infraSpecificType.toUpperCase(), 2);
			definitionalElements.add(infraSpecTypeElement);
			Logger.trace("adding infraSpecificType to the def hash: " + this.structurallyDiverse.infraSpecificType.toUpperCase());
		}

		if( this.structurallyDiverse.infraSpecificName != null && structurallyDiverse.infraSpecificName.length() >0 ) {
			DefinitionalElement infraSpecNameElement = DefinitionalElement.of("structurallyDiverse.infraSpecificName",
							this.structurallyDiverse.infraSpecificName.toUpperCase(), 2);
			definitionalElements.add(infraSpecNameElement);
			Logger.trace("adding infraSpecificName to the def hash: " + this.structurallyDiverse.infraSpecificName.toUpperCase());
		}

		if (structurallyDiverse.developmentalStage != null && structurallyDiverse.developmentalStage.length() > 0) {
			DefinitionalElement devStageElement = DefinitionalElement.of("structurallyDiverse.developmentalStage",
					this.structurallyDiverse.developmentalStage.toUpperCase(), 2);
			Logger.trace("adding developmentalStage to the def hash: " + this.structurallyDiverse.developmentalStage.toUpperCase());
			definitionalElements.add(devStageElement);
		}
		if( this.modifications != null ){
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
					Logger.trace("added def element for property " + defElementName);
					for(Parameter parameter : property.getParameters()) {
						defElementName = String.format("properties.%s.parameters.%s.value",
									property.getName(), parameter.getName());
						if( parameter.getValue() != null) {
							DefinitionalElement propertyParamValueDefElement =
											DefinitionalElement.of(defElementName,
															parameter.getValue().toString(), 2);
							definitionalElements.add(propertyParamValueDefElement);
							Logger.trace("added def element for property parameter " + defElementName);
						}
					}
				}
			}
		}

		return definitionalElements;
	}

}
