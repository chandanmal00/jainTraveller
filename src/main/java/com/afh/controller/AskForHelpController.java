package com.afh.controller;

import com.afh.constants.Constants;
import com.afh.constants.FilterType;
import com.afh.constants.FilterValue;
import com.afh.mail.SendMail;
import com.afh.ocr.IngredientsModel;
import com.afh.utilities.ControllerUtilities;
import com.afh.utilities.ElasticCacheSuggestorCallable;
import com.afh.worker.MongoDbJainUpdateWorker;
import com.afh.worker.MongoDbUpdateCommentWorker;
import com.afh.worker.MongoDbUpdateWorker;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.name.Rename;
import net.sourceforge.tess4j.Tesseract;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.SearchHit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import persistence.ElasticCacheConnection;
import persistence.MongoConnection;
import spark.Request;
import spark.Response;
import spark.Route;

import javax.servlet.http.Cookie;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;

import static spark.Spark.*;
/**
 * This class encapsulates the controllers for the Ask for Help web application.
 * Entry point into the web application.
 */
public class AskForHelpController {
    private final Configuration cfg;
    private final AskForHelpDAO askForHelpDAO;
    private final UserDAO userDAO;
    private final SessionDAO sessionDAO;
    private final StateDAO stateDAO;
    static final Logger logger = LoggerFactory.getLogger(AskForHelpController.class);
    static final Logger appLogger = LoggerFactory.getLogger("AppLogging");
    final static ThreadPoolExecutor MONGO_DB_THREAD_POOL = new ThreadPoolExecutor(2, // core size
            30, // max size
            10, // idle timeout
            TimeUnit.SECONDS,
            new ArrayBlockingQueue<Runnable>(40));

    final static ThreadPoolExecutor ELASTIC_SEARCH_THREAD_POOL = new ThreadPoolExecutor(2, // core size
            30, // max size
            30, // idle timeout
            TimeUnit.SECONDS,
            new ArrayBlockingQueue<Runnable>(40));


    public static void main(String[] args) throws IOException {
        logger.info("Starting Application:");
        try {
            if (args.length == 0) {
                new AskForHelpController(Constants.MONGO_URI);
            }
            else {
                new AskForHelpController(args[0]);
            }
        } catch(Exception e) {
            logger.info("Application shutting down, closing all connections:", e);
            try {
                MONGO_DB_THREAD_POOL.shutdown();
                ELASTIC_SEARCH_THREAD_POOL.shutdown();
                while (!MONGO_DB_THREAD_POOL.isTerminated() || !ELASTIC_SEARCH_THREAD_POOL.isTerminated()) {
                    Thread.sleep(1000);
                    logger.info("waiting for MONGO and ELASTIC threads to shutdown");
                }
            } catch(Exception ez) {
                logger.error("Error in clsoing Threads",ez);
            } finally {
                ElasticCacheConnection.shutDown();
                MongoConnection.shutDown();
                logger.info("Shutdown Complete");
            }
        }
    }

    public AskForHelpController(String mongoURIString) throws IOException {
        logger.info("Starting Application with Connection to mongo:"+mongoURIString);
        askForHelpDAO = new AskForHelpDAO();
        userDAO = new UserDAO();
        sessionDAO = new SessionDAO();
        stateDAO = new StateDAO();

        String passwordForCertificate = "badman123";
        String keyStoreName = "D:\\ssl\\jainTraveller\\keystore.jks";
        String truststoreFile = "D:\\ssl\\truststore.ts";

        cfg = createFreemarkerConfiguration();


        setPort(8082);
        //setSecure(keyStoreName, passwordForCertificate, truststoreFile, passwordForCertificate);
        //externalStaticFileLocation("D:\\var\\www\\public\\");
        externalStaticFileLocation("/local/public/www/");
        staticFileLocation("/public");
        //setSecure(keyStoreName,passwordForCertificate,truststoreFile,passwordForCertificate);

        initializeRoutes();
        initializeGetHelpListingRoutes();
        initializeListingRoutes();
        initializeNewListingRoutes();
        initializeProfileRoutes();
        initializeSearchRoutes();
        initializeStandardLoginSignUpRoutes();
    }

    abstract class FreemarkerBasedRoute extends Route {
        final Template template;
        final TemplateOverride templateOverride = new TemplateOverride();

        /**
         * Constructor
         *
         * @param path The route path which is used for matching. (e.g. /hello, users/:name)
         */
        protected FreemarkerBasedRoute(final String path, final String templateName) throws IOException {
            super(path);

            template = cfg.getTemplate(templateName);
            templateOverride.setTemplate(template);

        }

        @Override
        public Object handle(Request request, Response response) {
            StringWriter writer = new StringWriter();
            templateOverride.setRequest(request);
            templateOverride.setResponse(response);
            try {
                doHandle(request, response, writer);
            } catch (Exception e) {
                logger.error("Internal Error while reading the request,",e);
                response.redirect("/internal_error");
            }
            return writer;
        }

        protected abstract void doHandle(final Request request, final Response response, final Writer writer)
                throws IOException, TemplateException;

    }

