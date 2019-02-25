package ix.ginas.models.v1;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;

import ix.ginas.models.GinasAccessReferenceControlled;
import ix.ginas.models.GinasCommonSubData;

@Entity
@Table(name="ix_ginas_mixture")
public class Mixture extends GinasCommonSubData {
	@ManyToMany(cascade=CascadeType.ALL)
	@JoinTable(name="ix_ginas_substance_mix_comp")
	public List<Component> components = new ArrayList<>();
	
	@OneToOne(cascade=CascadeType.ALL)
	public SubstanceReference parentSubstance;

	public SubstanceReference getParentSubstance() {
		return parentSubstance;
	}

	public void setParentSubstance(SubstanceReference parentSubstance) {
		this.parentSubstance = parentSubstance;
	}

	@JsonIgnore
	public List<Component> getMixture() {
		return components;
	}

	public void setMixture(List<Component> mixture) {
		this.components = mixture;
	}
	
	public int size(){
		return components.size();
	}
	
	
	
/*
	public void setFromMap(Map m) {
		super.setFromMap(m);
		components = toDataHolderList(
				(List<Map>) m.get("components"),
				new DataHolderFactory<gov.nih.ncats.informatics.ginas.shared.model.v1.Component>() {
					@Override
					public gov.nih.ncats.informatics.ginas.shared.model.v1.Component make() {
						return new gov.nih.ncats.informatics.ginas.shared.model.v1.Component();
					}
				});
		parentSubstance = toDataHolder(
				m.get("parentSubstance"),
				new DataHolderFactory<gov.nih.ncats.informatics.ginas.shared.model.v1.SubstanceReference>() {
					@Override
					public gov.nih.ncats.informatics.ginas.shared.model.v1.SubstanceReference make() {
						return new gov.nih.ncats.informatics.ginas.shared.model.v1.SubstanceReference();
					}
				});
	}

	@Override
	public Map addAttributes(Map m) {
		super.addAttributes(m);

		m.put("components", toMapList(components));
		if (parentSubstance != null)
			m.put("parentSubstance", parentSubstance.toMap());
		return m;
	}*/

    @Override
	@JsonIgnore
	public List<GinasAccessReferenceControlled> getAllChildrenCapableOfHavingReferences() {
		List<GinasAccessReferenceControlled> temp = new ArrayList<GinasAccessReferenceControlled>();
		if(parentSubstance!=null){
			temp.addAll(parentSubstance.getAllChildrenAndSelfCapableOfHavingReferences());
		}
		if(components!=null){
			for(Component c: components){
				temp.addAll(c.getAllChildrenAndSelfCapableOfHavingReferences());
			}
		}
		return temp;
	}
}