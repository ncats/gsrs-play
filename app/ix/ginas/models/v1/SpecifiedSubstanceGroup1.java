package ix.ginas.models.v1;

import ix.ginas.models.GinasCommonSubData;
import ix.ginas.models.utils.JSONEntity;

import java.util.List;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name="ix_ginas_ssg1")
public class SpecifiedSubstanceGroup1 extends GinasCommonSubData {
	@ManyToMany(cascade=CascadeType.ALL)
	@JoinTable(name="ix_ginas_substance_ss_comp")
	public List<SpecifiedSubstanceComponent> constituents;
	
//	@OneToOne(cascade=CascadeType.ALL)
//	public SubstanceReference parentSubstance;

//	public SubstanceReference getParentSubstance() {
//		return parentSubstance;
//	}
//
//	public void setParentSubstance(SubstanceReference parentSubstance) {
//		this.parentSubstance = parentSubstance;
//	}
	
	public int size(){
		return constituents.size();
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

}