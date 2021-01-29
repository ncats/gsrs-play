package ix.ginas.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import ix.core.util.CachedSupplier;
import ix.ginas.controllers.v1.SubstanceFactory;
import ix.ginas.models.v1.Amount;
import ix.ginas.models.v1.ChemicalSubstance;
import ix.ginas.models.v1.DisulfideLink;
import ix.ginas.models.v1.Property;
import ix.ginas.models.v1.Protein;
import ix.ginas.models.v1.ProteinSubstance;
import ix.ginas.models.v1.Site;
import ix.ginas.models.v1.StructuralModification;
import ix.ginas.models.v1.Substance;
import ix.ginas.models.v1.Substance.SubstanceClass;
import ix.ginas.models.v1.Subunit;
import ix.utils.Tuple;
import ix.utils.Util;
import java.util.UUID;
import org.jcvi.jillion.core.residue.aa.AminoAcid;
import play.Logger;
import gov.nih.ncats.common.util.SingleThreadCounter;
import ix.core.chem.FormulaInfo;

public class ProteinUtils {

	//Based on analysis from existing MAB entries
	private static final CachedSupplier<Map<String,List<int[]>>> KNOWN_DISULFIDE_PATTERNS = CachedSupplier.of(()->{
		Map<String,List<int[]>> dstypes=Arrays.stream((
				"IGG4	0-1,11-12,13-31,14-15,18-19,2-26,20-21,22-23,24-25,27-28,29-30,3-4,5-16,6-17,7-8,9-10\n" + 
				"IGG2	0-1,11-12,13-14,15-35,16-17,2-30,22-23,24-25,26-27,28-29,3-4,31-32,33-34,5-18,6-19,7-20,8-21,9-10\n" + 
				"IGG1	0-1,11-12,13-14,15-31,18-19,2-3,20-21,22-23,24-25,27-28,29-30,4-26,5-16,6-17,7-8,9-10")
				.split("\n"))
				.map(s->s.split("\t"))
				.map(s->Tuple.of(s[0],s[1]))
				.map(Tuple.vmap(v->v.split(",")))
				.map(Tuple.vmap(v->Arrays.stream(v)
						.map(ds->{
							String[] idx=ds.split("-");
							return new int[]{Integer.parseInt(idx[0]),Integer.parseInt(idx[1])};
						})
						.collect(Collectors.toList())
						
						))
				.collect(Tuple.toMap());
		return dstypes;
	});
	
	
	//from
	//http://www.seas.upenn.edu/~cis535/Fall2004/HW/GCB535HW6b.pdf
  //note: above PDF is 404 as of December; correcting D and N
	private static String lookup=
			"A	71.09\n" + 
			"R	156.19\n" + 
			"D	115.088\n" +
			"N	114.104\n" +
			"C	103.15\n" + 
			"E	129.12\n" + 
			"Q	128.14\n" + 
			"G	57.05\n" + 
			"H	137.14\n" + 
			"I	113.16\n" + 
			"L	113.16\n" + 
			"K	128.17\n" + 
			"M	131.19\n" + 
			"F	147.18\n" + 
			"P	97.12\n" + 
			"S	87.08\n" + 
			"T	101.11\n" + 
			"W	186.12\n" + 
			"Y	163.18\n" + 
			"V	99.14";

	//	https://en.wikipedia.org/wiki/Amino_acid
	private static String lookupFormula =
					"A	C-3;H-7;N-1;O-2\n" +
					"R	C-6;H-14;N-4;O-2\n" +
					"D	C-4;H-8;N-2;O-3\n" +
					"N	C-4;H-7;N-1;O-4\n" +
					"C	C-3;H-7;N;O-2;S-1\n" +
					"E	C-5;H-9;N;O-4\n" +
					"Q	C-5;H-10;N-2;O-3\n" +
					"G	C-2;H-5;N;O-2\n" +
					"H	C-6;H-9;N-3;O-2\n" +
					"I	C-6;H-13;N;O-2\n" +
					"L	C-6;H-13;N;O-2\n" +
					"K	C-6;H-14;N-2;O-2\n" +
					"M	C-5;H-11;N;O-2;S\n" +
					"F	C-9;H-11;N;O-2\n" +
					"P	C-5;H-9;N;O-2\n" +
					"S	C-3;H-7;N;O-3\n" +
					"T	C-4;H-9;N;O-3\n" +
					"W	C-11;H-12;N-2;O-2\n" +
					"Y	C-9;H-11;N;O-3\n" +
					"V	C-5;H-11;N;O-2";

