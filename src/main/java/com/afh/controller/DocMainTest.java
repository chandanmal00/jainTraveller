package com.afh.controller;

/**
 * Created by chandan on 10/11/2015.
 */
public class DocMainTest {

    public static void main(String[] args) {
        AskForHelpDAO askForHelpDAO = new AskForHelpDAO();
        askForHelpDAO.updateListingWithJainFoodOption("indian_oven_restaurant_sanfrancisco_ca_us","chandan");
    }
}
