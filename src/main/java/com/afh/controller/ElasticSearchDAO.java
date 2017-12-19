package com.afh.controller;

import com.afh.constants.Constants;
import com.afh.constants.FilterType;
import com.afh.constants.FilterValue;
import com.afh.model.PaginationResponse;
import com.afh.utilities.ElasticSearchSuggestDataCreator;
import com.fasterxml.jackson.databind.util.JSONPObject;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.elasticsearch.action.count.CountResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.action.suggest.SuggestRequestBuilder;
import org.elasticsearch.action.suggest.SuggestResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.*;
import org.elasticsearch.index.search.MultiMatchQuery;
import org.elasticsearch.node.Node;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.suggest.Suggest;
import org.elasticsearch.search.suggest.completion.CompletionSuggestionBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import persistence.ElasticCacheConnection;

import java.util.*;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;
import static org.elasticsearch.node.NodeBuilder.nodeBuilder;

/**
 * Created by chandan on 9/5/2015.
 */
public class ElasticSearchDAO {

    static final Logger logger = LoggerFactory.getLogger(ElasticSearchDAO.class);
    private Client client;
    private static String INDEX_NAME=Constants.ELASTIC_INDEX_NAME;;
    private static String INDEX_TYPE=Constants.ELASTIC_TYPE_NAME;

    public ElasticSearchDAO() {
        this.client =ElasticCacheConnection.getInstance();
    }


    public static void globalSearch() {
        SearchResponse response = ElasticCacheConnection.getInstance().prepareSearch(INDEX_NAME).setTypes(INDEX_TYPE).execute().actionGet();
        showResults(response);

    }

    public static void showResults(SearchResponse response) {
        if(response!=null && response.getHits()!=null) {

            SearchHit[] results = response.getHits().getHits();

            System.out.println("Current results: " + results.length);
            for (SearchHit hit : results) {
                System.out.println("------------------------------");
                Map<String, Object> result = hit.getSource();
                for (String key : result.keySet()) {

                    if(result.get(key) !=null) {
                        logger.info(key + "::" + result.get(key).toString());
                    } else {
                        logger.info(key + "::" + null );
                    }
                }

            }
        }  else {
            logger.info("Nothing found");
        }
    }

    public static void searchDocument(String field, String value){
        SearchResponse response;
        logger.info("HERE");

        response = ElasticCacheConnection.getInstance().prepareSearch(INDEX_NAME)
                .setTypes(INDEX_TYPE)
                .setQuery(QueryBuilders.termQuery(field, value))
                .setFrom(0).setSize(60).setExplain(true)
                .execute()
                .actionGet();


        showResults(response);

    }

    /*
    public static void searchDocument(Client client,
                                      String query){
        SearchResponse response;
        logger.info("HERE");
        response = client.prepareSearch(INDEX_NAME)
                .setTypes(INDEX_TYPE)
                .setQuery(QueryBuilders.termQuery("_all", query))
                .execute()
                .actionGet();
        logger.info("HERE2");
        showResults(response);

    }
*/
    public static SearchResponse searchDocument(String query){

        SearchResponse response;

        logger.info("HERE");
        try {
            QueryBuilder qb1 = QueryBuilders.multiMatchQuery(query, "type", "skills", "city", "name", "state", "voters");
            response = ElasticCacheConnection.getInstance().prepareSearch(INDEX_NAME)
                    .setTypes(INDEX_TYPE)
                    .setQuery(qb1)
                    .execute()
                    .actionGet();
            logger.info("HERE2");
            showResults(response);
            return response;
        } catch(Exception e) {
            logger.error("Index "+INDEX_NAME + ", SEEMS TO not exist",e);
            return null;
        }

    }

