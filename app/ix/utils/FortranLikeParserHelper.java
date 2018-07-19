package ix.utils;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * This is a utility class that can help parse/standardize certain lines
 * of text, in particular those that appear in molfiles, to be more
 * in line with how a FORTRAN parser might parse that text. This is 
 * useful for pulling out certain information directly from a molfile/
 * similar format, or for standardizing a non-standardized formatting
 * of a given line in a molfile (as sometimes happens in a few libraries
 * supporting GSRS).
 * 
 * @author tyler
 *
 */
public class FortranLikeParserHelper {

	public static class ParsedSection{
		private String form;
		private String value="";
		
		public ParsedSection(String form, String value){
			this.form=form;
			this.value=value;
		}
		public String getValueRaw(){
			return this.value;
		}
		public String getValueTrimmed(){
			return value.trim();
		}
		public String getForm(){
			return form;
		}
		/**
		 * Returns a left space-padded string of the length
		 * specified by the format.
		 * @return
		 */
		public String getStandardForm(){
			String val = getValueTrimmed();
			String pad = IntStream.range(0, form.length() - val.length())
			         .mapToObj(i->" ")
			         .collect(Collectors.joining());
			return pad+val;
		}
	}
	
	/**
	 * 
	 * @author tyler
	 *
	 */
	public static class LineParser{
		private String format;
		private LinkedHashMap<String,SubsectionParser> subParsers = new LinkedHashMap<String,SubsectionParser>();
		

		public LineParser(String form){
			this.format=form;
			
			
			char[] arr=format.toCharArray();
			char p=arr[0];
			int start=0;
			for(int i=1;i<arr.length;i++){
				char c=arr[i];
				if(c!=p){
					add(new SubsectionParser(p,start,i));
					start=i;
					p=c;
				}
			}
			add(new SubsectionParser(p,start,form.length()));
		}
		
		private LineParser add(SubsectionParser sparser){
			subParsers.put(sparser.form,sparser);
			return this;
		}
		
		
		public String standardize(String line){
			return standardize(parse(line));
		}
		
		public String standardize(Map<String,ParsedSection> sections){
			return subParsers.entrySet()
					  .stream()
			          .map(t->t.getValue().form)
			          .map(t->sections.getOrDefault(t, new ParsedSection(t,"")))			          
			          .map(p->p.getStandardForm())
			          .collect(Collectors.joining());
		}
		
		public Map<String,ParsedSection> parse(String line){
			return subParsers.values().stream()
			                  .map(p->p.parse(line))
			                  .filter(p->p.isPresent())
			                  .map(p->p.get())
			                  .collect(Collectors.toMap(p->p.getForm(), p->p, (a,b)->a, ()->new LinkedHashMap<>()));
		}
		
		public Optional<ParsedSection> parseOnly(String line, String piece){
			return Optional.ofNullable(subParsers.get(piece))
					       .map(p->p.parse(line))
					       .filter(p->p.isPresent())
					       .map(p->p.get());
		}
		

		private static class SubsectionParser{
			String form;
			int start;
			int end;
			public SubsectionParser(char c,int start, int end){
				this.start=start;
				this.end=end;
				this.form=IntStream.range(start, end)
				         .mapToObj(i->c+"")
				         .collect(Collectors.joining());
			}
			public Optional<ParsedSection> parse(String line){
				if(start>=line.length())return Optional.empty();
				String val = line.substring(start, Math.min(end,line.length()));
				return Optional.of(new ParsedSection(form,val));
			}
		}
	}
	
	/**
	 * Creates a {@link LineParser} that will be able to parse
	 * lines of the specified format. Specifically, it expects
	 * a format for a line that looks something like this:
	 * 
	 * <pre>
	 * aaabbblllfffcccsssxxxrrrpppiiimmmvvvvvv
	 * </pre>
	 * 
	 * Where every repeated char means that the section extends.
	 * 
	 * For example, the above format parsing the string:
	 * 
	 * <pre>
	 * 123999001
	 * <pre>
	 * 
	 * Would mean that "bbb" is "999" in this case. The {@link LineParser}
	 * will allow certain trimming/standardization of this parsing
	 * into a format maximally compatible with a right-aligned
	 * parsing style common in older molfile parsers.
	 * 
	 * @param form
	 * @return
	 */
	public static LineParser createParserOfForm(String form){
		return new LineParser(form);
	}
}
