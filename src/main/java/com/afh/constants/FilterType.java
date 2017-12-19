package com.afh.constants;

/**
 * Created by chandan on 11/21/2015.
 */
public enum FilterType {

    Skill("skills"),
    Category("skills"),
    Type("type");
    /*
    City("city"),
    State("state"),
    Country("country");
    */

    private final String value;

    FilterType(final String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static FilterType fromValue(String v) {
        return valueOf(v);
    }
    @Override
    public String toString() {
        // TODO Auto-generated method stub
        return getValue();
    }

    public static void main(String[] args) {
        String t = "type";

        System.out.println(FilterType.Type + "::" + FilterType.Skill + "::" + fromValue("Type"));
        FilterType a = fromValue("Skill");
        System.out.println(a.toString());
    }
}
