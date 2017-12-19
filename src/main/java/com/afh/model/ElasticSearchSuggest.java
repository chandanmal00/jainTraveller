package com.afh.model;

/**
 * Created by chandan on 9/26/2015.
 */
public class ElasticSearchSuggest {
    Object input;
    Object output;
    Object payload;

    public ElasticSearchSuggest(Object input, Object output, Object payload) {
        this.input = input;
        this.output = output;
        this.payload = payload;
    }

    public ElasticSearchSuggest() {
    }

    public Object getInput() {
        return input;
    }

    public void setInput(Object input) {
        this.input = input;
    }

    public Object getOutput() {
        return output;
    }

    public void setOutput(Object output) {
        this.output = output;
    }

    public Object getPayload() {
        return payload;
    }

    public void setPayload(Object payload) {
        this.payload = payload;
    }
}
