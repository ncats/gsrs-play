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
5. Improved inital browse and paging now far more robust
   and less likely to cause problems.

Bug Fixes
---------
1. Fixed structure search reloading issue. A quickly 
   returning substructure search might have the wrong
   infomation before, and not refresh when the search
   is complete. Now it refreshes when ready. 
2. Reindexing previously wouldn't update autosuggest
   or sorting operations, unless the application was
   also restarted. Now the reindexing works in-place
3. In some bulk loads, the same record may have been 
   editted by different threads. In a few cases this
   was found to cause a OptimisticLockException, and
   the transaction was cancelled.  This is a problem
   when adding common inverted relationships. The 
   locking mechanism has been reorganized, and is now
   very unlikely for this to be an issue.
4. Fixed edit history bug related to #3.
 

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

3. Massive refactoring has greatly simplified the codebase.
   There are many places where dead, deprecated, or incomplete
   code had been present. Most of it is gone now. In addition,
   many duplicative function calls have been abstracted out.

4. Before, all browse and search requests from the 
   UI were loaded, in their entirety, one at a time, from the database. 
   Then each was put into the cache, as well as into
   a list found in a SearchResult (which is also in the cache)
   This all happens in a (possibly new) background thread,
   which may occupy the server for some time (particularly
   with a slow connection). Every unique browse / search
   will kick off this same process. Also, each different
   user (even with the same query results) has to restart
   this process, which contributes to the load for all 
   other users.

   Particularly problematic is that the cache is limitted
   only by number of objects, but not by the size of those
   objects. This is like specifiying that an elevator can
   hold 10 people, rather than saying 2000 lbs. The elevator
   actually cares about weight, and not the number of people,
   but people are uniform enough that it's a decent proxy.
   It's easier for someone to count the people on an elevator
   and say "That's more than 10", then it is to weigh each
   person, sum up their weight, and decide if it's over
   2000 lbs. It's only a real problem if there are extremely heavy
   people that tend to get on the elevator all at once,
   or, even worse, there is a BIAS for heavy people to get
   on the elevator in the first place! Worst of all, if
   heavy people tend to STAY on the elevator ...
   Much like this scenarion, ehCache has a "max elements"
   property that it uses to decide when to clear out the
   in-memory cache a bit. It actually cares about the 
   memory "weight", but that's really hard to calculate.
   The objects in our cache are not quite as uniform as 
   people are. Some are small (like similarity score) and
   some are moderate (a single record, maybe 10kb), and 
   some can be over 500 MiB (like a search result). Worse,
   we intentionally keep around the largest objects to avoid
   having to rerun the most expensive tasks.

   We tried making the cache use memory footprint limits
   instead, but that caused a giant performance problem.
   Then we tried to have the cache hold onto SoftReferences
   of objects instead of the objects themselves (that way
   they can be GC'd when need be) and the cache will just
   score a miss -- but there are some things that kept
   getting collected well before they should have. 

   This build is a pivot from these strategies. Instead,
   we made SearchResult store a "LazyList" of results.
   This looks like a normal list to everything else,
   but it actually stores instructions to retrieve the object,
   rather than the object itself. Now, when a search happens,
   the results aren't actually fetched. Instead, instructions
   on how to fetch each result is compiled from the lucene 
   index, and wrapped in a callable, which is put into the 
   "LazyList" in the Search Result. It will only be loaded 
   when it is specifically accessed from the list, and it 
   will use the cache if it has already been called before.

   This has 2 main advantages. First, the actual loading of
   the records in a background thread no longer happens,
   so CPU and thread-taxing operations can be greatly 
   avoided. The second advantage is that the memory footprint for
   the Callable is tiny in comparison to the other loaded
   records. Storing 100k records should now be closer to
   50B each, or about 5MiB total. Compared to 500MiB from
   the other method. This means that we no longer have
   giant objects (at least not THESE giant objects!) in 
   the cache, thus avoiding our elevator problem.

   There are disadvantages too. For one, after a search 
   is loaded, early paging will be a little slower (not
   usually noticibly). However, every page loads just
   as fast as any other page. Previously, clicking
   the last page on a large set could be very problematic.
   The second disadvantage is export. Previously, the
   export mechanism took advantage of having pre-loaded
   forms of the objects to iterate over. Now it may have
   to fetch each from the database, without having the
   pump "primed".





   

    


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
