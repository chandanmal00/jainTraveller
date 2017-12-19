<!DOCTYPE html>
<html lang="en">
  <head>
    <#include "/common/meta.ftl">
    <#include "/css/head.css">
    <title>${APP_TITLE} page for Getting Help from Friends</title>
    <style>
        #state {
        display: block;
        font-weight: bold;
        margin-bottom: 1em;
        }
    </style>

</head>
<body>
    <div class="container-fluid">
        <#include "/common/header.ftl">
        <div class="get_help_form">
            <form action="/getHelp" method="POST">
                <div class="error">
                ${errors!""}
                </div>
                <div class="success">
                ${success!""}
                </div>
                <h2>Get Help from your Network, Reach out to them</h2>
                <div class="row required"><div class="left">Type:</div> <div class="right"><input type="text" placeholder="Is it a Restaurant, Shopping..." name="type" size="120" value="${type!""}"></div></div>
                <div class="row required"><div class="left">Category:</div> <div class="right"><input type="text" placeholder="Jain, Vegan, Budget Shopping..." name="skill" size="120" value="${skill!""}"> Comma separated, please...</div></div>
                <div class="row required"><div class="left">City:</div> <div class="right"><input type="text" placeholder="city" name="city" size="120" value="${city!""}"></div></div>
                <div class="row required"><div class="left">State:</div> <div class="right"> <input type="text" id="state" placeholder="state" name="state" size="120" value="${state!""}"></div></div>
                <div class="row required"><div class="left">Country:</div> <div class="right"><input type="text" placeholder="country" name="country" size="120" value="${country!""}"></div></div>
                <div class="row required"><div class="left">Emails of Your Friends:</div> <div class="right"><textarea name="emails" placeholder="someone@help.com, friend@friend.com" cols="40" rows="3">${emails!""}</textarea></div></div>

                <input type="submit" class="search" value="Go Ahead and get that help :-)">
            </form>
        </div>
        <#include "/common/footer.ftl">
    </div>
    <#include "/js/footer_js.ftl">
    <#include "/js/alerts_js.ftl">
    <#include "/js/auto_complete_js.ftl">



</body>
</html>

