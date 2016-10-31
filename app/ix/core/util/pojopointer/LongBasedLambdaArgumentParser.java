package ix.core.util.pojopointer;

import java.util.Objects;
import java.util.function.Function;

public class LongBasedLambdaArgumentParser<T extends PojoPointer> extends LambdaArgumentParser<T>{
	Function<Long,T> fun;
	public LongBasedLambdaArgumentParser(final String key, final Function<Long,T> fun){
		super(key);
		Objects.requireNonNull(fun);
		this.fun=fun;
	}

	@Override
	public T parse(final String t) {
		final Long l=Long.parseLong(t);
		return this.fun.apply(l);
	}
	

	public static <T extends PojoPointer> LongBasedLambdaArgumentParser<T> of(String key, final Function<Long,T> fun){
		return new LongBasedLambdaArgumentParser<T>(key,fun);
	}
}