    public static SearchResponse searchDocumentWithFilter(String query, FilterType filterType, FilterValue filter){

        SearchResponse response;

        logger.info("HERE in filter based search");
        try {
            QueryBuilder qb1 = QueryBuilders.filteredQuery(
                    QueryBuilders.multiMatchQuery(query, "type", "skills", "city", "name", "state", "voters"),
                    FilterBuilders.termsFilter(filterType.toString(),filter.toString())
            );

            response = ElasticCacheConnection.getInstance().prepareSearch(INDEX_NAME)
                    .setTypes(INDEX_TYPE)
                    .setQuery(qb1)
                    .execute()
                    .actionGet();
            logger.info("HERE2");
            showResults(response);
            return response;
        } catch(Exception e) {
            logger.error("Index "+INDEX_NAME + ", SEEMS TO not exist",e);
            return null;
        }

    }
    /*
    public static PaginationResponse paginationSearchDocument(String query, int index){


        PaginationResponse paginationResponse = null;
        try {
            QueryBuilder qb1 = QueryBuilders.multiMatchQuery(query, "type", "skills", "city", "name", "state", "voters");
            SearchResponse response = ElasticCacheConnection.getInstance().prepareSearch(INDEX_NAME)
                    .setTypes(INDEX_TYPE)
                    .setQuery(qb1)
                    .setFrom(index)
                    .setSize(Constants.ELASTIC_SEARCH_RESULTS_SIZE)
                    .execute()
                    .actionGet();


            CountResponse countResponse = ElasticCacheConnection.getInstance().prepareCount(INDEX_NAME)
                    .setQuery(qb1)
                    .execute()
                    .actionGet();

            long count = countResponse.getCount();
            logger.info("HERE2, total size:" + count);
            showResults(response);
            paginationResponse = new PaginationResponse();
            paginationResponse.setSearchResponse(response);
            paginationResponse.setCountResponse(countResponse);
            return paginationResponse;
        } catch(Exception e) {
            logger.error("Index "+INDEX_NAME + ", SEEMS TO not exist",e);
            return paginationResponse;
        }

    }
    */

    public static SearchResponse paginationSearchDocument(String query, int index){


        SearchResponse response = null;
        try {
            QueryBuilder qb1 = QueryBuilders.multiMatchQuery(query, "type", "skills", "city", "name", "state", "voters");
            response = ElasticCacheConnection.getInstance().prepareSearch(INDEX_NAME)
                    .setTypes(INDEX_TYPE)
                    .setQuery(qb1)
                    .setFrom(index*Constants.ELASTIC_SEARCH_RESULTS_SIZE)
                    .setSize(Constants.ELASTIC_SEARCH_RESULTS_SIZE)
                    .execute()
                    .actionGet();

            logger.info("total size: {}", response.getHits().getTotalHits());
            //showResults(response);
            return response;
        } catch(Exception e) {
            logger.error("Index "+INDEX_NAME + ", SEEMS TO not exist",e);
            return response;
        }

    }


    public static SearchResponse paginationSearchDocumentWithFilter(String query, int index, FilterType filterType, FilterValue filter){


        SearchResponse response = null;
        try {
            QueryBuilder qb1 = QueryBuilders.filteredQuery(
                    QueryBuilders.multiMatchQuery(query, "type", "skills", "city", "name", "state", "voters"),
                    FilterBuilders.termsFilter(filterType.toString(), filter.toString())
            );
            response = ElasticCacheConnection.getInstance().prepareSearch(INDEX_NAME)
                    .setTypes(INDEX_TYPE)
                    .setQuery(qb1)
                    .setFrom(index*Constants.ELASTIC_SEARCH_RESULTS_SIZE)
                    .setSize(Constants.ELASTIC_SEARCH_RESULTS_SIZE)
                    .execute()
                    .actionGet();

            logger.info("total size: {}", response.getHits().getTotalHits());
            //showResults(response);
            return response;
        } catch(Exception e) {
            logger.error("Index "+INDEX_NAME + ", SEEMS TO not exist",e);
            return response;
        }

    }


