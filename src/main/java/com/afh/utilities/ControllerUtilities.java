package com.afh.utilities;

import com.afh.constants.Constants;
import com.afh.controller.AskForHelpController;
import com.afh.controller.Message;
import com.google.gson.Gson;
import freemarker.template.Configuration;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.name.Rename;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;

import javax.servlet.http.Cookie;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by chandan on 9/27/2015.
 */
public class ControllerUtilities {
    static final Logger logger = LoggerFactory.getLogger(ControllerUtilities.class);

    // helper function to get session cookie as string
    public static String getSessionCookie(final Request request) {
        if (request.raw().getCookies() == null) {
            return null;
        }
        for (Cookie cookie : request.raw().getCookies()) {
            if (cookie.getName().equals("session")) {
                return cookie.getValue();
            }
        }
        return null;
    }

    // helper function to get session cookie as string
    public static Cookie getSessionCookieActual(final Request request) {
        if (request.raw().getCookies() == null) {
            return null;
        }
        for (Cookie cookie : request.raw().getCookies()) {
            if (cookie.getName().equals("session")) {
                return cookie;
            }
        }
        return null;
    }

    public static void deleteProfileCookie(final Request request, Response response) {
        deleteCookie(request,response,"profile");
    }

    public static void deleteSessionCookie(final Request request, Response response) {
        deleteCookie(request,response,"session");
    }

    public static void deleteCookie(final Request request, Response response,String cookieStr) {
        if (request.raw().getCookies() == null) {
            return;
        }
        for (Cookie cookie : request.raw().getCookies()) {
            if (cookie.getName().equals(cookieStr)) {
                cookie.setMaxAge(0);
                response.raw().addCookie(cookie);
            }
        }
    }

    public static void deleteAllCookies(final Request request, Response response) {
        deleteProfileCookie(request,response);
        deleteSessionCookie(request,response);

    }
    // helper function to get session cookie as string
    public static Cookie getProfileCookieActual(final Request request) {
        if (request.raw().getCookies() == null) {
            return null;
        }
        for (Cookie cookie : request.raw().getCookies()) {
            if (cookie.getName().equals("profile")) {
                return cookie;
            }
        }
        return null;
    }

    // tags the tags string and put it into an array
    public static ArrayList<String> extractTags(String tags) {

        // probably more efficent ways to do this.
        //
        // whitespace = re.compile('\s')

        tags = tags.replaceAll("\\s", "");
        String tagArray[] = tags.split(",");

        // let's clean it up, removing the empty string and removing dups
        ArrayList<String> cleaned = new ArrayList<String>();
        for (String tag : tagArray) {
            if (!tag.equals("") && !cleaned.contains(tag)) {
                cleaned.add(tag);
            }
        }

        return cleaned;
    }

    // validates that the registration form has been filled out right and username conforms


    public static boolean validateSignupV2(String username, String password, String verify, String email,
                                         HashMap<String, String> errors) {
        //String USER_RE = "^[a-zA-Z0-9_-]{3,20}$";
        String PASS_RE = "^.{3,20}$";
        String EMAIL_RE = "^[\\S]+@[\\S]+\\.[\\S]+$";

        //errors.put("username_error", "");
        errors.put("password_error", "");
        errors.put("verify_error", "");
        errors.put("email_error", "");

        /*
        if (!username.matches(USER_RE)) {
            errors.put("username_error", "invalid username. try just letters and numbers");
            return false;
        }
        */

        if (!password.matches(PASS_RE)) {
            errors.put("password_error", "Invalid password length!!");
            return false;
        }


        if (!password.equals(verify)) {
            errors.put("verify_error", "Password must match!!");
            return false;
        }

        if (!email.equals("")) {
            if (!email.matches(EMAIL_RE)) {
                errors.put("email_error", "Invalid Email Address!!");
                return false;
            }
        }

        return true;
    }

    public static Boolean emailVerify(String email) {
        String EMAIL_RE = "^[\\S]+@[\\S]+\\.[\\S]+$";
        if (!email.equals("")) {
            if (!email.matches(EMAIL_RE)) {
                return false;
            }
        }
        return true;
    }

