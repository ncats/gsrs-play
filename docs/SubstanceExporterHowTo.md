#How To Write a Substance Exporter

Substances found in a `SearchResult` can be exported and written to an `OutputStream` in another format.  Currently,
this is used to be able to download the search results as either an SDF file or spreadsheet.

Clients may write their own custom exporters to either replace the built-in exporters or add new exporters.

##Exporter interface
An `Exporter` instance is used to write out a single export opperation.
The Exporter interface is very simple:
```
package ix.ginas.exporters;

public interface Exporter<T> extends Closeable{
	void export(T obj) throws IOException;
}
```

For every Object (A `Substance`, for the search result download) to export, the `export( obj)` method will be called.
When all the Substances have been iterated over, the exporter's `close()` method is called signaling that it is done.