package models.core;

import java.util.*;
import play.db.ebean.*;
import javax.persistence.*;

@Entity
public class Publication extends Model {
    @Id
    public Long id; // internal id

    public Long pmid; // pubmed id
    public Long pmcid; // pubmed central id

    @Column(length=1024)
    public String title; // publication title
    public Integer year;
    public String pages;
    public String doi;

    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="publication_keyword")
    public List<Keyword> keywords = new ArrayList<Keyword>();

    @Column(length=4000)
    public String abstractText;

    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="publication_author")
    public List<Author> authors = new ArrayList<Author>();


    /**
     * journal information
     */
    public String journal;
    public Integer volume;
    public Integer issue;
    @Column(length=10)
    public String issn;

    public Publication () {}
}
