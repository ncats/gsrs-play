package ix.ginas.models.v1;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import ix.core.util.ModelUtils;
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

	public static List<Site> parseShorthandRanges(String srsdisulf){
		return ModelUtils.parseShorthandRanges(srsdisulf);
	}
	
	public static String generateShorthand(List<Site> sites) {
		return ModelUtils.shorthandNotationFor(sites);
	}
	
}
