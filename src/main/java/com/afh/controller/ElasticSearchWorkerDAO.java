package com.afh.controller;

import com.afh.constants.Constants;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.node.Node;
import org.elasticsearch.search.SearchHit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import persistence.ElasticCacheConnection;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;
import static org.elasticsearch.node.NodeBuilder.nodeBuilder;

/**
 * Created by chandan on 9/5/2015.
 */
public class ElasticSearchWorkerDAO implements Runnable {


    static final Logger logger = LoggerFactory.getLogger(ElasticSearchWorkerDAO.class);
    String permalink = null;
    String username = null;

    public ElasticSearchWorkerDAO(String permalink,String username) {
        this.permalink = permalink;
        this.username =username;
    }

    @Override
    public void run() {
        logger.info(Thread.currentThread().getName()+" Start");
        ElasticSearchDAO.update(this.permalink, this.username);
        logger.info(Thread.currentThread().getName() + " End.:");
    }
}