    public static SearchResponse searchDocumentAutoComplete(String query){

        SearchResponse response;

        /*
        logger.info("HERE");
        try {
            SuggestResponse suggestResponse =
                    ElasticCacheConnection.getInstance().prepareSuggest(INDEX_NAME).addSuggestion(
                            new
                                    CompletionSuggestionBuilder("testSuggestions").field("type").text("foo").size(10);

                            QueryBuilder qb1 = QueryBuilders.multiMatchQuery(query, "type", "skills", "city", "name", "state", "voters");
            response = client.prepareSearch(INDEX_NAME)
                    .setTypes(INDEX_TYPE)
                    .setQuery(qb1)
                    .execute()
                    .actionGet();
            logger.info("HERE2");
            showResults(response);
            return response;
        } catch(Exception e) {
            logger.error("Index "+INDEX_NAME + ", SEEMS TO not exist",e);
            return null;
        }
        */
        return null;

    }

    public static SearchResponse getDocBasedOnPermalink(String permalink){

        SearchResponse response;
        logger.info("Getting listing based on permalink:{}",permalink);
        if(permalink!=null) {
            try {
                //  QueryBuilder qb1 = QueryBuilders.multiMatchQuery(query, "type", "skills", "city", "name", "state", "voters");
                response = ElasticCacheConnection.getInstance().prepareSearch(INDEX_NAME)
                        .setTypes(INDEX_TYPE)
                        .setQuery(QueryBuilders.termQuery("permalink", permalink))
                        .execute()
                        .actionGet();
                //showResults(response);
                return response;
            } catch (Exception e) {
                logger.error("Index " + INDEX_NAME + ", SEEMS TO not exist", e);
                return null;
            }
        } else {
            logger.error("Seems permalnik is null, so returning nothing");
            return null;
        }
    }


    public static void getDoc(String id) {

        GetResponse getResponse = ElasticCacheConnection.getInstance().prepareGet(INDEX_NAME, INDEX_TYPE, id).execute().actionGet();

        logger.info(getResponse.getSourceAsString().toString());
        Map<String, Object> source = getResponse.getSource();


        System.out.println("------------------------------");
        System.out.println("Index: " + getResponse.getIndex());
        System.out.println("Type: " + getResponse.getType());
        System.out.println("Id: " + getResponse.getId());
        System.out.println("Version: " + getResponse.getVersion());
        System.out.println(source);

        for( String key : source.keySet()) {

            System.out.println(key + "::" + source.get(key).toString());
        }
        System.out.println("------------------------------");
    }

    public static String putJson(String user, String type, String[] skill) {
        IndexResponse response = ElasticCacheConnection.getInstance().prepareIndex(INDEX_NAME, INDEX_TYPE)
                .setSource(putJsonDocument(user, type, skill)).execute().actionGet();
        return response.getId();
    }

    public static String putJson(AskForHelpObject askForHelpObject) {

        askForHelpObject.setSuggest(ElasticSearchSuggestDataCreator.getSuggestStructureForElasticSearchIndex(askForHelpObject));
        IndexResponse response = ElasticCacheConnection.getInstance().prepareIndex(INDEX_NAME, INDEX_TYPE)
                .setSource(askForHelpObject.toString()).execute().actionGet();
        return response.getId();

    }

    public static JsonArray getSuggestionsV1(String query) {
        CompletionSuggestionBuilder compBuilder = new CompletionSuggestionBuilder("suggestapi");
        compBuilder.field("suggest");
        compBuilder.text(query);

        SuggestRequestBuilder suggestRequestBuilder = ElasticCacheConnection.getInstance().prepareSuggest(INDEX_NAME);
        suggestRequestBuilder.addSuggestion(compBuilder);
        SuggestResponse suggestResponse = suggestRequestBuilder.execute().actionGet();

        Suggest suggest = suggestResponse.getSuggest();

       // suggest.getSuggestion("suggestapi");
        Gson gson = new Gson();
        JsonObject jsonObj = gson.fromJson(suggest.toString(), JsonObject.class);

        logger.info(suggest.toString());
        JsonArray jArray = jsonObj.get("suggestapi").getAsJsonArray();
        JsonObject jsonobj1 = jArray.get(0).getAsJsonObject();


        final Iterator<JsonElement> iterator = jsonobj1.get("options").getAsJsonArray().iterator();
        JsonArray jsonArray1 = new JsonArray();
        while(iterator.hasNext()) {
            JsonElement jsonElement = iterator.next();
            jsonArray1.add(jsonElement.getAsJsonObject().get("text"));
        }

        return jsonArray1;
    }

