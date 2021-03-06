package ix.core.models;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

import ix.core.History;
import ix.core.controllers.EntityFactory.EntityMapper;
import ix.core.util.IOUtil;
import ix.utils.Util;

@Entity
@Table(name="ix_core_backup")
@History(store=false)
public class BackupEntity extends IxModel{
	
	public static final EntityMapper em = EntityMapper.INTERNAL_ENTITY_MAPPER();
	
	@Id
	public Long id;
	
	@Column(unique = true)
	private String refid;
	private String kind;
	
	@Lob
    @JsonIgnore
    @Indexable(indexed=false)
    @Basic(fetch=FetchType.LAZY)
    public byte[] data;
//	@Lob
//	private String json;
	
	private String sha1;
	
	private boolean compressed=true;
	
	public BackupEntity(){
		
	}
	public BackupEntity(boolean compressed){
		this.compressed=compressed;
	}
	
	public Class<?> getKind(){
		try {
			return IOUtil.getGinasClassLoader().loadClass(kind);
		} catch (ClassNotFoundException e) {
			return null;
		}
	}
	
	
	@JsonIgnore
	private byte[] getBytes() throws Exception{
		if(compressed){
			return Util.decompress(this.data);
		}else{
			return this.data;
		}
	}
	
	@JsonIgnore
	private void setBytes(byte[] data) throws Exception{
		if(compressed){
			this.data= Util.compress(data);
		}else{
			this.data=data;
		}
	}
	
	
	@JsonIgnore
	public Object getInstantiated() throws Exception{
		Class<?> cls= getKind();
		if(cls==null){
			throw new IllegalStateException("Kind is not set for object");
		}
		
		Object inst=em.readValue(getBytes(), cls);
		return inst;
	}
	
	@JsonIgnore
	public void setInstantiated(BaseModel o) throws Exception{
		kind=o.getClass().getName();
		refid=o.fetchGlobalId();
		String json = em.toJson(o);
		Objects.requireNonNull(json);
		setBytes(json.getBytes(StandardCharsets.UTF_8));
		sha1=Util.sha1(data);
	}
	
	public boolean matchesHash(){
		String sha1=Util.sha1(data);
		return this.sha1.equals(sha1);
	}
	
	public boolean isOfType(Class<?> type){
		Class<?> cls= getKind();
		if(cls==null)return false;
		return type.isAssignableFrom(cls);
	}
}
