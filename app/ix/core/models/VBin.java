package ix.core.models;

import javax.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@DiscriminatorValue("BIN")
public class VBin extends Value {
    @JsonIgnore
    @Indexable(indexed=false)
    @Lob
    public byte[] data;
    public int size;
    public String cksum;
    @Column(length=32)
    public String mimeType;

    public VBin () {}
    public VBin (String label, byte[] data) {
        super (label);
        this.data = data;
        this.size = data.length;
    }
}
