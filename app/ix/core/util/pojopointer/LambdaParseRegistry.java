package ix.core.util.pojopointer;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import ix.core.util.CachedSupplier;

public class LambdaParseRegistry{
	static CachedSupplier<Map<String,Function<String,PojoPointer>>> subURIparsers= CachedSupplier.of(()->{
		final Map<String,Function<String,PojoPointer>> map = new HashMap<>();



		//Needs an argument, definitely
		map.put("map", new FieldBasedLambdaArgumentParser("map", (p)->new MapPath(p)));

		//Can use an argument, definitely
		map.put("sort", new FieldBasedLambdaArgumentParser("sort", (p)->new SortPath(p,false)));
		map.put("revsort", new FieldBasedLambdaArgumentParser("revsort", (p)->new SortPath(p,true)));
		map.put("flatmap", new FieldBasedLambdaArgumentParser("flatmap", (p)->new FlatMapPath(p)));


		map.put("distinct", new FieldBasedLambdaArgumentParser("distinct", (p)->new DistinctPath(p)));

		//Probably doesn't need an argument
		map.put("count", new FieldBasedLambdaArgumentParser("count", (p)->new CountPath(p)));


		//Not for collections
		map.put("$fields", new FieldBasedLambdaArgumentParser("$fields", (p)->new FieldPath(p)));


		map.put("group", new FieldBasedLambdaArgumentParser("group", (p)->new GroupPath(p)));

		map.put("limit", new LongBasedLambdaArgumentParser("limit", (p)->new LimitPath(p)));


		map.put("skip", new LongBasedLambdaArgumentParser("skip", (p)->new SkipPath(p)));



		return map;
	});

	public static Function<String,PojoPointer> getPojoPointerParser(final String key){
		return LambdaParseRegistry.subURIparsers.get().get(key);
	}


}