package ix.core.models;

public class BeanViews {
    public static class Compact {}
    public static class Full{}
    public static class Public {}
    public static class Internal extends Full {}
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