package crosstalk.core.models;

import play.db.ebean.Model;
import javax.persistence.*;

@Entity
@Table(name="ct_core_figure")
public class Figure extends Model {
    @Id
    public Long id; // internal id

    public String caption;
    public String mimeType;
    @Column(length=1024)
    public String url;
    @Lob
    public byte[] data;
    public int size;
    @Column(length=140)
    public String sha1;

    public Figure () {}
    public Figure (String caption) {
        this.caption = caption;
    }
}
