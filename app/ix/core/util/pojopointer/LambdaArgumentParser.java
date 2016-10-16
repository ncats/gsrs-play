package ix.core.util.pojopointer;

import java.util.Objects;
import java.util.function.Function;

public abstract class LambdaArgumentParser implements Function<String, PojoPointer>{
	private final String key;
	public LambdaArgumentParser(final String key){
		Objects.requireNonNull(key);
		this.key=key;
	}

	@Override
	public PojoPointer apply(String t) {
		t=t.substring(this.key.length()+1,t.length()-1);
		return parse(t);
	}

	public String getKey(){
		return this.key;
	}

	public abstract PojoPointer parse(String t);
}