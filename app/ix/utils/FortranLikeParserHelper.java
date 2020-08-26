package ix.utils;

import java.util.Arrays;
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

			int padSize = form.length() - val.length();
			if(padSize >0){

				char[] padd = new char[padSize];
				Arrays.fill(padd, ' ');
				return new StringBuilder(form.length())
						.append(padd)
						.append(val)
						.toString();

			}
			return val;

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
			int cursor=1;
			for(int i=1;i<arr.length;i++){
				char c=arr[i];
				if(c!=p){
					add(new SubsectionParser(p,start,cursor));
					start=cursor;
					p=c;
				}
				cursor++;
			}
			add(new SubsectionParser(p,start,form.length()));
		}
		
		@Override
		public String toString(){
			return format;
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
		
		public class ParsedOperation{
			private Map<String,ParsedSection> sections;
			public ParsedOperation(Map<String,ParsedSection> sections){
				this.sections=sections;
			}
			public String toLine(){
				return standardize(sections);
			}
			public ParsedOperation remove(String key){
				sections.remove(key);
				return this;
			}
			public ParsedOperation set(String key, String val){
				ParsedSection ps=sections.computeIfAbsent(key,k->new ParsedSection(key, val.trim()));
				ps.value=val.trim();
				return this;
			}
			public int getAsInt(String key) {
				ParsedSection m=sections.get(key);
				if(m==null){
					return 0;
				}
				String valueTrimmed = m.getValueTrimmed();
				if(valueTrimmed.isEmpty()){
					return 0;
				}
				return Integer.parseInt(valueTrimmed);
			}
			public String get(String key){
				ParsedSection m=sections.get(key);
				if(m==null){
					return null;
				}
				return m.getValueTrimmed();
			}
		}
		public ParsedOperation parseAndOperate(String line){
			return new ParsedOperation(parse(line));
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
				char[] array = new char[end-start];
				Arrays.fill(array, c);
				this.form=new String(array);
			}

			public SubsectionParser(String f,int start, int end){
				this.form=f;
				this.start=start;
				this.end=end;
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