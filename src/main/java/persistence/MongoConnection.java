package persistence;

import com.afh.constants.Constants;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by chandan on 9/21/2015.
 */
public class MongoConnection {
    static final Logger logger = LoggerFactory.getLogger(MongoConnection.class);

    private static MongoDatabase database = null;
    private static MongoClient mongoClient =null;


    private MongoConnection() {
        logger.info("Starting Application with conenction to mongo:"+Constants.MONGO_URI);
        mongoClient = new MongoClient(new MongoClientURI(Constants.MONGO_URI));
        database = mongoClient.getDatabase(Constants.MONGO_DATABASE);
    }

    public static MongoDatabase  getInstance() {
        if(database==null) {
            synchronized (MongoConnection.class) {
                if (database == null) {
                    new MongoConnection();
                }
            }
        }
        return database;
    }

    public static void shutDown() {
        if(mongoClient!=null) {
            mongoClient.close();
        }
    }
}