	private final static String MOLECULAR_FORMULA_PROPERTY_NAME = "Molecular Formula";

	static Map<String, Double> weights = new HashMap<String,Double>();
	
	static Map<String, Map<String, SingleThreadCounter>> atomCounts = new HashMap<>();

	static{
		for(String line:lookup.split("\n")){
			String[] cols=line.split("\t");
			weights.put(cols[0],Double.parseDouble(cols[1]));
		}
		for(String line: lookupFormula.split("\n")) {
			String[] cols=line.split("\t");
			String aminoAcidAbbrev = cols[0];
			Map<String, SingleThreadCounter> elementData = new HashMap<>();
			for( String atomData : cols[1].split("\\;")) {
				String[] atomDataParts = atomData.split("\\-");
				long count = 1;
				if( atomDataParts.length >1 && atomDataParts[1] != null ) {
					count = Long.parseLong(atomDataParts[1]);
				}
				elementData.put(atomDataParts[0], new SingleThreadCounter(count));
			}
			//remove one water
			removeWater(elementData);
			atomCounts.put(aminoAcidAbbrev, elementData);
		}
	}
	
	public static double getSingleAAWeight(String c){
		Double d=weights.get(c.toUpperCase());
		if(d==null)return 0;
		return d;
	}

	public static Map<String, SingleThreadCounter> getSingleAAFormula(String c) {
		if( atomCounts.containsKey(c.toUpperCase()) ) {
			Map aaMap = atomCounts.get(c.toUpperCase());
			return aaMap;
		}
		return null;
	}

	public static double getSubunitWeight(Subunit sub, Set<String> unknownRes){
		//start with extra water for end groups
		if(unknownRes==null)unknownRes=new LinkedHashSet<String>();
		if(sub.sequence==null || sub.sequence.length()==0)return 0.0;
		double total=18.015;
		for(char c: sub.sequence.toCharArray()){
			double w=getSingleAAWeight(c+"");
			if(w<=0){
				unknownRes.add(c+"");
			}
			total+=w;
			
		}
		return total;
	}

	public static Map<String, SingleThreadCounter> getSubunitFormulaInfo(String sub, Set<String> unknownRes){
		play.Logger.debug("starting in getSubunitFormulaInfo with sub " + sub);
		//start with extra water for end groups
		if(unknownRes==null)unknownRes=new LinkedHashSet<String>();
		Map<String, SingleThreadCounter> formula = new HashMap<>();
		for(char c: sub.toCharArray()){
			Map<String, SingleThreadCounter> residueContribution =getSingleAAFormula(""+c);
			if(residueContribution == null){
				unknownRes.add(c+"");
			}
			else {

				residueContribution.keySet().forEach(k->{
					if( formula.containsKey(k)){
						//play.Logger.debug(String.format("incrementing count for %s by %d", k, residueContribution.get(k).getAsLong()));
						formula.get(k).increment(residueContribution.get(k).getAsLong());
					}
					else {
						//play.Logger.debug(String.format("new item for %s with %d", k, residueContribution.get(k).getAsLong()));
						formula.put(k, new SingleThreadCounter( residueContribution.get(k).getAsLong()));
					}
				});
			}
		}
		return formula;
	}

