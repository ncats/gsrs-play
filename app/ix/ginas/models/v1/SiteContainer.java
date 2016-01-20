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

import ix.ginas.models.GinasCommonSubData;

@Entity
@Table(name="ix_ginas_site_lob")
public class SiteContainer extends GinasCommonSubData{
	@Lob
	@JsonIgnore
	String _shortHand;
	
	@Lob
	@JsonIgnore
	String _siteClob;
	
	String siteType;
	
	public SiteContainer(String type){
		this.siteType=type;
	}
	
	public List<Site> getSites(){
		ObjectMapper om = new ObjectMapper();
		om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		List<Site> sites=new ArrayList<Site>();
		try {
			sites = om.readValue(_siteClob, new TypeReference<List<Site>>(){});
		} catch (Exception e) {
			e.printStackTrace();
		} 
		return sites;
	}
	
	public String getShorthand(){
		return _shortHand;
	}
	public void setShorthand(String shorthand){
		this._shortHand=shorthand;
	}
	public void setSites(List<Site> sites){
		ObjectMapper om = new ObjectMapper();
		_siteClob=om.valueToTree(sites).toString();
	}
}
