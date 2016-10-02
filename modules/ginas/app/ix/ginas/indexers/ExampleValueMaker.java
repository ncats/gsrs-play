package ix.ginas.indexers;

import java.util.Arrays;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.google.common.util.concurrent.AtomicDouble;

import ix.core.search.text.IndexValueMaker;
import ix.core.search.text.IndexableValue;
import ix.ginas.models.v1.ChemicalSubstance;
import ix.ginas.models.v1.Relationship;
import ix.ginas.models.v1.Substance;


/**
 * This serves as an example for using an IndexValueMaker for a substance.
 * 
 * Specifically, this class adds the following facets / search terms:
 * 
 * <p><ul>
 * <li>Moiety Type : A flag on whether a substance is an active moiety, or has a link to an active moiety</li> 
 * <li>Relationship Count : Simple ranged count of the total number of relationships</li>
 * <li>Simple Ring Count : Smallest Set of smallest ring count for molecules, using Euler's formula</li>
 * <li>Carbon Mass Ratio : Ratio of carbon mass to the whole molecule. This is a ranged continuous facet.</li>
 * </ul></p>
 * 
 * Note that all of the above are distinct enough that one could have separate
 * IndexValueMaker instances for each calculation. That is typically desirable
 * if there is need for certain calculations to be disabled, and if there is
 * no significant advantage to calculations in having them share the same resource.
 * 
 * 
 * <p>
 * Including this IndexValueMaker will only allow for searching and faceting
 * at some technical level. To display the facets on the UI, additional configuration
 * may be required. 
 * </p>
 * 
 * @author peryeata
 *
 */
public class ExampleValueMaker implements IndexValueMaker<Substance>{

	private static final String MOIETY_TYPE_FACET="Moiety Type";
	private static final String RELATIONSHIP_COUNT_FACET="Relationship Count";
	private static final String SSSR_FACET="Simple Ring Count";
	private static final String CARBON_RATIO="Carbon Mass Ratio";
	
	private static long[] relationshipCountBuckets = new long[]{1,3,5,7};
	private static long[] sssrCount = new long[]{0,1,3,5,7};
	private static double[] carbonRatio = new double[]{0,0.2,0.4,0.6,0.8,1};
	
	
	
	private static final String CHILD_SUBSTANCE_RELATIONSHIP = "CHILD SUBSTANCE";

	//This is the method which does the work
	@Override
	public void createIndexableValues(Substance s, Consumer<IndexableValue> consumer) {
		addMoietyFacet(s,consumer);
		addRelationshipCountFacet(s,consumer);
		addSSSRCountFacet(s,consumer);
		addCarbonNonCarbonWeightRatioFacet(s,consumer);
		
	}
	/**
	 * Calculate the field(s) for the Moiety Type facet
	 * @param s
	 * @param consumer
	 */
	public void addMoietyFacet(Substance s, Consumer<IndexableValue> consumer){

		//Add
		//Look to see if this is its own active moiety
		//If it is, add it
		s.relationships
			.stream()
			.filter(r->r.type.equals(Relationship.ACTIVE_MOIETY_RELATIONSHIP_TYPE)) //only Active Moieties Relationships
			.filter(r->s.getUuid().toString().equals(r.relatedSubstance.refuuid)) //only reflexive relationships
			.findAny().ifPresent(r->{
				//Add a facet for reflexive Active Moiety
				consumer.accept(IndexableValue.simpleFacetStringValue(MOIETY_TYPE_FACET,Relationship.ACTIVE_MOIETY_RELATIONSHIP_TYPE));
			});
		
		//Look to see if this is NOT its own active Moiety 
		s.relationships
		.stream()
		.filter(r->r.type.equals(Relationship.ACTIVE_MOIETY_RELATIONSHIP_TYPE))
		.filter(r->!s.getUuid().toString().equals(r.relatedSubstance.refuuid))
		.findAny().ifPresent(r->{
			//Add a facet for things that link to Active Moiety
			consumer.accept(IndexableValue.simpleFacetStringValue(MOIETY_TYPE_FACET,CHILD_SUBSTANCE_RELATIONSHIP));
		});
	}
	
	/**
	 * Calculate the field(s) for the Relationship Count facet.
	 * 
	 * This facet is a ranged facet
	 * 
	 * @param s
	 * @param consumer
	 */
	public void addRelationshipCountFacet(Substance s, Consumer<IndexableValue> consumer){
		consumer.accept(IndexableValue.simpleFacetLongValue(RELATIONSHIP_COUNT_FACET
				,s.relationships.size()
				,relationshipCountBuckets));
	}
	
	
	
	/**
	 * Calculate the Simple Ring Count for a chemical substance
	 * 
	 * This facet is a ranged facet.
	 * 
	 * @param s
	 * @param consumer
	 */
	public void addSSSRCountFacet(Substance s, Consumer<IndexableValue> consumer){

		if(s instanceof ChemicalSubstance){
			int sssr=s.toChemical().getComponents().stream()
					.map(c->c.getBondCount()-c.getAtomCount()+1)
					.collect(Collectors.summingInt(i->i));
			
			consumer.accept(IndexableValue.simpleFacetLongValue(SSSR_FACET
					,sssr
					,sssrCount));
		}
	}
	
	/**
	 * Calculate the Ratio of SP3 carbons to other carbons
	 * 
	 * <p>
	 * This facet is a ranged facet of doubles.
	 * </p>
	 * 
	 * 
	 * This is probably not a very helpful facet, in general, but serves as a
	 * simple example of a continuous double-valued value that is faceted.
	 * 
	 * 
	 * @param s
	 * @param consumer
	 */
	public void addCarbonNonCarbonWeightRatioFacet(Substance s, Consumer<IndexableValue> consumer){

		if(s instanceof ChemicalSubstance){
			AtomicDouble ctot=new AtomicDouble();
			AtomicDouble tot=new AtomicDouble();
			
			Arrays.stream(s.toChemical()
					.getAtomArray())
					.forEach(ca->{
						double m=ca.getMass();
						if("C".equals(ca.getSymbol())){
							ctot.addAndGet(m);
						}
						tot.addAndGet(m);
					});
			
			consumer.accept(IndexableValue.simpleFacetDoubleValue(CARBON_RATIO
					,ctot.doubleValue()/tot.doubleValue()
					,carbonRatio));
		}
	}
	
	
	
	
	

}
