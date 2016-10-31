package ix.core.factories;

import ix.core.FieldNameDecorator;
import ix.core.search.text.TextIndexer;
import ix.core.util.EntityUtils;
import ix.core.util.EntityUtils.EntityInfo;
import ix.ginas.models.v1.Substance;
import play.Application;

public class FieldNameDecoratorFactory extends AccumlatingInternalMapEntityResourceFactory<FieldNameDecorator>{
	private static FieldNameDecoratorFactory _instance;
	
	public FieldNameDecoratorFactory(Application app){
		super(app);
		_instance=this;
	}
	
	public static FieldNameDecoratorFactory getInstance(Application app){
		if(_instance!=null){
			return _instance;
		}else{
			return new FieldNameDecoratorFactory(app);
		}
	}

	@Override
	public void initialize(Application app) {
		try{
			this.register(Substance.class, (FieldNameDecorator)EntityUtils.getEntityInfoFor("ix.ginas.utils.SubstanceFieldNameDecorator").getInstance(),true);
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	@Override
	public FieldNameDecorator getDefaultResourceFor(EntityInfo<?> ei) {
		return f->{
			return (f.equals(TextIndexer.FULL_TEXT_FIELD()))?"Global Search":f;
		};
	}

	@Override
	public FieldNameDecorator accumulate(FieldNameDecorator t1, FieldNameDecorator t2) {
		return t2.orIfNull(t1);
	}

}
