package ix.core.models;

import play.db.ebean.Model;
import javax.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name="ix_core_figure")
@Inheritance
@DiscriminatorValue("FIG")
public class Figure extends Model {
    @Id
    public Long id; // internal id

    public String caption;
    public String mimeType;
    @Column(length=1024)
    public String url;
    @Lob
    @JsonIgnore
    public byte[] data;
    public int size;
    @Column(length=140)
    public String sha1;

    public Figure () {}
    public Figure (String caption) {
        this.caption = caption;
    }
}
