package ix.ginas.datasource;

import ix.ginas.models.v1.Code;

public class CodeSystemMeta {
	String codeSystem;
	String url;

	public CodeSystemMeta(String cs, String url) {
		this.codeSystem = cs;
		this.url = url;
	}
	
	public void addURL(Code cd){
		if(url!=null && url.trim().length()>=0){
			cd.url = url.replace("$CODE$", cd.code);
		}
	}

}