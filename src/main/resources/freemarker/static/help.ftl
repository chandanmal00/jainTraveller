<!DOCTYPE html>
<html lang="en">
<head>
    <#include "/common/meta.ftl">
    <#include "/css/head.css">
    <title>Help and Support page for Jain Traveller</title>
</head>
<body>
    <div class="container-fluid">
        <#include "/common/username_check.ftl">
        <h2>Contact Us</h2>
        <hr class="hr_class"/>
        <br>
        <div>
        We would love to have your feedback for our site, or love to add more restaurants to our site especially ones that you already know and love!
        <br>Please send us an email at <b><a href="mailto:${INFO_EMAIL}">${INFO_EMAIL}</a></b> and let us know, we will do our best to make it happen! :)
        <br>
        <br>
        ${APP_LINK}’s mission is to organize the Jain Temples, Jain Restaurants, Jain Foods, Jain Festivals, anything related to Jain Religion and make it available and useful.
        </div>
        <#include "/common/footer.ftl">
    </div>
</body>
</html>