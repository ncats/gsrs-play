package ix.core.plugins;

import ix.core.models.Structure;

/**
 * This interface provides a mechanism for individual structure processing
 * via StructureProcessorPlugin.submit(). The received structure should 
 * already be persisted.
 */
public interface StructureReceiver extends java.io.Serializable {
    public enum Status {
        OK,
        FAILED
    }
    
    String getSource ();
    void receive (Status status, String mesg, Structure struc);
}
