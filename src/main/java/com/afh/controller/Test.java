package com.afh.controller;

import org.apache.commons.lang3.StringUtils;

import java.io.File;

/**
 * Created by chandan on 9/2/2015.
 */
public class Test {

    public static void main(String[] args) {
        String name ="chandan.abc.chan";
        String[] nameArr = name.split("\\.");
        System.out.println(nameArr.length);
        String finalName = nameArr[0] +"_"+System.currentTimeMillis();
        System.out.println(finalName);
        if(nameArr.length>=2) {
            finalName = finalName + "." + org.apache.commons.lang3.StringUtils.join(nameArr,'.',1,nameArr.length);
        }
        System.out.println(finalName);
    }

}