    public static JsonArray getSuggestions(String query) {
        CompletionSuggestionBuilder compBuilder = new CompletionSuggestionBuilder("suggestapi");
        compBuilder.field("suggest");
        compBuilder.text(query);

        SuggestRequestBuilder suggestRequestBuilder = ElasticCacheConnection.getInstance().prepareSuggest(INDEX_NAME);
        suggestRequestBuilder.addSuggestion(compBuilder);
        SuggestResponse suggestResponse = suggestRequestBuilder.execute().actionGet();

        Suggest suggest = suggestResponse.getSuggest();

        // suggest.getSuggestion("suggestapi");
        Gson gson = new Gson();
        JsonObject jsonObj = gson.fromJson(suggest.toString(), JsonObject.class);
        //logger.info(suggest.toString());
        JsonArray jArray = jsonObj.get("suggestapi").getAsJsonArray();
        JsonObject jsonobj1 = jArray.get(0).getAsJsonObject();


        return jsonobj1.get("options").getAsJsonArray();
    }




    public static Map<String, Object> putJsonDocument(String user, String type, String[] skill
    ){

        Map<String, Object> jsonDocument = new HashMap<String, Object>();

        jsonDocument.put("user", user);
        jsonDocument.put("type", type);
        jsonDocument.put("skill", skill);

        return jsonDocument;
    }

    public static void deleteIndexContent() {
        DeleteResponse response = ElasticCacheConnection.getInstance().prepareDelete(INDEX_NAME, INDEX_TYPE, "_all")
                .execute()
                .actionGet();
    }

    public static void update(String permalink, String username) {

        logger.info("updating Index for permalink:"+permalink+", for username:"+username);
        if(permalink==null || username ==null) {
            logger.info("permalink or username is null, returning...");
            return;
        }
        SearchResponse response = getDocBasedOnPermalink(permalink);
        //logger.info("response: {}"+response);
        String id = null;
        List<String> voters = null;
        if (response != null && response.getHits() != null) {

            SearchHit[] results = response.getHits().getHits();

            //System.out.println("Current size: " + results.length);
            for (SearchHit hit : results) {

              //  System.out.println("------------------------------");
                Map<String, Object> result = hit.getSource();
                logger.info(result.toString());
                String perm = result.get("permalink").toString();
                if(perm.equals(permalink)) {
                    String key = "voters";
                    id = hit.getId();
                    voters = (List<String>) result.get(key);
                }

            }
            logger.info("Id for the listing in Elastic Search is :" + id);
         //   logger.info("Id is :" + voters);

        }
        if(voters!=null) {
            voters.add(username);
        }

        if(id!=null) {
            Map<String, Object> updateObject = new HashMap<String, Object>();
            String field = "voters";
            updateObject.put(field, voters);
            try {
                UpdateRequest updateRequest = new UpdateRequest();
                updateRequest.index(INDEX_NAME);
                updateRequest.type(INDEX_TYPE);
                updateRequest.id(id);
                updateRequest.doc(jsonBuilder()
                        .startObject()
                        .field(field, voters)
                        .endObject());
                ElasticCacheConnection.getInstance().update(updateRequest).get();
                logger.info("Updated Elastic Search,id:"+id + ", permalink:"+permalink);
            } catch (Exception e) {
                logger.error("Failed to update Elastic Search for {}",permalink, e);
                return;
                //throw new Exception("Error updating Elastic Search for permalink:"+permalink+", user:"+username);

            }
        } else {
            logger.info("No match in permalink, so did not update Elastic Cache for:"+permalink);
            return;
        }
    }

