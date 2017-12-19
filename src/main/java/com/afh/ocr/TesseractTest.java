package com.afh.ocr;

import net.sourceforge.tess4j.Tesseract;

import java.io.File;

/**
 * Created by chandan on 5/7/2016.
 */
public class TesseractTest {

    public static void main(String[] args) {

        File imageFile = new File("D:\\ocr\\1.jpg");
        Tesseract instance = new Tesseract();
        //instance.setDatapath("C:\\Users\\chandan\\.m2\\repository\\net\\sourceforge\\tess4j\\tess4j\\3.1.0\\tessdata");
        instance.setDatapath(".");
        try {
            String result = instance.doOCR(imageFile);
            System.out.println("resut***");
            System.out.println(result);
            } catch(Exception e) {
            System.out.println("Error");
            e.printStackTrace();
        }

    }
}
