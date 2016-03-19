package ix.core.models;

import play.db.ebean.Model;
import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.annotation.JsonProperty;
import ix.utils.Global;

@Entity
@Table(name="ix_core_pubauthor")
public class PubAuthor extends BaseModel {
    @Id
    public Long id;

    public int position;
    public boolean isLast;
    // corresponding author?
    public Boolean correspondence;

    @OneToOne(cascade=CascadeType.ALL)
    @Basic(fetch=FetchType.EAGER)
    public Author author;

    public PubAuthor () {}
    public PubAuthor (int position, Author author) {
        this (position, false, author);
    }
    public PubAuthor (int position, boolean isLast, Author author) {
        this.position = position;
        this.isLast = isLast;
        this.author = author;
    }
}