	public static double generateProteinWeight(ProteinSubstance ps, Set<String> unknownRes){
		play.Logger.debug("starting in generateProteinWeight");
		double total=0;
		List<String> handledModTypes = Arrays.asList("AMINO ACID SUBSTITION","AMINO-ACID SUBSTITUTION","AMINO ACID SUBSTITUION",
						"AMINO_ACID_SUBSTITUTION","AMINO ACID SUBSTITTUION","AMINO ACID REMOVAL","AMINO ACID REPLACEMENT","AMINO ACID SUBSITUTE",
						"AMINO ACID REPLACMENT","AMINO ACID SUBSTITUTION");
		if(unknownRes==null)unknownRes=new LinkedHashSet<String>();
		for(Subunit su:ps.protein.subunits){
			total+=getSubunitWeight(su,unknownRes);
		}
		play.Logger.debug(String.format("default MW: %.2f", total));
		if( ps.hasModifications()  && ps.modifications.structuralModifications.size() > 0) {
			play.Logger.debug("considering structuralModifications");
			double waterMW = 18.0;//https://tripod.nih.gov/ginas/app/substances?q=water
			//double acetylGroupWt = 43.045d;//https://en.wikipedia.org/wiki/Acetyl_group
			for(StructuralModification mod :
							ps.modifications.structuralModifications.stream()
											.filter(m->m.molecularFragment != null && m.molecularFragment.refuuid != null
															&& (m.extent != null && m.extent.equalsIgnoreCase("COMPLETE"))
															&&handledModTypes.contains( m.structuralModificationType)
															&&m.getSites().size() > 0)
											.collect(Collectors.toSet())){
				//The following information can be used to determine whether it's useful to factor in partial extents with a numeric amount
				String message =
						String.format("mod.residueModified: %s; mod.molecularFragment.refuuid: %s, mod.molecularFragment.approvalID: %s; extent: %s, amount: %s, residue: %s, structuralModificationType: [%s]",
						mod.residueModified, mod.molecularFragment.refuuid, mod.molecularFragment.approvalID,
						mod.extent, (mod.extentAmount ==null) ? "null" : mod.extentAmount.toString(), mod.residueModified, mod.structuralModificationType);
				play.Logger.debug(message);

				MolecularWeightAndFormulaContribution contribution= getContributionForID(mod.molecularFragment.refuuid);
				//TODO: Technically we should have some kind of warning if the molecular weight for the modification
				// can't be determined. This shouldn't be that rare of an occurence. Right now, we just ignore these case
				// but that isn't sustainable for complex substances.

				if(contribution!=null){
					//modificationAddition= modificationAddition + contribution.getMw() + waterEffect;
					Logger.debug(String.format("handling modification (structural; has fragment; has sites)retrieved contribution: %.2f from %s; total sites: %d",
									contribution.getMw(), contribution.getSubstanceClass(), mod.getSites().size()));
					//in the case of a substitution, we consider the mw contribution of the residue that's replaced
					for(Site site : mod.getSites()){
						Subunit s= ps.protein.subunits.get((site.subunitIndex-1));
						char aa =s.sequence.charAt(site.residueIndex-1);
						String aaCode = ""+aa;
						AminoAcid acid = AminoAcid.parse(aa);
						double aaWt= getSingleAAWeight(aaCode);
						double siteContribution = contribution.getMw() - waterMW - aaWt; //the value from getSingleAAWeight includes the effect of removal of 1 H2O
						// the contribution's mw does not so we substract the mw of water
						Logger.debug(String.format("processing site with subunit %d; residue number %d; AA: %c; name: %s; aa mw: %.2f; net effect of site: %.2f",
									site.subunitIndex, site.residueIndex, aa, acid.getName(), aaWt, siteContribution));
						total += siteContribution;
					}
				}
			}
		}
		else {
			play.Logger.debug("no mods to consider");
		}

		play.Logger.debug("final total: " + total);
		return total;
	}
	public static MolecularWeightAndFormulaContribution generateProteinWeightAndFormula(ProteinSubstance ps, Set<String> unknownRes){
		play.Logger.debug("starting in generateProteinWeightAndFormula.  Total ps.protein.subunits: " + ps.protein.subunits.size());
		double total=0;
		Map<String, SingleThreadCounter> formulaCounts = new HashMap<>();
		MolecularWeightAndFormulaContribution result = null;

		List<String> handledModTypes = Arrays.asList("AMINO ACID SUBSTITION","AMINO-ACID SUBSTITUTION","AMINO ACID SUBSTITUION",
						"AMINO_ACID_SUBSTITUTION","AMINO ACID SUBSTITTUION","AMINO ACID REMOVAL","AMINO ACID REPLACEMENT","AMINO ACID SUBSITUTE",
						"AMINO ACID REPLACMENT","AMINO ACID SUBSTITUTION");
		if(unknownRes==null)unknownRes=new LinkedHashSet<String>();
		for(Subunit su:ps.protein.subunits){
			total+=getSubunitWeight(su,unknownRes);
			Map<String, SingleThreadCounter> contribution = getSubunitFormulaInfo(su.sequence, unknownRes);
			contribution.keySet().forEach(k->{
				if( formulaCounts.containsKey(k)) {
					//play.Logger.debug(String.format("incrementing count for element %s by %d", k, contribution.get(k).getAsLong()));
					formulaCounts.get(k).increment(contribution.get(k).getAsLong());
				}
				else {
					formulaCounts.put(k, new SingleThreadCounter( contribution.get(k).getAsLong()));
					//play.Logger.debug(String.format("creating count for element %s by %d", k, contribution.get(k).getAsLong()));
				}
			});
		}

		play.Logger.debug(String.format("default MW: %.2f; default formula: %s", total, makeFormulaFromMap(formulaCounts)));
		if( ps.hasModifications()  && ps.modifications.structuralModifications.size() > 0) {
			play.Logger.debug("considering structuralModifications");
			double waterMW = 18.0;//https://tripod.nih.gov/ginas/app/substances?q=water
			//double acetylGroupWt = 43.045d;//https://en.wikipedia.org/wiki/Acetyl_group
			for(StructuralModification mod :
							ps.modifications.structuralModifications.stream()
											.filter(m->m.molecularFragment != null && m.molecularFragment.refuuid != null
															&& (m.extent != null && m.extent.equalsIgnoreCase("COMPLETE"))
															&&handledModTypes.contains( m.structuralModificationType)
															&&m.getSites().size() > 0)
											.collect(Collectors.toSet())){
				String message =
						String.format("mod.residueModified: %s; mod.molecularFragment.refuuid: %s, mod.molecularFragment.approvalID: %s; extent: %s, amount: %s, residue: %s, structuralModificationType: [%s]",
						mod.residueModified, mod.molecularFragment.refuuid, mod.molecularFragment.approvalID,
						mod.extent, (mod.extentAmount ==null) ? "null" : mod.extentAmount.toString(), mod.residueModified, mod.structuralModificationType);
				play.Logger.debug(message);

				MolecularWeightAndFormulaContribution contribution= getContributionForID(mod.molecularFragment.refuuid);
				if(contribution==null){
					//There is no computable fragment to use
					System.out.println("DIdn't find features for:" + mod.molecularFragment.refuuid);

				}else{
					removeWater(contribution.getFormulaMap());
					//modificationAddition= modificationAddition + contribution.getMw() + waterEffect;
					Logger.debug(String.format("handling modification (structural; has fragment; has sites)retrieved contribution: %.2f from %s; total sites: %d",
									contribution.getMw(), contribution.getSubstanceClass(), mod.getSites().size()));
					//in the case of a substitution, we consider the mw contribution of the residue that's replaced
					for(Site site : mod.getSites()){
						Subunit s= ps.protein.subunits.get((site.subunitIndex-1));
						char aa =s.sequence.charAt(site.residueIndex-1);
						String aaCode = ""+aa;
						AminoAcid acid = AminoAcid.parse(aa);
						double aaWt= getSingleAAWeight(aaCode);
						double siteContribution = contribution.getMw() - waterMW - aaWt; //the value from getSingleAAWeight includes the effect of removal of 1 H2O
						// the contribution's mw does not so we substract the mw of water
						Logger.debug(String.format("processing site with subunit %d; residue number %d; AA: %c; name: %s; aa mw: %.2f; net effect of site: %.2f",
									site.subunitIndex, site.residueIndex, aa, acid.getName(), aaWt, siteContribution));
						total += siteContribution;
						contribution.getFormulaMap().keySet().forEach(k->{
							if( formulaCounts.containsKey(k)) {
								//formulaCounts.put(k, (formulaCounts.get(k) +contribution.getFormulaMap().get(k)));
								formulaCounts.get(k).increment(contribution.getFormulaMap().get(k).getAsLong());
							}
							else {
								formulaCounts.put(k, new SingleThreadCounter( contribution.getFormulaMap().get(k).getAsLong()));
							}
						});

						Map<String, SingleThreadCounter> aaContribution = getSingleAAFormula(aaCode);
						//It could be replacing an "X"
						if(aaContribution!=null){
							aaContribution.keySet().forEach(k->{
								if( formulaCounts.containsKey(k)) {
									formulaCounts.get(k).decrement(aaContribution.get(k).getAsLong());
								}
							});
						}
					}
				}

			}
		}
		else {
			play.Logger.debug("no mods to consider");
		}

		play.Logger.debug("final total: " + total + "counts: " + formulaCounts);
		result = new MolecularWeightAndFormulaContribution(total, ps.substanceClass.toString(), formulaCounts);
		result.setFormula(FormulaInfo.toCanonicalString(makeFormulaFromMap(formulaCounts)));
		return result;
	}

