package ix.core.chem;

public class StructureRenderingParameters {
    private String minHeight ="";
    private String minWidth="";
    private String maxHeight="";
    private String maxWidth="";
    private String bondLength="";

    public boolean hasValuesForAll() {
        return (minHeight!=null && maxHeight != null && minWidth!=null && maxWidth!=null && bondLength!=null);
    }

    public String getMinHeight() {
        return minHeight;
    }

    public void setMinHeight(String minHeight) {
        this.minHeight = minHeight;
    }

    public String getMinWidth() {
        return minWidth;
    }

    public void setMinWidth(String minWidth) {
        this.minWidth = minWidth;
    }

    public String getMaxHeight() {
        return maxHeight;
    }

    @Override
    public String toString() {
        return "StructureRenderingParameters{" +
                "minHeight=" + minHeight +
                ", minWidth=" + minWidth +
                ", maxHeight=" + maxHeight +
                ", maxWidth=" + maxWidth +
                ", bondLength=" + bondLength +
                '}';
    }

    public void setMaxHeight(String maxHeight) {
        this.maxHeight = maxHeight;
    }

    public String getMaxWidth() {
        return maxWidth;
    }

    public void setMaxWidth(String maxWidth) {
        this.maxWidth = maxWidth;
    }

    public String getBondLength() {
        return bondLength;
    }

    public void setBondLength(String bondLength) {
        this.bondLength = bondLength;
    }
}
