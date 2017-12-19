<!DOCTYPE html>
<html lang="en">
    <head>
        <#include "/common/meta.ftl">
        <#include "/css/head.css">
        <#include "/css/profile.css">
        <title>OCR Page for Jain Traveller</title>

    </head>
    <body>
        <div class="container-fluid">
        <#include "/common/username_check.ftl">
        <p><b>OCR text identified:</b></p>
        ${ocrText!""}
        <br>
        <br>
        <p>Results: ${jsonResult!""}</p>
        <br>
        <br>
        <#include "/common/footer.ftl">
        </div>
        <#include "/js/footer_js.ftl">
        <#include "/js/alerts_js.ftl">
        <#include "/js/carousel_js.ftl">
        <#include "/js/ajax_refresh_js.ftl">
    </body>
</html>