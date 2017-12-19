<!DOCTYPE html>
<html lang="en">
<head>
    <#include "/common/meta.ftl">
    <#include "/css/head.css">
    <#include "/common/title.ftl">
</head>
<body>
    <div class="container-fluid">
        <#include "/common/header.ftl">
        <br>
        <#assign cnt = 0>
        <#if postListing??>
        <#else>
            <h2>Listings added recently</h2>
        </#if>
        <#list askForHelpDocuments as post>
             <#include "/common/listing_display.ftl">
        <#assign cnt =cnt+1>
        </#list>
        <#include "/common/footer.ftl">
    </div>
    <#include "/js/footer_js.ftl">
    <#include "/js/alerts_js.ftl">
    <#include "/js/carousel_js.ftl">
    <#include "/js/ajax_refresh_js.ftl">

</body>
</html>