package ix.ginas.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import ix.ginas.models.v1.Amount;
import ix.ginas.models.v1.Property;
import ix.ginas.models.v1.ProteinSubstance;
import ix.ginas.models.v1.Subunit;

public class ProteinUtils {
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
				if(p.getName().startsWith("MOL_WEIGHT")){
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
}
