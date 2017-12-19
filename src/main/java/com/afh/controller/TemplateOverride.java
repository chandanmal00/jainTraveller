package com.afh.controller;

import com.afh.constants.Constants;
import com.afh.logging.LogResult;
import com.afh.utilities.ControllerUtilities;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

/**
 * Created by chandan on 9/27/2015.
 */
public class TemplateOverride  {

    Template template;
    Request request;
    Response response;
    static final Logger logger = LoggerFactory.getLogger(TemplateOverride.class);
    public TemplateOverride() {
    }

    public void setTemplate(Template template) {
        this.template = template;
    }


    public void setRequest(Request request) {
        this.request = request;
    }


    public void setResponse(Response response) {
        this.response = response;
    }

    public TemplateOverride(Request request, Response response, Template template) {
        this.request = request;
        this.response = response;
        this.template = template;
    }

    public void process(Object rootMap, Writer out) throws TemplateException, IOException {

        Map<String,Object> root = null;
        try {
            root = (Map<String, Object>) rootMap;
            root.put("APP_TITLE", Constants.APP_TITLE);
            root.put("APP_LINK", Constants.APP_LINK);
            root.put("APP_MESSAGE", Constants.APP_MESSAGE);
            root.put("APP_WEBSITE", Constants.APP_WEBSITE);
            root.put("APP_WEBSITE_BIZ", Constants.APP_WEBSITE_BIZ);
            root.put("INFO_EMAIL", Constants.INFO_EMAIL);
            root.put("ADDRESS", Constants.APP_ADDRESS);
            root.put("HEIGHT", Constants.THUMBNAIL_HEIGHT);
            root.put("WIDTH", Constants.THUMBNAIL_WIDTH);
            root.put("session", ControllerUtilities.getSessionCookie(request));
            this.template.process(root, out);
            LogResult.logIntoApplicationLogs(this.request, this.response, root);
        } catch(Exception e) {
            logger.warn("{} not matching map class",request.pathInfo());
            this.template.process(rootMap, out);
            LogResult.logIntoApplicationLogs(this.request, this.response,rootMap.toString());
        }
        /*
        } else {
            logger.warn("{} not matching map class",request.pathInfo());
            LogResult.logIntoApplicationLogs(this.request, this.response, "NULL");
        }
        */

    }
}
