package ix.core.models;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;

import play.db.ebean.*;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.annotation.JsonProperty;

import ix.utils.Global;

@Entity
@Table(name="ix_core_procrec")
public class ProcessingRecord extends LongBaseModel {
    public enum Status {
        OK, FAILED, PENDING, UNKNOWN, ADAPTED
    }

    @Id
    public Long id;
    @Column(name="rec_start")
    public Long start;
    @Column(name="rec_stop")
    public Long stop;

    @Column(length=128)
    public String name;
    
    @ManyToMany
    @JoinTable(name="ix_core_procrec_prop")
    public List<Value> properties = new ArrayList<Value>();
    
    
    @Version
    public Timestamp lastUpdate; // here
    
    
    /**
     * record status
     */
    public Status status = Status.PENDING;

    /**
     * detailed status message
     */
    @Lob
    @Basic(fetch=FetchType.EAGER)
    public String message;

    @OneToOne(cascade=CascadeType.ALL)
    public XRef xref;
    
    @ManyToOne(cascade = CascadeType.PERSIST)
    @JsonView(BeanViews.Full.class)
    public ProcessingJob job;

    public ProcessingRecord () {}
}
