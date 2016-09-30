G-SRS v 1.2.06
==============

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

3. Massive refactoring has greatly improves readability of the 
   codebase. There are many places where dead, deprecated, or 
   incomplete code had been present. Most of it is gone now. In 
   addition, many duplicative function calls have been abstracted out.

4. We used to fetch out all the possible Classes that we'd need
   to consider when fetching for a generic object. It turns out
   that was only necessary for fetching from the lucene index.
   Not only have we reduced the number of reflection calls made
   (by other means as well), but we have eliminated the need for
   6 or 7 database calls that were previously used in fetching
   edit history.

5. It used to be that the search results were populated by a
   thread which used an open IndexSearcher from lucene. It turns
   out this caused a problem, as the parent spawning thread would
   release the IndexSearcher as soon as the method call was complete.
   For rapid responding database fetches, this wasn't a problem. But
   it is a problem if database fetches are slow, as it's more likely
   that the IndexSearcher will be invalid by the time it's used. Now,
   when we spawn the new thread, we give it a new IndexSearcher,
   which will also be released upon completion.

6. Before, all browse and search requests from the 
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
   usually noticably). However, every page loads just
   as fast as any other page. Previously, clicking
   the last page on a large set could hang the system
   until all the results were fetched.
   The second disadvantage is export. Previously, the
   export mechanism took advantage of having pre-loaded
   forms of the objects to iterate over. Now it may have
   to fetch each from the database, without having the
   pump "primed".

7. We now use an `EntityFetcher` for acquiring objects
   from the database from a given `Key` (kind + id). This
   `EntityFetcher` has some configurability on how to use
   the cache. By default, now, all calls are fetched by their
   key from a globally accessible cache. However, it can
   be set instead to have the Fetcher itself store the object
   after an initial call (useful for a small, user-specific
   fetch that should not touch the global cache). It can also
   be set to eagerly fetch the objects, which is essentially
   equivalent to the previous mechanism used. There are other
   options as well, but those are the most useful for debugging.

8. Previously, updates from the API were handled differently than
   updates from anywhere else. Specifically: edit and locking
   mechanisms were only loosely in place for the API calls,
   while they were more rigorously enforced from other areas.
   Now all edits are preferentially handled by calling a 
   `performChange` method in `EntityPersistAdapter`. A call
   to this does 4 things. First, it gets a `Key` for the
   object to be changed, and sets a lock based on that `Key`.
   Second, it retrieves the current version of that record
   from the database based on that `Key`. Third, it creates
   an `Edit` with the version and serialized form of the object,
   storing it to be saved after the change is performed. Third,
   it performs the change using the provided change operation,
   which should also do any saving needed. Last, it stores the
   new form of the object in the Edit, saves, and unlocks the
   Key.
 