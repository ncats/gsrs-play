package ix.core.factories;

import java.util.List;
import java.util.Set;

import ix.core.util.CachedSupplier;
import ix.core.util.pojopointer.extensions.RegisteredFunction;
import play.Application;

public class ApiFunctionFactory extends InternalMapEntityResourceFactory<RegisteredFunction>{
	private static ApiFunctionFactory _instance;
	
	
	public ApiFunctionFactory(Application app) {
		super(app);
		_instance=this;
	}

	@Override
	public void initialize(Application app) {
		System.out.println("Initializing");
		this.getStandardResourceStream(app, "ix.api.registeredfunctions")
			.map(m->(String)m.get("class"))
			.map(n->CachedSupplier.ofThrowing(()->Class.forName(n).newInstance()))
			.filter(p->!p.getThrown().isPresent())
			.map(o->(RegisteredFunction)o.get())
			.forEach(rf->{
				this.register(RegisteredFunction.class, rf, false);
			});
	}
	
	public Set<RegisteredFunction> getRegisteredFunctions(){
		return this.getRegisteredResourcesFor(RegisteredFunction.class);
	}
	
	public static ApiFunctionFactory getInstance(Application app){
		if(_instance!=null){
			return _instance;
		}
		return new ApiFunctionFactory(app);
	}

}
