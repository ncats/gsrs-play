package ix.ginas.models.v1;

//import gov.nih.ncats.informatics.ginas.shared.model.v1.utils.JSONEntity;


import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.OneToOne;

@Entity
@Inheritance
@DiscriminatorValue("NA")
//@JSONEntity(name = "nucleicAcidSubstance", title = "Nucleic Acid Substance")
public class NucleicAcidSubstance extends Substance {
	@OneToOne(cascade= CascadeType.ALL)
	public NucleicAcid nucleicAcid;

	@Override
    public Modifications getModifications(){
    	return this.nucleicAcid.getModifications();
    }
    
    public void setModifications(Modifications m){
    	 this.nucleicAcid.setModifications(modifications);
    }
	
//	public NucleicAcid getNucleicAcid() {
//		return nucleicAcid;
//	}
//
//	public void setNucleicAcid(NucleicAcid nucleicAcid) {
//		this.nucleicAcid = nucleicAcid;
//	}
	
	
/*
	public void setFromMap(Map m) {
		super.setFromMap(m);
		nucleicAcid = toDataHolder(
				m.get("nucleicAcid"),
				new DataHolderFactory<gov.nih.ncats.informatics.ginas.shared.model.v1.NucleicAcid>() {
					@Override
					public gov.nih.ncats.informatics.ginas.shared.model.v1.NucleicAcid make() {
						return new gov.nih.ncats.informatics.ginas.shared.model.v1.NucleicAcid();
					}
				});
	}

	@Override
	public Map addAttributes(Map m) {
		super.addAttributes(m);

		if (nucleicAcid != null)
			m.put("nucleicAcid", nucleicAcid.toMap());
		return m;
	}
*/
}
