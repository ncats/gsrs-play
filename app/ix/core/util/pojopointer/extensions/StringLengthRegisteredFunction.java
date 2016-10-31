package ix.core.util.pojopointer.extensions;

import java.util.Optional;
import java.util.function.BiFunction;

import ix.core.util.pojopointer.LambdaArgumentParser;
import ix.core.util.pojopointer.LambdaPath;
import ix.core.util.pojopointer.extensions.StringLengthRegisteredFunction.StringLengthPath;

public class StringLengthRegisteredFunction implements RegisteredFunction<StringLengthPath, String, Integer> {
	public static String name = "$length";
	
	public static class StringLengthPath extends LambdaPath{
		@Override
		protected String thisURIPath() {
			return name + "()";
		}
	}
	
	@Override
	public Class<StringLengthPath> getFunctionClass() {
		return StringLengthPath.class;
	}
	
	@Override
	public LambdaArgumentParser<StringLengthPath> getFunctionURIParser() {
		return LambdaArgumentParser.NO_ARGUMENT_PARSER(name, ()->new StringLengthPath());
	}
	
	@Override
	public BiFunction<StringLengthPath, String, Optional<Integer>> getOperation() {
		return (fp, s)->{
    		return Optional.of(s.length());
		};
	}
}
