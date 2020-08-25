package ix.ginas.models.v1;

import ix.ginas.models.GinasAccessReferenceControlled;
import ix.ginas.models.GinasSubstanceDefinitionAccess;
import ix.ginas.models.v1.Substance.SubstanceClass;

import com.fasterxml.jackson.annotation.JsonIgnore;
import ix.core.models.DefinitionalElement;

import javax.persistence.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.function.Consumer;
import play.Logger;

@Entity
@Inheritance
@DiscriminatorValue("SSI")
public class SpecifiedSubstanceGroup1Substance extends Substance implements GinasSubstanceDefinitionAccess{
	@OneToOne(cascade=CascadeType.ALL)
    public SpecifiedSubstanceGroup1 specifiedSubstance;

    public SpecifiedSubstanceGroup1Substance() {
    	 super (SubstanceClass.specifiedSubstanceG1);
    }

    @JsonIgnore
    public GinasAccessReferenceControlled getDefinitionElement(){
        return specifiedSubstance;
    }

    @Override
   	@JsonIgnore
   	public List<GinasAccessReferenceControlled> getAllChildrenCapableOfHavingReferences(){
   		List<GinasAccessReferenceControlled> temp = super.getAllChildrenCapableOfHavingReferences();
   		if(this.specifiedSubstance!=null){
   			temp.addAll(this.specifiedSubstance.getAllChildrenAndSelfCapableOfHavingReferences());
   		}
   		return temp;
   	}

	@Override
	protected void additionalDefinitionalElements(Consumer<DefinitionalElement> consumer)	{
		Logger.debug("in SpecifiedSubstanceGroup1Substance.additionalDefinitionalElements");
		for(DefinitionalElement de : additionalElementsFor())	{
			consumer.accept(de);
		}
	}

	public List<DefinitionalElement> additionalElementsFor() {
		List<DefinitionalElement> definitionalElements = new ArrayList<>();
		Logger.debug("total components " + this.specifiedSubstance.constituents.size());
		for (int i =0; i <this.specifiedSubstance.constituents.size(); i++)	{
			SpecifiedSubstanceComponent component = this.specifiedSubstance.constituents.get(i);
			if( component.substance == null ){
				Logger.debug("null substance found in component " + i);
				continue;
			}
			Logger.debug("processing component " + i + " identified by " + component.substance.refuuid);
			DefinitionalElement componentRefUuid = DefinitionalElement.of("specifiedSubstance.constituents.substance.refuuid",
                                                                component.substance.refuuid, 1);
			definitionalElements.add(componentRefUuid);

			if( component.role != null){
				DefinitionalElement componentType = DefinitionalElement.of("specifiedSubstance.constituents.role",
								component.role, 2);
				Logger.debug("	component.role: " + component.role);
				definitionalElements.add(componentType);
			}
			if( component.amount != null) {
				DefinitionalElement constituentAmountElement = DefinitionalElement.of("specifiedsubstancegroup1.constituents.monomerSubstance.amount",
								component.amount.toString(), 2);
				Logger.debug("looking at constituent amount " + component.amount.toString());
				definitionalElements.add(constituentAmountElement);
			}
			Logger.debug("completed component processing");
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
