package ix.ginas.indexers;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import ix.core.search.text.IndexValueMaker;
import ix.core.search.text.IndexableValue;
import ix.core.util.CachedSupplier;
import ix.ginas.models.v1.Substance;

/**
 * Created by VenkataSaiRa.Chavali on 6/23/2017.
 */
public class INNStemIndexValueMaker implements IndexValueMaker<Substance> {
	
	private static class INNStem{
		private Pattern pattern;
		
		private String type;
		
		
		public INNStem(String regex, String type){
			this.pattern=Pattern.compile(regex);
			this.type=type;
		}
		
		public boolean matches(String name){
			return pattern.matcher(name).matches();
		}
		
		public String getType(){
			return this.type;
		}
		
	}
	
	private static CachedSupplier<List<INNStem>> stems = CachedSupplier.of(()->{
		String list=".*vir	Antiviral drug(-vir)\n" + 
		".*cillin	Penicillin-derived antibiotics(-cillin)\n" + 
		"cef.*	Cephem-type antibiotics(cef-)\n" + 
		".*mab	Monoclonal antibodies(-mab)\n" + 
		".*ximab	Chimeric antibody that responds to more than one antigen(-ximab)\n" + 
		".*zumab	Humanized antibody(-zumab)\n" + 
		".*tinib	Tyrosine-kinase inhibitors(-tinib)\n" + 
		".*vastatin	HMG-CoA reductase inhibitor(-vastatin)\n" + 
		".*prazole	Proton-pump inhibitor(-prazole)\n" + 
		".*lukast	Leukotriene receptor antagonists(-lukast)\n" + 
		".*grel.*	Platelet aggregation inhibitor(-grel-)\n" + 
		".*axine	Dopamine and serotonin–norepinephrine reuptake inhibitor(-axine)\n" + 
		".*olol	Beta-blockers(-olol)\n" + 
		".*oxetine	Antidepressant related to fluoxetine(-oxetine)\n" + 
		".*sartan	Angiotensin receptor antagonists(-sartan)\n" + 
		".*pril	Angiotensin converting enzyme inhibitor(-pril)\n" + 
		".*oxacin	Quinolone-derived antibiotics(-oxacin)\n" + 
		".*barb.*	Barbiturates(-barb-)\n" + 
		".*xaban	Direct Xa inhibitor(-xaban)\n" + 
		".*afil	Inhibitor of PDE5 with vasodilator action(-afil)\n" + 
		".*prost.*	Prostaglandin analogue(-prost-)\n" + 
		".*ine	Chemical substance(-ine)\n" + 
		".*parib	PARP inhibitor(-parib)\n" + 
		".*tide	Peptides and glycopeptides(-tide)\n" + 
		".*vec	Gene Therapy vectors(-vec)";
		
		return Arrays.stream(list.split("\n"))
		      .map(n->n.split("\t"))
		      .map(n->new INNStem(n[0],n[1]))
		      .collect(Collectors.toList());
		    
		
	});
	
	
	@Override
	public void createIndexableValues(Substance substance,
			Consumer<IndexableValue> consumer) {

		
		Set<String> innOrUsanReferences = substance.references.stream()
				                                    .filter(r->r.citation.contains("[INN]") || r.citation.contains("[USAN]"))
				                                    
				                                    .map(r->r.getUuid().toString())
				                                    .collect(Collectors.toSet());
		
		if (substance.names != null) {
			List<String> namesToMatch = substance.names
					                       .stream()
					                       .filter(n->n.languages.stream().map(l->l.term).anyMatch(t->t.equals("en")))
					                       .filter(n->(n.getName().contains("[INN]") ||n.getName().contains("[USAN]")) ||
					                    		       (!n.nameOrgs.isEmpty()) ||
					                    		       n.getReferences().stream()
					                    		        .map(r->r.term)
					                    		        .anyMatch(u->innOrUsanReferences.contains(u))
					                    		   )
					                       
					                       .map(n->n.getName().toLowerCase())
					                       .collect(Collectors.toList());
			
			stems.get()
			     .stream()
			     .filter(p->namesToMatch.stream()
			    		          .anyMatch(n->p.matches(n))
			    		 )
			     .forEach(p->{
			    	 consumer.accept(IndexableValue
								.simpleFacetStringValue("INN Stem", p.getType()));
			     });
		}
	}
}
