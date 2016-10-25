package ix.test.server;

import java.io.IOException;
import java.util.UUID;

import ix.test.query.builder.SimpleQueryBuilder;
import ix.test.query.builder.SubstanceCondition;

public interface SubstanceSearcherIFace {

    void setSearchOrder(String order);

    /**
     * Get the UUIDs of all the loaded substances that have this substructure.
     * Assumes the page size for searches is 16.
     * @param smiles a kekulized SMILES string of the substructure to search for.
     * @return a Set of UUIDs that match. will never be null but may be empty.
     *
     * @throws IOException if there is a problem parsing the results.
     */
    SearchResult substructure(String smiles) throws IOException;

    /**
     * Get the UUIDs of all the loaded substances that have this substructure.
     * @param smiles a kekulized SMILES string of the substructure to search for.
     * @param rows the number of rows per page to return
     * @return a Set of UUIDs that match. will never be null but may be empty.
     *
     * @throws IOException if there is a problem parsing the results.
     */
    SearchResult substructure(String smiles, int rows, boolean wait) throws IOException;

   
    /**
     * Performs an name search, where some name must contain
     * the "words" in the provided query string, in that order.
     * Words here are strings of non-whitespace/special characters
     * that lucene considers to be non-word. 
     * @param query
     * @return
     * @throws IOException
     */
    default SearchResult nameSearch(String query) throws IOException{
        String q = new SimpleQueryBuilder()
                .where()
                .condition(SubstanceCondition.name(query).phrase())
                .build();
        return query(q);
    }
    
    /**
     * Performs a name search, trusting that the query term is
     * formatted exactly as it needs to be, with any included
     * quotes or wildcard characters
     * @param query
     * @return
     * @throws IOException
     */
    default SearchResult nameRawSearch(String query) throws IOException{
        String q = new SimpleQueryBuilder()
                .where()
                .condition(SubstanceCondition.name(query).raw())
                .build();
        return query(q);
    }
    
    /**
     * Performs an exact name search, where some name must match
     * the provided query string, allowing for case-insensitive
     * and certain token/white space characters removed. 
     * @param query
     * @return
     * @throws IOException
     */
    default SearchResult exactNameSearch(String query) throws IOException{
        String q = new SimpleQueryBuilder()
                .where()
                .condition(SubstanceCondition.name(query).exact())
                .build();
        return query(q);
    }
    
    /**
     * Performs an code search, where some code must contain
     * the "words" in the provided query string, in that order.
     * Words here are strings of non-whitespace/special characters
     * that lucene considers to be non-word. 
     * @param query
     * @return
     * @throws IOException
     */
    default SearchResult codeSearch(String query) throws IOException{
        String q = new SimpleQueryBuilder()
                .where()
                .condition(SubstanceCondition.code(query).phrase())
                .build();
        return query(q);
    }
    
    
    /**
     * Performs an exact term search, where some value must match
     * the provided query string, allowing for case-insensitive
     * and certain token/white space characters removed. 
     * @param query
     * @return
     * @throws IOException
     */
    default SearchResult exactSearch(String query) throws IOException{
        String q = new SimpleQueryBuilder()
                .where()
                .globalMatchesExact(query)
                .build();
        return query(q);
    }
    

    /**
     * Get the UUIDs of all the loaded substances
     * @return a Set of UUIDs that match. will never be null but may be empty.
     * @throws IOException if there is a problem parsing the results.
     */
    default SearchResult query(UUID uuid) throws IOException{
        String q = new SimpleQueryBuilder()
                .where()
                .condition(SubstanceCondition.uuid(uuid.toString()).exact())
                .build();
        return query(q);
    };

    /**
     * Get the UUIDs of all the loaded substances
     * @return a Set of UUIDs that match. will never be null but may be empty.
     * @throws IOException if there is a problem parsing the results.
     */
    SearchResult query(String queryString) throws IOException;

    /**
     * Get the UUIDs of all the loaded substances
     * @return a Set of UUIDs that match. will never be null but may be empty.
     * @throws IOException if there is a problem parsing the results.
     */
    default SearchResult all() throws IOException{
        String q = new SimpleQueryBuilder()
                .where()
                .allDocsQuery()
                .build();
        return query(q);
    }

}