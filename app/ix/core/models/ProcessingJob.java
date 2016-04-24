package ix.core.models;

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
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Version;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import ix.core.plugins.GinasRecordProcessorPlugin;
import ix.core.processing.RecordExtractor;
import ix.core.processing.RecordPersister;
import ix.core.processing.RecordTransformer;
import ix.core.stats.Statistics;
import ix.utils.Global;

@Entity
@Table(name="ix_core_procjob")
public class ProcessingJob extends LongBaseModel {
	private static final String EXTRACTOR_KEYWORD = "EXTRACTOR";
	private static final String TRANSFORM_KEYWORD = "TRANSFORM";
	private static final String PERSISTER_KEYWORD = "PERSISTER";
	
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
    
    @Version
    public Long version;
    
    
    public Date lastUpdate; // here

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
    
    @JsonIgnore
    public Date _getStartAsDate(){
    	return new Date(start);
    }
    
    @JsonIgnore
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

    @JsonIgnore
	public RecordPersister getPersister() {
    	RecordPersister rec = RecordPersister
				.getInstanceOfPersister(this
						.getKeyMatching(PERSISTER_KEYWORD));
		return rec;
	}

    @JsonIgnore
	public RecordExtractor getExtractor() {
		RecordExtractor rec = RecordExtractor
				.getInstanceOfExtractor(this
						.getKeyMatching(EXTRACTOR_KEYWORD));
		return rec;
	}

    @JsonIgnore
	public RecordTransformer getTransformer() {
		RecordTransformer rt=RecordExtractor.getInstanceOfExtractor(
				this.getKeyMatching(EXTRACTOR_KEYWORD))
				.getTransformer(this.payload);
		return rt;
	}

    @JsonIgnore
	public void setExtractor(Class extractor) {
		this.keys.add(new Keyword(EXTRACTOR_KEYWORD, extractor.getName()));
	}
    
    @JsonIgnore
	public void setPersister(Class persister) {
		this.keys.add(new Keyword(PERSISTER_KEYWORD, persister.getName()));
	}
    
    @PreUpdate
    @PrePersist
    private void updateTime(){
    	lastUpdate=new Date();
    }
}
