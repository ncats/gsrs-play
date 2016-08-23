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
11. fixed error page overlap
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
