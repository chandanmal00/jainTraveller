package persistence;

import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.node.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.afh.constants.Constants.*;

/**
 * Created by chandan on 9/21/2015.
 */
public class ElasticCacheTrConnection {

    private static Client client = null;
    private static Node node = null;
    static final Logger logger = LoggerFactory.getLogger(ElasticCacheTrConnection.class);
    private ElasticCacheTrConnection() {
        Settings settings = ImmutableSettings.settingsBuilder()
                .put("cluster.name", ELASTIC_CLUSTER_NAME).build();
        client = new TransportClient(settings)
                .addTransportAddress(new InetSocketTransportAddress(ELASTIC_HOST, ELASTIC_PORT));

    }

    public static Client getInstance() {

        if(client==null) {
            synchronized(ElasticCacheTrConnection.class) {
                if(client==null) {
                    logger.info("Initializing Elastic Search Connection, for name:"+ ELASTIC_CLUSTER_NAME);
                    new ElasticCacheTrConnection();
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