    public static boolean validateSignup(String username, String password, String verify, String email,
                                           HashMap<String, String> errors) {
        String USER_RE = "^[a-zA-Z0-9_-]{3,20}$";
        String PASS_RE = "^.{3,20}$";
        String EMAIL_RE = "^[\\S]+@[\\S]+\\.[\\S]+$";

        errors.put("username_error", "");
        errors.put("password_error", "");
        errors.put("verify_error", "");
        errors.put("email_error", "");


        if (!username.matches(USER_RE)) {
            errors.put("username_error", "invalid username. try just letters and numbers");
            return false;
        }


        if (!password.matches(PASS_RE)) {
            errors.put("password_error", "invalid password.");
            return false;
        }


        if (!password.equals(verify)) {
            errors.put("verify_error", "password must match");
            return false;
        }

        if (!email.equals("")) {
            if (!email.matches(EMAIL_RE)) {
                errors.put("email_error", "Invalid Email Address");
                return false;
            }
        }

        return true;
    }


    public static Boolean verifyAdmin(String username) {

       if( username != null
               && username.equals(Constants.ADMIN_USER)) {
            return true;
        }
        return  false;
    }

    public static boolean shouldReturnHtml(Request request) {
        String accept = request.headers("Accept");
        return accept != null && accept.contains("text/html");
    }

    public static String writeImageToFile(String path, Request request, Response response) {

        try {
            File f = new File(path);
            boolean test = f.mkdir();
        } catch (Exception e) {
            logger.error("Exception in creating target dir in :{}", path, e);
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
                            String finalName = path + File.separator + imageFileName.toString();
                            logger.info("FinalName::" + finalName);

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
                            IOUtils.closeQuietly(fout);
                            return finalName;
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
        return null;

    }

    public static boolean createThumbnail(String name) {

        try {
            logger.info("Creating Thumbnails for :{}", name);
            Thumbnails.of(name)
                    .size(Constants.THUMBNAIL_WIDTH, Constants.THUMBNAIL_HEIGHT)
                    .outputFormat("jpg")
                    .toFiles(Rename.PREFIX_DOT_THUMBNAIL);
            return true;


        } catch (Exception e) {
            logger.error("Failed to create Thumbnail for:{}", name, e);
            return false;
        }
    }


    final static Set<String> jainSet;
    static {
        jainSet = new HashSet<String>();
        jainSet.add("onion");
        jainSet.add("potato");
        jainSet.add("garlic");
        jainSet.add("brinjal");
        jainSet.add("carrot");
    }

    final static Set<String> vegSet;
    static {
        vegSet = new HashSet<String>();
        vegSet.add("egg");
        vegSet.add("meat");
        vegSet.add("animal");
        vegSet.add("fish");
        vegSet.add("pork");
        vegSet.add("halal");
        vegSet.add("gelatin");
        vegSet.add("gluten");
    }

    final static Set<String> veganSet;
    static {
        veganSet = new HashSet<String>();
        veganSet.add("dairy");
        veganSet.add("milk");
        veganSet.add("cheese");
        veganSet.add("butter");
    }

    public static String checkAgainstSet(String text, Set<String> checkSet) {
        Set<String> returnSet = new HashSet<String>();
        text = text.toLowerCase();
        String[] arr = text.split(" ");
        for(String elem : arr) {
            if(checkSet.contains(elem)) {
                returnSet.add(elem);
            }
        }

        if(returnSet.size()>0) {
            return StringUtils.join(returnSet,",");
        }

        return null;
    }
    public static String notJainIngredients(String text) {

        Set<String> checkSets = new HashSet<String>();
        checkSets.addAll(jainSet);
        checkSets.addAll(vegSet);
        return checkAgainstSet(text,checkSets);

    }

    public static String notVegIngredients(String text) {
        Set<String> checkSets = new HashSet<String>();
        checkSets.addAll(vegSet);
        return checkAgainstSet(text, checkSets);
    }

    public static String notVeganIngredients(String text) {
        Set<String> checkSets = new HashSet<String>();
        checkSets.addAll(vegSet);
        checkSets.addAll(veganSet);
        return checkAgainstSet(text, checkSets);
    }
}