	public static List<Property> getMolWeightProperties(ProteinSubstance ps){
		List<Property> props = new ArrayList<Property>();
		if(ps.properties!=null){
			for(Property p:ps.properties){
				ObjectMapper om = new ObjectMapper();
				JsonNode asJson=om.valueToTree(p);
				//System.out.println(p.type + "\t" + p.name +"\t" + p.propertyType + "\t" + p.value.average +"\t" + asJson);
				if(p.getName() != null && p.getName().startsWith("MOL_WEIGHT")){
					props.add(p);
				}
			}
		}
		return props;
	}

	public static List<Property> getMolFormulaProperties(ProteinSubstance ps){
		List<Property> props = new ArrayList<Property>();
		if(ps.properties!=null){
			for(Property p:ps.properties){
				if(p.getName() != null && p.getName().startsWith(MOLECULAR_FORMULA_PROPERTY_NAME)){
					props.add(p);
				}
			}
		}
		return props;
	}
	public static double roundToSignificantFigures(double num, int n) {
	    if(num == 0) {
	        return 0;
	    }

	    final double d = Math.ceil(Math.log10(num < 0 ? -num: num));
	    final int power = n - (int) d;

	    final double magnitude = Math.pow(10, power);
	    final long shifted = Math.round(num*magnitude);
	    return shifted/magnitude;
	}
	public static Property makeMolWeightProperty(double avg){
		Property p= new Property();
		p.setName("MOL_WEIGHT:NUMBER(CALCULATED)");
		p.setType("amount");
		p.setPropertyType("CHEMICAL");

		Amount amt = new Amount();
		amt.type="ESTIMATED";
		amt.average=roundToSignificantFigures(avg,3);
		amt.units="Da";
		p.setValue(amt);
		
		return p;
	}
	
