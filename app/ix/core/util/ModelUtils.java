package ix.core.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import ix.ginas.models.v1.DisulfideLink;
import ix.ginas.models.v1.Site;

/**
 * Collection of utility methods that are Model-specific,
 * but which can not, or should not exist on the Models
 * themselves.
 * 
 * 
 * @author peryeata
 *
 */
public class ModelUtils {
	/**
	 * Collect a set of Sites as shorthand notation  
	 * @return
	 */
	public static SiteShorthandCollector toShorthand(){
		return new SiteShorthandCollector();
	}
	
	private static class SiteShorthandCollector implements Collector<Site,List<Site>,String>{
		@Override
		public Set<java.util.stream.Collector.Characteristics> characteristics() {
			return new HashSet<java.util.stream.Collector.Characteristics>();
		}
		@Override
		public Supplier<List<Site>> supplier() {
			return ()->new ArrayList<Site>();
		}
		@Override
		public BiConsumer<List<Site>, Site> accumulator() {
			return (ls,s)->{
				ls.add(s);
			};
		}
		@Override
		public BinaryOperator<List<Site>> combiner() {
			return (l1,l2)->{
				l1.addAll(l2);
				return l1;
			};
		}

		@Override
		public Function<List<Site>, String> finisher() {
			return (l)->{
				return generateSiteShorthand(l.stream());
			};
		}
		
	}
	
	public static String shorthandNotationForLinks(Collection<DisulfideLink> links){
		return links
			.stream()
			.map(dsl->dsl.getLinksShorthand())
			.collect(Collectors.joining(";"));
			//;
	}
	
	/**
	 * Return the shorthand notation for a collection of sites
	 * @param sites
	 * @return
	 */
	public static String shorthandNotationFor(Collection<Site> sites){
		return sites.stream().collect(ModelUtils.toShorthand());
	}
	
	private static String generateSiteShorthand(Stream<Site> slist){
		return slist.collect(Collectors.groupingBy(s->s.subunitIndex))
			.entrySet().stream().map(e->{
				int i = e.getKey();
				List<Site> sl = e.getValue();
				List<int[]> myInts=new ArrayList<>();
				
				sl.stream()
				.map(s->s.residueIndex)
				.sorted()
				.map(s->new int[]{s,s})
				.reduce((ia1,ia2)->{
					if(ia1[1]+1==ia2[0] || ia1[1]==ia2[0]){
						ia1[1]=ia2[0];
						return ia1;
					}
					myInts.add(ia1);
					return ia2;
				}).ifPresent(ia->{
					myInts.add(ia);
				});
				
				return myInts.stream().map(s->{
					if(s[0]==s[1])return i+"_"+s[0];
					return i+"_"+s[0]+"-"+i+"_"+s[1];
				})
				.collect(Collectors.joining(";"));
			}).collect(Collectors.joining(";"));
	}
	
	public static List<Site> parseShorthandAtSubunit(String contents,
			String subunitindex) {
		List<Site> links = new ArrayList<Site>();
		String[] allds = contents.replace(",", ";").split(";");
		for(String p:allds){
			if(!p.trim().equals("")){
				String[] rng = p.trim().split("-");
				if(rng.length>1){
					Site site1 = parseShorthandLinkage(rng[0]);
					Site site2 = parseShorthandLinkage(rng[1]);
					if (site1.subunitIndex != site2.subunitIndex) {
	                    throw new IllegalStateException("INVALID SITE: \"" + rng + "\" is not a valid shorthand for a site range. Must be between the same subunits.");
	                }
	                if (site2.residueIndex <= site1.residueIndex) {
	                	throw new IllegalStateException("INVALID SITE: \"" + rng + "\" is not a valid shorthand for a site range. Second residue index must be greater than first.");
	                }
	                links.add(site1);
	                for (int j = site1.residueIndex + 1; j < site2.residueIndex; j++) {
	                	Site st= new Site();
	                	st.subunitIndex=site1.subunitIndex;
	                	st.residueIndex=j;
	                    links.add(st);
	                }
	                links.add(site2);
				}else{
					try{
						links.add(parseShorthandLinkage(p));
					}catch(Exception e){
						links.add(parseShorthandLinkage(subunitindex + "_" + p));
					}
				}
			}
		}
		return links;
	}
	
	public static List<Site> parseShorthandLinkages(String srsdisulf){
		List<Site> links = new ArrayList<Site>();
		String[] allds = srsdisulf.split(";");
		for(String p:allds){
			if(!p.trim().equals("")){
				links.add(parseShorthandLinkage(p));
			}
		}
		return links;
	}
	public static List<Site> parseShorthandRanges(String srsdisulf){
		return parseShorthandAtSubunit(srsdisulf,null);
	}
	public static Site parseShorthandLinkage(String site){
		try{
			String[] parts = site.trim().split("-");
			int s1= Integer.parseInt(parts[0].split("_")[0]);
			int r1= Integer.parseInt(parts[0].split("_")[1]);
			return new Site(s1,r1);
		}catch(Exception e){
			throw new IllegalStateException("Illegal Residue Site:\"" + site + "\"");
		}
	}
}