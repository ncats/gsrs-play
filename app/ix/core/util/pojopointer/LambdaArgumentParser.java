package ix.core.util.pojopointer;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class LambdaArgumentParser<T extends PojoPointer> implements Function<String, T>{
	private final String key;
	public LambdaArgumentParser(final String key){
		Objects.requireNonNull(key);
		this.key=key;
	}

	@Override
	public T apply(String t) {
		t=t.substring(this.key.length()+1,t.length()-1);
		return parse(t);
	}

	public String getKey(){
		return this.key;
	}
	public abstract T parse(String t);
	
	
	public static <T extends PojoPointer> LambdaArgumentParser<T> NO_ARGUMENT_PARSER(String key, Supplier<T> supplier){
		return new LambdaArgumentParser<T>(key){
			@Override
			public T parse(String t) {
				if(t.length()!=0){
					throw new IllegalArgumentException("Function '" + key + "' does not take any arguments! Found:'" + t +"'");
				}
				return supplier.get();
			}
		};
	}
	
	public static <T extends PojoPointer> LambdaArgumentParser<T> SINGLE_STRING_ARGUMENT_PARSER(String key, Function<String,T> generator){
		return new LambdaArgumentParser<T>(key){
			@Override
			public T parse(String t) {
				return generator.apply(t);
			}
		};
	}
}