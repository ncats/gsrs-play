package ix.ginas.initializers;

import ix.core.adapters.EntityPersistAdapter;
import ix.core.initializers.Initializer;
import ix.ginas.controllers.v1.ControlledVocabularyFactory;
import ix.ginas.models.v1.CodeSystemVocabularyTerm;
import ix.ginas.models.v1.ControlledVocabulary;
import ix.ginas.models.v1.VocabularyTerm;
import ix.ginas.utils.GinasGlobal;
import java.util.Map;
import play.Application;
import play.Logger;

/**
 *
 * @author Mitch Miller
 */
public class ApprovalCodeVocabularyItemInitializer implements Initializer
{

	String codeSystem;

	@Override
	public Initializer initializeWith(Map<String, ?> map)
	{
		Initializer.super.initializeWith(map);
		codeSystem = (String) map.get("codeSystem");
		return this;
	}

	@Override
	public void onStart(Application aplctn)
	{
		assureCodeSystem();
	}

	private void assureCodeSystem()
	{
		Runnable r = new Runnable()
		{
			@Override
			public void run()
			{
				if (codeSystem != null)
				{
					ControlledVocabulary cvv = ControlledVocabularyFactory.getControlledVocabulary("CODE_SYSTEM");
					boolean addNew = true;
					for (VocabularyTerm vt1 : cvv.terms)
					{
						if (vt1.value.equals(codeSystem))
						{
							addNew = false;
							break;
						}
					}
					if (addNew)
					{
						Logger.debug("Will add new voabulary term for code system: " + codeSystem);
						CodeSystemVocabularyTerm vt = new CodeSystemVocabularyTerm();
						vt.display = codeSystem;
						vt.value = codeSystem;
						vt.hidden = true;

						//*************************************
						// This causes problems if done first
						// may have ramifications elsewhere
						//*************************************
						//vt.save();
						cvv.addTerms(vt);
						cvv.update();

						//Needed because update doesn't necessarily
						//trigger the update hooks
						EntityPersistAdapter.getInstance().reindex(cvv);
					}
				}
			}
		};
		GinasGlobal.runAfterStart(r);
	}
}
