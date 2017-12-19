
        <#assign flag = 1>
        <#assign jainFlag = 1>
        <#assign cnt = cnt+1>
        <#if username!="guest"  && post["voters"]??>
             <#list post["voters"] as user>
                <#if username==user>
                    <#assign flag = 0>
                </#if>
             </#list>
        </#if>
        <#if username!="guest" && post["jainVoters"]??>
             <#list post["jainVoters"] as user>
                <#if username==user>
                    <#assign jainFlag = 0>
                </#if>
             </#list>
        </#if>

        <div class="listing_details">
            <p id="title_${cnt}">Type: ${post["type"]!""} :
                <a href="/post/${post["permalink"]}">${post["name"]}</a>
                <span title="Liked by You" id="liked_${cnt}" class="label label-danger fa" style="display:none;background-color: #c9302c;border-color: #761c19;">
                    <i class="fa fa-heart-o fa"></i>
                </span>

                <#if flag==0>
                      <span title="Liked by You" class="label label-danger fa" style="background-color: #c9302c; border-color: #761c19;">
                        <i class="fa fa-heart-o fa"></i>
                      </span>
                </#if>
                <#if flag!=0>
                     <#assign val = post['permalink']>
                     <span id="plus_${cnt}">
                         <a title="Press the Button to Like this" class="btn btn-primary fa" href="javaScript:void(0);" onclick='plusone(${cnt}, "${val}","${username}");'>
                         <i class="fa fa-heart-o fa-fw"></i> Like It
                         </a>
                     </span>
                </#if>

                <span title="Jain Option confirmed by You" id="jain_liked_${cnt}" class="label label-danger fa" style="display:none;background-color: #c9302c;border-color: #761c19;">
                    <i class="fa fa-certificate fa"></i>
                </span>

                <#if jainFlag==0>
                      <span title="Jain Option confirmed by You" class="label label-danger fa" style="background-color: #c9302c; border-color: #761c19;">
                        <i class="fa fa-certificate fa"></i>
                      </span>
                </#if>
                <#if jainFlag!=0>
                     <#assign val = post['permalink']>
                     <span id="jainVote_${cnt}">
                       <a title="Press the Button to confirm they serve Jain" class="btn btn-primary fa" href="javaScript:void(0);" onclick='plusJain(${cnt}, "${val}","${username}");'>
                           <i class="fa fa-certificate fa-fw"></i> Jain Options
                       </a>
                     </span>
                </#if>


                <#assign sizeVotes = post["voters"]?size>
                <#if sizeVotes gte 2 >
                    <span class="label fa-lg" title="Verified Listing"><i style="color:green;" class="fa fa-check-square">Verified</i></span>
                    <span class="label fa-lg" title="Verified Jain Listing"><i style="color:green;" class="fa fa-certificate">Jain Options Confirmed</i></span>
                </#if>
            </p>
            <div> Category:
                <#list post["skills"] as skill>
                    ${skill},
                </#list>
            </div><br>
            <span class="label label-default">upvotes:</span><span id="upvote_${cnt}" class="badge">${post["voters"]?size}</span>
            <span class="label label-default">inNetworkUpvotes:</span><span id="networkupvote_${cnt}" class="badge">${post["voters"]?size}</span>
            <br>

    <#if post["voters"]?? && post["voters"]?size gt 0 >
            Created  <i>By ${post["voters"][0]}</i><br>
            Upvoted  By:
            <span id="upvoters_${cnt}">
                <#list post["voters"] as user>
                     <i>${user} </i>,
                </#list>
            </span>
    </#if>

            <br>
            Location: ${post["city"]}, ${post["state"]} ${post["country"]}
            <br>
            <div style="clear:both"></div>

            <#-- only show the upload form on the listing page -->
            <#if showComments?? && showComments==1>
                <div class="upload_photo_${cnt}">
                     <span id="uploadPhoto_${cnt}">
                          <a title="Press the Button to Upload Photo" class="btn btn-primary fa" href="javaScript:void(0);" onclick='showUpload(${cnt});'>
                          <i class="fa fa-file-image-o fa-fw"></i> Upload Photo
                          </a>
                          <a title="Press the Button to Upload Photo" class="btn btn-primary fa" href="javaScript:void(0);" onclick='hideUpload(${cnt});'>
                                            <i class="fa fa-file-image-o fa-fw"></i> Hide Photo
                                            </a>
                      </span>
                      <div id="upload_photo_form_${cnt}" style="display:none">
                            <form action="/upload_listing/${post['permalink']}" method="post" enctype="multipart/form-data">
                              Select image to upload:
                              <input type="file" name="fileToUpload_${cnt}" id="fileToUpload">
                              <input type="submit" class="btn btn-primary fa" value="Upload Image" name="submit">
                            </form>
                      </div>

                </div>
            </#if>
            <br>
            <div style="clear:both"></div>

            Comments:
            <#if post["comments"]??>
                <#assign numComments = post["comments"]?size>
            <#else>
                <#assign numComments = 0>
            </#if>

            <a id="cnt_comments_${cnt}" href="/post/${post["permalink"]}">${numComments}</a><br>

            <#-- only show the comments on the listing page -->
            <#if showComments?? && showComments==1>
                <#assign counter = 0>
                <#if post["comments"]??>
                    <#list post["comments"] as tag>
                       <p>
                       ${counter} : <i>user:${tag["username"]}</i> <br>
                       <i>${tag["comment"]}</i>
                       </p>
                       <hr style="
                           border-color: gray;
                           border-style: solid;
                           border-bottom: 1px;
                           margin:0px;
                           padding:0px;"
                       >
                       <#assign counter = counter + 1>
                    </#list>
                </#if>
            </#if>

            <div id="commentsDiv_${cnt}">
                <input type="hidden" id="permalink_input_${cnt}" value="${post["permalink"]}" name="permalink">
                <textarea class="col-xs-5" id="comments_input_${cnt}" rows=1 name="comments" placeholder="comment here..." >${comments!""}</textarea>
                <span id="loaddata_${cnt}">
                    <a title="Press to Comment..." id="loaddata_${cnt}" onclick='loadData(${cnt});'  href="javaScript:void(0);" class="btn btn-primary fa">
                         <i class="fa fa-comment fa"></i> Comment
                    </a>
                 </span>
                 <span id="success_${cnt}" style="display:none;">posted</span>
            </div>

            <br>
            <br>
            <br>
            <#if post["userImages"]?has_content >
               <div class="entry-content" id="listing_images_${cnt}">
                    <div class="imgWidget">
                        <a class="prev" href="#">&laquo;</a>
                        <div class="carousel" style="visibility: visible; overflow: hidden; position: relative; z-index: 2; left: 0px; width: 400px;">
                            <ul style="margin: 0px; padding: 0px; position: relative; list-style: outside none none; z-index: 1; width: 1760px;left:0px;">
                                 <#list post["userImages"] as tag>
                                <li style=" border-color: #ccc; border-style: solid; border: 1px, padding:2px;overflow: hidden; float: left; width: ${WIDTH}px; height: ${HEIGHT}px;"><img src="/${post["permalink"]}/thumbnail.${tag["imageName"]}" class="img-circle" alt="Image"></li>
                                 </#list>
                           </ul>
                        </div>
                        <a class="next" href="#">&raquo;</a>
                        <div class="clear"></div>
                    </div>

                </div>
            </#if>
            <div class="clear" style="clear:both"></div>
            <br>
            <hr class="hr_class"/>
            <br>
        </div>