    private void initializeStandardLoginSignUpRoutes() throws IOException {

        //New

        get(new FreemarkerBasedRoute("/login", "login.ftl") {
            @Override
            protected void doHandle(Request request, Response response, Writer writer) throws IOException, TemplateException {
                //SimpleHash root = new SimpleHash();
                String username = sessionDAO.findUserNameBySessionId(ControllerUtilities.getSessionCookie(request));
                if (username != null) {
                    response.redirect("/welcome");
                } else {
                    HashMap<String, Object> root = new HashMap<String, Object>();
                    String permalink = request.queryParams("permalink");
                    root.put("username", "");
                    root.put("permalink", permalink);
                    root.put("login", "true");

                    templateOverride.process(root, writer);
                }
            }
        });

        // process output coming from login form. On success redirect folks to the welcome page
        // on failure, just return an error and let them try again.
        post(new FreemarkerBasedRoute("/login", "login.ftl") {
            @Override
            protected void doHandle(Request request, Response response, Writer writer) throws IOException, TemplateException {

                String username = request.queryParams("email");
                String password = request.queryParams("password");
                System.out.println("Login: User submitted: " + username + "  " + password);

                Document user = userDAO.validateLogin(username, password);

                if (user != null) {
                    // valid user, let's log them in
                    String sessionID = sessionDAO.startSession(user.get("_id").toString());

                    if (sessionID == null) {
                        response.redirect("/internal_error");
                    } else {
                        // set the cookie for the user's browser
                        String permalink = request.queryParams("permalink");
                        ControllerUtilities.deleteAllCookies(request, response);
                        response.raw().addCookie(new Cookie("session", sessionID));
                        Document userProfile = userDAO.getUserProfile(username);
                        if (userProfile != null) {
                            logger.info("Looks like we have profile for username:{}, doc:{}", username, userProfile);
                            ControllerUtilities.deleteProfileCookie(request, response);
                            response.raw().addCookie(new Cookie("profile", "true"));
                        }
                        if (permalink != null && StringUtils.isNotBlank(permalink)) {
                            response.redirect("/post/" + permalink);
                        } else {
                            response.redirect("/welcome");
                        }
                    }
                } else {
                    HashMap<String, Object> root = new HashMap<String, Object>();
                    root.put("username", StringEscapeUtils.escapeHtml4(username));
                    root.put("password", "");
                    root.put("login_error", "Invalid Login");
                    root.put("login", "true");
                    templateOverride.process(root, writer);
                }
            }
        });



        // present signup form for blog
        get(new FreemarkerBasedRoute("/signup", "login.ftl") {
            @Override
            protected void doHandle(Request request, Response response, Writer writer)
                    throws IOException, TemplateException {

                String permalink = request.queryParams("permalink");
                logger.info("Signup request from user,{}", permalink);
                HashMap<String, Object> root = new HashMap<String, Object>();

                // initialize values for the form.
                root.put("username", "");
                root.put("password", "");
                root.put("email", "");
                root.put("permalink", permalink);
                root.put("signup", "true");

                templateOverride.process(root, writer);
            }
        });

        post(new FreemarkerBasedRoute("/signup", "login.ftl") {
            @Override
            protected void doHandle(Request request, Response response, Writer writer) throws IOException, TemplateException {
                String email = request.queryParams("email");
                String username = email;
                String password = request.queryParams("password");
                String verify = request.queryParams("password_confirm");
                logger.info("handler for Signup request from user,{}" + username);

                HashMap<String, String> root = new HashMap<String, String>();
                root.put("username", StringEscapeUtils.escapeHtml4(username));
                root.put("email", StringEscapeUtils.escapeHtml4(email));

                if (ControllerUtilities.validateSignupV2(username, password, verify, email, root)) {
                    // good user
                    logger.info("Signup: Creating user with: " + username + " " + password);
                    if (!userDAO.addUser(username, password, email)) {
                        // duplicate user
                        logger.error("Username already in use, Please choose another, input:{}", username);
                        root.put("username_error", "Username already in use!!, Please choose another");
                        templateOverride.process(root, writer);
                    } else {
                        // good user, let's start a session
                        String sessionID = sessionDAO.startSession(username);
                        logger.info("Session ID is" + sessionID);
                        String permalink = request.queryParams("permalink");
                        response.raw().addCookie(new Cookie("session", sessionID));
                        if (permalink != null && StringUtils.isNotBlank(permalink)) {
                            /*
                            String cookie = ControllerUtilities.getSessionCookie(request);
                            String username = sessionDAO.findUserNameBySessionId(cookie);
                            */
                            logger.info("permalink:{} present so redirecting", permalink);
                            response.redirect("/post/" + permalink);
                        } else {
                            response.redirect("/welcome");
                        }
                    }
                } else {
                    // bad signup
                    //root.put("username_error", "Validation issues, Please try again respecting each field type or the passwords are not identical");
                    logger.error("User Registration did not validate, user:{}", username);
                    root.put("signup_error", "Validation Issue");
                    root.put("signup", "true");
                    templateOverride.process(root, writer);
                }
            }
        });

        // present signup form for blog
        get(new FreemarkerBasedRoute("/reset", "reset.ftl") {
            @Override
            protected void doHandle(Request request, Response response, Writer writer)
                    throws IOException, TemplateException {


                logger.info("Reset request");
                if( ControllerUtilities.getSessionCookie(request)==null )  {
                    //String username = sessionDAO.findUserNameBySessionId(ControllerUtilities.getSessionCookie(request));
                    response.raw().addCookie(new Cookie("session", sessionDAO.startVanillaSession()));
                }
                HashMap<String, Object> root = new HashMap<String, Object>();
                // initialize values for the form.
                templateOverride.process(root, writer);
            }
        });

        post(new FreemarkerBasedRoute("/reset", "reset_success.ftl") {
            @Override
            protected void doHandle(Request request, Response response, Writer writer) throws IOException, TemplateException {
                String email = request.queryParams("email");
                String session = request.queryParams("sessionId");

                final String sessionCookie = ControllerUtilities.getSessionCookie(request);
                logger.info("email:{}, session:{}, sessionCookie:{}", email, session, sessionCookie);
                HashMap<String, String> root = new HashMap<String, String>();
                if (session == null ||
                        StringUtils.isBlank(session) ||
                        (session != null && session.equals(sessionCookie))) {
                    logger.info("Post handler for reset request from email,{},{},{}", email, session, sessionCookie);
                    userDAO.addResetUserRequest(email);

                } else {
                    logger.warn("Session mismatch session={}, comparedTo:{}", session, sessionCookie);
                }
                root.put("email", email);
                templateOverride.process(root, writer);
            }
        });


        // allows the user to logout of the blog
        get(new FreemarkerBasedRoute("/logout", "login.ftl") {
            @Override
            protected void doHandle(Request request, Response response, Writer writer) throws IOException, TemplateException {

                String sessionID = ControllerUtilities.getSessionCookie(request);
                HashMap<String, String> root = new HashMap<String, String>();
                root.put("logout", "true");
                root.put("login", "true");
                if (sessionID != null) {
                    // deletes from session table
                    sessionDAO.endSession(sessionID);
                    ControllerUtilities.deleteAllCookies(request, response);
                    //response.redirect("/login");
                }
                templateOverride.process(root, writer);
            }
        });


    }

    private void initializeSearchRoutes() throws IOException {
        get(new FreemarkerBasedRoute("/search/:phrase", "search_template.ftl") {
            @Override
            protected void doHandle(Request request, Response response, Writer writer) throws IOException, TemplateException {
                String phrase = request.params(":phrase");
                String filter = request.queryParams("filter");

                String username = sessionDAO.findUserNameBySessionId(ControllerUtilities.getSessionCookie(request));
                logger.info("/search for phrase:{}, filterType:{}", phrase, StringEscapeUtils.escapeHtml4(filter));
                //Here you need to call Elastic Search
                SearchResponse searchResponse = null;
                int index = 0;
                Boolean filterFlag = false;

                try {
                    if(filter!=null
                            && filter.split("_").length==2
                            && FilterType.valueOf(filter.split("_")[0])!=null
                            && FilterValue.valueOf(filter.split("_")[1])!=null
                            ) {

                        filterFlag = true;
                    }

                } catch(Exception e) {
                    logger.error("Exception while reading filter:{}",filter,e);
                }

                if(filterFlag) {
                    logger.info("Seems a filtered search");
                    String[] filterArr=filter.split("_");
                    searchResponse = ElasticSearchDAO.paginationSearchDocumentWithFilter(phrase, index,
                            FilterType.valueOf(filterArr[0]),FilterValue.valueOf(filterArr[1]));
                } else {
                    searchResponse = ElasticSearchDAO.paginationSearchDocument(phrase, index);
                }


                if (searchResponse != null) {
                    long totalHits = searchResponse.getHits().getTotalHits();
                    logger.info("TotalHist::" + totalHits);
                    SearchHit[] results = searchResponse.getHits().getHits();
                    List<SearchHit> searchHitList = Arrays.asList(results);
                    // empty comment to hold new comment in form at bottom of blog entry detail page

                    HashMap<String, Object> root = new HashMap<String, Object>();
                    double ceil = Math.ceil(totalHits / Constants.ELASTIC_SEARCH_RESULTS_SIZE);
                    logger.info("Ceil:{} count:{}", ceil, totalHits);

                    logger.info("Index:" + index);
                    root.put("index", index);
                    root.put("searchHitList", searchHitList);
                    root.put("username", username);
                    root.put("countPages", ceil);
                    root.put("count", totalHits);
                    String displayPhrase = phrase;
                    if (phrase.length() > 50) {
                        displayPhrase = phrase.substring(0, 50);
                    }

                    root.put("search", displayPhrase);
                    root.put("searchActual", phrase);
                    templateOverride.process(root, writer);


                } else {

                    logger.info("Nothing found");
                    response.redirect("/search");
                }
            }
        });

        /**
         * TODO need to combine both codes in below and above search call code
         */

        get(new FreemarkerBasedRoute("/search/:phrase/:index", "search_template.ftl") {
            @Override
            protected void doHandle(Request request, Response response, Writer writer) throws IOException, TemplateException {
                String phrase = request.params(":phrase");
                int index = 0;
                try {
                    index = Integer.parseInt(request.params(":index"));
                } catch(Exception e) {
                    logger.error("Bad index, so resorting to index 0",e);
                }
                String username = sessionDAO.findUserNameBySessionId(ControllerUtilities.getSessionCookie(request));
                logger.info("/search for phrase:{} for page:{}", phrase, index);
                String filter = request.queryParams("filter");
                logger.info("/search for phrase:{}, filterType:{}", phrase, StringEscapeUtils.escapeHtml4(filter));
                //Here you need to call Elastic Search
                SearchResponse searchResponse = null;
                Boolean filterFlag = false;

                try {
                    if(filter!=null
                            && filter.split("_").length==2
                            && FilterType.valueOf(filter.split("_")[0])!=null
                            && FilterValue.valueOf(filter.split("_")[1])!=null
                            ) {

                        filterFlag = true;
                    }



                } catch(Exception e) {

                    logger.error("Exception while reading filter:{}",filter,e);
                }

                if(filterFlag
                        ) {
                    logger.info("Seems a filtered search");
                    String[] filterArr=filter.split("_");
                    searchResponse = ElasticSearchDAO.paginationSearchDocumentWithFilter(phrase, index,
                            FilterType.valueOf(filterArr[0]),FilterValue.valueOf(filterArr[1]));
                } else {
                    searchResponse = ElasticSearchDAO.paginationSearchDocument(phrase, index);
                }

                //Here you need to call Elastic Search
                if (searchResponse!=null){
                    long totalHits = searchResponse.getHits().getTotalHits();
                    logger.info("TotalHist::"+ totalHits);
                    SearchHit[] results = searchResponse.getHits().getHits();
                    List<SearchHit> searchHitList = Arrays.asList(results);
                    // empty comment to hold new comment in form at bottom of blog entry detail page
                    double ceil = Math.ceil(totalHits / Constants.ELASTIC_SEARCH_RESULTS_SIZE);
                    logger.info("Ceil:{} count:{}",ceil,totalHits);
                    HashMap<String, Object> root = new HashMap<String, Object>();

                    logger.info("Index:"+index);
                    root.put("index", index);
                    root.put("searchHitList", searchHitList);
                    root.put("username", username);
                    root.put("countPages",Math.ceil(ceil));
                    root.put("count", totalHits);
                    String displayPhrase = phrase;
                    if (phrase.length() > 50) {
                        displayPhrase = phrase.substring(0, 50);
                    }

                    root.put("search", displayPhrase);
                    root.put("searchActual", phrase);
                    templateOverride.process(root, writer);

                } else {

                    logger.info("Nothing found");
                    response.redirect("/search");
                }
            }
        });






        // used to display actual blog post detail page
        post(new FreemarkerBasedRoute("/search", "search.ftl") {
            @Override
            protected void doHandle(Request request, Response response, Writer writer) throws IOException, TemplateException {
                String phrase = StringEscapeUtils.escapeHtml4(request.queryParams("search"));
                String filter = StringEscapeUtils.escapeHtml4(request.queryParams("filter"));
                //String username = sessionDAO.findUserNameBySessionId(ControllerUtilities.getSessionCookie(request));

                logger.info("Post search: get phrase:{}, filter:{}" ,phrase ,filter);

                if (phrase.equals("")) {
                    // redisplay page with errors
                    HashMap<String, String> root = new HashMap<String, String>();
                    root.put("errors", "Search is Empty, Go ahead and try the new improved Search");
                    root.put("search", phrase);
                    templateOverride.process(root, writer);
                } else {
                    //Here you need to call Elastic Search
                    //response.redirect("/search/" + phrase);
                    response.redirect("/search/" + phrase + "?filter="+filter);
                }
            }
        });

        get(new FreemarkerBasedRoute("/search", "search.ftl") {
            @Override
            protected void doHandle(Request request, Response response, Writer writer) throws IOException, TemplateException {

                HashMap<String, Object> root = new HashMap<String, Object>();
                String username = sessionDAO.findUserNameBySessionId(ControllerUtilities.getSessionCookie(request));
                root.put("username", username);
                templateOverride.process(root, writer);
            }
        });

        get(new FreemarkerBasedRoute("/search/", "search.ftl") {
            @Override
            protected void doHandle(Request request, Response response, Writer writer) throws IOException, TemplateException {
                HashMap<String, Object> root = new HashMap<String, Object>();
                String username = sessionDAO.findUserNameBySessionId(ControllerUtilities.getSessionCookie(request));
                root.put("username", username);
                templateOverride.process(root, writer);
            }
        });

        get(new Route("/get_suggestions/:query") {
            @Override
            public Object handle(Request request, Response response) {
                logger.info("GET getting Suggestions");
                String query = request.params(":query");

                if (query != null) {

                    //Create MyCallable instance
                    Callable<JsonArray> callable = new ElasticCacheSuggestorCallable(query);
                    Future<JsonArray> future = ELASTIC_SEARCH_THREAD_POOL.submit(callable);
                    try {
                        logger.info("Getting Future obj for query:{}", query);
                        JsonArray jsonArray = future.get(Constants.ELASTIC_SEARCH_SUGGEST_TIMEOUT + 500, TimeUnit.MILLISECONDS);
                        System.out.println("Done with getting");
                        return jsonArray;
                    } catch (Exception e) {
                        logger.info("Timeout while getting Suggestions for query :{}, timeout:{}", Constants.ELASTIC_SEARCH_SUGGEST_TIMEOUT, e);
                        return "failure";
                    }

                } else {
                    return "failure";
                }
            }
        });


    }

