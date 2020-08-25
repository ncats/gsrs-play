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

import ix.core.search.text.TextIndexer.Facet.Comparators;
import ix.core.util.CachedSupplier;
import ix.ginas.models.v1.Amount;
import ix.ginas.models.v1.DisulfideLink;
import ix.ginas.models.v1.Property;
import ix.ginas.models.v1.Protein;
import ix.ginas.models.v1.ProteinSubstance;
import ix.ginas.models.v1.Site;
import ix.ginas.models.v1.Subunit;
import ix.utils.Tuple;
import ix.utils.Util;

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
	private static String lookup=
			"A	71.09\n" + 
			"R	156.19\n" + 
			"D	114.11\n" + 
			"N	115.09\n" + 
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
	static Map<String, Double> weights = new HashMap<String,Double>();
	
	static{
		for(String line:lookup.split("\n")){
			String[] cols=line.split("\t");
			weights.put(cols[0],Double.parseDouble(cols[1]));
		}
	}
	
	public static double getSingleAAWeight(String c){
		Double d=weights.get(c.toUpperCase());
		if(d==null)return 0;
		return d;
	}
	public static double getSubunitWeight(Subunit sub, Set<String> unknownRes){
		//start with extra water for end groups
		if(unknownRes==null)unknownRes=new LinkedHashSet<String>();
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
	public static double generateProteinWeight(ProteinSubstance ps, Set<String> unknownRes){
		double total=0;
		if(unknownRes==null)unknownRes=new LinkedHashSet<String>();
		for(Subunit su:ps.protein.subunits){
			total+=getSubunitWeight(su,unknownRes);
		}
		return total;
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
	
}
