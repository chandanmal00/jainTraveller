
        <#assign flag = 1>
        <#assign jainFlag = 1>
        <#assign cnt = cnt+1>
        <#if username?? && post["voters"]??>
             <#list post["voters"] as user>
                <#if username==user>
                    <#assign flag = 0>
                </#if>
             </#list>
        </#if>
        <#if username?? && post["jainVoters"]??>
             <#list post["jainVoters"] as user>
                <#if username==user>
                    <#assign jainFlag = 0>
                </#if>
             </#list>
        </#if>

<#macro getAjaxMarkers flag prefix fa cnt post extra>
    <#if flag==0>
        <span title="${prefix?cap_first}" class="label label-success fa" style="background-color: #c9302c; border-color: #761c19;">
            <i class="fa fa-${fa} fa"></i>
        </span>
    </#if>
    <#if flag!=0>
        <#assign val = post['permalink']>
        <span id="${prefix}_${cnt}">
            <a title="Press the Button to ${prefix?cap_first}" class="btn btn-primary fa" href="javaScript:void(0);" onclick='${prefix}PlusOne(${cnt}, "${val}");'>
                <i class="fa fa-${fa} fa-fw"></i> ${prefix?cap_first}${extra}
            </a>
        </span>
    </#if>
</#macro>

<#function min a b>
   <#if a gt b>
     <#return b />
   <#else>
     <#return a />
   </#if>
 </#function>

<#function showJainConfirmed post>

    <#if post["flag"]?? && post["flag"]==1>
        <#return true>
    </#if>

    <#if post["jainVoters"]??>
         <#assign sizeVotes = post["jainVoters"]?size>
         <#if sizeVotes gte 2 >
            <#return true>
         </#if>
    </#if>
    <#return false>
