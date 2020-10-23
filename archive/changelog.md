GSRS v1.3.0-RC4
============

Changes:

Enhancements
-------------
1. For 'Tags' dropdown - the 'X' button (to clear the selected value) is hidden if the value is too long. We couldn't move the 'X' to the beginning of the value as it is part of a java script library. To correct the issue I increased the width of the 'Tags' drop down under 'References' 
2. Relationships: Cleaned up the Relationships form. Showing only Type, Related Substance, References and Access. Added a 'Show Details' button to show the rest of the fields.
3. Added Validation for "Type" as required field under "Relationships" 
4. Add a tooltip to "click link" under Codes in Substance View
5. Cleaned up the Audit information by displaying everything in a single line
6. Added the 'Search' button for Subref search (since the type ahead displays only first 10 matches). Also added the ability to search by 'Approval ID'.
7. Autocomplete shows exact matches first

Bug Fixes
---------
1. Removed extra amount view from relationships (wrong sign for high/low)
2. Allow promoted concepts to have definitional references


GSRS v1.3.0-RC3
============
Changes:

Enhancements
-------------
1.issue inxight#1046 - Differentiate same facets by path will let us avoid having too high a count for lastEditedBy
  in the search results.

2. improvements to test code - makes loading json data simplier.
3. improvements to test code- makes parsing search results and getting substances easier.


Bug Fixes
---------
1. Fixed bug with upgrading concept to different substance class
2. Validation rule to require codes to have codeSystems.

GSRS v1.3.0-RC2
============
Changes:

Enhancements
-------------
1. issue inxight-#1042 - Added support for including private commonData in spreadsheet exporter. Added checkbox to export drop down
2. Added similarity in grid view, sort by similarity as default for similarity search
3. Improvements in test code - Added helper methods to SubstanceAPI to submit from builder
4. issue inxight- #990 Improved suggestions during text box entries to show exact matches first.

Bug Fixes
---------
1. Added "Add" buttons to top of the forms
2. inxight-#1032, #1039 - Fixed few display issues
    -  In the Substance details page - Audit info should be visible only to admin and should be moved down
    -  In the Substance details page - All the expand sections should be collapsed by default
    - Few button name changes to a more meaningful name and to make it uniform across the application
    - Register substances - Some Add buttons were hidden. Fixed the issue
    - Cleaned the forms by hiding the additional information by default. Added 'Show Details' link to view the additional information
    - substance class should be put over the icon
    - Protein Edit - Relationships: "Add a Reference" tooltip flickering which sometimes prevents from adding a new Reference
3. Fixed the issue with applying references


GSRS v1.3.0-RC1
============
Changes:

Enhancements
-------------
1.  Added new facets - Record Level Access,
    Display Name Level Access, Definiton Level Access
