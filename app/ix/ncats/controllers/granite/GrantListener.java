package ix.ncats.controllers.granite;

import java.util.EventListener;
import ix.ncats.models.Grant;

public interface GrantListener extends EventListener {
    void newGrant (Grant g);
}
