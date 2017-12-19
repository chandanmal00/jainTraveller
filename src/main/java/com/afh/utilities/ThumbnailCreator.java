package com.afh.utilities;

import com.afh.constants.Constants;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.name.Rename;

/**
 * Created by chandan on 10/18/2015.
 */
public class ThumbnailCreator {

    static final String IMAGE_LOC = "D://var//www//public//Penguins.jpg";
    public static void main(String[] args) {
       // makeLogo();
        //makeFavicon();
        makeStandard();
    }

    public static void  makeLogo() {
        try {
            Thumbnails.of(IMAGE_LOC)
                    .size(100, 25)
                    .outputFormat("png")
                    .toFiles(Rename.PREFIX_HYPHEN_THUMBNAIL);
        } catch(Exception e) {
            e.printStackTrace();
            System.out.println("error");
        }

    }

    public static void makeFavicon() {
        try {
            Thumbnails.of(IMAGE_LOC)
                    .size(32, 8)
                    .outputFormat("png")
                    .toFiles(Rename.PREFIX_DOT_THUMBNAIL);
        } catch(Exception e) {
            e.printStackTrace();
            System.out.println("error");
        }

    }


    public static void makeStandard() {
        try {
            Thumbnails.of(IMAGE_LOC)
                    .size(Constants.THUMBNAIL_WIDTH, Constants.THUMBNAIL_HEIGHT)
                    .outputFormat("jpg")
                    .toFiles(Rename.PREFIX_DOT_THUMBNAIL);
        } catch(Exception e) {
            e.printStackTrace();
            System.out.println("error");
        }

    }
}
