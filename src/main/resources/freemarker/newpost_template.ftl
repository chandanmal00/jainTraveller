<!DOCTYPE html>
<html lang="en">
  <head>
    <#include "/common/meta.ftl">
    <#include "/css/head.css">
    <title>Create a new listing for ${APP_TITLE}</title>
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
        <div class="new_post_form">
        <form action="/newpost" method="POST">
            ${errors!""}
            <h2>Create a new listing and help your friends make the best decision, Go Ahead!!</h2>
                <div class="row required"><div class="left">Type:</div> <div class="right"><input type="text" placeholder="Is it a Restaurant, Shopping Location..." name="type" size="120" value="${type!""}"></div></div>
                <div class="row required"><div class="left">Skill:</div> <div class="right"> <input type="text" placeholder="Jain, Vegetarian, Vegan, Budget Shopping..." name="skill" size="120" value="${skill!""}"> Comma separated, please...</div></div>
                <div class="row required"><div class="left">Name:</div> <div class="right"> <input type="text" placeholder="Name of the Restaurant..." name="name" size="120" value="${name!""}"></div></div>
                <div class="row required"><div class="left">City:</div> <div class="right"> <input type="text" placeholder="city" name="city" size="120" value="${city!""}"></div></div>
                <div class="row required"><div class="left">State:</div> <div class="right"> <input type="text" id="state" placeholder="state" name="state" size="120"></div></div>
                <div class="row required"><div class="left">Country:</div> <div class="right"> <input type="text" placeholder="country" name="country" size="120" value="${country!""}"></div></div>
                <div class="row"><div class="left">Notes:</div> <div class="right"> <textarea name="notes" placeholder="what best do you remember about it... This is going to help your friends" cols="40" rows="3">${notes!""}</textarea></div></div>

            <input type="submit" class="search" value="Add Post and help Your Friends :-)">
        </form>
        </div>
        <#include "/common/footer.ftl">
    </div>
    <#include "/js/footer_js.ftl">
    <#include "/js/alerts_js.ftl">
    <#include "/js/auto_complete_js.ftl">
</body>
</html>

