package models.core;

import play.db.ebean.Model;
import javax.persistence.*;

@Entity
public class Author extends Model {
    @Id
    public Long id; // internal id

    public String fullname;
    public String lastname;
    public String firstname;

    public String affiliation;

    public Author () {}
}