</#function>

        <div class="listing_details">
            <div class="row">
                <div class="col-md-2">
                        <#if post["userImages"]?has_content>
                            <#assign imgCnt = post["userImages"]?size>
                            Total Images: ${imgCnt} <br>
                            <div id="gallery">
                                    <#assign cnt=0>
                                    <#list post["userImages"] as tag>
                                        <#if cnt gte 1>
                                        <a style="visibility:hidden" href="/images/${post["permalink"]}/${tag["imageName"]}"><img style="visibility:hidden" src="/images/${post["permalink"]}/thumbnail.${tag["imageName"]}" alt="Image"/></a>
                                        <#else>
                                        <a href="/images/${post["permalink"]}/${tag["imageName"]}"><img src="/images/${post["permalink"]}/thumbnail.${tag["imageName"]}" alt="Image"/></a>
                                        </#if>
                                    <#assign cnt=cnt+1>
                                    </#list>
                             </div>
                        </#if>
                </div>
                <div class="col-md-8">
                    <div class="row">
                        <span id="title_${cnt}">
                            Type: ${post["type"]!""} : <a href="/post/${post["permalink"]}">${post["name"]}</a>
                        </span>
                            <#-- show likes, Jain food confirmed options-->
                            <span title="Liked by You" id="done_like_${cnt}" class="label label-danger fa" style="display:none;background-color: #c9302c;border-color: #761c19;">
                                <i class="fa fa-heart-o fa"></i>
                            </span>
                            <@getAjaxMarkers flag=flag prefix="like" fa="heart-o" cnt=cnt post=post extra=""/>

                            <#-- only applies to Restaurants -->
                            <#if post["type"]?cap_first == "Restaurant">
                                <#if showJainConfirmed(post)>
                                    <span class="label fa-lg" title="Verified Jain Listing"><i style="color:green;" class="fa fa-certificate">Jain Options Confirmed</i></span>
                                <#else>
                                    <span title="Jain Option confirmed by You" id="done_jain_${cnt}" class="label label-danger fa" style="display:none;background-color: #c9302c;border-color: #761c19;">
                                        <i class="fa fa-certificate fa"></i>
                                    </span>
                                    <@getAjaxMarkers flag=jainFlag prefix="jain" fa="certificate" cnt=cnt post=post extra=" options??"/>
                                </#if>
                            </#if>
                            <#-- verified markers -->
                            <#if post["voters"]??>
                                <#assign sizeVotes = post["voters"]?size>
                                <#if sizeVotes gte 2 >
                                    <span class="label fa-lg" title="Verified Listing"><i style="color:green;" class="fa fa-check-square">Verified</i></span>
                                </#if>
                            </#if>
                            <span class="flag_${cnt}">
                                <a id="button_flag_${cnt}" title="click to flag this listing for review" class="btn btn-primary fa" data-target="#exampleModal_${cnt}" data-toggle="modal" onclick='enable(${cnt});' data-name="${post["name"]}">
                                   <i class="fa fa-flag fa"></i> Flag
                                </a>
                            </span>
                    </div> <!--end of row-->

                    <div class="row">
                        <div class="class-md-8">
                        Category:
                                <#list post["skills"] as skill>
                                    ${skill},
                                </#list>
                        <br>
                        Details:
                        <br>
                        <div class="extraInfo">
                                        <ul>
                                        <#if post["type"]?cap_first == "Temple">
                                            <#assign infoList = ["zipcode", "area", "contact", "address"]>
                                            <#list infoList as element>
                                                 <#if post[element]??>
                                                      <li>${element} : ${post[element]}</li>
                                                  </#if>
                                            </#list>
                                        </#if>
                                            <li>Location: ${post["city"]}, ${post["state"]} ${post["country"]}</li>
                                         </ul>
                                    </div>
                        </div>
                    </div><!--end of row-->
                    <div class="row">
                                            <div class="class-md-8">
                                                <span class="label label-default">upvotes:</span><span id="upvote_${cnt}" class="badge">${post["voters"]?size}</span>
                                                    <span class="label label-default">inNetworkUpvotes:</span><span id="networkupvote_${cnt}" class="badge">${post["voters"]?size}</span>
                                                    <br>
                                                    <#if post["voters"]?? && post["voters"]?size gt 0 >
                                                    <ul>
                                                            <li>Upvoted  By:
                                                            <span id="upvoters_${cnt}">
                                                                <#--<#list post["voters"] as user>-->
                                                                <#assign voters=post["voters"]>
                                                                <#assign voteCnt=0>
                                                                <#list voters as user>
                                                                     <#if voteCnt lt 5>
                                                                <#--<#list 0..min(post["voters"]?size,5) as userIndex>-->
                                                                        <i>${user}</i>,
                                                                      <#else>
                                                                         <#break>
                                                                      </#if>
                                                                     <#assign voteCnt=voteCnt+1>
                                                                </#list>
                                                                <#if post["voters"]?size gt 5>
                                                                     <i>...</i>
                                                                </#if>
                                                            </span>
                                                            </li>
                                                    </ul>
                                                    </#if>
                                            </div>
                                        </div><!--end of row-->
                </div>
            </div>

            <div class="row">
                <div class="col-md-8">
                    <#-- only show the upload form on the listing page -->
                    <#if showComments?? && showComments==1>
                        <div class="upload_photo_${cnt}">
                             <span id="uploadPhoto_${cnt}">
                                  <a id="uploadButton_${cnt}"title="Press the Button to Upload Photo" class="btn btn-primary fa" href="javaScript:void(0);" onclick='showUpload(${cnt});'>
                                  <i class="fa fa-file-image-o fa-fw"></i> Upload Photo
                                  </a>
                                  <a id="hideButton_${cnt}" title="Press the Button to Hide Upload Photo" class="btn btn-primary fa" href="javaScript:void(0);" onclick='hideUpload(${cnt});' style="display:none">
                                                    <i class="fa fa-file-image-o fa-fw"></i> Hide Photo Upload
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
                </div>
            </div> <!--end of row-->

        <div class="row>
        <div class="col-md-8">

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
                                <#--${counter} : <i>user:${tag["username"]}</i> <br> -->
                               ${counter} :<i>${tag["comment"]}</i>
                               </p>
                               <hr class="hr_comments">
                               <br>
                               <#assign counter = counter + 1>
                            </#list>
                        </#if>
                    </#if>

        </div>
        </div>
        <div class="clear" style="clear:both"></div>
        <div class="row>
        <div id="commentsDiv_${cnt}">
                        <input type="hidden" id="permalink_input_${cnt}" value="${post["permalink"]}" name="permalink">
                        <textarea class="col-xs-5" id="comments_input_${cnt}" rows=1 name="comments" placeholder="comment here..." >${comments!""}</textarea>
                        <span id="loaddata_${cnt}">
                            <a title="Press to Comment..." id="loaddata_${cnt}" onclick='loadData(${cnt});'  href="javaScript:void(0);" class="btn btn-primary fa">
                                 <i class="fa fa-comment fa"></i> Comment
                            </a>
                         </span>

                    </div>

        </div>

        <div class="clear" style="clear:both"></div>




<div class="modal fade" id="exampleModal_${cnt}" tabindex="-1" role="dialog" aria-labelledby="exampleModalLabel">
  <div class="modal-dialog" role="document">
    <div class="modal-content">
      <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
        <h4 class="modal-title" id="exampleModalLabel"><small>Report the listing!!</small></h4>
      </div>
      <div class="modal-body">
        <form class="contact_${cnt}" name="contact_${cnt}">
          <input type="hidden" id="permalink_input_${cnt}" value="${post["permalink"]}" name="permalink">
          <input type="hidden" id="cnt_input_${cnt}" value="${cnt}" name="cnt">

          <div class="form-group">
            <label for="message-text" class="control-label"><b><small>Let us know the reason to flag this listing:</small></b></label>
            <textarea class="form-control" id="reason" rows=1 name="reason"></textarea>
          </div>
          <div id="successModal_${cnt}" style="display:none;">posted</div>
        </form>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
        <input class="btn btn-primary" onclick="flagData(${cnt});" type="submit" value="Report!" id="submit_${cnt}">
      </div>


    </div>
  </div>
</div>
                        <div class="clear" style="clear:both"></div>
                        <br>


            <div id="success_${cnt}" style="display:none;">posted</div>
            <div class="clear" style="clear:both"></div>
            <br>
            <br>
            <br>
            <div class="clear" style="clear:both"></div>
            <br>


            <#if isAdmin??>
               <#if jsonListing??>
                  <br>
                  ${jsonListing}
                  <br>
               </#if>
            </#if>
            <hr class="hr_class"/>
            <br>
        </div>