2.  REST API now supports PATCH and PUT operations
    for granular changes (needed for form and
    webservice improvements.
3.  Validation messages no longer are duplicated if
    the same error happens more than once.
4.  Structure search now available via REST API (needed
    for ajaxing advanced searches)
5.  REST API can now explore facet values granularly,
    allowing for finding rare facet values (needed for
    expanding the facet values on browse/search).
6.  REST API now shows simplified data by default for
    many collections.
7.  REST API now allows selecting specific sub elements
    and some basic processing functions.
8.  REST API supports asynchronous calls
9.  REST API allows for new configurable function calls
10. Simplified authentication, to allow more flexibility / extensibility.
11. Cache now honors evictable and non-evictable entries.
12. Pass-through write-to-disk cache now is preserved between restarts.
13. Structure images cached are invalidated after changes.
14. Subref (e.g. relationships) search select upon clicking type-ahead value
15. Additional validation rules stop null relationships from being added.
16. Improvements to codebase which reduce redundancy, improve performance.
17. Added 500+ additional automated tests
18. Added suffix searching support now *foo and *foo* are supported
19. inxight#1026 - Added a FASTA view for the sequence in proteins

Bug Fixes
----------
1. issue # GSRS-187 - Error message when
    trying to view a substance that I approved
2. issue # GSRS-184 - SSG1 Submission
   failed without any error. Added validation for SSG1
3. issue # GSRS-179 - Could not submit a polymer record
4. issue # ???????? - Last Edited By facets are wrong / sometimes break
5. issue # GSRS-190 - Updates to CV now allowed.
6. issue # GSRS-189 - download button doesnt work on IE
7. Fixed the LuceneSearchSuggestreindex test failure on windows
8. Fixed the CV typo in jurisdiction fields
9. Fixed the issue - cant edit polymers
10. Fixed the validation error from protein edit
11. Fixed the issue with the disulfide links when updating a sequence (pointed out by Larry)
12. Bug fix to prevent 500 error when searches returned 0 results
  
    

GSRS v1.2.08
============
Changes:

Enhancements
------------
1. API allows fetching diffs on Edits.
2. Allow loading a dump file from command line at startup.
3. ExportFactory now has mechanism to Memoized values,
   so that expensive export operations are not run multiple
   times.
4. Removed unneeded files
5. Substance reference search now does exact name by
   default, with type-ahead.
6. Allow list and views to be more easily adapted,
   and standardized.
7. Moved most strerr output to log file instead
8. List view elements wait to display until they
   are loaded (getting rid of "{{V}}" and other
   display issues.
9. Minor UI improvements
10. References to other substances no longer
    squashed on the side.
11. History link now present on detail views.
12. Error pages standardized

Bug Fixes
---------
1. Exporting an invalid set, or lots of slow sets 
   could cause zombie threads that would imparct 
   performance and stability. The number of threads
   is now limited, and submission of a new job is 
   now deferred if there are too many being processed.
2. Updating a record would cause substructure search
   to fail for related records. This is now fixed.
3. Reindexing records after update would not reindex
   updated records unless the server is restarted.
   Now it does.
4. Allow structure/sequence indexers to return 
   duplicate keys, and still have results show.
5. Fixed display alignment on sequence search,
   show query and target
6. Fixed flickering tooltip on some hover overs
7. Don't show facets with 0 entries
8. Hover-over on structure search now shows
   structure again.


GSRS v1.2.07
============

Changes:

Enhancements
------------

1.  Specific properties can now be added as facets
    (e.g. viscosity)
2.  Password no longer required to make user
    (autogenerated)
3.  Can sort on browse / search
4.  Added missing CV value for relationship
5.  Password no longer required on edit user
6.  Show history and change reason on record view
7.  Improved facet overflow handling
8.  Moved Audit information to top of record, 
    collapsed by default.
9.  Java API now allows to do see the diff in changes
10. Improved performance of cache, by allowing multiple
    identical requests to use the same fetching process,
    rather than recalculating every time.
11. Only allow Super-users to override duplicate warnings
12. Show molecular weight on list views
13. Improved look and feel of name list on browse, show
    code-system with code
14. Sort list of codes before showing on list view
15. Allow incomplete proteins to have 0 subunits
16. Allow for indexing outside of the model, for adding
    new facets / sorters / suggest fields without having
    to modify the entity models directly.
17. Allow for configurable display priority for codes
    in list view.
18. Allow configuration of what facets show on UI without
    need to recompile.
19. It is now simpler to remove a facet value from view
    as a developer.
20. Minor UI improvements
21. Allow admins to show deprecated records with a checkbox
22. Searches which match an exact field that's flagged as special
    come up first, before everything else.
23. Field suggestions view updated to be simpler
24. Alignment on sequence search display enhanced
25. Include runTests scripts for windows / unix


Bug Fixes
---------
1. Substructure searches sometimes attempted to render
   an ID as a molecule. This is fixed now.
2. Residue lookup on edit would show all residues as invalid
   until one change happened. This is fixed now.
3. To more than one element would sometimes increase the 
   substance version twice or more, causing issues with
   retrieving null edit history.
4. Improved release strategy for Locks on records. Pervious
   Locks were released logically, but were not released
   from memory.
5. Making no change to a record, but saving it would add
   an entry to the history table, but do nothing else. 
   This caused a problem, as the software assumes that
   each version of the substance has 1 entry in the history
   table. We no longer allow making a non-change to a record.
6. Cancelling the persistence of an inverted relationship
   would persist an Edit for that attempted change, even
   though none was performed, due to a misreporting by
   the trigger. The trigger was corrected.
7. Don't fail text indexing on empty strings. We now
   allow empty strings to be ignored from indexing
   or to add them with a special "EMTPY" keyword.
8. Removed search from protein thumb if no subunits
9. Added validation rule to proteins, not to allow
   records with no subunits
10.A new session previously invalidated certain
   cache elements. This meant that a search would
   be relaunched if a new user or new session
   was activated since the last run. New sessions
   no longer invalidate the cache.
11. Display of structurally diverse icon standardized
12. Added code to log Structure Search request in the access log.
13. Upgraded javaassist to remove errors on using certian java 8
    interface features on dev.
14. Deadlock could occur in load if, during an update, a 
    database timeout or other fetching error occurred. This
    is now fixed.
15. Attempting to add audit information from a load, which
    referred to users who don't exist in they system could
    cause very few records to persist previously, due to a
    caching error, where a user is stored in a cache after
    an insert, but is not un-stored if that insert is rolled
    back.
16. Allow admin to see edit / update pieces, as intended.
17. Non-authenticated redirect now redirects, even if the root
    application path is changed.
18. Sorting and some other features failed when showing 
    deprecated records. This was due to lucene/sql naming
    differences. Now defaulting to always using lucene,
    even for deprecated records.


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