    private void initializeProfileRoutes() throws IOException {

        get(new FreemarkerBasedRoute("/profile", "profile.ftl") {
            @Override
            protected void doHandle(Request request, Response response, Writer writer) throws IOException, TemplateException {
                String username = sessionDAO.findUserNameBySessionId(ControllerUtilities.getSessionCookie(request));

                logger.info("Calling route /profile: for username:" + username);
                if (username == null) {
                    logger.info("userName null, so redirecting to signup...");
                    response.redirect("/login");
                    logger.info("Returning:");
                    return;
                } else {
                    Map<String, Object> root = new HashMap<String, Object>();
                    root.put("username", username);
                    root.put("defaultPhoto", Constants.DEFAULT_PROFILE_PHOTO);

                    Document userProfile = userDAO.getUserProfile(username);
                    if (userProfile != null) {
                        root.put("userProfile", userProfile);
                    }
                    templateOverride.process(root, writer);
                }
            }
        });

        // handle the new post submission
        post(new FreemarkerBasedRoute("/profile", "profile.ftl") {
            @Override
            protected void doHandle(Request request, Response response, Writer writer)
                    throws IOException, TemplateException {

                String username = sessionDAO.findUserNameBySessionId(ControllerUtilities.getSessionCookie(request));

                if (username == null) {
                    response.redirect("/login");    // only logged in users can post to blog
                } else {

                    final String UPLOAD_DIRECTORY = Constants.UPLOAD_DIRECTORY_MAC;
                    Gson gson = new Gson();
                    Message message = new Message();
                    if (ServletFileUpload.isMultipartContent(request.raw())) {
                        InputStream stream = null;
                        FileOutputStream fout = null;
                        try {
                            Map<String, Object> root = new HashMap<String, Object>();
                            root.put("username", username);
                            UserProfile userprofile = new UserProfile();

                            ServletFileUpload upload = new ServletFileUpload();
                            final int BUFF_SIZE = 100000;
                            final byte[] buffer = new byte[BUFF_SIZE];
                            Boolean flagImageNotUpdated = false;
                            FileItemIterator iter = upload.getItemIterator(request.raw());
                            while (iter.hasNext()) {
                                FileItemStream item = iter.next();
                                String name = item.getFieldName();
                                stream = item.openStream();
                                if (item.isFormField()) {
                                    String value = Streams.asString(stream);
                                    root.put(name, value.trim());
                                    logger.info("Form field " + name + " with value detected:" + value);
                                    //IOUtils.closeQuietly(stream);
                                } else {

                                    logger.info("NON Form field " + name);
                                    root.put("profilephoto", item.getName());
                                    logger.info("File field " + name + " with file name "
                                            + item.getName() + " detected.");
                                    // Process the input stream
                                    if (item.getName() == null
                                            || StringUtils.isBlank(item.getName())
                                            || item.getName().equals(Constants.DEFAULT_PROFILE_PHOTO)) {
                                        logger.info("Image file not being updated");
                                        flagImageNotUpdated = true;

                                    } else {
                                        logger.info("Image file getting updated");
                                        userprofile.setProfilephoto(root.get("profilephoto").toString());
                                        logger.info("name::" + item.getName());
                                        String name1 = new File(item.getName()).getName();
                                        String[] nameArr = name1.split("\\.");
                                        String finalName = UPLOAD_DIRECTORY + File.separator + nameArr[0];
                                        logger.info(" 1 FinalName::" + finalName);
                                        if (nameArr.length >= 2) {
                                            finalName = finalName + "." + StringUtils.join(nameArr, '.', 1, nameArr.length);
                                        }
                                        String fileType = finalName.substring(finalName.lastIndexOf(".")+1);
                                        logger.info("FinalName::"+finalName+", fileType:"+fileType);

                                        fout = new FileOutputStream(finalName);
                                        while (true) {
                                            synchronized (buffer) {
                                                int amountRead = stream.read(buffer);
                                                if (amountRead == -1) {
                                                    break;
                                                }
                                                fout.write(buffer, 0, amountRead);
                                            }
                                        }
                                        IOUtils.closeQuietly(stream);
                                        try {
                                            logger.info("Creating Thumbnails for :{}",finalName);
                                            Thumbnails.of(finalName)
                                                    .size(Constants.THUMBNAIL_WIDTH, Constants.THUMBNAIL_HEIGHT)
                                                    .outputFormat(fileType)
                                                    .toFiles(Rename.PREFIX_DOT_THUMBNAIL);


                                        } catch(Exception e) {
                                            logger.error("Failed to create Thumbnail for:{}",finalName,e);
                                        }
                                    }
                                }
                                IOUtils.closeQuietly(stream);
                                IOUtils.closeQuietly(fout);

                            }

                            if (flagImageNotUpdated) {
                                root.put("profilephoto", root.get("oldFileName"));
                            }

                            userprofile.setAddress(root.get("address").toString());
                            userprofile.setEmail(root.get("email").toString());
                            userprofile.setCity(root.get("city").toString());
                            userprofile.setCountry(root.get("country").toString());
                            userprofile.setState(root.get("state").toString());
                            userprofile.setFirstname(root.get("firstname").toString());
                            userprofile.setLastname(root.get("lastname").toString());
                            userprofile.setMobile(root.get("mobile").toString());
                            userprofile.setProfilephoto(root.get("profilephoto").toString());

                            if (StringUtils.isBlank(userprofile.getFirstname())
                                    || StringUtils.isBlank((userprofile.getLastname()))) {
                                logger.warn("One of the required fields is empty, firstname:{} or lastname:{}",
                                        userprofile.getFirstname(), userprofile.getLastname());

                                root.put("username", username);
                                root.put("errors", "One of the required fields is empty, firstname or lastname!!");
                                root.put("defaultPhoto", root.get("oldFileName"));
                                templateOverride.process(root, writer);
                                return;
                            }

                            root.put("userProfile", userprofile);

                            logger.info("Done:" + gson.toJson(userprofile));

                            Document userprofile1 = userDAO.addUserProfile(username, userprofile);
                            if (userprofile1 != null) {
                                logger.info("Looks like we have profile for username:{}, doc:{}", username, userprofile1);
                                ControllerUtilities.deleteProfileCookie(request, response);
                                response.raw().addCookie(new Cookie("profile", "true"));
                            }
                            logger.info("database updated status::" + userprofile1.toJson());
                            templateOverride.process(root, writer);
                            return;

                        } catch (Exception e) {
                            logger.info("error in multi upload", e);
                        } finally {
                            IOUtils.closeQuietly(fout);
                            IOUtils.closeQuietly(stream);
                        }
                    }
                    HashMap<String, Object> root = new HashMap<String, Object>();
                    root.put("username", username);
                    templateOverride.process(root, writer);

                }
            }
        });

        post(new FreemarkerBasedRoute("/profile1", "profile.ftl") {
            @Override
            protected void doHandle(Request request, Response response, Writer writer)
                    throws IOException, TemplateException {

                String firstname = StringEscapeUtils.escapeHtml4(request.queryParams("firstname"));
                String lastname = StringEscapeUtils.escapeHtml4(request.queryParams("lastname"));
                String mobile = StringEscapeUtils.escapeHtml4(request.queryParams("mobile"));
                String address = StringEscapeUtils.escapeHtml4(request.queryParams("address"));
                String city = StringEscapeUtils.escapeHtml4(request.queryParams("city"));
                String country = StringEscapeUtils.escapeHtml4(request.queryParams("country"));
                String state = StringEscapeUtils.escapeHtml4(request.queryParams("state"));
                String email = StringEscapeUtils.escapeHtml4(request.queryParams("email"));


                String username = sessionDAO.findUserNameBySessionId(ControllerUtilities.getSessionCookie(request));

                if (username == null) {
                    response.redirect("/login");    // only logged in users can post to blog
                } else if (firstname.equals("") || lastname.equals("")) {
                    // redisplay page with errors
                    HashMap<String, Object> root = new HashMap<String, Object>();
                    root.put("errors", "lastname and firsname fields are empty.");
                    root.put("firstname", firstname);
                    root.put("lastname", lastname);
                    root.put("username", username);
                    root.put("mobile", mobile);
                    root.put("address", address);
                    root.put("city", city);
                    root.put("country", country);
                    root.put("state", state);
                    root.put("email", email);
                    templateOverride.process(root, writer);
                } else {
                    // extract tags
                    HashMap<String, Object> root = new HashMap<String, Object>();
                    root.put("success", "updated successfully");
                    root.put("firstname", firstname);
                    root.put("lastname", lastname);
                    root.put("username", username);
                    root.put("mobile", mobile);
                    root.put("address", address);
                    root.put("city", city);
                    root.put("country", country);
                    root.put("state", state);
                    root.put("email", email);
                    UserProfile userprofile = new UserProfile();
                    userprofile.setEmail(email);
                    userprofile.setState(state);
                    userprofile.setAddress(address);
                    userprofile.setCity(city);
                    userprofile.setCountry(country);
                    userprofile.setMobile(mobile);
                    userprofile.setFirstname(firstname);
                    userprofile.setLastname(lastname);

                    Document userProfileFound = userDAO.addUserProfile(username, userprofile);

                    templateOverride.process(root, writer);

                }
            }
        });

    }

