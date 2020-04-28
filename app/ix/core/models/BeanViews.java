package ix.core.models;

public class BeanViews {
    public static class Compact {}
    public static class Full{}
    public static class Public {}

    /**
     * View of the Json that considers more fields
     * than public but less than {@link Internal}
     * and is used when finding differences between
     * different versons of an entity.
     */
    public static class JsonDiff extends Full {}

    /**
     * View that includes more fields than Full
     * and JsonDiff and mostly used for making Entity backups.
     */
    public static class Internal extends JsonDiff {}


    public static class Private {}
}
/*

If something says FULL:
	A full em should find it
	An internal em should find it
	
	for this to be true, all Fulls should also be internals
If something says INTERNAL:
	A full em should NOT find it
	An internal em SHOULD find it
	
	for this to be true, all Fulls should also be internals
	




*/