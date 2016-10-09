package ix.core.factories;

import java.util.List;

import play.Application;
import play.Logger;

public class SpecialFieldFactory extends InternalMapEntityResourceFactory<String> {
	public static final String IX_CORE_EXACTSEARCHFIELDS = "ix.core.exactsearchfields";
	private static SpecialFieldFactory _instance = null;

	public SpecialFieldFactory(Application app) {
		super(app);
		_instance=this;
	}
	
	public static SpecialFieldFactory getInstance(Application app){
		if(_instance!=null)return _instance;
		return new SpecialFieldFactory(app);
	}

	@Override
	public void initialize(Application app) {
		System.out.println("initializing");
		
		getStandardResourceStream(app,IX_CORE_EXACTSEARCHFIELDS)
			.forEach(m->{
				System.out.println("Found:" + m.toString());
				String cls=m.get("class").toString();
				List<String> fields=(List<String>) m.get("fields");
				System.out.println("Found fields:" + fields.toString());
				fields.stream().forEach(exact->{
					try {
						System.out.println("Registering:" + cls + " and " + exact);
						register(cls, exact, true);
					} catch (Exception e) {
						Logger.error("Error registering exact field \"" + exact + "\" for:\"" + cls +"\"", e);
					}
				});
				
			});
	}
	
	

}