    private void initializeListingRoutes() throws IOException {

        get(new FreemarkerBasedRoute("/", "/static/home.ftl") {
            @Override
            public void doHandle(Request request, Response response, Writer writer) throws IOException, TemplateException {
                String username = sessionDAO.findUserNameBySessionId(ControllerUtilities.getSessionCookie(request));
                Map<String,Object> root = new HashMap<String, Object>();
                if (username != null) {
                    root.put("username", username);
                }

                templateOverride.process(root, writer);
            }
        });

        get(new FreemarkerBasedRoute("/about", "/static/about.ftl") {
            @Override
            public void doHandle(Request request, Response response, Writer writer) throws IOException, TemplateException {
                String username = sessionDAO.findUserNameBySessionId(ControllerUtilities.getSessionCookie(request));
                Map<String,Object> root = new HashMap<String, Object>();
                if (username != null) {
                    root.put("username", username);
                }

                templateOverride.process(root, writer);
            }
        });

        get(new FreemarkerBasedRoute("/help", "/static/help.ftl") {
            @Override
            public void doHandle(Request request, Response response, Writer writer) throws IOException, TemplateException {
                String username = sessionDAO.findUserNameBySessionId(ControllerUtilities.getSessionCookie(request));
                Map<String,Object> root = new HashMap<String, Object>();
                if (username != null) {
                    root.put("username", username);
                }

                templateOverride.process(root, writer);
            }
        });

        get(new FreemarkerBasedRoute("/privacy", "/static/privacy.ftl") {
            @Override
            public void doHandle(Request request, Response response, Writer writer) throws IOException, TemplateException {
                String username = sessionDAO.findUserNameBySessionId(ControllerUtilities.getSessionCookie(request));
                Map<String,Object> root = new HashMap<String, Object>();
                if (username != null) {
                    root.put("username", username);
                }

                templateOverride.process(root, writer);
            }
        });

        get(new FreemarkerBasedRoute("/tos", "/static/tos.ftl") {
            @Override
            public void doHandle(Request request, Response response, Writer writer) throws IOException, TemplateException {
                String username = sessionDAO.findUserNameBySessionId(ControllerUtilities.getSessionCookie(request));
                Map<String,Object> root = new HashMap<String, Object>();
                if (username != null) {
                    root.put("username", username);
                }

                templateOverride.process(root, writer);
            }
        });

        get(new FreemarkerBasedRoute("/disclaimer", "/static/disclaimer.ftl") {
            @Override
            public void doHandle(Request request, Response response, Writer writer) throws IOException, TemplateException {
                String username = sessionDAO.findUserNameBySessionId(ControllerUtilities.getSessionCookie(request));
                Map<String,Object> root = new HashMap<String, Object>();
                if (username != null) {
                    root.put("username", username);
                }

                templateOverride.process(root, writer);
            }
        });
        /*
        get(new FreemarkerBasedRoute("/", "blog_template.ftl") {
            @Override
            public void doHandle(Request request, Response response, Writer writer) throws IOException, TemplateException {
                String username = sessionDAO.findUserNameBySessionId(ControllerUtilities.getSessionCookie(request));

                List<Document> askForHelpDocuments = askForHelpDAO.findByDateDescending(10);
                Map<String,Object> root = new HashMap<String, Object>();

                root.put("askForHelpDocuments", askForHelpDocuments);
                if (username != null) {
                    root.put("username", username);
                }

                templateOverride.process(root, writer);
            }
        });
        */

        // used to display actual listing detail page
        get(new FreemarkerBasedRoute("/post/:permalink", "blog_template.ftl") {
            @Override
            protected void doHandle(Request request, Response response, Writer writer) throws IOException, TemplateException {
                String permalink = request.params(":permalink");
                String username = sessionDAO.findUserNameBySessionId(ControllerUtilities.getSessionCookie(request));
                logger.info("Calling route /post: get " + permalink + ", username:" + username);
                Document post = askForHelpDAO.findByPermalink(permalink);

                if (post == null) {
                    response.redirect("/post_not_found");
                }
                else {
                    // empty comment to hold new comment in form at bottom of blog entry detail page

                    Map<String,Object> root = new HashMap<String, Object>();

                    List<Document> askForHelpDocuments = new ArrayList<Document>();
                    askForHelpDocuments.add(post);


                    //root.put("post", post);
                    root.put("askForHelpDocuments", askForHelpDocuments);
                    root.put("username", username);
                    root.put("showComments", 1);
                    root.put("postListing", 1);
                    root.put("jsonListing", post.toJson());
                    root.put("showList", Arrays.asList("zipcode","contact","address","area"));

                    if(ControllerUtilities.verifyAdmin(username)) {
                        root.put("isAdmin",1);
                    }

                    templateOverride.process(root, writer);
                }
            }
        });

        get(new FreemarkerBasedRoute("/update/:permalink", "blog_template.ftl") {
            @Override
            protected void doHandle(Request request, Response response, Writer writer) throws IOException, TemplateException {
                String username = sessionDAO.findUserNameBySessionId(ControllerUtilities.getSessionCookie(request));
                String permalink = request.params(":permalink");
                if(!ControllerUtilities.verifyAdmin(username)) {
                    logger.info("Update not allowed as its not admin, permalink:{},username:{}",permalink,username);
                    response.redirect("/internal_error");
                }

                JsonObject jsonObject= null;
                try {
                    String jsonStr = request.queryParams("jsonStr");
                    logger.info(jsonStr);
                    if(jsonStr==null || StringUtils.isBlank(jsonStr)) {
                        throw new IllegalArgumentException("Null json");
                    }
                    Gson gson = new Gson();
                    jsonObject = gson.fromJson(jsonStr, JsonObject.class);
                } catch(Exception e) {
                    logger.info("Bad Json, json:{}, permalink:{},username:{}",permalink,username);
                    response.redirect("/internal_error");
                }

                logger.info("Planning to update /post: get " + permalink + ", username:" + username);
                askForHelpDAO.updateListingWithInputJson(permalink, jsonObject);
                response.redirect("/post/"+permalink);
            }
        });

        get(new Route("/post/") {
            @Override
            public Object handle(Request request, Response response) {
                response.redirect("/post_not_found");
                return null;
            }
        });


        post(new Route("/newcomment") {
            @Override
            public Object handle(Request request, Response response) {

                Gson gson = new Gson();
                Message message = new Message();
                String cookie = ControllerUtilities.getSessionCookie(request);
                String username = sessionDAO.findUserNameBySessionId(cookie);
                String email = StringEscapeUtils.escapeHtml4(request.queryParams("email"));
                String body = StringEscapeUtils.escapeHtml4(request.queryParams("comments"));
                String permalink = StringEscapeUtils.escapeHtml4(request.queryParams("permalink"));
                logger.info(permalink + "::" + email + "::body:" + body + "**");

                if (username != null) {

                    if (body != null && !StringUtils.isBlank(body)) {

                        Comments comment = new Comments();
                        comment.setComments(body);
                        comment.setEmail(email);
                        comment.setUsername(username);
                        try {
                            logger.info("Thread comment update");
                            MONGO_DB_THREAD_POOL.execute(new MongoDbUpdateCommentWorker(permalink, username, comment));
                            Map<String, Object> root = new HashMap<String, Object>();
                            root.put("comment", comment);
                            root.put("permalink", permalink);
                        } catch (Exception e) {
                            logger.error("Issue in adding comment for permalink:" + permalink);
                            halt(503);
                        }
                    } else {
                        logger.info("Comment is null or blank, ignoring...");
                        message.setContents(body);
                    }

                } else {
                    //Do nothing
                    //prompt user for login
                    logger.warn("user not registered, so not applying comment, so redirecting to login...");
                    message.setRedirect("/login?permalink=" + permalink);
                }

                message.setContents(body);
                return gson.toJson(message);
            }
        });

        post(new Route("/plus") {
            @Override
            public Object handle(Request request, Response response) {
                logger.info("Doing a plus vote");
                Message message = new Message();
                message.setContents("failure");
                Gson gson = new Gson();
                String permalink = StringEscapeUtils.escapeHtml4(request.queryParams("permalink"));
                String username = sessionDAO.findUserNameBySessionId(ControllerUtilities.getSessionCookie(request));
                if (permalink == null || username == null) {
                    logger.info("userName is {} or permalink is {}, so redirecting to signup...", username, permalink);
                    message.setRedirect("/login?permalink=" + permalink);
                    return gson.toJson(message);
                }

                logger.info("user name:" + username + ",/post: get " + permalink);
                try {
                    logger.info("Using Thread based update");
                    MONGO_DB_THREAD_POOL.execute(new MongoDbUpdateWorker(permalink, username));
                    ELASTIC_SEARCH_THREAD_POOL.execute(new ElasticSearchWorkerDAO(permalink, username));
                    message.setContents("success");
                } catch (Exception e) {
                    logger.error("Exception, unable to update permalink:{} for user{}", permalink, username, e);
                    message.setRedirect("/login?permalink=" + permalink);
                }
                return gson.toJson(message);

            }
        });

        post(new Route("/delete/:permalink") {
            @Override
            public Object handle(Request request, Response response) {
                logger.info("Doing a delete vote");
                String username = sessionDAO.findUserNameBySessionId(ControllerUtilities.getSessionCookie(request));
                String permalink = request.params(":permalink");
                if(!ControllerUtilities.verifyAdmin(username)) {
                    logger.info("Update not allowed as its not admin, permalink:{},username:{}", permalink, username);
                    response.redirect("/internal_error");
                    return null;
                }
                askForHelpDAO.deleteListing(permalink);
                ElasticSearchDAO.deleteDocument(permalink);

                logger.info("Successfully deleted permalink:{}",permalink);
                response.redirect("/welcome");
                return null;
            }
        });

        post(new Route("/report") {
            @Override
            public Object handle(Request request, Response response) {
                logger.info("Doing a report vote in new post");
                String username = sessionDAO.findUserNameBySessionId(ControllerUtilities.getSessionCookie(request));
                String reason = request.queryParams("reason");
                String permalink = request.queryParams("permalink");
                if(username==null || StringUtils.isBlank(username) || permalink==null || StringUtils.isBlank(permalink)) {
                    logger.info("Not logged in User so redirecting or permalink empty");
                    response.redirect("/post/"+permalink);
                    return null;
                } else {
                    askForHelpDAO.reportListing(permalink, username, reason);
                    logger.info("Successfully reported permalink:{}", permalink);
                    return true;
                }
            }

        });


    }

