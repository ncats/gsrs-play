package ix.core.models;

import java.util.UUID;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.Lob;
import javax.persistence.Table;

import play.db.ebean.Model;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name="ix_core_filedata")
@Inheritance
@DiscriminatorValue("FIG")
public class FileData extends BaseModel {
    @Id
    public UUID id; // internal id
    public String mimeType;
    
    @Lob
    @JsonIgnore
    @Indexable(indexed=false)
    @Basic(fetch=FetchType.EAGER)
    public byte[] data;

    @Column(name="data_size")
    public long size;
    @Column(length=140)
    public String sha1;

    public FileData () {}

	@Override
	public String fetchIdAsString() {
		if(this.id==null)return null;
		return id.toString();
	}
    
}
