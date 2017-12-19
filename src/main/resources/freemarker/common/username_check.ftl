
      <div class="masthead">
          <span>
              <a href="/"><img style="vertical-align: middle;" src="/images/logo-jainTraveller.png" alt="${APP_TITLE}" class="jainTravellerLogoSmall"><sup><b><small>(Beta!)</small></b></sup></a>
              <small><b> Welcome ${username!"Guest"}!! </b></small>
          </span>
          <#if username??>
              <nav>
                  <ul class="nav nav-justified">
                      <li><a href="/welcome">Home</a></li>
                      <li><a href="/profile">Profile</a></li>
                      <li><a href="/newpost">Add NewListing</a></li>
                      <li><a href="/getHelp">Ask Friends</a></li>
                      <li><a href="/search">Search</a></li>
                      <li><a href="/help">Contact Us</a></li>
                      <li><a href="/logout">Logout</a></li>
                  </ul>
              </nav>
          <#else>
               <nav>
                  <ul class="nav nav-justified">
                      <li><a href="/">Home</a></li>
                      <li><a href="/signup">SignUp</a></li>
                      <li><a href="/login">Login</a></li>
                      <li><a href="/search">Search</a></li>
                      <li><a href="/help">Contact Us</a></li>
                  </ul>
               </nav>
          </#if>
       </div>
       <#include "/common/alerts.ftl">