    private void initializeGetHelpListingRoutes() throws IOException {
        // will present the form used to Get help from friends, social media
        get(new FreemarkerBasedRoute("/getHelp", "get_help.ftl") {
            @Override
            protected void doHandle(Request request, Response response, Writer writer) throws IOException, TemplateException {

                logger.info("Get Help...");
                // get cookie
                String username = sessionDAO.findUserNameBySessionId(ControllerUtilities.getSessionCookie(request));

                if (username == null) {
                    // looks like a bad request. user is not logged in
                    response.redirect("/login");
                } else {
                    //SimpleHash root = new SimpleHash();
                    HashMap<String, Object> root = new HashMap<String, Object>();
                    root.put("username", username);

                    templateOverride.process(root, writer);
                }
            }
        });

        // handle the new post submission
        post(new FreemarkerBasedRoute("/getHelp", "get_help.ftl") {
            @Override
            protected void doHandle(Request request, Response response, Writer writer)
                    throws IOException, TemplateException {

                String type = StringEscapeUtils.escapeHtml4(request.queryParams("type"));
                String skill = StringEscapeUtils.escapeHtml4(request.queryParams("skill"));
                String city = StringEscapeUtils.escapeHtml4(request.queryParams("city"));
                String country = StringEscapeUtils.escapeHtml4(request.queryParams("country"));
                String state = StringEscapeUtils.escapeHtml4(request.queryParams("state"));
                String emails = StringEscapeUtils.escapeHtml4(request.queryParams("emails"));

                String username = sessionDAO.findUserNameBySessionId(ControllerUtilities.getSessionCookie(request));

                if (username == null) {
                    response.redirect("/login");    // only logged in users can post to blog
                } else if (type.equals("") || emails.equals("")) {
                    // redisplay page with errors
                    HashMap<String, Object> root = new HashMap<String, Object>();
                    root.put("errors", "Type and email fields are empty.");
                    root.put("type", type);
                    root.put("username", username);
                    root.put("skill", skill);
                    root.put("emails", emails);
                    root.put("country", country);
                    root.put("city", city);
                    root.put("state", state);
                    templateOverride.process(root, writer);
                } else {
                    // extract tags
                    ArrayList<String> skillSet = ControllerUtilities.extractTags(skill);
                    AskForHelpObject askForHelpObject = new AskForHelpObject.AskForHelpObjectBuilder()
                            .city(city)
                            .type(type)
                            .city(city)
                            .state(state)
                            .country(country)
                            .skills(skillSet)
                            .build();
                    ArrayList<String> emailList = ControllerUtilities.extractTags(emails);
                    askForHelpDAO.addGetHelpListing(username,emailList,askForHelpObject);

//String type, List<String> skill, String name, String notes, String city, String state, String country, String username) {
                    SendMail sendMail = new SendMail();
                    for (String em : emailList) {
                        sendMail.sendEmail(em, username, askForHelpObject);
                    }
                    sendMail.sendEmail(Constants.DEFAULT_EMAIL, username, askForHelpObject);
                    HashMap<String, Object> root = new HashMap<String, Object>();
                    root.put("type", type);
                    root.put("username", username);
                    root.put("skill", skill);
                    root.put("emails", emails);
                    root.put("country", country);
                    root.put("city", city);
                    root.put("state", state);
                    root.put("success", "Email Send Successfully to all recipients:" + emails);
                    logger.info("Successfully send email:", emails);
                    templateOverride.process(root, writer);
                    // now redirect to the blog permalink
                }
            }
        });

    }

