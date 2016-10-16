package ix.core.util.pojopointer;

import java.util.Objects;
import java.util.function.Function;

public class LongBasedLambdaArgumentParser extends LambdaArgumentParser{
	Function<Long,PojoPointer> fun;
	public LongBasedLambdaArgumentParser(final String key, final Function<Long,PojoPointer> fun){
		super(key);
		Objects.requireNonNull(fun);
		this.fun=fun;
	}

	@Override
	public PojoPointer parse(final String t) {
		final Long l=Long.parseLong(t);
		return this.fun.apply(l);
	}
}