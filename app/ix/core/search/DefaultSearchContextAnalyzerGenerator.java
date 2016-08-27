package ix.core.search;

import java.lang.reflect.Constructor;
import java.util.Map;

public class DefaultSearchContextAnalyzerGenerator implements SearchContextAnalyzerGenerator{

	Class<?> entityCls;
	Class<?> analyzerCls;
	Map params;
	
	public DefaultSearchContextAnalyzerGenerator(Class<?> entityCls, Class<?> analyzerCls, Map with){
		this.entityCls=entityCls;
		this.analyzerCls=analyzerCls;
		this.params=with;
		
	}
	@Override
	public SearchContextAnalyzer create() {
		SearchContextAnalyzer analyzer=null;
		if(params!=null){
			try{
				Constructor c=analyzerCls.getConstructor(Map.class);
				analyzer= (SearchContextAnalyzer) c.newInstance(params);
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		if(analyzer==null){
			try {
				analyzer = (SearchContextAnalyzer) analyzerCls.newInstance();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return analyzer;
	}
	
}