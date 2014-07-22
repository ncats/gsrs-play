package models.core;

import play.db.ebean.*;
import javax.persistence.*;


@Entity
@Table(name="ct_payload")
public class Payload extends Model {
    @Id
    public Long id;

    @Column(length=1024)
    public String name;

    @Column(length=40)
    public String sha1;

    @Column(length=128)
    public String mimeType;

    public Long size;

    public Payload () {}
}
