package ix.core.util.pojopointer.extensions;

import java.util.Optional;
import java.util.function.BiFunction;

import ix.core.models.Structure;
import ix.core.util.Unchecked;
import ix.core.util.pojopointer.LambdaArgumentParser;
import ix.core.util.pojopointer.LambdaPath;
import ix.core.util.pojopointer.extensions.InChIRegisteredFunction.InChIPath;
import play.Logger;

public class InChIRegisteredFunction implements RegisteredFunction<InChIPath, Structure, String> {
	public static String name = "$inchikey";
	
	public static class InChIPath extends LambdaPath{
		@Override
		protected String thisURIPath() {
			return name + "()";
		}
	}
	
	@Override
	public Class<InChIPath> getFunctionClass() {
		return InChIPath.class;
	}
	
	@Override
	public LambdaArgumentParser<InChIPath> getFunctionURIParser() {
		return LambdaArgumentParser.NO_ARGUMENT_PARSER(name, ()->new InChIPath());
	}
	
	@Override
	public BiFunction<InChIPath, Structure, Optional<String>> getOperation() {
		return (fp, s)->{

			try{
				return Optional.ofNullable(s.getInChIKeyAndThrow());
			}catch(Exception e){
                Logger.error("error computing inchi key of structure ID " + s.id, e);
				throw new RuntimeException("error computing inchi key of structure ID " + s.id, e);
			}
		};
	}
}
