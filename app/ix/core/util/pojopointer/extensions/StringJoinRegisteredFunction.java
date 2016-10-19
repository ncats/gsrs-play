package ix.core.util.pojopointer.extensions;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import ix.core.util.pojopointer.LambdaArgumentParser;
import ix.core.util.pojopointer.SingleElementPath;
import ix.core.util.pojopointer.extensions.StringJoinRegisteredFunction.JoinPath;

public class StringJoinRegisteredFunction implements RegisteredFunction<JoinPath, Collection<String>, String> {
	public static String name = "$join";
	
	public static class JoinPath extends SingleElementPath<String>{
		public JoinPath(String t) {
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
	public Class<JoinPath> getFunctionClass() {
		return JoinPath.class;
	}
	
	@Override
	public LambdaArgumentParser<JoinPath> getFunctionURIParser() {
		return LambdaArgumentParser.SINGLE_STRING_ARGUMENT_PARSER(name, (s)->new JoinPath(s));
	}
	
	@Override
	public BiFunction<JoinPath, Collection<String>, Optional<String>> getOperation() {
		return (fp, s)->{
			try{
				return Optional.of(s.stream().collect(Collectors.joining(fp.getValue())));
			}catch(Exception e){
				return Optional.empty();
			}
		};
	}
}
