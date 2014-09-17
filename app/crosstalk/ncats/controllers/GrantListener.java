package crosstalk.ncats.controllers;

import java.util.EventListener;
import crosstalk.ncats.models.Grant;

public interface GrantListener extends EventListener {
    void newGrant (Grant g);
}
