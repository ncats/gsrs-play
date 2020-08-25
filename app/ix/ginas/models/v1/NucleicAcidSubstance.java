package ix.ginas.models.v1;

import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.OneToOne;
import javax.persistence.Transient;


import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

import com.fasterxml.jackson.annotation.JsonIgnore;

import ix.core.models.DefinitionalElement;
import ix.ginas.models.GinasAccessReferenceControlled;
import ix.ginas.models.GinasSubstanceDefinitionAccess;
import org.jcvi.jillion.core.residue.nt.Nucleotide;
import org.jcvi.jillion.core.residue.nt.NucleotideSequence;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import play.Logger;

@Entity
@Inheritance
@DiscriminatorValue("NA")
//@JSONEntity(name = "nucleicAcidSubstance", title = "Nucleic Acid Substance")
public class NucleicAcidSubstance extends Substance implements GinasSubstanceDefinitionAccess{
	@OneToOne(cascade= CascadeType.ALL)
	public NucleicAcid nucleicAcid;

	
	public NucleicAcidSubstance(){
    	super(SubstanceClass.nucleicAcid);
	}
	
	
	@Override
    public Modifications getModifications(){
    	return this.nucleicAcid.getModifications();
    }
    
	
	
    
    @Transient
    private boolean _dirtyModifications=false;
    
    
    public void setModifications(Modifications m){
    	if(this.nucleicAcid==null){
    		this.nucleicAcid = new NucleicAcid();
    		_dirtyModifications=true;
    	}
    	this.nucleicAcid.setModifications(m);
    	this.modifications=m;
    }
    
    public void setNucleicAcid(NucleicAcid p){
    	this.nucleicAcid=p;
    	if(_dirtyModifications){
    		this.nucleicAcid.setModifications(this.modifications);
    		_dirtyModifications=false;
    	}
    }


    @Override
    public void delete(){
    	Modifications old=this.modifications;
    	this.modifications=null;
    	super.delete();
    	for(Subunit su:this.nucleicAcid.subunits){
    		su.delete();
    	}
    	if(old!=null){
    		old.delete();
    	}
    }
    
    public int getTotalSites(boolean includeEnds){
    	int tot=0;
    	for(Subunit s:this.nucleicAcid.getSubunits()){
    		tot+=s.getLength();
    		if(!includeEnds){
    			tot--;
    		}
    	}
    	return tot;
    }

	@JsonIgnore
	public GinasAccessReferenceControlled getDefinitionElement(){
		return nucleicAcid;
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

	@Override
	@JsonIgnore
	public List<GinasAccessReferenceControlled> getAllChildrenCapableOfHavingReferences(){
		List<GinasAccessReferenceControlled> temp = super.getAllChildrenCapableOfHavingReferences();
		if(this.nucleicAcid!=null){
			temp.addAll(this.nucleicAcid.getAllChildrenAndSelfCapableOfHavingReferences());
		}
		return temp;
	}

	@Override
	protected void additionalDefinitionalElements(Consumer<DefinitionalElement> consumer) {
		if(nucleicAcid ==null || nucleicAcid.subunits ==null){
			return;
		}
		/*for(Subunit s : this.nucleicAcid.subunits){
			if(s !=null && s.sequence !=null){
				NucleotideSequence seq = NucleotideSequence.of(Nucleotide.cleanSequence(s.sequence));
				UUID uuid = s.getOrGenerateUUID();
				consumer.accept(DefinitionalElement.of("subunitIndex."+ uuid, s.subunitIndex==null? null: Integer.toString(s.subunitIndex)));
				consumer.accept(DefinitionalElement.of("subunitSeq."+ uuid , seq.toString()));
				consumer.accept(DefinitionalElement.of("subunitSeqLength."+ uuid , Long.toString(seq.getLength())));

			}
		}*/
		performAddition(this.nucleicAcid, consumer, Collections.newSetFromMap(new IdentityHashMap<>()));
	}

	private void performAddition(NucleicAcid nucleicAcid, Consumer<DefinitionalElement> consumer, Set<NucleicAcid> visited)
	{
		Logger.debug("performAddition of nucleic acid substance");
		if (nucleicAcid != null )
		{
			visited.add(nucleicAcid);
			Logger.debug("main part");
			List<DefinitionalElement>	definitionalElements = additionalElementsFor();
			for(DefinitionalElement de : definitionalElements)
			{
				Logger.debug("adding DE with key " + de.getKey() + " to consumer for a NA");
				consumer.accept(de);
			}
			Logger.debug("DE processing complete");
			}
		}

	public List<DefinitionalElement> additionalElementsFor() {
		List<DefinitionalElement> definitionalElements = new ArrayList<>();

		if(this.nucleicAcid !=null) {
			//this can happen be null for incomplete? so we should check it

			if(this.nucleicAcid.subunits !=null) {
				for(int i = 0; i< this.nucleicAcid.subunits.size(); i++) {
					Subunit s = this.nucleicAcid.subunits.get(i);
					Logger.debug("processing subunit with sequence " + s.sequence);
					DefinitionalElement sequenceHash = DefinitionalElement.of("nucleicAcid.subunits.sequence", s.sequence, 1);
					definitionalElements.add(sequenceHash);
				}
			}

			if(this.nucleicAcid.linkages !=null) {
				for(int i = 0; i< this.nucleicAcid.linkages.size(); i++) {
					Linkage l = this.nucleicAcid.linkages.get(i);
					Logger.debug("processing linkage " + l.getLinkage());
					DefinitionalElement linkageHash = DefinitionalElement.of("nucleicAcid.linkages.linkage", l.getLinkage(), 2);
					definitionalElements.add(linkageHash);

					//check if siteContainer is null
					if(l.siteContainer!=null) {
						Logger.debug("processing l.siteContainer.sitesShortHand " + l.siteContainer.sitesShortHand);
						DefinitionalElement siteElement = DefinitionalElement.of("nucleicAcid.linkages.site", l.siteContainer.sitesShortHand, 2);
						definitionalElements.add(siteElement);
					}
				}

			}
			if(this.nucleicAcid.sugars !=null) {
				for(int i = 0; i<this.nucleicAcid.sugars.size(); i++)	{
					Sugar s = this.nucleicAcid.sugars.get(i);
					Logger.debug("processing sugar " + s.sugar);
					DefinitionalElement sugarElement = DefinitionalElement.of("nucleicAcid.sugars.sugar", s.sugar, 2);
					definitionalElements.add(sugarElement);
					//check if siteContainer is null
					if(s.siteContainer!=null) {
						Logger.debug("processing s.siteContainer.sitesShortHand " + s.siteContainer.sitesShortHand);
						DefinitionalElement siteElement = DefinitionalElement.of("nucleicAcid.sugars.site", s.siteContainer.sitesShortHand, 2);
						definitionalElements.add(siteElement);
					}
				}
			}
		}

		if( this.modifications != null ){
			definitionalElements.addAll(this.modifications.getDefinitionalElements().getElements());
		}

		return definitionalElements;
	}
}
