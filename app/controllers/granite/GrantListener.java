package controllers.granite;

import java.util.EventListener;
import models.granite.Grant;

public interface GrantListener extends EventListener {
    void newGrant (Grant g);
}
