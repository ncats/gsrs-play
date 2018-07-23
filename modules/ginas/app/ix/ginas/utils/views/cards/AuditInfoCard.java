package ix.ginas.utils.views.cards;

import java.util.stream.Collectors;

import ix.ginas.models.v1.Substance;
import play.twirl.api.Html;

/**
 * Card view for detail overview of substance record
 * @author tyler
 *
 */
public class AuditInfoCard extends AngularCardCard{

	Substance s;
	public AuditInfoCard(Substance s){
		super(0, "Audit Info", "audit");
		this.s=s;
	}

	@Override
	public Html innerContent() {
		return ix.ginas.views.html.details.properties.auditinfo.render(s);
	}
	
	
}
