package ix.ginas.models.v1;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;


import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import ix.core.models.BeanViews;
import ix.core.models.Indexable;
import ix.core.util.ModelUtils;
import ix.ginas.models.GinasAccessReferenceControlled;
import ix.ginas.models.GinasCommonSubData;
import ix.utils.Global;
import play.Logger;

@SuppressWarnings("serial")
@Entity
@Table(name = "ix_ginas_protein")
public class Protein extends GinasCommonSubData {
	@Indexable(facet = true, name = "Protein Type")
	public String proteinType;


	public String proteinSubType;

	@Indexable(facet = true, name = "Sequence Origin")
	public String sequenceOrigin;

	@Indexable(facet = true, name = "Sequence Type")
	public String sequenceType;

	@Lob
	@JsonIgnore
	@Indexable(indexed = false)
	public String disulfJSON = null;

	@OneToOne(mappedBy = "protein")
	private ProteinSubstance proteinSubstance;

	@Transient
	protected transient ObjectMapper mapper = new ObjectMapper();

	@Transient
	List<DisulfideLink> tmpDisulfides = null;

	@JsonView(BeanViews.Full.class)
	public List<DisulfideLink> getDisulfideLinks() {
		if (tmpDisulfides != null)
			return tmpDisulfides;
		List<DisulfideLink> rolekinds = new ArrayList<DisulfideLink>();
		if (this.disulfJSON != null) {
			try {
				ObjectMapper om = new ObjectMapper();
				List l = om.readValue(disulfJSON, List.class);
				for (Object o : l) {
					try {
						rolekinds.add(om.treeToValue(om.valueToTree(o), DisulfideLink.class));
					} catch (Exception e) {
						System.err.println(e.getMessage());
						Logger.trace("Error parsing disulfides", e);
					}
				}
			} catch (Exception e) {
				Logger.trace("Error parsing disulfides", e);
			}

		}
		tmpDisulfides = rolekinds;
		return tmpDisulfides;
	}

	@JsonView(BeanViews.Compact.class)
	@JsonProperty("_disulfideLinks")
	public JsonNode getJsonDisulfideLinks() {
		JsonNode node = null;
		List<DisulfideLink> links = this.getDisulfideLinks();
		if (links.size() > 0) {
			try {
				ObjectNode n = mapper.createObjectNode();
				n.put("count", links.size());
				n.put("href", Global.getRef(getProteinSubstance().getClass(), getProteinSubstance().getUuid())
						+ "/protein/disulfideLinks");
				n.put("shorthand", ModelUtils.shorthandNotationForLinks(links));

				node = n;
			} catch (Exception ex) {
				ex.printStackTrace();
				node = mapper.valueToTree(links);
			}
		}
		return node;
	}

	@JsonView(BeanViews.Compact.class)
	@JsonProperty("_glycosylation")
	public JsonNode getJsonGlycosylation() {
		JsonNode node = null;
		Glycosylation glyc = this.glycosylation;
		if (glyc != null) {
			try {
				ObjectNode n = mapper.createObjectNode();
				if (glyc.glycosylationType != null) {
					n.put("type", glyc.glycosylationType);
				}
				n.put("nsites", glyc._NGlycosylationSiteContainer.siteCount);
				n.put("osites", glyc._OGlycosylationSiteContainer.siteCount);
				n.put("csites", glyc._CGlycosylationSiteContainer.siteCount);
				n.put("href", Global.getRef(getProteinSubstance().getClass(), getProteinSubstance().getUuid())
						+ "/protein/glycosylation");
				node = n;
			} catch (Exception ex) {
				ex.printStackTrace();
				node = mapper.valueToTree(glyc);
			}
		}
		return node;
	}

	public void setDisulfideLinks(List<DisulfideLink> links) {
		ObjectMapper om = new ObjectMapper();
		disulfJSON = om.valueToTree(links).toString();
		tmpDisulfides = null;
	}

	@JsonView(BeanViews.Full.class)
	@OneToOne(cascade = CascadeType.ALL)
	public Glycosylation glycosylation;

	//@JsonIgnore
	//@OneToOne(cascade = CascadeType.ALL)
	//public Modifications modifications;

	@ManyToMany(cascade = CascadeType.ALL)
	@JoinTable(name = "ix_ginas_protein_subunit")
	@OrderBy("subunitIndex asc")
	public List<Subunit> subunits = new ArrayList<Subunit>();


	/**
	 * This is something that should have existed in earlier versions but did not.
	 * Basically the subtypes _shoud_ have been a list or set to begin with,
	 * but were accidentally stored as a single string.
	 * @return
	 */
	@JsonIgnore
	@Indexable(facet = true, name = "Protein Subtype")
	public List<String> getProteinSubtypes(){
		return Optional.ofNullable(this.proteinSubType)
			    .flatMap(new Function<String,Optional<Stream<String>>>(){
					@Override
					public Optional<Stream<String>> apply(String arg0) {
						return Optional.of(Arrays.stream(arg0.split("\\|")));
					}
			    })
			    .orElse(Stream.empty())
			    .collect(Collectors.toList());
	}

	/**
	 * This is a setter for some future version where protein subtypes are allowed
	 * to be lists rather than single entities.
	 * @param subtypes
	 */
	@JsonIgnore
	public void setProteinSubtypes(List<String> subtypes){
		if(subtypes !=null){
			this.proteinSubType = subtypes.stream().collect(Collectors.joining("|"));
		}
	}

