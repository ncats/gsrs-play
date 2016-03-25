package ix.core.models;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

import ix.core.controllers.EntityFactory.EntityMapper;
import ix.utils.Util;

@Entity
@Table(name="ix_core_backup")
public class BackupEntity extends IxModel{
	
	@Id
	private Long id;
	
	@Column(unique = true)
	private String refid;
	private String kind;
	
	@Lob
	private String json;
	
	private String sha1;
	
	
	public Class<?> getKind(){
		try {
			return Class.forName(kind);
		} catch (ClassNotFoundException e) {
			return null;
		}
	}
	private InputStream asStream(){
		InputStream stream = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));
		return stream;
	}
	
	@JsonIgnore
	public Object getInstantiated() throws Exception{
		Class<?> cls= getKind();
		if(cls==null){
			throw new IllegalStateException("Kind is not set for object");
		}
		EntityMapper em = EntityMapper.FULL_ENTITY_MAPPER();
		Object inst=em.readValue(asStream(), cls);
		return inst;
	}
	@JsonIgnore
	public void setInstantiated(BaseModel o){
		kind=o.getClass().getName();
		refid=o.fetchIdAsString();
		EntityMapper em = EntityMapper.FULL_ENTITY_MAPPER();
		json=em.toJson(o);
		sha1=Util.sha1(json);
	}
	
	public boolean matchesHash(){
		String sha1=Util.sha1(json);
		return this.sha1.equals(sha1);
	}
}
