package ix.ginas.models.v1;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import ix.core.models.DefinitionalElement;
import ix.ginas.models.GinasAccessReferenceControlled;
import ix.ginas.models.GinasSubstanceDefinitionAccess;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;
import java.util.function.Consumer;
import play.Logger;

@Entity
@Inheritance
@DiscriminatorValue("MIX")
public class MixtureSubstance extends Substance implements GinasSubstanceDefinitionAccess {
	@OneToOne(cascade=CascadeType.ALL)
    public Mixture mixture;
	public static String MixtureDefinitionPrefix = "mixture.definition";

    public MixtureSubstance () {
    	super(SubstanceClass.mixture);
    }
    
    
    @JsonIgnore
	public List<SubstanceReference> getDependsOnSubstanceReferences(){
    	
    	List<SubstanceReference> sref = new ArrayList<SubstanceReference>();
    	sref.addAll(super.getDependsOnSubstanceReferences());
    	for(Component c:mixture.getMixture()){
			sref.add(c.substance);
		}
    	
		return sref;
	}
    @Override
    public void delete(){
    	super.delete();
    	for(Component c:mixture.components){
    		c.delete();
    	}
    	
    }

	@JsonIgnore
	public GinasAccessReferenceControlled getDefinitionElement(){
		return mixture;
	}
	@Override
	@JsonIgnore
	public List<GinasAccessReferenceControlled> getAllChildrenCapableOfHavingReferences(){
		List<GinasAccessReferenceControlled> temp = super.getAllChildrenCapableOfHavingReferences();
		if(this.mixture!=null){
			temp.addAll(this.mixture.getAllChildrenAndSelfCapableOfHavingReferences());
		}
		return temp;
	}

	@Override
	protected void additionalDefinitionalElements(Consumer<DefinitionalElement> consumer)
	{
		Logger.debug("in additionalDefinitionalElements");
		//addMixtureDefinitionalElementsFor(mixture, "mixture.properties", consumer, Collections.newSetFromMap(new IdentityHashMap<>()));
		performAddition(this.mixture, consumer, Collections.newSetFromMap(new IdentityHashMap<>()));
	}

	private void performAddition(Mixture mix, Consumer<DefinitionalElement> consumer, Set<Mixture> visited)
	{
		Logger.debug("performAddition of mixture substance");
		if (mix != null && !mix.components.isEmpty())
		{
			visited.add(mix);
			Logger.debug("main part");
			List<DefinitionalElement>	definitionalElements = additionalElementsFor();
			for(DefinitionalElement de : definitionalElements)
			{
				consumer.accept(de);
			}
			Logger.debug("DE processing complete");
		}
		/*List<DefinitionalElement> definitionalElementsAdd = additionalElementsFor();
		for(int i =0; i< definitionalElementsAdd.size(); i++)
		{
			DefinitionalElement de = definitionalElementsAdd.get(i);
			consumer.accept(de);
		}*/
	}

	public List<DefinitionalElement> additionalElementsFor()
	{
		List<DefinitionalElement> definitionalElements = new ArrayList<>();
		for (int i =0; i <this.mixture.components.size(); i++)
		{
				Component component = this.mixture.components.get(i);

				Logger.debug("looking at component " + i + " identified by " + component.substance.refuuid);
				DefinitionalElement componentRefUuid = DefinitionalElement.of("mixture.components.substance.refuuid",
                                                                component.substance.refuuid, 1);
				definitionalElements.add(componentRefUuid);

				DefinitionalElement componentAnyAll = DefinitionalElement.of("mixture.components.type",
								component.type, 2);
				Logger.debug("component.type: " + component.type);
				definitionalElements.add(componentAnyAll);
				Logger.debug("completed component processing");
			}
			if( this.mixture.parentSubstance != null && this.mixture.parentSubstance.refuuid != null
							&& this.mixture.parentSubstance.refuuid.length() >0 )
			{
				System.out.println("mix.parentSubstance");
				DefinitionalElement parentSubstanceDE = DefinitionalElement.of("mixture.parentSubstance.refuuid",
								this.mixture.parentSubstance.refuuid, 2);
				definitionalElements.add(parentSubstanceDE);
			}
			return definitionalElements;
	}

}
