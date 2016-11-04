package ix.core.util.pojopointer;

import java.util.Objects;
import java.util.function.Function;

public class FieldBasedLambdaArgumentParser<T extends PojoPointer> extends LambdaArgumentParser<T>{
	Function<PojoPointer,T> fun;
	public FieldBasedLambdaArgumentParser(final String key, final Function<PojoPointer,T> fun){
		super(key);
		Objects.requireNonNull(fun);
		this.fun=fun;
	}
	@Override
	public T parse(final String t) {
		return this.fun.apply(PojoPointer.fromURIPath(t));
	}
	
	public static <T extends PojoPointer> FieldBasedLambdaArgumentParser<T> of(String key, final Function<PojoPointer,T> fun){
		return new FieldBasedLambdaArgumentParser<T>(key,fun);
	}
}