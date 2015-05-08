package ix.core.models;

import javax.persistence.*;

@Entity
@Table(name="ix_core_principal")
@Inheritance
@DiscriminatorValue("PRI")
public class Principal extends IxModel {
    // provider of this principal
    public String provider; 
    
    @Indexable(facet=true,name="Principal")
    public String username;
    public String email;
    public boolean admin = false;

    @Column(length=1024)
    public String uri; // can be email or any unique uri

    @ManyToOne(cascade=CascadeType.ALL)
    public Figure selfie;

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
    public Principal (String username, String email) {
        this.username = username;
        this.email = email;
    }

    public boolean isAdmin () { return admin; }
}
