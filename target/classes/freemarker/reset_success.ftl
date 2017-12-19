<!DOCTYPE html>
<html lang="en">
<head>
    <#include "/common/meta.ftl">
    <#include "/css/head.css">
    <title>Reset password page for Jain Traveller</title>
</head>
<body>
    <div class="container-fluid">
        <#include "/common/header.ftl">
        <h4>Reset password for email: '${email}'</h4>
        <hr class="hr_class"/>
        <br>
        <div>
            <span class="label label-success fa-lg">
                Password reset successful!!
            </span>
            <br>
            <br>
            We have received your request, we will contact you for resetting your password<br>
            Look for email in you inbox for instructions!!
            <br>
            <br>
            <span class="label label-default fa-lg">
                <b>Note</b>: You'll only receive instructions if an account by that email exists.
            </span>
        </div>
        <#include "/common/footer.ftl">
    </div>
    <#include "/js/footer_js.ftl">
</body>
</html>