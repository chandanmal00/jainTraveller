package com.afh.constants;

/**
 * Created by chandan on 11/21/2015.
 */
public enum FilterValue {


    JainTemple("jaintemple"),
    Indian("indian"),
    Vegan("vegan"),
    Vegetarian("vegetarian"),
    Restaurant("restaurant"),
    Temple("temple");

    private final String value;

    FilterValue(final String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        // TODO Auto-generated method stub
        return getValue();
    }

    public static void main(String[] args) {
        String t = "type";
    }

}
