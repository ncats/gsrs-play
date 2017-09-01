package ix.ginas.exporters;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import ix.ginas.models.v1.Substance;

public class SPLValidatorXMLExporter implements Exporter<Substance> {
	private final BufferedWriter out;
	private static final String PREAMBLE = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<codeList><unii>\n";
	private static final String POSTAMBLE = "</unii></codeList>";
	
	private AtomicBoolean addedPreamble = new AtomicBoolean(false);
	 
	public SPLValidatorXMLExporter(OutputStream out){
		 Objects.requireNonNull(out);
	     this.out = new BufferedWriter(new OutputStreamWriter(out));	   
	     
	}
	
	
	public SPLValidatorXMLExporter(File outputFile) throws IOException{
        this(new BufferedOutputStream(new FileOutputStream(outputFile)));
    }
	
	private void addPreambleIfNeeded() throws IOException{
		if(!addedPreamble.getAndSet(true)){
			this.out.write(PREAMBLE);
		}
	}
	
	
	@Override
	public void close() throws IOException {
		addPreambleIfNeeded();
		out.write(POSTAMBLE);
		out.close();
	}

	@Override
	public void export(Substance obj) throws IOException {
		addPreambleIfNeeded();
		if(obj.isPublic() && !obj.isDeprecated()){
			if(obj.getApprovalID()!=null || (obj.getParentSubstanceReference()!=null && obj.getParentSubstanceReference().approvalID!=null)){
				String approvalID = obj.getApprovalIDDisplay();
				String temp = "<choice><label>$Name</label><value>" +approvalID + "</value></choice>";
				try{
					obj.names.stream()
				        .filter(n->!n.isDeprecated())
				        .filter(n->n.isDisplayName() || n.preferred)
				        .filter(n->n.isPublic())
				        .map(n->n.getName())
				        .map(n->temp.replace("$Name", encodeXML(n)))
						.forEach(n->{
							
							try {
								out.write(n);
								out.write("\n");
							} catch (IOException e) {
								throw new IllegalStateException(e);
							}
						});
				}catch(IllegalStateException e){
					throw new IOException(e.getCause());
				}
			}
		}
		
	}
	
	
	static String[] lookup = new String[127];
	
	static{
		for(int i=0; i< lookup.length; i++){
			lookup[i] = "" +(char)i;
		}
		lookup['\"'] = "&quot";
		lookup['<'] = "&lt;";
		lookup['>'] = "&gt;";
		lookup['&'] = "&amp;";
		lookup['\''] = "&apos;";
		
	}
	
	private static String encodeXML(String t){
		StringBuilder sb = new StringBuilder();
		
		
		
		for(int i = 0; i < t.length(); i++){
		      char c = t.charAt(i);
		      if(c>0x7e) {
		    	  sb.append("&#"+((int)c)+";");
		      }else{
		    	  sb.append(lookup[c]);
		      }
		   }
		return sb.toString();
	}
}
