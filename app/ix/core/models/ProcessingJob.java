package ix.core.models;

import java.io.IOException;
import java.util.*;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import ix.core.controllers.v1.routes;
import ix.core.plugins.GinasRecordProcessorPlugin;
import ix.core.processing.RecordExtractor;
import ix.core.processing.RecordPersister;
import ix.core.processing.RecordTransformer;
import ix.core.stats.Statistics;
import ix.core.util.RestUrlLink;
import ix.core.util.TimeUtil;
import ix.utils.Global;

@Entity
@Table(name="ix_core_procjob")
@Backup
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

    @Transient
    @JsonIgnore
    private Map<String, Keyword> keywordLabelMap;

    @Transient
    @JsonIgnore
    private Map<String, Keyword> keywordTermMap;

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
    @Transient
    @JsonIgnore
    private static ObjectMapper om = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);;

    public ProcessingJob () {
    }
    @JsonView(BeanViews.Compact.class)
    @JsonProperty("_self")
    public RestUrlLink selfUrl () {
        for(Keyword key : keys){

            if(GinasRecordProcessorPlugin.class.getName().equals(key.label)){
                return RestUrlLink.from(routes.LoadController.monitor(key.term));
            }
        }
        return null;
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
        try {
			return om.readValue(om.valueToTree(getStatistics())+"",Map.class);
		} catch (Exception e) {
			e.printStackTrace();
		}
        return null;
    }
    
    public String getKeyMatching(String label){
        populateKeywordMapsIfNeeded();
        Keyword keyword = keywordLabelMap.get(label);
    	return keyword ==null ? null : keyword.getValue();
    }
    public boolean hasKey(String term){
        populateKeywordMapsIfNeeded();
        return keywordTermMap.containsKey(term);

    }


    public void addKeyword(Keyword keyword){
        Objects.requireNonNull(keyword);
        populateKeywordMapsIfNeeded();

        keys.add(keyword);
        keywordLabelMap.put(keyword.label, keyword);
        keywordTermMap.put(keyword.term, keyword);

    }

    private void populateKeywordMapsIfNeeded() {
        if(keywordLabelMap !=null) {
            return;
        }
        keywordLabelMap = new HashMap<>();
        keywordTermMap = new HashMap<>();

        for (Keyword k : keys) {
            keywordLabelMap.put(k.label, k);
            keywordTermMap.put(k.term, k);
        }

    }

    //    @JsonView(BeanViews.Compact.class)
//    @JsonProperty("_statistics")
    public Statistics getStatistics(){
    	if(this.statistics!=null){
    		try {
				return om.readValue(statistics, Statistics.class);
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
    
    @JsonIgnore
    public Long _getDurationAsMs(){
    	if(stop!=null){
    		return stop-start;
    	}
    	return TimeUtil.getCurrentTimeMillis()-start;
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
		this.addKeyword(new Keyword(EXTRACTOR_KEYWORD, extractor.getName()));
	}
    
    @JsonIgnore
	public void setPersister(Class persister) {
		this.addKeyword(new Keyword(PERSISTER_KEYWORD, persister.getName()));
	}
    
    @PreUpdate
    @PrePersist
    private void updateTime(){
    	lastUpdate=new Date();
    }
}
