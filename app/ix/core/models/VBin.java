package ix.core.models;

import java.security.MessageDigest;
import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@DiscriminatorValue("BIN")
public class VBin extends Value {
    @JsonIgnore
    @Indexable(indexed=false)
    @Lob
    public byte[] data;
    @Column(name="data_size")
    public int size;

    @Column(length=40)
    public String sha1;

    @Column(length=32)
    public String mimeType;

    public VBin () {}
    public VBin (String label, byte[] data) {
        super (label);
        this.data = data;
        this.size = data.length;
    }

    @PrePersist
    @PreUpdate
    public void calcChecksum () {
        if (data == null) 
            return;

        try {
            MessageDigest md = MessageDigest.getInstance("SHA1");
            byte[] digest = md.digest(data);
            StringBuilder sb = new StringBuilder ();
            for (int i = 0; i < digest.length; ++i)
                sb.append(String.format("%1$02x", digest[i] & 0xff));
            sha1 = sb.toString();
        }
        catch (Exception ex) {
            throw new RuntimeException (ex);
        }
    }
}
