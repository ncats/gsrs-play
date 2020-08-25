/*
	* When a substance is saved and has an approvalID, check for a corresponding Code.
	* If necessary, create a new Code
 */
package ix.ginas.processors;

import ix.core.EntityProcessor;
import ix.core.adapters.EntityPersistAdapter;
import ix.ginas.controllers.v1.ControlledVocabularyFactory;
import ix.ginas.models.v1.Code;
import ix.ginas.models.v1.CodeSystemVocabularyTerm;
import ix.ginas.models.v1.ControlledVocabulary;
import ix.ginas.models.v1.Substance;
import ix.ginas.models.v1.VocabularyTerm;
import ix.ginas.utils.GinasGlobal;
import java.util.Map;

/**
 *
 * @author Mitch Miller
 */
public class ApprovalIdProcessor implements EntityProcessor<Substance>
{

	private final String codeSystem;

	public ApprovalIdProcessor(Map m)
	{
		codeSystem = (String) m.get("codesystem");
		addCodeSystem();
	}

	private void addCodeSystem()
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

	@Override
	public void prePersist(Substance s)
	{
		copyCodeIfNecessary(s);
	}

	@Override
	public void preUpdate(Substance obj)
	{
//		System.out.println("preUpdate");
		prePersist(obj);
	}

	public void copyCodeIfNecessary(Substance s)
	{
		if (s.approvalID != null && s.approvalID.length() > 0)
		{
			play.Logger.info("handling approval ID " + s.approvalID);
			boolean needCode = true;
			for (Code code : s.getCodes())
			{
				if (code.codeSystem.equals(codeSystem))
				{
					if (code.code == null || code.code.length() == 0 || !code.code.equals(s.approvalID))
					{
						code.code = s.approvalID;
						code.delete();
						play.Logger.info("deleted old code");
					}
					else if (code.code != null && code.code.equals(s.approvalID))
					{
						needCode = false;
					}
				}
			}
			if (needCode)
			{
				Code newCode = new Code(codeSystem, s.approvalID);
				s.codes.add(newCode);
				play.Logger.info("Added new code for approvalId");
			}
		}
	}
}
