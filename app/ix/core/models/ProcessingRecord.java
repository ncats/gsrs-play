package ix.core.models;

import java.util.Date;
import play.db.ebean.*;
import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.annotation.JsonProperty;
import ix.utils.Global;

@Entity
@Table(name="ix_core_procrecord")
public class ProcessingRecord extends Model {
    public enum Status {
        OK, FAILED, PENDING, UNKNOWN
    }

    @Id
    public Long id;
    @Column(name="rec_start")
    public Long start;
    @Column(name="rec_stop")
    public Long stop;

    @Column(length=128)
    public String name;
    
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
    
    @ManyToOne(cascade=CascadeType.ALL)
    @JsonView(BeanViews.Full.class)
    public ProcessingJob job;

    public ProcessingRecord () {}
}
