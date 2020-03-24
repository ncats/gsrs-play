package ix.ginas.models.v1;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonIgnore;

import ix.core.models.DefinitionalElement;
import ix.ginas.models.GinasAccessReferenceControlled;
import ix.ginas.models.GinasSubstanceDefinitionAccess;
import org.jcvi.jillion.core.residue.aa.AminoAcid;
import org.jcvi.jillion.core.residue.aa.ProteinSequence;
import org.jcvi.jillion.core.residue.nt.Nucleotide;
import org.jcvi.jillion.core.residue.nt.NucleotideSequence;

@SuppressWarnings("serial")
@Entity
@Inheritance
@DiscriminatorValue("PRO")
public class ProteinSubstance extends Substance implements GinasSubstanceDefinitionAccess {

	@OneToOne(cascade=CascadeType.ALL)
    public Protein protein;

    
    public ProteinSubstance () {
        super (SubstanceClass.protein);
    }
    @Override
    public boolean hasModifications(){
    	if(this.modifications!=null){
    		if(this.modifications.agentModifications.size()>0 || this.modifications.physicalModifications.size()>0 || this.modifications.structuralModifications.size()>0){
    			return true;
    		}
    	}
		return false;
    }
    @Override
    public int getModificationCount(){
    	int ret=0;
    	if(this.modifications!=null){
    		ret+=this.modifications.agentModifications.size();
    		ret+=this.modifications.physicalModifications.size();
    		ret+=this.modifications.structuralModifications.size();
    	}
    	return ret;
    }
    
    
    @Override
    public Modifications getModifications(){
    	return this.modifications;
    }
    
    
    @Transient
    private boolean _dirtyModifications=false;
    
    
    
    public void setModifications(Modifications m){
    	if(this.protein==null){
    		this.protein = new Protein();
    		_dirtyModifications=true;
    	}
    	this.modifications=m;
    	this.protein.setModifications(m);
    }
    
    public void setProtein(Protein p){
    	this.protein=p;
    	this.protein.setProteinSubstance(this);
    	if(_dirtyModifications){
    		this.protein.setModifications(this.modifications);
    		_dirtyModifications=false;
    	}
    }
    
    @Override
    public void delete(){
    	super.delete();
    	for(Subunit su:this.protein.subunits){
    		su.delete();
    	}
    	//protein.delete();
    }

	@JsonIgnore
	public GinasAccessReferenceControlled getDefinitionElement(){
		return protein;
	}
    
	@Override
	@JsonIgnore
	public List<GinasAccessReferenceControlled> getAllChildrenCapableOfHavingReferences(){
		List<GinasAccessReferenceControlled> temp = super.getAllChildrenCapableOfHavingReferences();
		if(this.protein!=null){
			temp.addAll(this.protein.getAllChildrenAndSelfCapableOfHavingReferences());
		}
		return temp;
	}


	@Override
	protected void additionalDefinitionalElements(Consumer<DefinitionalElement> consumer) {
		if(protein ==null || protein.subunits ==null){
			return;
		}
		for(Subunit s : this.protein.subunits){
			if(s !=null && s.sequence !=null){
				ProteinSequence seq = ProteinSequence.of(AminoAcid.cleanSequence(s.sequence));
				UUID uuid = s.getOrGenerateUUID();
				consumer.accept(DefinitionalElement.of("subunitIndex.", s.subunitIndex==null? null: Integer.toString(s.subunitIndex), 1));
				consumer.accept(DefinitionalElement.of("subunitSeq.", seq.toString(), 1));
				consumer.accept(DefinitionalElement.of("subunitSeqLength.", Long.toString(seq.getLength()), 1));

			}
		}

		Glycosylation glycosylation = this.protein.glycosylation;
		if(glycosylation !=null){
			handleGlycosylationSites(glycosylation.getNGlycosylationSites(), "N", consumer);
			handleGlycosylationSites(glycosylation.getOGlycosylationSites(), "O", consumer);
			handleGlycosylationSites(glycosylation.getCGlycosylationSites(), "C", consumer);
			if(glycosylation.glycosylationType !=null){
				consumer.accept(DefinitionalElement.of("protein.glycosylation.type", glycosylation.glycosylationType, 2));
			}
		}
		List<DisulfideLink> disulfideLinks = this.protein.getDisulfideLinks();
		if(disulfideLinks !=null){
			for(DisulfideLink disulfideLink : disulfideLinks){
				if(disulfideLink !=null) {
					consumer.accept(DefinitionalElement.of("protein.disulfide", disulfideLink.getSitesShorthand(), 2));
				}
			}
		}

		List<OtherLinks> otherLinks = this.protein.otherLinks;
		if(otherLinks !=null){
			for(OtherLinks otherLink : otherLinks){
				if(otherLink ==null){
					continue;
				}
				List<Site> sites = otherLink.getSites();
				if(sites !=null) {
					String shortHand = SiteContainer.generateShorthand(sites);
					consumer.accept(DefinitionalElement.of("protein."+shortHand, shortHand, 2));
					String type = otherLink.linkageType;
					if(type !=null){
						consumer.accept(DefinitionalElement.of("protein."+shortHand +".linkageType", type, 2));
					}
				}
			}
		}
	}

	private void handleGlycosylationSites(List<Site> sites, String letter, Consumer<DefinitionalElement> consumer){
		if(sites ==null || sites.isEmpty()){
			return;
		}

		consumer.accept(DefinitionalElement.of("protein.glycosylation."+letter, SiteContainer.generateShorthand(sites), 2));

	}
}
