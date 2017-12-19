<noscript>
    <div class="alert alert-danger" id="js_test">
        <a href="#" class="close" data-dismiss="alert">&times;</a>
        <strong>Javascript Disabled!!</strong> Site will not work as expected, so please enable Javascript to enjoy the site!.
        Here are the <a href="http://www.enable-javascript.com/" target="_blank"> instructions how to enable JavaScript in your web browser</a>
    </div>
</noscript>

<div class="alert alert-info" id="new_info">
    <a href="#" class="close" data-dismiss="alert">&times;</a>
    Updates to site happen every day, please do visit the site or email <strong>${INFO_EMAIL}</strong> if you have any feedback, Thanks for your support.
</div>
<div class="alert alert-danger" style="display:none" id="cookie_test">
    <a href="#" class="close" data-dismiss="alert">&times;</a>
    <strong>Cookies Disabled!!</strong> Looks like you have cookies disabled, please enable them to enjoy the site!.
    Here are the <a href="https://support.mozilla.org/en-US/kb/enable-and-disable-cookies-website-preferences?redirectlocale=en-US&redirectslug=Enabling+and+disabling+cookies" target="_blank"> instructions how to enable Cookies in your web browser</a>
</div>

<div class="alert alert-info" style="display:none" id="profile_test" >
    <a href="#" class="close" data-dismiss="alert">&times;</a>
    <strong>ProfileUpdate!!</strong> Looks like you do not have an updated profile, please do so by clicking <b><a href="/profile">Profile</a></b> link!.
</div>

<#if logout??>
    <div class="alert alert-info" id="logout_test" >
        <a href="#" class="close" data-dismiss="alert">&times;</a>
        <strong>Logout!!</strong> You have successfully Logout!!.
    </div>
</#if>