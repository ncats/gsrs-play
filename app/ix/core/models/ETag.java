package ix.core.models;

import java.security.SecureRandom;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import ix.core.controllers.RequestOptions;
import ix.core.search.text.TextIndexer.Facet;
import ix.utils.Global;
import ix.utils.Util;
import play.mvc.Controller;
import play.mvc.Http;

@Entity
@Table(name = "ix_core_etag")
@Indexable(indexed = false)
@JsonPropertyOrder(
{ "id", 
  "version", 
  "created", 
  "etag", 
  "path", 
  "uri" , 
  "nextPageUri", 
  "previousPageUri",
  "method",
  "sha1",
  "total",
  "count",
  "skip",
  "top",
  "query",
  "sideway",
  "facets",
  "content"})
public class ETag extends IxModel {

	@Column(length = 16, unique = true)
	public final String etag;

	@Column(length = 4000)
	public String uri;

	public String getNextPageUri() {
		if (this.skip + top >= this.total) {
			return null;
		}
		Integer skip = this.skip;
		String nskip = "skip=" + (skip + top);
		String nuri = (uri + "").replace("skip=" + this.skip, nskip);
		if (!uri.contains("skip=" + skip)) {
			if (nuri.contains("?")) {
				nuri = nuri + "&" + nskip;
			} else {
				nuri = nuri + "?" + nskip;
			}
		}
		return nuri;
	}

	public String getPreviousPageUri() {
		if (this.skip - top < 0) {
			return null;
		}
		Integer skip = this.skip;
		String nskip = "skip=" + Math.max((skip - top), 0);
		String nuri = (uri + "").replace("skip=" + skip, nskip);
		if (!uri.contains("skip=" + skip)) {
			if (nuri.contains("?")) {
				nuri = nuri + "&" + nskip;
			} else {
				nuri = nuri + "?" + nskip;
			}
		}
		return nuri;
	}

	public String path;
	@Column(length = 10)
	public String method;
	@Column(length = 40)
	public String sha1; // SHA1

	public Integer total;
	public Integer count;
	public Integer skip;
	public Integer top;

	public Integer status;

	@Column(length = 2048)
	public String query;
	@Column(length = 4000)
	public String filter;

	public ETag() {
		this(nextETag());
	}

	public ETag(String etag) {
		this.etag = etag;
	}
	
	@JsonIgnore
	public boolean getDeprecated(){
		return this.deprecated;
	}
	
	@JsonIgnore
	public Date getModified(){
		return this.modified;
	}

	static final SecureRandom rand = new SecureRandom();

	public static String nextETag() {
		return nextETag(8);
	}

	public static String nextETag(int size) {
		byte[] buf = new byte[size];
		rand.nextBytes(buf);

		StringBuilder id = new StringBuilder();
		for (int i = 0; i < buf.length; ++i)
			id.append(String.format("%1$02x", buf[i] & 0xff));

		return id.toString();
	}

	@Transient
	@JsonIgnore
	private transient Object content = null;

	@Transient
	@JsonIgnore
	private transient List<Facet> facets = null;

	@Transient
	@JsonIgnore
	private transient List<String> selected = null;

	@Transient
	@JsonIgnore
	private transient boolean sideway = false;

	public static class Builder {
		private String uri;
		private String path;
		private String query;
		private String method;

		private String sha1;

		private Integer total;
		private Integer count;
		private Integer skip;
		private Integer top;
		private Integer status;
		private String filter;

		public Builder fromRequest(Http.Request req) {
			uri = Global.getHost() + req.uri();
			path = req.path();
			query = Util.canonicalizeQuery(req);
			method = req.method();
			return this;
		}

		public Builder options(RequestOptions op) {
			this.top = op.getTop();
			this.skip = op.getSkip();
			this.filter = op.getFilter();
			return this;
		}

		public Builder uri(String uri) {
			this.uri = uri;
			return this;
		}

		public Builder path(String path) {
			this.path = path;
			return this;
		}

		public Builder method(String method) {
			this.method = method;
			return this;
		}

		public Builder sha1(String sha1) {
			this.sha1 = sha1;
			return this;
		}

		public Builder sha1OfRequest(String... params) {
			sha1 = Util.sha1(Controller.request(), params);
			return this;
		}

		public Builder total(Integer total) {
			this.total = total;
			return this;
		}

		/**
		 * This is the set of records currently returned
		 * via a given request. This is in contrast to
		 * {@link #total(Integer)} which is the total
		 * count that would be accessible via paging
		 * on that request
		 * 
		 * @param count
		 * @return
		 */
		public Builder count(Integer count) {
			this.count = count;
			return this;
		}

		public Builder skip(Integer skip) {
			this.skip = skip;
			return this;
		}

		public Builder top(Integer top) {
			this.top = top;
			return this;
		}

		public Builder status(Integer status) {
			this.status = status;
			return this;
		}

		public Builder query(String query) {
			this.query = query;
			return this;
		}

		public Builder filter(String filter) {
			this.filter = filter;
			return this;
		}

		public ETag build() {
			return new ETag(this);
		}
		
		public static Builder begin(){
			return new Builder();
		}
	}

	private ETag(Builder builder) {
		this();
		this.uri = builder.uri;
		this.path = builder.path;
		this.method = builder.method;
		this.sha1 = builder.sha1;
		this.total = builder.total;
		this.count = builder.count;
		this.skip = builder.skip;
		this.top = builder.top;
		this.status = builder.status;
		this.query = builder.query;
		this.filter = builder.filter;
	}

	public void setContent(Object cont) {
		this.content = cont;
	}

	public void setFacets(List<Facet> facets) {
		this.facets = facets;
	}

	public void setSelected(List<String> selected, boolean sideway) {
		this.selected = selected;
		this.sideway=sideway;
	}


	// Maybe make this a link unless full bean view?
	public Object getContent() {
		return this.content;
	}

	// Maybe make this a link unless full bean view?
	public List<Facet> getFacets() {
		return this.facets;
	}

	// Maybe make this a link unless full bean view?
	public List<String> getSideway() {
		if(!sideway)return null;
		return this.selected;
	}
	
	public List<String> getDrilldown() {
		if(sideway)return null;
		return this.selected;
	}

	
	
}