    public static void main(String[] args) {
/*
        String id = ElasticSearchDAO.putJson(node.client(),"abc","maloo",new String[] {"AB,CD"});
        logger.info("ID:"+id);
        ElasticSearchDAO.getDoc(node.client(),id);
        */
        //ElasticSearchDAO.searchDocument(node.client(),"user","test");

/*
        AskForHelpObject askForHelpObject = new AskForHelpObject.AskForHelpObjectBuilder()
                .type("doc")
                .name("chandan")
                .permalink("123")
                .city("palo")
                .state("CA")
                .country("US")
                .skills(Arrays.asList("pediatrian"))
                .voters(Arrays.asList("maloo123"))
                .build();
        logger.info("{}",askForHelpObject);

      ElasticSearchDAO.putJson(node.client(),askForHelpObject);
*/
        // ElasticSearchDAO.searchDocument(node.client(),"chandan");
        logger.info("HERE");
        Gson gson = new Gson();
        // ElasticSearchDAO.update(node.client(), "doctor_pediatrician_paul_protter_sunnyvale_us1441949238302","ypu");
        //logger.info("Results:"+gson.toJson(ElasticSearchDAO.getSuggestions("shr")).toString());

        logger.info("Results from filtered Search:" + gson.toJson(ElasticSearchDAO.searchDocumentWithFilter("jain", FilterType.Type, FilterValue.Restaurant)));

        ElasticCacheConnection.shutDown();
    }

    public static void deleteDocument(String permalink) {

        logger.info("deleteDocument Index for permalink:{}",permalink);
        if(permalink==null || permalink.isEmpty()) {
            logger.info("permalink is null, returning...");
            return;
        }
        SearchResponse response = getDocBasedOnPermalink(permalink);
        //logger.info("response: {}"+response);
        String id = null;
        List<String> voters = null;
        if (response != null && response.getHits() != null) {

            SearchHit[] results = response.getHits().getHits();

            //System.out.println("Current size: " + results.length);
            for (SearchHit hit : results) {

                //  System.out.println("------------------------------");
                Map<String, Object> result = hit.getSource();
                logger.info(result.toString());
                String perm = result.get("permalink").toString();
                if(perm.equals(permalink)) {
                    String key = "voters";
                    id = hit.getId();
                    voters = (List<String>) result.get(key);
                }

            }
            logger.info("Id for the listing in Elastic Search is :" + id);
            //   logger.info("Id is :" + voters);

        }


        if(id!=null) {
            Map<String, Object> updateObject = new HashMap<String, Object>();
            String field = "voters";
            updateObject.put(field, voters);
            try {
                UpdateRequest updateRequest = new UpdateRequest();
                updateRequest.index(INDEX_NAME);
                updateRequest.type(INDEX_TYPE);
                updateRequest.id(id);
                updateRequest.doc(jsonBuilder()
                        .startObject()
                        .field(field, voters)
                        .endObject());

                DeleteRequest deleteRequest = new DeleteRequest();
                deleteRequest.index(INDEX_NAME);
                deleteRequest.type(INDEX_TYPE);
                deleteRequest.id(id);


                ElasticCacheConnection.getInstance().delete(deleteRequest);

                logger.info("Deleted Elastic Search,id:"+id + ", permalink:"+permalink);
            } catch (Exception e) {
                logger.error("Failed to delete Elastic Search for {}",permalink, e);
                return;
                //throw new Exception("Error updating Elastic Search for permalink:"+permalink+", user:"+username);

            }
        } else {
            logger.info("No match in permalink, so did not update Elastic Cache for:"+permalink);
            return;
        }
    }


}
