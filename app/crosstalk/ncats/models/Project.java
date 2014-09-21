package crosstalk.ncats.models;

import play.db.ebean.Model;
import javax.persistence.*;
import java.util.List;
import java.util.ArrayList;

import crosstalk.core.models.Author;
import crosstalk.core.models.Keyword;
import crosstalk.core.models.Figure;
import crosstalk.core.models.Acl;
import crosstalk.core.models.Value;
import crosstalk.core.models.Event;

@Entity
@Table(name="ct_ncats_project")
public class Project extends Model {
    @Id
    public Long id;
    public String title;
    @Lob
    public String description;
    public String team;

    // access control
    @ManyToOne(cascade=CascadeType.ALL)
    public Acl acl;

    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="ct_ncats_project_annotation")
    public List<Value> annotations = new ArrayList<Value>();

    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="ct_ncats_project_member")
    public List<Employee> members = new ArrayList<Employee>();

    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="ct_ncats_project_collaborator")
    public List<Author> collaborators = new ArrayList<Author>();

    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="ct_ncats_project_figure")
    public List<Figure> figures = new ArrayList<Figure>();

    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="ct_ncats_project_milestone")
    public List<Event> milestones = new ArrayList<Event>();
    
    public Project () {}
    public Project (String title) {
        this.title = title;
    }
}
