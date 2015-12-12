package ix.ginas.models.v1;


import ix.ginas.models.GinasCommonSubData;
import ix.ginas.models.utils.JSONConstants;
import ix.ginas.models.utils.JSONEntity;

import java.util.List;
import java.util.Map;

@JSONEntity(title = "Nucleic Acid", isFinal = true)
public class NucleicAcid extends GinasCommonSubData {
	@JSONEntity(title = "Linkages")
	List<Linkage> linkages;
	Modifications modifications;
	@JSONEntity(title = "Nucleic Acid Type", format = JSONConstants.CV_NUCLEIC_ACID_TYPE)
	String nucleicAcidType;
	@JSONEntity(title = "Nucleic Acid Subtypes", isUniqueItems = true, format = "table", itemsTitle = "Subtype", itemsFormat = JSONConstants.CV_NUCLEIC_ACID_SUBTYPE)
	List<String> nucleicAcidSubType;
	@JSONEntity(title = "Sequence Origin")
	String sequenceOrigin;
	@JSONEntity(title = "Sequence Type")
	String sequenceType;
	@JSONEntity(name = "subunits", title = "Subunits")
	List<Subunit> subunits;
	@JSONEntity(title = "Sugars", isRequired = true)
	List<Sugar> sugars;

	public List<Linkage> getLinkages() {
		return linkages;
	}

	public void setLinkages(List<Linkage> linkages) {
		this.linkages = linkages;
	}

	public Modifications getModifications() {
		return modifications;
	}

	public void setModifications(Modifications modifications) {
		this.modifications = modifications;
	}

	public String getNucleicAcidType() {
		return nucleicAcidType;
	}

	public void setNucleicAcidType(String nucleicAcidType) {
		this.nucleicAcidType = nucleicAcidType;
	}

	public List<String> getNucleicAcidSubType() {
		return nucleicAcidSubType;
	}

	public void setNucleicAcidSubType(List<String> nucleicAcidSubType) {
		this.nucleicAcidSubType = nucleicAcidSubType;
	}

	public String getSequenceOrigin() {
		return sequenceOrigin;
	}

	public void setSequenceOrigin(String sequenceOrigin) {
		this.sequenceOrigin = sequenceOrigin;
	}

	public String getSequenceType() {
		return sequenceType;
	}

	public void setSequenceType(String sequenceType) {
		this.sequenceType = sequenceType;
	}

	public List<Subunit> getSubunits() {
		return subunits;
	}

	public void setSubunits(List<Subunit> subunits) {
		this.subunits = subunits;
	}

	public List<Sugar> getSugars() {
		return sugars;
	}

	public void setSugars(List<Sugar> sugars) {
		this.sugars = sugars;
	}
/*
	public void setFromMap(Map m) {
		super.setFromMap(m);
		linkages = toDataHolderList(
				(List<Map>) m.get("linkages"),
				new DataHolderFactory<gov.nih.ncats.informatics.ginas.shared.model.v1.Linkage>() {
					@Override
					public gov.nih.ncats.informatics.ginas.shared.model.v1.Linkage make() {
						return new gov.nih.ncats.informatics.ginas.shared.model.v1.Linkage();
					}
				});
		modifications = toDataHolder(
				m.get("modifications"),
				new DataHolderFactory<gov.nih.ncats.informatics.ginas.shared.model.v1.Modifications>() {
					@Override
					public gov.nih.ncats.informatics.ginas.shared.model.v1.Modifications make() {
						return new gov.nih.ncats.informatics.ginas.shared.model.v1.Modifications();
					}
				});
		nucleicAcidType = (java.lang.String) (m.get("nucleicAcidType"));
		nucleicAcidSubType = null;
		if (m.get("nucleicAcidSubType") != null)
			nucleicAcidSubType = new java.util.ArrayList<java.lang.String>(
					(java.util.List<java.lang.String>) m
							.get("nucleicAcidSubType"));
		sequenceOrigin = (java.lang.String) (m.get("sequenceOrigin"));
		sequenceType = (java.lang.String) (m.get("sequenceType"));
		subunits = toDataHolderList(
				(List<Map>) m.get("subunits"),
				new DataHolderFactory<gov.nih.ncats.informatics.ginas.shared.model.v1.Subunit>() {
					@Override
					public gov.nih.ncats.informatics.ginas.shared.model.v1.Subunit make() {
						return new gov.nih.ncats.informatics.ginas.shared.model.v1.Subunit();
					}
				});
		sugars = toDataHolderList(
				(List<Map>) m.get("sugars"),
				new DataHolderFactory<gov.nih.ncats.informatics.ginas.shared.model.v1.Sugar>() {
					@Override
					public gov.nih.ncats.informatics.ginas.shared.model.v1.Sugar make() {
						return new gov.nih.ncats.informatics.ginas.shared.model.v1.Sugar();
					}
				});
	}

	@Override
	public Map addAttributes(Map m) {
		super.addAttributes(m);

		m.put("linkages", toMapList(linkages));
		if (modifications != null)
			m.put("modifications", modifications.toMap());
		m.put("nucleicAcidType", nucleicAcidType);
		m.put("nucleicAcidSubType", nucleicAcidSubType);
		m.put("sequenceOrigin", sequenceOrigin);
		m.put("sequenceType", sequenceType);
		m.put("subunits", toMapList(subunits));
		m.put("sugars", toMapList(sugars));
		return m;
	}*/

}