    private void initializeNewListingRoutes() throws IOException {
        // handle the new post submission

        // will present the form used to process new blog posts
        get(new FreemarkerBasedRoute("/newpost", "newpost_template.ftl") {
            @Override
            protected void doHandle(Request request, Response response, Writer writer) throws IOException, TemplateException {

                logger.info("Create a new post..");
                // get cookie
                String username = sessionDAO.findUserNameBySessionId(ControllerUtilities.getSessionCookie(request));

                if (username == null) {
                    // looks like a bad request. user is not logged in
                    response.redirect("/login");
                } else {
                    HashMap<String, Object> root = new HashMap<String, Object>();
                    root.put("username", username);

                    templateOverride.process(root, writer);
                }
            }
        });
        post(new FreemarkerBasedRoute("/newpost", "newpost_template.ftl") {
            @Override
            protected void doHandle(Request request, Response response, Writer writer)
                    throws IOException, TemplateException {

                String type = StringEscapeUtils.escapeHtml4(request.queryParams("type"));
                String skill = StringEscapeUtils.escapeHtml4(request.queryParams("skill"));
                String name = StringEscapeUtils.escapeHtml4(request.queryParams("name"));
                String notes = StringEscapeUtils.escapeHtml4(request.queryParams("notes"));
                String city = StringEscapeUtils.escapeHtml4(request.queryParams("city"));
                String country = StringEscapeUtils.escapeHtml4(request.queryParams("country"));
                String state = StringEscapeUtils.escapeHtml4(request.queryParams("state"));


                String username = sessionDAO.findUserNameBySessionId(ControllerUtilities.getSessionCookie(request));

                if (username == null) {
                    response.redirect("/login");    // only logged in users can post to blog
                } else if (type.equals("") || name.equals("")) {
                    // redisplay page with errors
                    HashMap<String, String> root = new HashMap<String, String>();
                    root.put("errors", "Type and Name fields are empty.");
                    root.put("type", type);
                    root.put("name", name);
                    root.put("username", username);
                    root.put("skill", skill);
                    root.put("notes", notes);
                    templateOverride.process(root, writer);
                } else {
                    // extract tags

                    ArrayList<String> skillSet = ControllerUtilities.extractTags(skill);
/*
                    // substitute some <p> for the paragraph breaks
                    post = post.replaceAll("\\r?\\n", "<p>");
                    */
//String type, List<String> skill, String name, String notes, String city, String state, String country, String username) {
                    String permalink = askForHelpDAO.addAskForHelpDocument(type, skillSet, name, notes, city, state, country, username);

                    logger.info("Permalink to be redirected {}", permalink);
                    // now redirect to the blog permalink
                    response.redirect("/post/" + permalink);
                }
            }
        });


    }

