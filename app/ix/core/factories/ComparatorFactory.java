package ix.core.factories;

import java.util.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import ix.core.util.EntityUtils.EntityInfo;
import ix.core.util.IOUtil;
import ix.ginas.models.GinasCommonData;
import ix.utils.Tuple;
import play.Application;
import play.Logger;

public class ComparatorFactory extends AccumlatingInternalMapEntityResourceFactory<Comparator>{
	private static ComparatorFactory _instance;
	
	public ComparatorFactory(Application app) {
		super(app);
		_instance=this;
	}
	public static ComparatorFactory getInstance(Application app){
		if(_instance!=null){
			return _instance;
		}else{
			return new ComparatorFactory(app);
		}
	}
	
	private static class ComparatorConfig{
		String entityClassName;
		String comparatorClassName;
		Map parameters=null;

		private static ThreadLocal<ObjectMapper> mapper = new ThreadLocal<ObjectMapper>(){
			@Override
			protected ObjectMapper initialValue() {
				ObjectMapper mapper= new ObjectMapper();
				TypeFactory tf = TypeFactory.defaultInstance()
						.withClassLoader(IOUtil.getGinasClassLoader());
				mapper.setTypeFactory(tf);

				return mapper;

			}
		};
		
		private ComparatorConfig(String className, String comparatorName, Map parameters){
			this.entityClassName=className;
			this.comparatorClassName=comparatorName;
			this.parameters=parameters;
		}
		
		private Comparator getComparator() throws Exception{
			Class<?> comparatorCls;
			try {
				comparatorCls = IOUtil.getGinasClassLoader().loadClass(comparatorClassName);
			}catch(Exception e){
				throw new Exception("error loading comparator class " + comparatorClassName, e);
			}
			if(parameters ==null){
				return  (Comparator)mapper.get().convertValue(Collections.emptyMap(), comparatorCls);

			}
			return  (Comparator) mapper.get().convertValue(parameters, comparatorCls);

		}
		public String toString() {
			return "ComparatorConfig{" +
					"entityClassName='" + entityClassName + '\'' +
					", comparatorClassName='" + comparatorClassName + '\'' +
					", parameters=" + parameters +
					'}';
		}
	}

	@Override
	public void initialize(Application app) {
		getStandardResourceStream(app,"ix.core.comparators")
		.map(m->new ComparatorConfig((String)m.get("class"), (String)m.get("comparator"), (Map)m.get("parameters")))
		.map(cc->{
			try{
				Comparator c = cc.getComparator();
				return Tuple.of(cc.entityClassName, c);
			}catch(Exception e){
				e.printStackTrace();
				Logger.warn("Unable to make comparator:" + e.getMessage());
			}
			return null;
		})
		.filter(Objects::nonNull)
		.forEach(t->{
			try {
				register(t.k(), t.v(), true);
			} catch (Exception e) {
				Logger.warn("Unable to register comparator:" + e.getMessage());
			}
		});
	}
	
	@Override
	public Comparator getDefaultResourceFor(EntityInfo<?> ei){
		return new Comparator<GinasCommonData>(){
			@Override
			public int compare(GinasCommonData o1, GinasCommonData o2) {
				try {
					if (o1.getCreated() == null)return -1;
					return o1.getCreated().compareTo(o2.getCreated());
				}catch(Exception e){
					e.printStackTrace();
					Logger.warn("Unable to compare:" + e.getMessage());
					return -1;
				}
			}
		};
	}
	
	@Override
	public Comparator accumulate(Comparator t1, Comparator t2) {
		if(t2 != null){
			return t2;
		}
		return t1;
	}
}
