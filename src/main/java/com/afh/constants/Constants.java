package com.afh.constants;

/**
 * Created by chandan on 9/14/2015.
 */
public class Constants {

    //has privilege to upload pics for listings
    public final static String ADMIN_USER="master@test.com";

    public final static String APP_TITLE="Jain Traveller";
    public final static String DOMAIN_NAME="maloo.xyz";
    public final static String APP_LINK="JainTraveller";
    public final static String APP_WEBSITE="www.jaintraveller.xyz";
    public final static String APP_WEBSITE_BIZ="Jain Traveller";
    public final static String APP_ADDRESS="Sunnyvale, CA";
    public final static String APP_MESSAGE="Jiyo aur Jine Do";

    public final static String HOST_IP_REMOTE = "10.0.0.70";
    public final static String HOST_IP_LOCAL = "localhost";
    public final static String HOST_IP = HOST_IP_LOCAL;

    public final static String MONGO_URI="mongodb://"+HOST_IP;
    public static final String MONGO_DATABASE = "jains";
    public static final String MONGO_LISTING_COLLECTION = "listings";
    public static final String MONGO_LISTING_COLLECTION_REPORT = "listings_report";
    public static final String MONGO_GET_HELP_COLLECTION = "gethelp_listings";
    public static final String MONGO_STATE_COLLECTION = "states";
    public static final String MONGO_USERS_COLLECTION = "users";
    public static final String MONGO_USERS_RESET_COLLECTION = "user_resets";
    public static final String MONGO_USERS_PROFILE_COLLECTION = "user_profiles";
    public static final String MONGO_USERS_PROFILE_HISTORY_COLLECTION = "user_profiles_history";

    public static final String MONGO_SESSIONS_COLLECTION = "sessions";
    public final static Integer ELASTIC_PORT = 9300;
    public final static String ELASTIC_HOST = HOST_IP;
    public final static String ELASTIC_CLUSTER_NAME="elasticsearch";
    public final static String ELASTIC_INDEX_NAME="listings";
    public final static String ELASTIC_TYPE_NAME="newdata";
    //public final static String ELASTIC_TYPE_NAME="restaurants";

    public final static String DEFAULT_PROFILE_PHOTO="Penguins.jpg";

    public final static int ELASTIC_SEARCH_RESULTS_SIZE = 10;
    public final static int ELASTIC_SEARCH_SUGGEST_TIMEOUT = 2000;


    //Thumbnail Size
    public final static int THUMBNAIL_WIDTH = 160;
    public final static int THUMBNAIL_HEIGHT = 160;

    public final static String INFO_EMAIL = "info-jaintraveller@maloo.xyz";

    public final static String DEFAULT_EMAIL = "test@gmail.com";
    public final static String DEFAULT_EMAIL_ID = "test";
    public final static String DEFAULT_EMAIL_LINK_SEND = "https://jaintraveller.maloo.xyz/newpost";

    public final static int SITE_PORT = 443;

    public final static String UPLOAD_DIRECTORY = "D://var//www//public//";
    public final static String UPLOAD_DIRECTORY_MAC = "/local/public/www/images/";
}
