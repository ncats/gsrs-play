package ix.core.search.text;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import ix.core.search.text.TextIndexer.FV;
import ix.core.search.text.TextIndexer.Facet;

public class FacetMeta {

	public Integer getFtotal() {
        return ftotal;
    }

    public int getFdim() {
        return fdim;
    }
    
    public String getFacetName() {
        return name;
    }

    public int getFskip() {
        return fskip;
    }

    public int getFcount() {
        return fcount;
    }

    public String getFfilter() {
        return ffilter;
    }

    public List<FV> getContent() {
        return content;
    }

    public String getUri() {
        return uri;
    }

    private final Integer ftotal;
    private final int fdim;
	private final int fskip;
	private final int fcount;

	private final String ffilter;
	private final String name;
	private final List<FV> content;

	private final String uri;

	@JsonIgnore
	public int getEffectiveTotal() {
		if (this.ftotal != null) {
			return this.ftotal;
		} else {
			return Integer.MAX_VALUE;
		}
	}

	public String getNextPageUri() {
		if (this.fskip + fdim >= this.getEffectiveTotal()) {
			return null;
		}
		Integer skip = this.fskip;
		String nskip = "fskip=" + (skip + fdim);
		String nuri = (uri + "").replace("fskip=" + this.fskip, nskip);
		if (!uri.contains("fskip=" + skip)) {
			if (nuri.contains("?")) {
				nuri = nuri + "&" + nskip;
			} else {
				nuri = nuri + "?" + nskip;
			}
		}
		return nuri;
	}

	public String getPreviousPageUri() {
		if (this.fskip - fdim < 0) {
			return null;
		}
		Integer skip = this.fskip;
		String nskip = "fskip=" + Math.max((skip - fdim), 0);
		String nuri = (uri + "").replace("fskip=" + skip, nskip);
		if (!uri.contains("fskip=" + skip)) {
			if (nuri.contains("?")) {
				nuri = nuri + "&" + nskip;
			} else {
				nuri = nuri + "?" + nskip;
			}
		}
		return nuri;
	}

	public static class Builder {
		private Integer ftotal;
		private int fdim;
		private int fskip;
		private String ffilter;
		private Facet facet;
		private String uri;

		public Builder ftotal(Integer ftotal) {
			this.ftotal = ftotal;
			return this;
		}

		public Builder fdim(int fdim) {
			this.fdim = fdim;
			return this;
		}

		public Builder fskip(int fskip) {
			this.fskip = fskip;
			return this;
		}

		public Builder ffilter(String ffilter) {
			this.ffilter = ffilter;
			return this;
		}

		public Builder facets(Facet facets) {
			this.facet = facets;
			return this;
		}

		public Builder uri(String uri) {
			this.uri = uri;
			return this;
		}

		public FacetMeta build() {
			return new FacetMeta(this);
		}
	}

	private FacetMeta(Builder builder) {
		this.ftotal = builder.ftotal;
		this.fdim = builder.fdim;
		this.fskip = builder.fskip;
		this.fcount = builder.facet.size();
		this.ffilter = builder.ffilter;
		this.content = builder.facet.getValues();
		this.name=builder.facet.getName();
		this.uri = builder.uri;
	}
}