package ix.core.util.pojopointer.extensions;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;

import ix.core.util.pojopointer.LambdaArgumentParser;
import ix.core.util.pojopointer.SingleElementPath;
import ix.core.util.pojopointer.extensions.StringSplitRegisteredFunction.SplitPath;

public class StringSplitRegisteredFunction implements RegisteredFunction<SplitPath, String, List<String>> {
	public static String name = "$split";
	
	public static class SplitPath extends SingleElementPath<String>{
		public SplitPath(String t) {
			super(t);
		}

		@Override
		protected String thisURIPath() {
			return name + "(" + getValue() + ")";
		}

		@Override
		public String name() {
			return name;
		}
	}
	
	@Override
	public Class<SplitPath> getFunctionClass() {
		return SplitPath.class;
	}
	
	@Override
	public LambdaArgumentParser<SplitPath> getFunctionURIParser() {
		return LambdaArgumentParser.SINGLE_STRING_ARGUMENT_PARSER(name, (s)->new SplitPath(s));
	}
	
	@Override
	public BiFunction<SplitPath, String, Optional<List<String>>> getOperation() {
		return (fp, s)->{
			
			try{
				return Optional.of(Arrays.asList(s.split(fp.getValue())));
			}catch(Exception e){
				return Optional.empty();
			}
		};
	}
}
