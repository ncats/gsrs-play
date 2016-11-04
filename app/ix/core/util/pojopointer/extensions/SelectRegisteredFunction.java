package ix.core.util.pojopointer.extensions;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import ix.core.util.EntityUtils.EntityWrapper;
import ix.core.util.pojopointer.LambdaArgumentParser;
import ix.core.util.pojopointer.PojoPointer;
import ix.core.util.pojopointer.SingleElementPath;
import ix.core.util.pojopointer.extensions.SelectRegisteredFunction.SelectPath;

public class SelectRegisteredFunction implements RegisteredFunction<SelectPath, Object, List<Object>> {
	public static String name = "$select";
	
	public static class SelectPath extends SingleElementPath<List<PojoPointer>>{
		public SelectPath(List<PojoPointer> t) {
			super(t);
		}

		@Override
		protected String thisURIPath() {
			return name + "(" + 
							getValue()
							.stream()
							.map(p->p.toURIpath())
							.collect(Collectors.joining(","))
							+ ")";
		}

		@Override
		public String name() {
			return name;
		}
	}
	
	@Override
	public Class<SelectPath> getFunctionClass() {
		return SelectPath.class;
	}
	
	@Override
	public LambdaArgumentParser<SelectPath> getFunctionURIParser() {
		return LambdaArgumentParser.SINGLE_STRING_ARGUMENT_PARSER(name, (s)->{
			String[] args = s.split(",");
			List<PojoPointer> pplist = Arrays.stream(args)
				.map(PojoPointer::fromURIPath)
				.collect(Collectors.toList());
			return new SelectPath(pplist);	
		});
	}
	
	@Override
	public BiFunction<SelectPath, Object, Optional<List<Object>>> getOperation() {
		return (fp, s)->{
			try{
				EntityWrapper<?> ew=EntityWrapper.of(s);
				
				List<Object> list =fp.getValue()
					.stream()
					.map(p->ew.at(p))
					.map(o->o.map(EntityWrapper::getValue))
					.map(o->{
						if(!o.isPresent()){
							return (Object)(null);
						}else{
							return o.get();
						}
					})
					.collect(Collectors.toList());
				
				return Optional.of(list);
			}catch(Exception e){
				return Optional.empty();
			}
		};
	}
}
