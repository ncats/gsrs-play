package ix.core.util.pojopointer.extensions;

import java.util.Optional;
import java.util.function.BiFunction;

import gov.nih.ncats.molwitch.Chemical;
import gov.nih.ncats.molwitch.inchi.Inchi;
import ix.core.models.Structure;
import ix.core.util.pojopointer.LambdaArgumentParser;
import ix.core.util.pojopointer.LambdaPath;
import ix.core.util.pojopointer.extensions.InChIFullRegisteredFunction.InChIFullPath;

public class InChIFullRegisteredFunction implements RegisteredFunction<InChIFullPath, Structure, String> {
    public static String name = "$inchi";

    public static class InChIFullPath extends LambdaPath{
        @Override
        protected String thisURIPath() {
            return name + "()";
        }
    }

    @Override
    public Class<InChIFullPath> getFunctionClass() {
        return InChIFullPath.class;
    }

    @Override
    public LambdaArgumentParser<InChIFullPath> getFunctionURIParser() {
        return LambdaArgumentParser.NO_ARGUMENT_PARSER(name, ()->new InChIFullPath());
    }

    @Override
    public BiFunction<InChIFullPath, Structure, Optional<String>> getOperation() {
        return (fp, s)->{
            try{
                return Optional.of(Inchi.asStdInchi(Chemical.parse(s.molfile), true).getInchi());
            }catch(Exception e){
                return Optional.empty();
            }
        };
    }
}
