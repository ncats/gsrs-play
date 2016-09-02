GSRS v1.2.06
============

Changes:

Enhancements
------------
1. Improve indexing performance
2. Lazy-load search results to improve search performance
   and stability.
3. Improved search analyzer to show more accurate suggestions
   for restricting global searches.
4. Allow searching by code systems (BDNUM, CAS, etc)

Bug Fixes
---------
1. Fixed structure search reloading issue. A quickly 
   returning substructure search might have the wrong
   infomation before, and not refresh when the search
   is complete. Now it refreshes when ready. 
2. Reindexing previously wouldn't update autosuggest
   or sorting operations, unless the application was
   also restarted. Now the reindexing works in-place


A Deeper look
-------------

1. EntityWraper now used to avoid repeated and unstandardized
   reflection. There were issues before where the mechanism
   to get an ID was slightly different based on where it was
   written or rewritten. Similar issues happen with other
   properties computed via reflection. Now there is one
   common mechanism, complete with caching for faster 
   access.

2. EntityFetcher now uses common cache for entities instead
   of user-specific cache.

3. More to come
   




GSRS v1.2.05
============

Changes:

1. Fixed caching issue ajaxing for status of request
2. Added download option to server-side for SDF
3. Restrict Approval to SuperUpdater, not updater
4. Change download to download SDF by default
5. Improved API for exporting record sets
6. Allow downloading CSV as export option via URL
7. Removed expression producing ArrayOutOfBounds 
   stacktrace exception
8. Refactored export mechanism to consume less memory
9. Fixed flex match caching issue
10. Fixed navlist on details view not scrollable
11. Fixed error page overlap
12. Hide "+" icon on text search with known results
13. Added canonicalization algorithm for mol formulas
    to approximate modified Hill System Order, 
    compatible with MDL cartridge.
14. Allow UI download of SDF and CSV for results
15. Show structure of query on flex / exact search
16. Preserve drawn structure coordinates on structure 
    query
17. Use molfile for search rather than smiles by default
    to avoid structure processing errors from drawing
    tool, which lead to improper results.
18. Fixed paging buttons to be more standardized, allow
    seeing next 2 pages on browse.
19. Make browse page labels collapsible
20. Fix tooltip locator overlap issue
21. Fix missing button in subref search
22. Force creation date to be overwritten on API submission
23. Update on the user admin page to show the top menu
24. Add navigation link for most of the response pages
25. Hide 'Edit Substance' button if user doesnt have access to edit
26. Fixed text search paging status ajax issue
27. Show more information about page loading when waiting for page


GSRS v1.2.04
============

Changes:

1. No longer require URLs in code form
2. Fix relationship type facet UI overflow
3. Refactored UI code
4. Improved audit and definition reference views
5. Allow server failures on validation to be displayed to user
6. Fixed change reason hidden bug
7. Only show change reason on substance edit
8. Improve physical parameter form
