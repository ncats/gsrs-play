package crosstalk.core.models;

import java.util.*;
import play.db.ebean.*;
import javax.persistence.*;

@Entity
@Table(name="ct_core_publication")
public class Publication extends Model {
    @Id
    public Long id; // internal id

    public Long pmid; // pubmed id
    public Long pmcid; // pubmed central id

    @Column(length=1024)
    public String title; // publication title
    public String pages;
    public String doi;

    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="ct_core_publication_keyword")
    public List<Keyword> keywords = new ArrayList<Keyword>();

    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="ct_core_publication_mesh")
    public List<Mesh> mesh = new ArrayList<Mesh>();

    @Lob
    @Basic(fetch=FetchType.EAGER)
    public String abstractText;

    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="ct_core_publication_author")
    public List<Author> authors = new ArrayList<Author>();

    @ManyToOne(cascade=CascadeType.ALL)
    public Journal journal;

    public Publication () {}
}
