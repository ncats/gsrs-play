package crosstalk.core.models;

import play.db.ebean.Model;
import javax.persistence.*;

@Entity
@Table(name="ct_core_principal")
public class Principal extends Model {

    @Id
    public Long id;

    public String name;
    public String email;
    public boolean admin = false;

    @Column(length=1024)
    public String uri; // can be email or any unique uri

    @Column(length=256,unique=true)
    public String pkey; // private key

    public Principal () {}
    public Principal (boolean admin) {
        this.admin = admin;
    }
    public Principal (String email) {
        this.email = email;
    }
    public Principal (boolean admin, String email) {
        this.admin = admin;
        this.email = email;
    }
    public Principal (String name, String email) {
        this.name = name;
        this.email = email;
    }

    public boolean isAdmin () { return admin; }
}
