package ix.core.util.pojopointer;

import java.util.Objects;
import java.util.function.Function;

public class FieldBasedLambdaArgumentParser extends LambdaArgumentParser{
	Function<PojoPointer,PojoPointer> fun;
	public FieldBasedLambdaArgumentParser(final String key, final Function<PojoPointer,PojoPointer> fun){
		super(key);
		Objects.requireNonNull(fun);
		this.fun=fun;
	}
	@Override
	public PojoPointer parse(final String t) {
		//            if(!t.startsWith("/") && t.length()>0 && t.charAt(0)!='(' && t.charAt(0)!=LAMBDA_CHAR){
		//                t="/"+t;
		//            }
		return this.fun.apply(PojoPointer.fromUriPath(t));
	}
}