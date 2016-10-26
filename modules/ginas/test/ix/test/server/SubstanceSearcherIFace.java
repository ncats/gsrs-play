package ix.test.server;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;

import ix.test.query.builder.SimpleQueryBuilder;
import ix.test.query.builder.SubstanceCondition;
import ix.test.server.SubstanceSearcher.WebExportRequest;
import play.libs.ws.WSResponse;

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
     * Get the UUIDs of all the loaded substances that have similarity
     * above the specified cutoff
     * @param smiles a kekulized SMILES string of structure for similarity search
     * @return a Set of UUIDs that match. will never be null but may be empty.
     *
     * @throws IOException if there is a problem parsing the results.
     */
    SearchResult similarity(String smiles, double cutoff) throws IOException;

    /**
     * Get the UUIDs of all the loaded substances that have a defined moiety
     * which matches the given structure by a lychi level 3 match.
     * This is also called "flex match"
     * 
     * @param smiles a kekulized SMILES string of structure search
     * @return a Set of UUIDs that match. will never be null but may be empty.
     *
     * @throws IOException if there is a problem parsing the results.
     */
    SearchResult flex(String smiles) throws IOException;
    
    
    /**
     * Get the UUIDs of all the loaded substances that have a defined structure
     * which matches the given structure by a lychi level 4 match.
     * This is also called "exact match"
     * 
     * @param smiles a kekulized SMILES string of structure search
     * @return a Set of UUIDs that match. will never be null but may be empty.
     *
     * @throws IOException if there is a problem parsing the results.
     */
    SearchResult exact(String smiles) throws IOException;

    
    
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