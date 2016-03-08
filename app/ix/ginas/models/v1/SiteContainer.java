package ix.ginas.models.v1;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import ix.ginas.models.GinasCommonSubData;

@SuppressWarnings("serial")
@Entity
@Table(name="ix_ginas_site_lob")
public class SiteContainer extends GinasCommonSubData{
	@Lob
	@JsonIgnore
	String sitesShortHand;
	@Lob
	@JsonIgnore
	String sitesJSON;	
	long siteCount;
	String siteType;
	
	public SiteContainer(String type){
		this.siteType=type;
	}
	
	public List<Site> getSites(){
		ObjectMapper om = new ObjectMapper();
		om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		List<Site> sites=new ArrayList<Site>();
		try {
			sites = om.readValue(sitesJSON, new TypeReference<List<Site>>(){});
		} catch (Exception e) {
			e.printStackTrace();
		} 
		return sites;
	}
	
	public String getShorthand(){
		return sitesShortHand;
	}
	public void setShorthand(String shorthand){
		setSites(parseShorthandRanges(shorthand));
	}
	public void setSites(List<Site> sites){
		if(sites!=null){
			sitesShortHand=generateShorthand(sites);
			List<Site> nlist=parseShorthandRanges(sitesShortHand);
			ObjectMapper om = new ObjectMapper();
			sitesJSON=om.valueToTree(nlist).toString();
			siteCount=nlist.size();
		}
	}

	public static String generateShorthand(List<Site> slist){
		List<Site> sitelist = new ArrayList<Site>(new HashSet<Site>(slist));
		Collections.sort(sitelist,new Comparator<Site>(){
			@Override
			public int compare(Site o1, Site o2) {
				int d = -(o2.subunitIndex-o1.subunitIndex);
				if(d!=0) return d;
				d = -(o2.residueIndex-o1.residueIndex);
				return d;
			}});
		StringBuilder sb = new StringBuilder();
		Site lastSite=null;
		Site startSite=null;
		String add=null;
//		int lastsi=-1;
//		int startri=-1;
//		int lastri=-1;
		boolean range=false;
		for(Site s:sitelist){
			add=null;
			//range shorthand
			boolean isNext=false;
			
			if(lastSite!=null){
				if(s.subunitIndex==lastSite.subunitIndex){
					if(s.residueIndex==lastSite.residueIndex+1){
						isNext=true;
					}
				}
			}
			if(isNext){
				if(!range){
					range=true;
					startSite=lastSite;
				}else{
					//do nothing
				}
			}else{
				
				if(range){
					add=startSite.toString() + "-" + lastSite.toString();
					range=false;
				}else{
					if(lastSite!=null){
						add=lastSite.toString();
					}
				}
			}
			if(add!=null){
				if(sb.length()>0){
					sb.append(";");
				}
				sb.append(add);
			}
			lastSite = s;
		}
		if(range){
			add=startSite.toString() + "-" + lastSite.toString();
			range=false;
		}else{
			if(lastSite!=null){
				add=lastSite.toString();
			}
		}
		if(add!=null){
			if(sb.length()>0){
				sb.append(";");
			}
			sb.append(add);
		}
		return sb.toString();
	}
	
	
	public static List<Site> parseShorthandAtSubunit(String contents,
			String subunitindex2) {
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
						links.add(parseShorthandLinkage(subunitindex2 + "_" + p));
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
