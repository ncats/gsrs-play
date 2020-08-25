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
import com.fasterxml.jackson.core.JsonProcessingException;

import ix.core.models.DefinitionalElement;
import ix.core.util.EntityUtils;
import ix.core.util.LogUtil;
import ix.ginas.models.GinasAccessReferenceControlled;
import ix.ginas.models.GinasSubstanceDefinitionAccess;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

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
		play.Logger.trace("Starting in additionalDefinitionalElements for protein" );
		try	{
			this.copy()
						.canonicalize()
						.addDefinitionalElements(consumer);
		}
		catch(JsonProcessingException ex){
			play.Logger.error("error during definitional hash processing: " + ex.getMessage());
			ex.printStackTrace();
		}
	}

	private void addDefinitionalElements(Consumer<DefinitionalElement> consumer) {
		play.Logger.trace("starting in ProteinSubstance.addDefinitionalElements");
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

		if (this.modifications != null) {
			for(DefinitionalElement element : this.modifications.getDefinitionalElements().getElements()) {
				consumer.accept(element);
			}
		}
	}

	public ProteinSubstance copy() throws JsonProcessingException {
		play.Logger.trace("in ProteinSubstance.copy method");
			return EntityUtils.EntityWrapper.of(this).getClone();
	}

	public ProteinSubstance canonicalize() {
		play.Logger.trace("starting in ProteinSubstance.canonicalize");
		//make a copy of the subunits
		List<Subunit> orderedSubunits = new ArrayList<>(this.protein.subunits);//sort the subunits by canonical sort order
		Collections.sort(orderedSubunits, SubunitComparator.INSTANCE);//look through each subunit
		Map<Integer, Integer> subunitIndexMap = new HashMap<>();
		for(int i=0;i<orderedSubunits.size();i++){
			//the OLD index (as used by sites, etc) is whatever subunitIndex it had
			int oindex = orderedSubunits.get(i).subunitIndex; //already 1-index on the actual property
			//the NEW index (as it would be used by sites after canonicalization) is whatever its current
			//index is in the sorted array (+1)
			int nindex = i + 1; // 0-index on the incremental count, so add 1   //a map from old index to new index is added to the map for later use
			subunitIndexMap.put(oindex, nindex);
		}
		boolean performTranslations = false;
		play.Logger.trace("subunit map after sorting");
		for (Integer k : subunitIndexMap.keySet()) {
			String msg = String.format("mapped site %d to %d", k, subunitIndexMap.get(k));
			if (!k.equals(subunitIndexMap.get(k))) {
				performTranslations = true;
			}
			play.Logger.debug(msg);
		}
		play.Logger.trace("performTranslations: " + performTranslations);
		Glycosylation glycosylation = this.protein.glycosylation;
		if (glycosylation != null && performTranslations) {
			List<Site> translatedNGlycosylationSites = translateSites(glycosylation.getNGlycosylationSites(), subunitIndexMap);
			glycosylation.setNGlycosylationSites(translatedNGlycosylationSites);
			List<Site> translatedOGlycosylationSites = translateSites(glycosylation.getOGlycosylationSites(), subunitIndexMap);
			glycosylation.setOGlycosylationSites(translatedOGlycosylationSites);
			List<Site> translatedCGlycosylationSites = translateSites(glycosylation.getCGlycosylationSites(), subunitIndexMap);
			glycosylation.setCGlycosylationSites(translatedCGlycosylationSites);
		}

		List<DisulfideLink> disulfideLinks = this.protein.getDisulfideLinks();
		if (disulfideLinks != null && performTranslations) {
			play.Logger.trace("going to translate sites within disulfide links");
			for (DisulfideLink disulfideLink : disulfideLinks) {
				if (disulfideLink != null) {
					List<Site> originalDisulfideLinkSites = disulfideLink.getSites();
					play.Logger.trace("going to translate sites for link " + disulfideLink.getSitesShorthand());
					List<Site> translatedDisulfideLinkSites = translateSites(originalDisulfideLinkSites, subunitIndexMap);
					//temporarily add debug info
					LogUtil.trace(new Supplier<String>() {

						@Override
						public String get() {

							StringBuilder builder = new StringBuilder();
							for (int i = 0; i < disulfideLink.getSites().size(); i++) {
								builder.append(String.format("old site: %s; new site: %s%n", originalDisulfideLinkSites.get(i).toString(),
										translatedDisulfideLinkSites.get(i).toString()));

							}
							return builder.toString();
						}
					});
					disulfideLink.setSites(translatedDisulfideLinkSites);
				}
				this.protein.setDisulfideLinks(disulfideLinks);
			}

			List<OtherLinks> otherLinks = this.protein.otherLinks;
			if (otherLinks != null && performTranslations) {
				for (OtherLinks otherLink : otherLinks) {
					if (otherLink == null) {
						continue;
					}
					List<Site> sites = otherLink.getSites();
					List<Site> translatedOtherLinkSites = sites;
					play.Logger.trace("going to translate sites for other links");
					translatedOtherLinkSites = translateSites(sites, subunitIndexMap);
					otherLink.setSites(translatedOtherLinkSites);
				}
				this.protein.otherLinks = otherLinks;
			}
		}
		if(performTranslations&& this.hasModifications() && this.modifications.structuralModifications.size() >0 ) {
			play.Logger.trace("going to translate sites for struct mods");
			for(StructuralModification structMod : this.modifications.structuralModifications) {
				for(Site site: structMod.siteContainer.getSites()) {
					int originalSubunitIndex = site.subunitIndex;
					site.subunitIndex = subunitIndexMap.get(site.subunitIndex);
					LogUtil.trace(new Supplier<String>() {

						@Override
						public String get() {
							return String.format("translated site subunit for struct mod from %d to %d",
									originalSubunitIndex,
									site.subunitIndex);
						}

					});

				}
			}
		}
		return this;
	}

	private void handleGlycosylationSites(List<Site> sites, String letter, Consumer<DefinitionalElement> consumer){
		if(sites ==null || sites.isEmpty()){
			return;
		}

		consumer.accept(DefinitionalElement.of("protein.glycosylation."+letter, SiteContainer.generateShorthand(sites), 2));

	}

	private List<Site> translateSites(List<Site> startingSites, Map<Integer, Integer> subunitChanges) {
		play.Logger.trace("starting in translateSites. total sites " + startingSites.size());
		List<Site> translatedSites = new ArrayList<>();
		for (Site site : startingSites) {
			int startingSiteIndex = site.subunitIndex;
			play.Logger.trace("Looking for mapped subunit index " + startingSiteIndex);
			if (!subunitChanges.containsKey(startingSiteIndex)) {
				play.Logger.error("site index not found for site " + site.toString());
			}
			int newSubunitIndex = subunitChanges.get(startingSiteIndex);
			int newSubunit = newSubunitIndex;
			LogUtil.trace(new Supplier<String>() {

							  @Override
							  public String get() {
								  return String.format("index changed from %d to %d", site.subunitIndex, newSubunit);
							  }
						  });
			translatedSites.add(new Site(newSubunit, site.residueIndex));
		}
		return translatedSites;
	}
}