	public static Property makeMolFormulaProperty(String formula){
		Property p= new Property();
		p.setName(MOLECULAR_FORMULA_PROPERTY_NAME);
		p.setPropertyType("CHEMICAL");

		Amount amt = new Amount();
		amt.type="ESTIMATED";
		amt.nonNumericValue=formula;
		p.setValue(amt);

		return p;
	}
	/**
	 * Return a stream of the sites for a protein, labeled by their residue
	 * @param su
	 * @return
	 */
	public static Stream<Tuple<String,Site>> extractSites(Subunit su){
		if(su.sequence==null)return Stream.empty();
		
		return su.sequence.chars()
		      .mapToObj(i->Character.toString((char)i))
		      .map(Util.toIndexedTuple())
		      .map(t->t.swap())
		      .map(Tuple.vmap(i->{
		    	  Site s = new Site();
		    	  s.residueIndex=i+1;
		    	  s.subunitIndex=su.subunitIndex;
		    	  return s;
		      }));
	}
	
	
	
	/**
	 * Attempts to predict a list of the disulfide links based on the protein subtype.
	 * <p>
	 * Specifically, at present, this will look at the subtype of the protein, and determine if it
	 * is one of the known types. If it is, it uses the most common disulfide pattern found for that
	 * subtype, based on the sequence of cysteine residues found in the subunits, sorted by size, largest
	 * first. So, for example, IGG1 monoclonal antibodies tend to have a disulfide pattern specific to the
	 * order of cyteines, where the first C is linked to the second, the third is linked to the forth, etc ...
	 * </p>
	 * 
	 * <p>
	 * If the subtype does not have a known pattern, then an empty optional is returned.
	 * </p>
	 * 
	 * @param p
	 * @return
	 */
	public static Optional<List<DisulfideLink>> predictDisulfideLinks(Protein p){
		String subtype = p.proteinSubType;
		
		List<int[]> predicted=p.getProteinSubtypes().stream()
		 .map(st->KNOWN_DISULFIDE_PATTERNS.get().get(st))
		 .filter(li->li!=null)
		 .findFirst()
		 .orElse(null);

		if(predicted==null)return Optional.empty();
		
		
		//First, get out the cystiene sites, in order.
		
		List<Tuple<Integer,Site>> cSites=p.subunits.stream()
		           .sorted(Comparator.comparing(su->-su.getLength()))
		           .flatMap(su->extractSites(su))
		           .filter(t->"C".equalsIgnoreCase(t.k()))
		           .map(Util.toIndexedTuple())
		           .map(Tuple.vmap(t->t.v()))
		           .collect(Collectors.toList());
		
		return Optional.of(predicted.stream()
		         .map(s->{
		        	 DisulfideLink dl=new DisulfideLink();
		        	 List<Site> sites = new ArrayList<Site>();
		        	 sites.add( cSites.get(s[0]).v());
		        	 sites.add( cSites.get(s[1]).v());
		        	
		        	 dl.setSites(sites);
		        	 return dl;
		         })
		         .collect(Collectors.toList()));
	}
	
