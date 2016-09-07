GSRS v1.2.06
============

Changes:

Enhancements
------------
1. Improve indexing performance (Addresses GSRS-102 and GSRS-76)
2. Lazy-load search results to improve search performance
   and stability. (Addresses GSRS-102 and GSRS-76)
3. Improved search analyzer to show more accurate suggestions
   for restricting global searches. (Addresses GSRS-102 and GSRS-76)
4. Allow searching by code systems (BDNUM, CAS, etc)
5. Improved keyboard navigation for 508 compliance
6. Restrict to names or code search options (GSRS-87)
7. Improved initial browse and paging now far more robust
   and less likely to cause problems
8. Added debugging utilities for developers to add specific
   delays to database fetches and structure processing,
   to find problematic areas under stress testing.
9. A new Role "Approver" has been added. Users with this role
   can approve substances. SuperUpdater role no longer has
   approval access. Instead, SuperUpdater can now also
   update record information for previously approved
   substances. (GSRS-86)
10.Fields to be indexed for analysis have been restricted
   to a few names fields. (Addresses GSRS-102)
11.Added CodeSystem indexes to speed up validation.
12.Only load sketcher on pages that use it, to speed up
   page loads.
13.Structure searches now use cache in processing. (Addresses GSRS-102)

Bug Fixes
---------
1. Fixed structure search reloading issue. A quickly 
   returning substructure search might have the wrong
   infomation before, and not refresh when the search
   is complete. Now it refreshes when ready. (Addresses GSRS-102)
2. Reindexing previously wouldn't update autosuggest
   or sorting operations, unless the application was
   also restarted. Now the reindexing will correctly update autosuggest
   without a restart.
3. Empty modifications were being saved which would break display,
   wrapped each display with a null check.
4. In some bulk loads, the same record may have been 
   editted by different threads. In a few cases this
   was found to cause a OptimisticLockException, and
   the transaction was cancelled.  This is a problem
   when adding common inverted relationships. The 
   locking mechanism has been reorganized, and is now
   very unlikely for this to be an issue.
5. Fixed edit history bug related to #3.
6. Fixed IndexAlreadyClosedException problem, where
   the Lucene index was closed before all the
   results were fetched from the database.
7. Tags now different than Reference tags, as intended
 

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
