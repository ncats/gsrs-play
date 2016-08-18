package ix.ginas.exporters;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Arrays;

public class ExporterBuilder {

	private OutputStream os=null;
	
	private OutputFormat of;
	
	
	public static enum OutputFormat{
		   SDF,
		   JSON_DUMP,
		   CSV,
		   EXCEL;
		   public static OutputFormat getValue(String val){
			   try{
				   return OutputFormat.valueOf(val.toUpperCase());
			   }catch(Exception e){
				   return OutputFormat.SDF;
			   }
		   }
	}
	
	public ExporterBuilder(){
		
	}
	
	public ExporterBuilder(OutputStream os){
		setOutputStream(os);
	}
	
	public ExporterBuilder setOutputStream(OutputStream os){
		this.os=os;
		return this;
	}
	
	public ExporterBuilder setOutputFile(File outfile) throws FileNotFoundException{
		this.os= new FileOutputStream(outfile);
		return this;
	}
	
	public ExporterBuilder setOutputFormat(OutputFormat of){
		this.of=of;
		return this;
	}
	public ExporterBuilder setOutputFormat(String of){
		this.of=OutputFormat.getValue(of);
		return this;
	}
	
	
	public Exporter build(){
		switch(of){
			case SDF:
				return new SdfExporter(os);
			case CSV:
				return new CsvSubstanceExporter(os,"\t",Arrays.asList(CsvSubstanceExporter.Columns.values()));
			case EXCEL:
			case JSON_DUMP:
			default:
				throw new UnsupportedOperationException("Export format:" + of + " not supported at this time");
		}
	}
	
	
	
}
