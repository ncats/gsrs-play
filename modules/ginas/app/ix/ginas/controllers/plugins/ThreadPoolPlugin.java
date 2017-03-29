package ix.ginas.controllers.plugins;

import play.api.Plugin;

/**
 * Created by katzelda on 3/23/17.
 */
public class ThreadPoolPlugin implements Plugin{
    @Override
    public boolean enabled() {
        return true;
    }

    @Override
    public void onStart() {
    }

    @Override
    public void onStop() {
    }
}