	private static MolecularWeightAndFormulaContribution getContributionForID(String uuidInput) {
		Logger.debug("in getContributionForID with input " + uuidInput);
		UUID uuid = UUID.fromString(uuidInput);
		Substance referencedSubstance =SubstanceFactory.getSubstance(uuid);
		if(referencedSubstance != null) {
			System.out.println("Found reference");
			Double mw = null;
			String formula = null;
			if( referencedSubstance.substanceClass.equals(SubstanceClass.chemical)) {
				System.out.println("It's a chemical");
				ChemicalSubstance chemical = (ChemicalSubstance) referencedSubstance;
				mw=chemical.structure.getMwt();
				formula = chemical.structure.getFormula();

			}else {
				//TODO: This currently only considers the average number, but that's
				// not ideal since modifications may have ranges. However,
				// propagation of errors is a concern here, and would need a more
				// robust handling.
				Logger.debug("other than chemical; looking at properties");
				for(Property property : referencedSubstance.properties) {
					if( property.getName() != null && property.getName().startsWith("MOL_WEIGHT") && property.getValue() != null) {
						if( property.getValue().average != null) {
							mw = property.getValue().average;
							break;
						}
					}
				}
}
			if( mw!=null) {
				return new MolecularWeightAndFormulaContribution( mw, referencedSubstance.substanceClass.name(), formula);
			}
		}
		return null;
	}




	public static String makeFormulaFromMap(Map<String, SingleThreadCounter> map) {
    if( map.isEmpty() ) {
      Logger.trace("empty map in makeFormulaFromMap");
      return "";
    }

		StringBuilder formula = new StringBuilder();
		List<String> symbols = new ArrayList<>();
		symbols.addAll(Arrays.asList("C", "H", "O", "N"));

		map.keySet().forEach(k->{
			if( !symbols.contains(k) ){
				symbols.add(k);
			}
		});
		symbols.forEach(s->{
			formula.append(s);
			if(map.containsKey(s) && map.get(s).getAsInt()>1) {
				formula.append(map.get(s).getAsInt());
			}
			//formula.append(" ");
		});
		return formula.toString().trim();
	}


	public static void removeWater(Map<String, SingleThreadCounter> formulaInfo ) {
		if(formulaInfo.containsKey("H") && formulaInfo.containsKey("O")) {
			formulaInfo.get("H").decrement(2);
			formulaInfo.get("O").decrement(1);
		}
	}
}


