package ix.ginas.exporters;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import ix.ginas.models.v1.Name;
import ix.ginas.models.v1.Substance;
import ix.utils.Tuple;

public class SRSLegacyDictionaryExporter implements Exporter<Substance> {
	private final BufferedWriter out;
	private final PrintWriter pw;
	
	private Map<String,List<String>> approvalIDMap = new ConcurrentHashMap<>();
	 
	public SRSLegacyDictionaryExporter(OutputStream out){
		 Objects.requireNonNull(out);
	     this.out = new BufferedWriter(new OutputStreamWriter(out));     
	     this.pw = new PrintWriter(out);
	}
	
	
	public SRSLegacyDictionaryExporter(File outputFile) throws IOException{
        this(new BufferedOutputStream(new FileOutputStream(outputFile)));
    }
	
	
	
	@Override
	public void close() throws IOException {
		approvalIDMap.entrySet()
		             .stream()
		             .map(Tuple::of)
		             .forEach(t->{
		            	String res=t.v().stream().collect(Collectors.joining("|"));
		            	printN(t.k(),res);
		             });
		pw.flush();
		pw.close();
	}

	@Override
	public void export(Substance obj) throws IOException {
		String subid=obj.uuid.toString();
		String unii=obj.getApprovalIDDisplay();
		
		if(unii==null||unii.contains(" ")){
			unii=null;
		}
		
		List<String> casNums=obj.codes.stream()
		         					  .filter(c->c.codeSystem.equals("CAS"))
		         					  .map(c->c.code)
		         					  .collect(Collectors.toList());
		
		String bdnum = obj.codes.stream()
								.filter(cd->cd.type.equals("PRIMARY"))
				                .filter(cd->cd.codeSystem.equals("BDNUM"))
				                .findFirst()
				                .map(cd->cd.code)
				                .orElse("FAKEBDNUM:" + subid);
		
		List<Tuple<String,String>> names = obj.names.stream()
				                                    .map(n->Tuple.of(n.getName(), getOldNameType(n)))
				                                    .collect(Collectors.toList());
		List<Tuple<String,String>> lookups = new ArrayList<>();
		
		names.stream().forEach(t->{
			lookups.add(Tuple.of(t.v(), t.k()));
		});
		
		
		
		
		
		names.stream()
		     .forEach(t->{
		    	 printN(t.k(),bdnum);
		     });
		
		casNums.stream()
	     .forEach(ct->{
	    	 printN(ct, bdnum);
	     });
		if(unii!=null){
			approvalIDMap.computeIfAbsent(unii, k->new ArrayList<String>())
			     .add(bdnum);
			lookups.add(Tuple.of("UNII",unii));			
		}
		
		lookups.add(Tuple.of("SUBSTID",subid));
		
		printN("SUBSTID|" + subid,bdnum);
		printN(bdnum, lookups.stream().map(l->l.k()+"|" + l.v()).collect(Collectors.joining("\\")));
	}
	
	private String getOldNameType(Name n){
		if(n.displayName){
			return "PT";
		}
		if(n.type.equals("cn")){
			return "SY";
		}
		if(n.type.equals("sys")){
			return "SN";
		}
		if(n.type.equals("bn")){
			return "TR";
		}
		return n.type.toUpperCase();
	}
	
	private void printN(String r, String bdnum){
		pw.println("N\t" + r + "\t" + bdnum);
	}
	
}
