package ix.core.models;

import ix.core.plugins.GinasRecordProcessorPlugin;
import ix.core.stats.Statistics;
import ix.utils.Global;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import play.Logger;
import play.db.ebean.Model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

@Entity
@Table(name="ix_core_procjob")
public class ProcessingJob extends Model {
    public enum Status {
        COMPLETE, RUNNING, NOT_RUN, FAILED, PENDING, STOPPED, UNKNOWN
    }
    
    @Id
    public Long id;

    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="ix_core_procjob_key")
    public List<Keyword> keys = new ArrayList<Keyword>();

    @Indexable(facet=true, name="Job Status")
    public Status status = Status.PENDING;
    
    @Column(name="job_start")
    public Long start;
    @Column(name="job_stop")
    public Long stop;

    @Lob
    @Basic(fetch=FetchType.EAGER)
    public String message;
    
    @Lob
    @Basic(fetch=FetchType.EAGER)
    @JsonView(BeanViews.Private.class)
    public String statistics;

    @OneToOne(cascade=CascadeType.ALL)
    @JsonView(BeanViews.Full.class)
    public Principal owner;
    
    @OneToOne(cascade=CascadeType.ALL)
    @JsonView(BeanViews.Full.class)
    public Payload payload;
    

    public ProcessingJob () {
    }

    @JsonView(BeanViews.Compact.class)
    @JsonProperty("_payload")
    public String getJsonPayload () {
        return payload != null
            ? Global.getRef(getClass (), id)+"/payload" : null;
    }

    @JsonView(BeanViews.Compact.class)
    @JsonProperty("_owner")
    public String getJsonOwner () {
        return owner != null
            ? Global.getRef(getClass (), id)+"/owner" : null;
    }
    
    @JsonView(BeanViews.Compact.class)
    @JsonProperty("statistics")
    public Map getStatisticsForAPI () {
    	ObjectMapper om = new ObjectMapper();
    	om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        try {
			return om.readValue(om.valueToTree(getStatistics())+"",Map.class);
		} catch (Exception e) {
			e.printStackTrace();
		}
        return null;
    }
    
    public String getKeyMatching(String label){
    	for(Keyword k : keys){
    		if(label.equals(k.label)){
    			return k.getValue();
    		}
    	}
    	return null;
    }
    public boolean hasKey(String term){
    	for(Keyword k : keys){
    		if(term.equals(k.term)){
    			return true;
    		}
    	}
    	return false;
    }
    
//    @JsonView(BeanViews.Compact.class)
//    @JsonProperty("_statistics")
    public Statistics getStatistics(){
    	ObjectMapper om = new ObjectMapper();
    	om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    	if(this.statistics!=null){
    		try {
				Statistics s= om.readValue(statistics, Statistics.class);
				return s;
			} catch (IOException e) {
				e.printStackTrace();
			}
    	}
    	return GinasRecordProcessorPlugin.getStatisticsForJob(this);
    }
    
    public Date _getStartAsDate(){
    	return new Date(start);
    }
    
    public Date _getStoppedAsDate(){
    	if(stop==null)return null;
    	return new Date(stop);
    }
    
    public String getName(){
    	if(payload!=null){
    		return "Import batch file \"" + payload.name + "\"";
    	}else{
    		return "Unnamed Batch";
    	}
    }
}
