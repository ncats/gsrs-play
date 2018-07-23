package ix.ginas.datasource;

import ix.ginas.models.v1.Code;
import org.apache.commons.lang3.StringUtils;

public class CodeSystemMeta {
	String codeSystem;
	String url;

	public CodeSystemMeta(String cs, String url) {
		this.codeSystem = cs;
		this.url = url;
	}
	
	public void addURL(Code cd){
		String url = generateUrlFor(cd);
		if(url !=null){
			cd.url = url;
		}
	}

	public String generateUrlFor(Code code){
		if(url==null && url.trim().isEmpty()){
			return null;
		}
		return StringUtils.replace(url,"$CODE$", code.code).trim();
	}

}