	@OneToMany(mappedBy = "owner", cascade = CascadeType.ALL)
	public List<OtherLinks> otherLinks = new ArrayList<OtherLinks>();

	public Protein() {
	}

	/**
	 * mark our child subunits as ours.
	 * Mostly used so we know what kind of type
	 * this subunit is by walking up the tree
	 * to inspect its parent (us).
	 */
	//@PostLoad
	@PreUpdate
	@PrePersist
	public void adoptChildSubunits(){
		List<Subunit> subunits=this.subunits;
		for(Subunit s: subunits){
			s.setParent(this);
		}
	}

	@JsonIgnore
	@Transient
	private List<Runnable> onParentSet = new ArrayList<>();
	
	
	public void setModifications(Modifications mod) {
		if (mod == null) {
			return;
		}
		Runnable r=new Runnable(){

			@Override
			public void run() {
				if(mod!=getProteinSubstance().modifications){
					getProteinSubstance().setModifications(mod);
				}
			}
			
		};
		if(this.proteinSubstance==null){
			onParentSet.add(r);
		}else{
			r.run();
		}
		
		//this.modifications = mod;
	}

	@JsonIgnore
	@Transient
	private Map<String, String> _modifiedCache = null;

	@JsonIgnore
	public Map<String, String> getModifiedSites() {
		if (_modifiedCache != null) {
			return _modifiedCache;
		}

		_modifiedCache = new HashMap<String, String>();
		//disulfides
		for (DisulfideLink dsl : this.getDisulfideLinks()) {
			for (Site s : dsl.getSites()) {
				_modifiedCache.put(s.toString(), "disulfide");
			}
		}
		//glycosylation
		if (this.glycosylation != null) {
			for (Site s : this.glycosylation.getNGlycosylationSites()) {
				_modifiedCache.put(s.toString(), "nglycosylation");
			}
			for (Site s : this.glycosylation.getOGlycosylationSites()) {
				_modifiedCache.put(s.toString(), "oglycosylation");
			}
			for (Site s : this.glycosylation.getCGlycosylationSites()) {
				_modifiedCache.put(s.toString(), "cglycosylation");
			}
		}
		Modifications m=getProteinSubstance().getModifications();
		if (m != null) {
			//modifications
			for (StructuralModification sm : m.structuralModifications) {
				if (sm.getSites() != null) {
					for (Site s : sm.getSites()) {
						_modifiedCache.put(s.toString(), "structuralModification");
					}
				}
			}
		}
		if (this.otherLinks != null) {
			//modifications
			for (OtherLinks sm : this.otherLinks) {
				if (sm.getSites() != null) {
					for (Site s : sm.getSites()) {
						_modifiedCache.put(s.toString(), "otherLinkage");
					}
				}
			}
		}
		return _modifiedCache;
	}

	public List<Subunit> getSubunits() {
		Collections.sort(subunits, new Comparator<Subunit>() {
			@Override
			public int compare(Subunit o1, Subunit o2) {
				return o1.subunitIndex - o2.subunitIndex;
			}
		});
		adoptChildSubunits();
		return this.subunits;
	}

	public void setSubunits(List<Subunit> subunits) {
		this.subunits = subunits;
		adoptChildSubunits();
	}

	@Override
	public void update() {
		super.update();
	}

	/**
	 * Returns a string to describe any modification that happens at the specified 
	 * site. Returns null if there is no modification.
	 * @param subunitIndex
	 * @param residueIndex
	 * @return
	 */
	public String getSiteModificationIfExists(int subunitIndex, int residueIndex) {
		return getModifiedSites().get(subunitIndex + "_" + residueIndex);
	}

	/**
	 * Get the residue string at the specified site. Returns null if it does not exist.
	 * @param site
	 * @return
	 */
	public String getResidueAt(Site site) {
		Integer i = site.subunitIndex;
		Integer j = site.residueIndex;

		try {
			for (Subunit su : this.subunits) {
				if (su.subunitIndex.equals(i)) {
					if (j - 1 >= su.sequence.length() || j - 1 < 0)
						return null;
					char res = su.sequence.charAt(j - 1);
					return res + "";
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@JsonIgnore
	public void setProteinSubstance(ProteinSubstance proteinSubstance) {
		this.proteinSubstance = proteinSubstance;
		for(Runnable r:onParentSet){
			r.run();
		}
		onParentSet.clear();
	}
	
	@JsonIgnore
	public ProteinSubstance getProteinSubstance() {
		return this.proteinSubstance;
	}

	 @Override
	   	@JsonIgnore
	   	public List<GinasAccessReferenceControlled> getAllChildrenCapableOfHavingReferences() {
	   		List<GinasAccessReferenceControlled> temp = new ArrayList<GinasAccessReferenceControlled>();

	   		if(this.glycosylation!=null){
	   				temp.addAll(glycosylation.getAllChildrenAndSelfCapableOfHavingReferences());
	   		}
	   		if(this.subunits!=null){
	   			for(Subunit s : this.subunits){
	   				temp.addAll(s.getAllChildrenAndSelfCapableOfHavingReferences());
	   			}
	   		}
	   		if(this.otherLinks!=null){
	   			for(OtherLinks ol : this.otherLinks){
	   				temp.addAll(ol.getAllChildrenAndSelfCapableOfHavingReferences());
	   			}
	   		}

	   		return temp;
	   	}
}
