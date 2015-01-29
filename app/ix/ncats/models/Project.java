package ix.ncats.models;

import play.db.ebean.Model;

import javax.persistence.*;

import java.util.List;
import java.util.ArrayList;

import ix.core.models.Author;
import ix.core.models.Indexable;
import ix.core.models.Keyword;
import ix.core.models.Figure;
import ix.core.models.Value;
import ix.core.models.Event;
import ix.core.models.Curation;
import ix.core.models.Publication;
import ix.core.models.Indexable;

@Entity
@Table(name="ix_ncats_project")
public class Project extends Model {
    @Id
    public Long id;

    @Column(length=2048)
    @Indexable(suggest=true,name="Project")
    public String title;
    @Lob
    public String objective;
    @Lob
    public String scope;
    @Lob
    public String opportunities; // collaboration opportunities
    public String team;
    public boolean isPublic = true;

    @ManyToOne(cascade=CascadeType.ALL)
    public Curation curation;

    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="ix_ncats_project_program")
    public List<Program> programs = new ArrayList<Program>();

    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="ix_ncats_project_keyword")
    public List<Keyword> keywords = new ArrayList<Keyword>();

    /*
    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="ix_ncats_project_annotation")
    public List<Value> annotations = new ArrayList<Value>();
    */

    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="ix_ncats_project_member")
    public List<Employee> members = new ArrayList<Employee>();

    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="ix_ncats_project_collaborator")
    public List<Author> collaborators = new ArrayList<Author>();

    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="ix_ncats_project_figure")
    public List<Figure> figures = new ArrayList<Figure>();

    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="ix_ncats_project_milestone")
    public List<Event> milestones = new ArrayList<Event>();

    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="ix_ncats_project_publication")
    public List<Publication> publications = new ArrayList<Publication>();
    
    public Project () {}
    public Project (String title) {
        this.title = title;
    }
}
