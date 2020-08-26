package ix.core.plugins;

import play.Application;
import play.Plugin;

public class ExportPlugin extends Plugin {
    private final Application app;

    public ExportPlugin(Application app) {
        this.app = app;
    }

    @Override
    public void onStart() {

    }

    @Override
    public void onStop() {

    }

    @Override
    public boolean enabled() {
        return true;
    }
}
