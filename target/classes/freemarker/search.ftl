<!DOCTYPE html>
<html lang="en">
    <head>
        <#include "/common/meta.ftl">
        <#include "/css/head.css">
        <#include "/css/profile.css">
        <title>Search Page for Jain Traveller</title>
        <#include "/css/extra_search.css">
          <style type="text/css">

.searchImage {
      	width: 400px;
      	height: 100px;
}

          </style>
    </head>
    <body>
        <div class="container-fluid">
        <#include "/common/username_check.ftl">
        <br>
        <div id="image">
            <img src="/images/jainTraveller.png" alt="${APP_TITLE}" class="searchImage">
        </div>
        <br>
        <div class="search_form">
            <form action="/search" method="POST">
                <p class="error">${errors!""}</p>
                        <select name="filter" id="filter" style="width: 150px !important; min-width: 150px; max-width: 150px;">
            <option value="all">Everything</option>
            <option value="Type_Restaurant">Restaurant</option>
            <option value="Type_Temple">Temple</option>
            <option value="Category_JainTemple">JainTemple</option>
            <option value="Category_Indian">Indian Restaurants</option>
            <option value="Category_Vegetarian">Indian Vegetarian Restaurants</option>
            <option value="Category_Vegan">Indian Vegan Restaurants</option>
                        </select>
                <input type="text" id="search" name="search" size="150" value="${search!""}" placeholder="Search for Jain Temples, Jain Restaurants, Choose dropdown to drill down...">
                <input type="submit" class="search" value="Go">
            </form>
        </div>

        <#include "/common/footer.ftl">
        </div>
        <#include "/js/footer_js.ftl">
        <#include "/js/alerts_js.ftl">
        <#include "/js/carousel_js.ftl">
        <#include "/js/ajax_refresh_js.ftl">
    </body>
</html>