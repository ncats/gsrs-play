package ix.core.initializers;

import play.Application;

public interface Initializer {
    public void onStart(Application app);
}
