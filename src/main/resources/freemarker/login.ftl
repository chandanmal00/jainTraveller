<!DOCTYPE html>
<html lang="en">
  <head>
    <#include "/common/meta.ftl">
    <#include "/css/head.css">
    <#include "/css/login.css">
    <#include "/js/footer_js.ftl">
    <#include "/js/login_js.ftl">
</head>
<body>

    <div class="container" style="margin-top:150px;">
        <nav class="navbar navbar-default navbar-fixed-top"
             role="navigation">
            <div class="container">
                <div class="navbar-header">
                    <h1 class="jainTraveller-logo">
                        <a href="/">${APP_LINK}<sup><small>(Beta!)</small></sup></a>
                    </h1>

                </div>

                <div class="collapse navbar-collapse navbar-right">
                    <ul class="nav navbar-nav">
                        <li><a href="#" onclick="showLogin();">Login</a></li>
                        <li><a href="#" onclick="showSignup();">Signup</a></li>
                    </ul>
                </div>
            </div>
        </nav>

        <#include "/common/alerts.ftl">

        <div class="content">
            <div class="content-section content-section-dark">
                <div class="container">
                    <div class="row">
                        <div id="loginShow">
                            <div id="login-section">

                                <div class="login info-card info-card-padding roundify">
                                    <div class="content-section-headings" style="text-align:center;">
                                        <h3 class="content-section-header">Jai Jinendra!!</h3>
                                        <h5 class="content-section-subheader">Nice to see you again, Welcome Back! New User <a onclick="showSignup();" href="#">Signup</a> Please!!</h5>
                                    </div>

                                    <form id="login-form"
                                          class="form-horizontal"
                                          method="post"
                                          action="/login"
                                          data-bv-message="This value is not valid"
                                          data-bv-feedbackicons-valid="glyphicon glyphicon-ok"
                                          data-bv-feedbackicons-invalid="glyphicon glyphicon-remove"
                                          data-bv-feedbackicons-validating="glyphicon glyphicon-refresh"
                                            >
                                          <#if login_error??>
                                          <div class="alert alert-danger">
                                                ${login_error!""}
                                          </div>
                                          </#if>
                                        <input type="hidden" name="sessionId" value="sessionId" />
                                        <input type="hidden" name="permalink" value="${permalink!""}" />
                                        <div class="form-group">
                                            <label class="control-label col-xs-4" for="email">Email</label>
                                            <div class="col-xs-7">
                                                <input type="email" class="form-control"
                                                       name="email" value="${username!""}" placeholder="Email" required>
                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label class="control-label col-xs-4" for="password">Password</label>
                                            <div class="col-xs-7">
                                                <input type="password" class="form-control"
                                                       name="password" placeholder="Password" required>
                                            </div>
                                        </div>

                                        <div class="form-group">
                                            <div class="col-xs-offset-4 col-xs-7">
                                                <button type="submit" class="btn btn-primary">Sign in</button>
                                                <span>New User <a onclick="showSignup();" href="#">Signup</a> Please!!</span>
                                            </div>
                                        </div>


                                    </form>


                                    <center>
                                        Forgot password? <a href="/reset">Click here to reset</a>
                                    </center>
                                </div>
                            </div>

                        </div>


                        <div id="signupShow">
                            <div id="signup-section">
                                <div id="sign-up-info" class="info-card info-card-padding roundify">
                                    <div class="content-section-headings" style="text-align:center;">
                                        <h3 class="content-section-header">Create Account</h3>
                                        <h5 class="content-section-subheader">Sign up takes less than 10 seconds!! Already registered, <a onclick="showLogin();" href="#">Login</a>!!</h5>
                                    </div>

                                    <form id="sign-up-info-form" class="form-horizontal"
                                          action="/signup"
                                          method="post"
                                          data-bv-message="This value is not valid"
                                          data-bv-feedbackicons-valid="glyphicon glyphicon-ok"
                                          data-bv-feedbackicons-invalid="glyphicon glyphicon-remove"
                                          data-bv-feedbackicons-validating="glyphicon glyphicon-refresh">
                                          <#if signup_error??>
                                              <div class="alert alert-danger">
                                                    ${signup_error!""}
                                              </div>
                                          </#if>
                                        <div class="form-group">
                                            <label class="control-label col-xs-4" for="email">Email Address</label>
                                            <div class="col-xs-7">
                                                <input type="email" class="form-control" id="email"
                                                       name="email" placeholder="Email address" value="${username!""}" required/>
                                            </div>
                                        </div>

                                        <div class="form-group">
                                            <label class="control-label col-xs-4" for="password">Password</label>
                                            <div class="col-xs-7">
                                                <input type="password" class="form-control" id="password"
                                                       name="password" placeholder="Password"
                                                       data-bv-stringlength="true"
                                                       data-bv-stringlength-min="8" required/>
                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label class="control-label col-xs-4" for="password_confirm">Password (confirm)</label>
                                            <div class="col-xs-7">
                                                <input type="password" class="form-control" id="password_confirm"
                                                       name="password_confirm" placeholder="Password"
                                                       data-bv-identical="true"
                                                       data-bv-identical-field="password" required/>
                                            </div>
                                        </div>
                                        <input type="hidden" name="permalink" value="${permalink!""}" />
                                        <#if username_error?? || email_error?? || verify_error?? || password_error?? >
                                            <div class="alert alert-danger">
                                                 ${username_error!""}
                                                 ${email_error!""}
                                                 ${verify_error!""}
                                                 ${password_error!""}
                                            </div>
                                        </#if>
                                        <div class="form-group">
                                            <div class="col-xs-offset-4 col-xs-7">
                                                <button type="submit" class="btn btn-primary">Sign Up!</button>
                                                <span>
                                                      By signing up, you agree to our <a href="/tos">Terms and Conditions</a>!
                                                </span>
                                                <div class="disclaimer">
                                                      Please also read our <a href="/disclaimer">Disclaimer</a>!
                                                </div>
                                                <span>Already registered, <a onclick="showLogin();" href="#">Login</a>!!</span>
                                            </div>
                                        </div>
                                    </form>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
        <#include "/js/alerts_js.ftl">
</body>

</html>