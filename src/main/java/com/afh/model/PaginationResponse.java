package com.afh.model;

import org.elasticsearch.action.count.CountResponse;
import org.elasticsearch.action.search.SearchResponse;

/**
 * Created by chandan on 9/24/2015.
 */
public class PaginationResponse {
    private SearchResponse searchResponse;
    private CountResponse countResponse;

    public CountResponse getCountResponse() {
        return countResponse;
    }

    public void setCountResponse(CountResponse countResponse) {
        this.countResponse = countResponse;
    }

    public SearchResponse getSearchResponse() {
        return searchResponse;
    }

    public void setSearchResponse(SearchResponse searchResponse) {
        this.searchResponse = searchResponse;
    }
}

