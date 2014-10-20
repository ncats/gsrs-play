package ix.core.models;

import java.util.*;
import play.db.ebean.*;
import javax.persistence.*;

@Entity
@Table(name="ix_core_publication")
public class Publication extends Model {
    @Id
    public Long id; // internal id

    @Column(unique=true)
    public Long pmid; // pubmed id
    @Column(unique=true)
    public String pmcid; // pubmed central id

    @Column(length=1024)
    public String title; // publication title
    public String pages;
    public String doi;

    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="ix_core_publication_keyword")
    public List<Keyword> keywords = new ArrayList<Keyword>();

    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="ix_core_publication_mesh")
    public List<Mesh> mesh = new ArrayList<Mesh>();

    @Lob
    @Basic(fetch=FetchType.EAGER)
    public String abstractText;

    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="ix_core_publication_author")
    public List<PubAuthor> authors = new ArrayList<PubAuthor>();
    //public List<Author> authors = new ArrayList<Author>();
        

    @ManyToOne(cascade=CascadeType.ALL)
    public Journal journal;

    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="ix_core_publication_figure")
    public List<Figure> figures = new ArrayList<Figure>();

    public Publication () {}
    public Publication (String title) {
        this.title = title;
    }
}
