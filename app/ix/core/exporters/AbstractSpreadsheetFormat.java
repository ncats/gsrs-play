package ix.core.exporters;

import java.io.OutputStream;
import java.util.Objects;
import java.util.function.Function;

public abstract class AbstractSpreadsheetFormat extends OutputFormat {

    public AbstractSpreadsheetFormat(String extension, String displayname) {
        super(extension, displayname);
    }

    abstract Spreadsheet createSpeadsheet(OutputStream out);

    public AbstractSpreadsheetFormat withInfo(Function<StringBuilder, String> extension, Function<StringBuilder, String> displayName){
        Objects.requireNonNull(extension);
        Objects.requireNonNull(displayName);

        return newSubclass(this, extension.apply(new StringBuilder(this.getExtension())), displayName.apply(new StringBuilder(this.getDisplayName())));
    }

    private AbstractSpreadsheetFormat newSubclass(AbstractSpreadsheetFormat parentClass, String ext, String display){
        return new AbstractSpreadsheetFormat(ext, display) {
            @Override
            Spreadsheet createSpeadsheet(OutputStream out) {
                return parentClass.createSpeadsheet(out);
            }
        };
    }
}
