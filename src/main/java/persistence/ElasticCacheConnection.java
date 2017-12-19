package persistence;

import com.afh.constants.Constants;
import org.elasticsearch.client.Client;
import org.elasticsearch.node.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.elasticsearch.node.NodeBuilder.nodeBuilder;

/**
 * Created by chandan on 9/21/2015.
 */
public class ElasticCacheConnection {

    private static Client client = null;
    private static Node node = null;
    static final Logger logger = LoggerFactory.getLogger(ElasticCacheConnection.class);
    private ElasticCacheConnection() {
        node = nodeBuilder().clusterName(Constants.ELASTIC_CLUSTER_NAME).client(true).node();
        client = node.client();
    }

    public static Client getInstance() {

        if(client==null) {
            synchronized(ElasticCacheConnection.class) {
                if(client==null) {
                    logger.info("Initializing Elastic Search Connection, for name:"+Constants.ELASTIC_CLUSTER_NAME);
                    new ElasticCacheConnection();
                }
            }

        }

        return client;
    }

    public static void shutDown() {
        if(client!=null) {
            client.close();
            client = null;
        }

        if(node!=null) {
            node.close();
            while(!node.isClosed()) {
                logger.info("Still not closed: node");
            }
            node = null;
        }
    }

}