    private void initializeRoutes() throws IOException {

        get(new Route("/getState") {
            @Override
            public Object handle(Request request, Response response) {
                List<Document> stateDocs = stateDAO.getAllStates();
                Gson gson = new Gson();
                return gson.toJson(stateDocs);
            }
        });

        post(new Route("/jainVote") {
            @Override
            public Object handle(Request request, Response response) {
                String permalink = request.queryParams("permalink");
                String cookie = ControllerUtilities.getSessionCookie(request);
                String username = sessionDAO.findUserNameBySessionId(cookie);
                Gson gson = new Gson();
                Message message = new Message();
                message.setContents("failure");

                if (permalink == null || username == null || StringUtils.isBlank(permalink)) {
                    logger.info("userName is {} or permalink is {}, so redirecting to signup...", username, permalink);
                    message.setRedirect("/login?permalink=" + permalink);
                    return gson.toJson(message);
                }

                logger.info("user name:" + username + ",/post: get " + permalink);
                try {
                    logger.info("Using Thread based update");
                    MONGO_DB_THREAD_POOL.execute(new MongoDbJainUpdateWorker(permalink, username));
                    //ELASTIC_SEARCH_THREAD_POOL.execute(new ElasticSearchWorkerDAO(permalink, username));
                    //askForHelpDAO.updateListingWithJainFoodOption(permalink, username)
                    message.setContents("success");
                } catch (Exception e) {
                    logger.error("Exception, unable to update permalink:{} for user{}", permalink, username, e);
                    message.setRedirect("/login?permalink=" + permalink);
                }
                return gson.toJson(message);
            }
        });



        get(new FreemarkerBasedRoute("/welcome", "blog_template.ftl") {
            @Override
            protected void doHandle(Request request, Response response, Writer writer) throws IOException, TemplateException {

                String cookie = ControllerUtilities.getSessionCookie(request);
                String username = sessionDAO.findUserNameBySessionId(cookie);

                if (username == null) {
                    System.out.println("welcome() can't identify the user, redirecting to signup");
                    response.redirect("/login");

                } else {
                    List<Document> askForHelpDocuments = askForHelpDAO.findByDateDescending(10);
                    //SimpleHash root = new SimpleHash();
                    HashMap<String, Object> root = new HashMap<String, Object>();

                    root.put("username", username);
                    root.put("askForHelpDocuments", askForHelpDocuments);

                    templateOverride.process(root, writer);
                }
            }
        });



        post(new Route("/upload_old") {
            @Override
            public Object handle(Request request, Response response) {
                final String UPLOAD_DIRECTORY = Constants.UPLOAD_DIRECTORY_MAC;
                Gson gson = new Gson();
                Message message = new Message();

                final String fileToUpload = request.queryParams("fileToUpload");

                OutputStream out = null;
                InputStream filecontent = null;

                if (ServletFileUpload.isMultipartContent(request.raw())) {
                    try {
                        List<FileItem> multiparts = new ServletFileUpload(
                                new DiskFileItemFactory()).parseRequest(request.raw());
                        String name = null;

                        for (FileItem item : multiparts) {
                            if (!item.isFormField()) {
                                name = new File(item.getName()).getName();
                                item.write(new File(UPLOAD_DIRECTORY + File.separator + name));
                            }
                        }

                        //File uploaded successfully
                        logger.info("File Uploaded Successfully");
                    } catch (Exception ex) {
                        logger.error("File Upload Failed due to ", ex);
                    }

                }


                //    logger.info(request.body());
                logger.info(request.headers().toString());

                return "success";
            }
        });




        post(new Route("/upload") {
            @Override
            public Object handle(Request request, Response response) {
                final String UPLOAD_DIRECTORY = Constants.UPLOAD_DIRECTORY_MAC;

                Gson gson = new Gson();
                Message message = new Message();

                final String fileToUpload = request.queryParams("fileToUpload");
                logger.info(fileToUpload + ", filetoUpload");

                OutputStream out = null;
                InputStream stream = null;
                FileOutputStream fout = null;
                final int BUFF_SIZE = 100000;
                final byte[] buffer = new byte[BUFF_SIZE];

                if (ServletFileUpload.isMultipartContent(request.raw())) {
                    try {

                        ServletFileUpload upload = new ServletFileUpload();

                        FileItemIterator iter = upload.getItemIterator(request.raw());
                        while (iter.hasNext()) {
                            FileItemStream item = iter.next();
                            String name = item.getFieldName();
                            stream = item.openStream();
                            if (item.isFormField()) {
                                logger.info("Form field " + name + " with value "
                                        + Streams.asString(stream) + " detected.");
                            } else {

                                logger.info("File field " + name + " with file name "
                                        + item.getName() + " detected.");
                                // Process the input stream

                                logger.info("name::" + item.getName());
                                String name1 = new File(item.getName()).getName();
                                String[] nameArr = name1.split("\\.");
                                String finalName = UPLOAD_DIRECTORY + File.separator + nameArr[0] + "_" + System.currentTimeMillis();
                                logger.info(" 1 FinalName::" + finalName);
                                if (nameArr.length >= 2) {
                                    finalName = finalName + "." + StringUtils.join(nameArr, '.', 1, nameArr.length);
                                }
                                String fileType = finalName.substring(finalName.lastIndexOf(".")+1);
                                logger.info("FinalName::"+finalName+", fileType:"+fileType);

                                fout = new FileOutputStream(finalName);
                                while (true) {
                                    synchronized (buffer) {
                                        int amountRead = stream.read(buffer);
                                        if (amountRead == -1) {
                                            break;
                                        }
                                        fout.write(buffer, 0, amountRead);
                                    }
                                }
                                IOUtils.closeQuietly(stream);
                            }
                        }
                    } catch (Exception e) {
                        logger.error("File Upload Failed due to ", e);
                    } finally {
                        IOUtils.closeQuietly(fout);
                        IOUtils.closeQuietly(stream);

                    }
                }

                logger.info(request.headers().toString());

                return "success";
            }
        });

        get(new FreemarkerBasedRoute("/upload_listing/:permalink", "listing_image.ftl") {
            @Override
            protected void doHandle(Request request, Response response, Writer writer) throws IOException, TemplateException {

                logger.info("Create a new image..");
                String permalink = request.params(":permalink");
                String cookie = ControllerUtilities.getSessionCookie(request);
                String username = sessionDAO.findUserNameBySessionId(cookie);
                if (!ControllerUtilities.verifyAdmin(username) ||
                        permalink == null ||
                        StringUtils.isBlank(permalink)
                        ) {
                    logger.error("Unauthorized access by user: {} or bad permalink:{}", username, permalink);
                    response.redirect("/internal_error");
                    return;
                }

                HashMap<String, Object> root = new HashMap<String, Object>();
                root.put("username", username);
                root.put("permalink", permalink);

                templateOverride.process(root, writer);
            }
        });

        post(new Route("/upload_listing") {
            @Override
            public Object handle(Request request, Response response) {
                logger.info("HERE in post upload");
                String permalink = request.queryParams("permalink");
                String cookie = ControllerUtilities.getSessionCookie(request);
                String username = sessionDAO.findUserNameBySessionId(cookie);
                if(username==null ||
                        StringUtils.isBlank(username) ||
                        permalink==null ||
                        StringUtils.isBlank(permalink)
                        ) {
                    logger.error("Unauthorized access by user: {} or bad permalink:{}",username,permalink);
                    response.redirect("/internal_error");
                    return null;
                }
                final String UPLOAD_DIRECTORY = Constants.UPLOAD_DIRECTORY_MAC;
                String dirName = UPLOAD_DIRECTORY + "//" + permalink;
                try {
                    File f = new File(dirName);
                    boolean test = f.mkdir();
                } catch(Exception e) {
                    logger.error("Exception in creating target dir in :{}",UPLOAD_DIRECTORY,e);

                    response.redirect("/internal_error");
                    return null;
                }

                Gson gson = new Gson();
                Message message = new Message();

                final String fileToUpload = request.queryParams("fileToUpload");
                logger.info(fileToUpload+ ", filetoUpload");

                OutputStream out = null;
                InputStream stream = null;
                FileOutputStream fout = null;
                final int BUFF_SIZE = 100000;
                final byte[] buffer = new byte[BUFF_SIZE];

                if(ServletFileUpload.isMultipartContent(request.raw())) {
                    try {

                        ServletFileUpload upload = new ServletFileUpload();

                        FileItemIterator iter = upload.getItemIterator(request.raw());
                        while (iter.hasNext()) {
                            FileItemStream item = iter.next();
                            String name = item.getFieldName();
                            stream = item.openStream();
                            if (item.isFormField()) {
                                logger.info("Form field " + name + " with value "
                                        + Streams.asString(stream) + " detected.");
                            } else {

                                logger.info("File field " + name + " with file name "
                                        + item.getName() + " detected.");
                                // Process the input stream



                                String name1 =  new File(item.getName()).getName();
                                String[] nameArr = name1.split("\\.");
                                StringBuffer imageFileName = new StringBuffer();
                                imageFileName.append(nameArr[0]).append("_" + System.currentTimeMillis());

                                if(nameArr.length>=2) {
                                    imageFileName.append(".").append(StringUtils.join(nameArr, '.', 1, nameArr.length));
                                }
                                logger.info("name::"+item.getName() + ", imageName:{}",imageFileName);
                                String finalName = dirName + File.separator + imageFileName.toString();
                                String fileType = finalName.substring(finalName.lastIndexOf(".")+1);
                                logger.info("FinalName::"+finalName+", fileType:"+fileType);



                                fout= new FileOutputStream (finalName);
                                while (true) {
                                    synchronized (buffer) {
                                        int amountRead = stream.read(buffer);
                                        if (amountRead == -1) {
                                            break;
                                        }
                                        fout.write(buffer, 0, amountRead);
                                    }
                                }
                                askForHelpDAO.updateListingWithPhotos(permalink,username, imageFileName.toString());
//InsertInto Mongo and relate it the listing now
                                try {
                                    logger.info("Creating Thumbnails for :{}",finalName);
                                    Thumbnails.of(finalName)
                                            .size(Constants.THUMBNAIL_WIDTH, Constants.THUMBNAIL_HEIGHT)
                                            .outputFormat(fileType)
                                            .toFiles(Rename.PREFIX_DOT_THUMBNAIL);


                                } catch(Exception e) {
                                    logger.error("Failed to create Thumbnail for:{}",finalName,e);
                                }
                                IOUtils.closeQuietly(stream);

                            }
                        }
                    }
                    catch(Exception e) {
                        logger.error("File Upload Failed due to ",e);
                    } finally {
                        IOUtils.closeQuietly(fout);
                        IOUtils.closeQuietly(stream);

                    }
                }

                logger.info(request.headers().toString());

                return "success";
            }
        });

        post(new Route("/upload_listing/") {
            @Override
            public Object handle(Request request, Response response) {
                String permalink = request.params(":permalink");
                String cookie = ControllerUtilities.getSessionCookie(request);
                String username = sessionDAO.findUserNameBySessionId(cookie);
                if (!ControllerUtilities.verifyAdmin(username) ||
                        permalink == null ||
                        StringUtils.isBlank(permalink)
                        ) {
                    logger.error("Unauthorized access by user: {} or bad permalink:{}", username, permalink);
                }
                response.redirect("/internal_error");
                return "success";
            }
        });

        post(new Route("/upload_listing/:permalink") {
            @Override
            public Object handle(Request request, Response response) {
                String permalink = request.params(":permalink");
                String cookie = ControllerUtilities.getSessionCookie(request);
                String username = sessionDAO.findUserNameBySessionId(cookie);
                if (username == null ||
                        StringUtils.isBlank(username) ||
                        permalink == null ||
                        StringUtils.isBlank(permalink)
                        ) {
                    logger.error("Unauthorized access by user: {} or bad permalink:{}", username, permalink);
                    response.redirect("/internal_error");
                    return null;
                }
                final String UPLOAD_DIRECTORY = Constants.UPLOAD_DIRECTORY_MAC;
                String dirName = UPLOAD_DIRECTORY + "//" + permalink;
                try {
                    File f = new File(dirName);
                    boolean test = f.mkdir();
                } catch (Exception e) {
                    logger.error("Exception in creating target dir in :{}", UPLOAD_DIRECTORY, e);

                    response.redirect("/internal_error");
                    return null;
                }

                Gson gson = new Gson();
                Message message = new Message();

                final String fileToUpload = request.queryParams("fileToUpload");
                logger.info(fileToUpload + ", filetoUpload");

                OutputStream out = null;
                InputStream stream = null;
                FileOutputStream fout = null;
                final int BUFF_SIZE = 100000;
                final byte[] buffer = new byte[BUFF_SIZE];

                if (ServletFileUpload.isMultipartContent(request.raw())) {
                    try {

                        ServletFileUpload upload = new ServletFileUpload();

                        FileItemIterator iter = upload.getItemIterator(request.raw());
                        while (iter.hasNext()) {
                            FileItemStream item = iter.next();
                            String name = item.getFieldName();
                            stream = item.openStream();
                            if (item.isFormField()) {
                                logger.info("Form field " + name + " with value "
                                        + Streams.asString(stream) + " detected.");
                            } else {
                                boolean flagImageNotUpdated = false;
                                logger.info("File field " + name + " with file name "
                                        + item.getName() + " detected.");
                                // Process the input stream

                                if (item.getName() == null
                                        || StringUtils.isBlank(item.getName())) {
                                    logger.info("Image file not being updated, so doing nothing");
                                    flagImageNotUpdated = true;

                                } else {

                                    String name1 = new File(item.getName()).getName();
                                    String[] nameArr = name1.split("\\.");
                                    StringBuffer imageFileName = new StringBuffer();
                                    imageFileName.append(nameArr[0]).append("_" + System.currentTimeMillis());

                                    if (nameArr.length >= 2) {
                                        imageFileName.append(".").append(StringUtils.join(nameArr, '.', 1, nameArr.length));
                                    }
                                    logger.info("name::" + item.getName() + ", imageName:{}", imageFileName);
                                    String finalName = dirName + File.separator + imageFileName.toString();
                                    String fileType = finalName.substring(finalName.lastIndexOf(".")+1);
                                    logger.info("FinalName::"+finalName+", fileType:"+fileType);

                                    fout = new FileOutputStream(finalName);
                                    while (true) {
                                        synchronized (buffer) {
                                            int amountRead = stream.read(buffer);
                                            if (amountRead == -1) {
                                                break;
                                            }
                                            fout.write(buffer, 0, amountRead);
                                        }
                                    }
                                    askForHelpDAO.updateListingWithPhotos(permalink, username, imageFileName.toString());
//InsertInto Mongo and relate it the listing now
                                    try {
                                        logger.info("Creating Thumbnails for :{}", finalName);
                                        Thumbnails.of(finalName)
                                                .size(Constants.THUMBNAIL_WIDTH, Constants.THUMBNAIL_HEIGHT)
                                                .outputFormat(fileType)
                                                .toFiles(Rename.PREFIX_DOT_THUMBNAIL);


                                    } catch (Exception e) {
                                        logger.error("Failed to create Thumbnail for:{}", finalName, e);
                                    }
                                    IOUtils.closeQuietly(stream);
                                    IOUtils.closeQuietly(fout);
                                }

                            }
                        }
                    } catch (Exception e) {
                        logger.error("File Upload Failed due to ", e);
                    } finally {
                        IOUtils.closeQuietly(fout);
                        IOUtils.closeQuietly(stream);

                    }
                }
                response.redirect("/post/" + permalink);
                return null;
            }
        });

        // tells the user that the URL is dead
        get(new FreemarkerBasedRoute("/post_not_found", "/static/post_not_found.ftl") {
            @Override
            protected void doHandle(Request request, Response response, Writer writer) throws IOException, TemplateException {
                String username = sessionDAO.findUserNameBySessionId(ControllerUtilities.getSessionCookie(request));
                HashMap<String, Object> root = new HashMap<String, Object>();
                root.put("username", StringEscapeUtils.escapeHtml4(username));
                templateOverride.process(root, writer);
            }
        });

        get(new FreemarkerBasedRoute("/ocr", "/ocr/ocr_req.ftl") {
            @Override
            protected void doHandle(Request request, Response response, Writer writer) throws IOException, TemplateException {

                String cookie = ControllerUtilities.getSessionCookie(request);
                String username = sessionDAO.findUserNameBySessionId(cookie);
                HashMap<String, Object> root = new HashMap<String, Object>();
                root.put("username", username);

                templateOverride.process(root, writer);
            }
        });

        get(new FreemarkerBasedRoute("/ocr_cam", "/ocr/ocr_req_cam.ftl") {
            @Override
            protected void doHandle(Request request, Response response, Writer writer) throws IOException, TemplateException {

                String cookie = ControllerUtilities.getSessionCookie(request);
                String username = sessionDAO.findUserNameBySessionId(cookie);
                HashMap<String, Object> root = new HashMap<String, Object>();
                root.put("username", username);

                templateOverride.process(root, writer);
            }
        });

        post(new FreemarkerBasedRoute("/ocr/", "/ocr/ocr.ftl") {
            @Override
            protected void doHandle(Request request, Response response, Writer writer) throws IOException, TemplateException {

                HashMap<String, Object> root = new HashMap<String, Object>();
                String cookie = ControllerUtilities.getSessionCookie(request);
                String username = sessionDAO.findUserNameBySessionId(cookie);
                Tesseract instance = new Tesseract();
                instance.setDatapath(".");
                try {
                    String imageFile = ControllerUtilities.writeImageToFile(Constants.UPLOAD_DIRECTORY_MAC + "//ocr", request, response);
                    if (imageFile != null) {
                        String result = instance.doOCR(new File(imageFile));
                        logger.info("result***");
                        logger.info(result);
                        String negJainText = ControllerUtilities.notJainIngredients(result);
                        String negVegText = ControllerUtilities.notVegIngredients(result);
                        String negVeganText = ControllerUtilities.notVeganIngredients(result);

                        IngredientsModel ingredientsModel = new IngredientsModel();
                        ingredientsModel.setNotJain(negJainText);
                        ingredientsModel.setNotVeg(negVegText);
                        ingredientsModel.setNotVegan(negVeganText);

                        Gson gson = new Gson();

                        root.put("ocrText", result);
                        root.put("jsonResult", gson.toJson(ingredientsModel));

                        templateOverride.process(root, writer);
                        return;
                    } else {
                        logger.error("Some Exception in saving the file for user OCR");
                    }
                } catch (Exception e) {
                    logger.error("Error reading file ");
                    e.printStackTrace();
                }

                root.put("ocrText", "error");
                templateOverride.process(root, writer);
                return;
            }
        });



        // used to process internal errors
        get(new FreemarkerBasedRoute("/internal_error", "/static/internal_error.ftl") {
            @Override
            protected void doHandle(Request request, Response response, Writer writer) throws IOException, TemplateException {
                HashMap<String, Object> root = new HashMap<String, Object>();
                String username = sessionDAO.findUserNameBySessionId(ControllerUtilities.getSessionCookie(request));
                root.put("username", username);
                root.put("error", "System has encountered an error.");
                templateOverride.process(root, writer);
            }
        });


    }

    private Configuration createFreemarkerConfiguration() {
        Configuration retVal = new Configuration();
        retVal.setClassForTemplateLoading(AskForHelpController.class, "/freemarker");

        return retVal;
    }


}
