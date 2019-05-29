
// Copyright 2012 Google Inc. All rights reserved.
(function(w,g){w[g]=w[g]||{};w[g].e=function(s){return eval(s);};})(window,'google_tag_manager');(function(){

var data = {
"resource": {
  "version":"139",
  "macros":[{
      "function":"__v",
      "vtp_name":"gtm.element",
      "vtp_dataLayerVersion":1
    },{
      "function":"__v",
      "vtp_name":"gtm.elementClasses",
      "vtp_dataLayerVersion":1
    },{
      "function":"__aev",
      "vtp_varType":"TEXT"
    },{
      "function":"__jsm",
      "vtp_javascript":["template","(function(){var a=",["escape",["macro",0],8,16],";return a.matches(\".slideshow-arrow-control\")?",["escape",["macro",1],8,16],":a.matches(\".by_line_date *\")||a.matches(\".content *\")||a.matches(\"#network_contents *\")||a.matches(\".fixed-bar-nav *\")||a.matches(\"ul.top-nav *\")||a.matches(\".trend-bar-spacer *\")||a.matches(\".headlines *\")||a.matches(\".footer-lcolumn *\")||a.matches(\".album_right_control *\")?",["escape",["macro",2],8,16],":a.matches(\".bio_social *\")?(a=a.children[0].alt)?a:void 0:a.matches(\"#slideshow_images *\")?a.getAttribute(\"title\"):\na.matches(\"a.footer-button *\")?(a=a.getAttribute(\"title\"))?a:void 0:a.children[0].matches(\"img.main_logo\")?\"LiveScience Logo\":a.matches(\".ls-presents *\")?(a=a.children[0].alt)?a:void 0:a.matches(\"#sticky_social *\")?(a=a.parent().parent().getAttribute(\"class\"))?a:void 0:",["escape",["macro",2],8,16],"})();"]
    },{
      "function":"__jsm",
      "vtp_javascript":["template","(function(){var a=",["escape",["macro",0],8,16],";return a.matches(\"#slideshow *\")?\"Slideshow\":a.matches(\".ls-presents *\")?\"LiveScience Presents\":a.matches(\".content *\")?\"Article Body\":a.matches(\"#parsely_most_popular *\")?\"More from LiveScience\":a.matches(\"#sticky_social *\")?\"Sticky Share\":a.matches(\".article_additional *\")?\"Article Body - Editor's Recommendations\":a.matches(\".by_line_date *\")?\"Title Byline\":a.matches(\".trend-bar-spacer *\")?\"Top Navigation Bar - Trending\":a.matches(\".bio_social *\")?\"Author Bio\":\na.matches(\".album_right_control *\")?\"Image Control\":a.matches(\".headlines *\")?(a=jQuery(a).closest(\".headlines\").find(\"a\").text().split(\"\/\",1).toString())?a:void 0:a.matches(\".three-stories *\")?(a=jQuery(a).closest(\".three-stories\").find(\"h2\").text().split(\"\/\",1).toString())?a:void 0:\"View the Archives\"==",["escape",["macro",2],8,16],"?\"View the Archives\":a.matches(\".nw_container *\")?(a=jQuery(a).closest(\".nw_scont\").prevAll(\"div.nw_image:first\").children(\"img\").attr(\"alt\"))?a:void 0:a.matches(\"a.footer-button *\")?\n\"Follow Us - \":a.matches(\".footer *\")?(a=jQuery(a).prevAll(\"span\").text())?a:void 0:a.matches(\"ul.top-nav *\")||a.children[0].matches(\"img.main_logo\")?\"Top Navigation Bar\":a.matches(\"div.most_pop_b *\")?\"Most Popular\":a.matches(\".content-preview *\")?(a=\"view all \\u00bb\"==",["escape",["macro",2],8,16],"?jQuery(a).closest(\".content-preview\").find(\"h3\").text().split(\"\/\",1).toString():jQuery(a).prevAll(\"h3\").text().split(\"\/\",1).toString())?a:void 0:\"\"})();"]
    },{
      "function":"__jsm",
      "vtp_javascript":["template","(function(){var a=",["escape",["macro",0],8,16],";return a.matches(\".headlines *\")?\"Headlines - \":a.matches(\".nw_container *\")?\"Network Contents - \":a.matches(\".footer *\")?\"Footer - \":a.matches(\"#side_nav *\")?\"Right Rail - \":\"\"})();"]
    },{
      "function":"__jsm",
      "vtp_javascript":["template","(function(){var a=",["escape",["macro",0],8,16],";if(a.matches(\"span.bar *\"))return\"Footer - Social\";if(a.matches(\".subscribe_cont *\"))return\"Right Rail - Social\";if(a.matches(\"div.social *\"))return\"Top Navigation Bar - Social\"})();"]
    },{
      "function":"__jsm",
      "vtp_javascript":["template","(function(){var a=",["escape",["macro",0],8,16],";if(a.matches(\"span.bar *\")||a.matches(\".subscribe_cont *\")||a.matches(\"div.social *\"))return(a=jQuery(a).find(\"img\").attr(\"title\"))?a:void 0})();"]
    },{
      "function":"__jsm",
      "vtp_javascript":["template","(function(){var a=document.getElementsByClassName(\"gtmStickyShare\");return a[0]?!0:!1})();"]
    },{
      "function":"__jsm",
      "vtp_javascript":["template","(function(){return document.title})();"]
    },{
      "function":"__jsm",
      "vtp_javascript":["template","(function(){var a=",["escape",["macro",1],8,16],";return 1\u003Ea.length?\"undefined\":a})();"]
    },{
      "function":"__u",
      "vtp_component":"QUERY",
      "vtp_queryKey":"cmpid"
    },{
      "function":"__jsm",
      "vtp_javascript":["template","(function(){var a=",["escape",["macro",11],8,16],",b=a.split(\"_\");return 1\u003Cb.length?b[3]:a})();"]
    },{
      "function":"__jsm",
      "vtp_javascript":["template","(function(){var a=",["escape",["macro",11],8,16],",b=a.split(\"_\");return 1\u003Cb.length?b[1]:a})();"]
    },{
      "function":"__jsm",
      "vtp_javascript":["template","(function(){var a=",["escape",["macro",11],8,16],",b=a.split(\"_\");return 1\u003Cb.length?b[2]:a})();"]
    },{
      "function":"__jsm",
      "vtp_javascript":["template","(function(){var a=",["escape",["macro",11],8,16],",b=a.split(\"_\");return 1\u003Cb.length?b[0]:a})();"]
    },{
      "function":"__jsm",
      "vtp_javascript":["template","(function(){var a=document.getElementsByClassName(\"article-content\");return a[0]?!0:!1})();"]
    },{
      "function":"__jsm",
      "vtp_javascript":["template","(function(){var a=document.getElementById(\"rev-content-image-album\");return a[0]?!0:!1})();"]
    },{
      "function":"__v",
      "vtp_name":"gtm.elementUrl",
      "vtp_dataLayerVersion":1
    },{
      "function":"__jsm",
      "vtp_javascript":["template","(function(){var el=",["escape",["macro",0],8,16],";var url=",["escape",["macro",18],8,16],";var start=url.lastIndexOf(\"\/\")+1;var end=url.indexOf(\".jpg\");return url.substring(start,end)})();"]
    },{
      "function":"__v",
      "vtp_dataLayerVersion":2,
      "vtp_setDefaultValue":false,
      "vtp_name":"truncated"
    },{
      "function":"__v",
      "vtp_dataLayerVersion":2,
      "vtp_setDefaultValue":false,
      "vtp_name":"button"
    },{
      "function":"__jsm",
      "vtp_javascript":["template","(function(){var a=",["escape",["macro",20],8,16],",b=",["escape",["macro",21],8,16],";return\"viewable\"==b?\"viewable\":\"true\"==a?\"BTF\":\"undefined\"})();"]
    },{
      "function":"__jsm",
      "vtp_javascript":["template","(function(){var a=document.querySelector(\"div.byline\").innerText;return a.split(\"|\")[0]})();"]
    },{
      "function":"__jsm",
      "vtp_javascript":["template","(function(){var a=document.querySelector(\"div.byline\").innerText;return a.split(\"|\")[1]})();"]
    },{
      "function":"__v",
      "vtp_dataLayerVersion":2,
      "vtp_setDefaultValue":false,
      "vtp_name":"ramp-id"
    },{
      "function":"__jsm",
      "vtp_javascript":["template","(function(){var a=",["escape",["macro",25],8,16],";return\"undefined\"===typeof a||null===a?\"undefined\":a})();"]
    },{
      "function":"__u",
      "vtp_component":"QUERY",
      "vtp_queryKey":"utm_source"
    },{
      "function":"__u",
      "vtp_component":"QUERY",
      "vtp_queryKey":"utm_medium"
    },{
      "function":"__jsm",
      "vtp_javascript":["template","(function(){var b=",["escape",["macro",27],8,16],",a=",["escape",["macro",28],8,16],";return\"ppc\"===a\u0026\u0026\"revcontent\"===b?!0:\"ppc\"===a?!1:!0})();"]
    },{
      "function":"__e"
    },{
      "function":"__v",
      "vtp_dataLayerVersion":2,
      "vtp_setDefaultValue":false,
      "vtp_name":"PurchOmnitureVars.prop4"
    },{
      "function":"__v",
      "vtp_dataLayerVersion":2,
      "vtp_setDefaultValue":false,
      "vtp_name":"PurchOmnitureVars.prop5"
    },{
      "function":"__v",
      "vtp_dataLayerVersion":2,
      "vtp_setDefaultValue":false,
      "vtp_name":"PurchOmnitureVars.channel"
    },{
      "function":"__v",
      "vtp_dataLayerVersion":2,
      "vtp_setDefaultValue":false,
      "vtp_name":"PurchOmnitureVars.pageName"
    },{
      "function":"__v",
      "vtp_dataLayerVersion":2,
      "vtp_setDefaultValue":false,
      "vtp_name":"PurchOmnitureVars.prop2"
    },{
      "function":"__v",
      "vtp_dataLayerVersion":2,
      "vtp_setDefaultValue":false,
      "vtp_name":"PurchOmnitureVars.prop6"
    },{
      "function":"__v",
      "vtp_dataLayerVersion":2,
      "vtp_setDefaultValue":false,
      "vtp_name":"datas.fingerprint"
    },{
      "function":"__v",
      "vtp_dataLayerVersion":2,
      "vtp_setDefaultValue":false,
      "vtp_name":"datas.gus_id"
    },{
      "function":"__c",
      "vtp_value":"UA-72111741-7"
    },{
      "function":"__gas",
      "vtp_cookieDomain":"auto",
      "vtp_doubleClick":true,
      "vtp_setTrackerName":false,
      "vtp_useDebugVersion":false,
      "vtp_useHashAutoLink":false,
      "vtp_decorateFormsAutoLink":false,
      "vtp_enableLinkId":true,
      "vtp_dimension":["list",["map","index","1","dimension",["macro",31]],["map","index","2","dimension",["macro",32]],["map","index","4","dimension",["macro",33]],["map","index","5","dimension",["macro",34]],["map","index","6","dimension",["macro",35]],["map","index","7","dimension",["macro",36]],["map","index","9","dimension",["macro",23]],["map","index","10","dimension",["macro",24]],["map","index","40","dimension",["macro",37]],["map","index","41","dimension",["macro",38]]],
      "vtp_enableEcommerce":false,
      "vtp_trackingId":["macro",39],
      "vtp_enableUaRlsa":false,
      "vtp_enableUseInternalVersion":false
    },{
      "function":"__v",
      "vtp_name":"gtm.triggers",
      "vtp_dataLayerVersion":2,
      "vtp_setDefaultValue":true,
      "vtp_defaultValue":""
    },{
      "function":"__c",
      "vtp_value":"auto"
    },{
      "function":"__c",
      "vtp_value":"anandtech.com, businessnewsdaily.com, laptopmag.com, newsarama.com, space.com, tomsguide.fr, tomsguide.com, tomshardware.de, tomshardware.fr, tomshardware.co.uk, tomshardware.com, tomsitpro.com, tomshw.it, activejunky.com, shopsavvy.com, buyerzone.com, toptenreviews.com, business.com"
    },{
      "function":"__gas",
      "vtp_cookieDomain":"auto",
      "vtp_doubleClick":true,
      "vtp_setTrackerName":false,
      "vtp_useDebugVersion":false,
      "vtp_fieldsToSet":["list",["map","fieldName","allowLinker","value","true"],["map","fieldName","cookieDomain","value",["macro",42]]],
      "vtp_useHashAutoLink":false,
      "vtp_autoLinkDomains":["macro",43],
      "vtp_decorateFormsAutoLink":true,
      "vtp_enableLinkId":true,
      "vtp_dimension":["list",["map","index","1","dimension",["macro",31]],["map","index","2","dimension",["macro",32]],["map","index","4","dimension",["macro",33]],["map","index","5","dimension",["macro",34]],["map","index","6","dimension",["macro",35]],["map","index","7","dimension",["macro",36]],["map","index","9","dimension",["macro",23]],["map","index","10","dimension",["macro",24]],["map","index","40","dimension",["macro",37]],["map","index","41","dimension",["macro",38]]],
      "vtp_enableEcommerce":false,
      "vtp_trackingId":["macro",39],
      "vtp_enableUaRlsa":false,
      "vtp_enableUseInternalVersion":false
    },{
      "function":"__v",
      "vtp_dataLayerVersion":2,
      "vtp_setDefaultValue":false,
      "vtp_name":"event category"
    },{
      "function":"__v",
      "vtp_dataLayerVersion":2,
      "vtp_setDefaultValue":false,
      "vtp_name":"event action"
    },{
      "function":"__v",
      "vtp_dataLayerVersion":2,
      "vtp_setDefaultValue":false,
      "vtp_name":"event label"
    },{
      "function":"__e"
    },{
      "function":"__v",
      "vtp_dataLayerVersion":2,
      "vtp_setDefaultValue":false,
      "vtp_name":"action"
    },{
      "function":"__v",
      "vtp_dataLayerVersion":2,
      "vtp_setDefaultValue":false,
      "vtp_name":"location"
    },{
      "function":"__u",
      "vtp_component":"URL"
    },{
      "function":"__v",
      "vtp_name":"gtm.elementId",
      "vtp_dataLayerVersion":1
    },{
      "function":"__smm",
      "vtp_setDefaultValue":false,
      "vtp_input":["macro",1],
      "vtp_map":["list",["map","key","st_facebook_custom fa fa-facebook","value","Facebook"],["map","key","st_twitter_custom fa fa-twitter","value","Twitter"],["map","key","st_stumbleupon_custom fa fa-stumbleupon","value","StumbleUpon"],["map","key","st_googleplus_custom fa fa-google-plus","value","Google+"],["map","key","st_reddit_custom fa fa-reddit","value","Reddit"]]
    },{
      "function":"__smm",
      "vtp_setDefaultValue":false,
      "vtp_input":["macro",52],
      "vtp_map":["list",["map","key","fbCount","value","Facebook"],["map","key","twtrCount","value","Twitter"],["map","key","strumbleCount","value","StumbleUpon"],["map","key","gPlusCount","value","Google+"],["map","key","redditCount","value","Reddit"]]
    },{
      "function":"__u",
      "vtp_component":"URL"
    },{
      "function":"__v",
      "vtp_dataLayerVersion":2,
      "vtp_setDefaultValue":false,
      "vtp_name":"ConversionCategory"
    },{
      "function":"__v",
      "vtp_dataLayerVersion":2,
      "vtp_setDefaultValue":false,
      "vtp_name":"ConversionAction"
    },{
      "function":"__v",
      "vtp_dataLayerVersion":2,
      "vtp_setDefaultValue":false,
      "vtp_name":"ConversionLabel.href"
    },{
      "function":"__v",
      "vtp_dataLayerVersion":2,
      "vtp_setDefaultValue":false,
      "vtp_name":"action"
    },{
      "function":"__v",
      "vtp_dataLayerVersion":2,
      "vtp_setDefaultValue":false,
      "vtp_name":"content.url"
    },{
      "function":"__v",
      "vtp_dataLayerVersion":2,
      "vtp_name":"OptiMonk action"
    },{
      "function":"__smm",
      "vtp_setDefaultValue":true,
      "vtp_input":["macro",61],
      "vtp_defaultValue":"true",
      "vtp_map":["list",["map","key","filled","value","false"]]
    },{
      "function":"__v",
      "vtp_dataLayerVersion":2,
      "vtp_name":"OptiMonk category"
    },{
      "function":"__v",
      "vtp_dataLayerVersion":2,
      "vtp_name":"OptiMonk label"
    },{
      "function":"__v",
      "vtp_dataLayerVersion":2,
      "vtp_setDefaultValue":false,
      "vtp_name":"page"
    },{
      "function":"__v",
      "vtp_dataLayerVersion":2,
      "vtp_setDefaultValue":false,
      "vtp_name":"pages_total"
    },{
      "function":"__v",
      "vtp_dataLayerVersion":2,
      "vtp_setDefaultValue":false,
      "vtp_name":"action"
    },{
      "function":"__v",
      "vtp_dataLayerVersion":2,
      "vtp_setDefaultValue":false,
      "vtp_name":"button"
    },{
      "function":"__v",
      "vtp_dataLayerVersion":2,
      "vtp_setDefaultValue":false,
      "vtp_name":"deviceModel"
    },{
      "function":"__v",
      "vtp_dataLayerVersion":2,
      "vtp_setDefaultValue":false,
      "vtp_name":"deviceVersion"
    },{
      "function":"__v",
      "vtp_dataLayerVersion":2,
      "vtp_setDefaultValue":false,
      "vtp_name":"action"
    },{
      "function":"__v",
      "vtp_dataLayerVersion":2,
      "vtp_setDefaultValue":false,
      "vtp_name":"value"
    },{
      "function":"__v",
      "vtp_dataLayerVersion":2,
      "vtp_setDefaultValue":false,
      "vtp_name":"action"
    },{
      "function":"__v",
      "vtp_dataLayerVersion":2,
      "vtp_setDefaultValue":false,
      "vtp_name":"label"
    },{
      "function":"__u",
      "vtp_component":"PATH"
    },{
      "function":"__v",
      "vtp_dataLayerVersion":2,
      "vtp_setDefaultValue":false,
      "vtp_name":"section"
    },{
      "function":"__v",
      "vtp_dataLayerVersion":2,
      "vtp_setDefaultValue":false,
      "vtp_name":"link"
    },{
      "function":"__v",
      "vtp_dataLayerVersion":2,
      "vtp_setDefaultValue":false,
      "vtp_name":"action"
    },{
      "function":"__v",
      "vtp_dataLayerVersion":2,
      "vtp_setDefaultValue":false,
      "vtp_name":"option"
    },{
      "function":"__v",
      "vtp_dataLayerVersion":2,
      "vtp_setDefaultValue":false,
      "vtp_name":"action"
    },{
      "function":"__v",
      "vtp_dataLayerVersion":2,
      "vtp_setDefaultValue":false,
      "vtp_name":"action"
    },{
      "function":"__v",
      "vtp_dataLayerVersion":2,
      "vtp_setDefaultValue":false,
      "vtp_name":"purch.highImpactAdImp.datas.eventAction"
    },{
      "function":"__v",
      "vtp_dataLayerVersion":2,
      "vtp_setDefaultValue":false,
      "vtp_name":"purch.highImpactAdImp.datas.eventLabel"
    },{
      "function":"__v",
      "vtp_dataLayerVersion":2,
      "vtp_setDefaultValue":false,
      "vtp_name":"action"
    },{
      "function":"__v",
      "vtp_dataLayerVersion":2,
      "vtp_setDefaultValue":false,
      "vtp_name":"label"
    },{
      "function":"__v",
      "vtp_dataLayerVersion":2,
      "vtp_setDefaultValue":false,
      "vtp_name":"action"
    },{
      "function":"__v",
      "vtp_dataLayerVersion":2,
      "vtp_setDefaultValue":false,
      "vtp_name":"button"
    },{
      "function":"__v",
      "vtp_dataLayerVersion":2,
      "vtp_setDefaultValue":false,
      "vtp_name":"purch.shopclick.datas.evars.price"
    },{
      "function":"__v",
      "vtp_dataLayerVersion":2,
      "vtp_setDefaultValue":false,
      "vtp_name":"purch.shopclick.datas.evars.merchant"
    },{
      "function":"__v",
      "vtp_dataLayerVersion":2,
      "vtp_setDefaultValue":false,
      "vtp_name":"purch.shopclick.datas.evars.position"
    },{
      "function":"__v",
      "vtp_dataLayerVersion":2,
      "vtp_setDefaultValue":false,
      "vtp_name":"purch.shopclick.datas.evars.product_id"
    },{
      "function":"__v",
      "vtp_dataLayerVersion":2,
      "vtp_setDefaultValue":false,
      "vtp_name":"purch.shopclick.datas.evars.transaction_id"
    },{
      "function":"__gas",
      "vtp_cookieDomain":"auto",
      "vtp_doubleClick":true,
      "vtp_setTrackerName":false,
      "vtp_useDebugVersion":false,
      "vtp_useHashAutoLink":false,
      "vtp_metric":["list",["map","index","1","metric","1"]],
      "vtp_decorateFormsAutoLink":false,
      "vtp_enableLinkId":true,
      "vtp_dimension":["list",["map","index","1","dimension",["macro",31]],["map","index","2","dimension",["macro",32]],["map","index","4","dimension",["macro",33]],["map","index","5","dimension",["macro",34]],["map","index","6","dimension",["macro",35]],["map","index","7","dimension",["macro",36]],["map","index","9","dimension",["macro",23]],["map","index","10","dimension",["macro",24]],["map","index","11","dimension",["macro",89]],["map","index","12","dimension",["macro",90]],["map","index","13","dimension",["macro",88]],["map","index","14","dimension",["macro",91]],["map","index","15","dimension",["macro",92]],["map","index","40","dimension",["macro",37]],["map","index","41","dimension",["macro",38]]],
      "vtp_enableEcommerce":false,
      "vtp_trackingId":["macro",39],
      "vtp_enableUaRlsa":false,
      "vtp_enableUseInternalVersion":false
    },{
      "function":"__v",
      "vtp_dataLayerVersion":2,
      "vtp_setDefaultValue":false,
      "vtp_name":"action"
    },{
      "function":"__v",
      "vtp_dataLayerVersion":2,
      "vtp_setDefaultValue":false,
      "vtp_name":"partner"
    },{
      "function":"__v",
      "vtp_dataLayerVersion":2,
      "vtp_setDefaultValue":false,
      "vtp_name":"action"
    },{
      "function":"__v",
      "vtp_dataLayerVersion":2,
      "vtp_setDefaultValue":false,
      "vtp_name":"action"
    },{
      "function":"__v",
      "vtp_dataLayerVersion":2,
      "vtp_setDefaultValue":false,
      "vtp_name":"label"
    },{
      "function":"__v",
      "vtp_dataLayerVersion":2,
      "vtp_setDefaultValue":false,
      "vtp_name":"categ"
    },{
      "function":"__v",
      "vtp_dataLayerVersion":2,
      "vtp_setDefaultValue":false,
      "vtp_name":"action"
    },{
      "function":"__v",
      "vtp_dataLayerVersion":2,
      "vtp_setDefaultValue":false,
      "vtp_name":"label"
    },{
      "function":"__v",
      "vtp_dataLayerVersion":2,
      "vtp_setDefaultValue":false,
      "vtp_name":"action"
    },{
      "function":"__v",
      "vtp_dataLayerVersion":2,
      "vtp_setDefaultValue":false,
      "vtp_name":"action"
    },{
      "function":"__v",
      "vtp_dataLayerVersion":2,
      "vtp_setDefaultValue":false,
      "vtp_name":"label"
    },{
      "function":"__v",
      "vtp_dataLayerVersion":2,
      "vtp_setDefaultValue":false,
      "vtp_name":"count"
    },{
      "function":"__v",
      "vtp_dataLayerVersion":2,
      "vtp_setDefaultValue":false,
      "vtp_name":"action"
    },{
      "function":"__v",
      "vtp_dataLayerVersion":2,
      "vtp_setDefaultValue":false,
      "vtp_name":"button"
    },{
      "function":"__v",
      "vtp_dataLayerVersion":2,
      "vtp_setDefaultValue":false,
      "vtp_name":"action"
    },{
      "function":"__aev",
      "vtp_setDefaultValue":false,
      "vtp_varType":"CLASSES"
    },{
      "function":"__c",
      "vtp_value":"25,50,75,90,100"
    },{
      "function":"__j",
      "vtp_name":"s.eVar37"
    },{
      "function":"__v",
      "vtp_dataLayerVersion":2,
      "vtp_setDefaultValue":false,
      "vtp_name":"PurchOmnitureVars.eVar12"
    },{
      "function":"__j",
      "vtp_name":"s.eVar11"
    },{
      "function":"__j",
      "vtp_name":"s.eVar36"
    },{
      "function":"__c",
      "vtp_value":"UA-75127772-1"
    },{
      "function":"__v",
      "vtp_dataLayerVersion":2,
      "vtp_setDefaultValue":false,
      "vtp_name":"eventAction"
    },{
      "function":"__v",
      "vtp_dataLayerVersion":2,
      "vtp_setDefaultValue":false,
      "vtp_name":"eventLabel"
    },{
      "function":"__v",
      "vtp_dataLayerVersion":2,
      "vtp_setDefaultValue":false,
      "vtp_name":"eventValue"
    },{
      "function":"__v",
      "vtp_dataLayerVersion":2,
      "vtp_setDefaultValue":false,
      "vtp_name":"eventCategory"
    },{
      "function":"__v",
      "vtp_dataLayerVersion":2,
      "vtp_setDefaultValue":false,
      "vtp_name":"PurchOmnitureVars"
    },{
      "function":"__u",
      "vtp_component":"QUERY",
      "vtp_queryKey":"type"
    },{
      "function":"__v",
      "vtp_dataLayerVersion":2,
      "vtp_setDefaultValue":false,
      "vtp_name":"InstantArticle"
    },{
      "function":"__smm",
      "vtp_setDefaultValue":true,
      "vtp_input":["macro",14],
      "vtp_defaultValue":["macro",14],
      "vtp_map":["list",["map","key","AB","value","Abandon"],["map","key","BH","value","Behavioral"],["map","key","CS","value","Cross Sell"],["map","key","p","value","Promotional"],["map","key","RN","value","Renewal"],["map","key","WL","value","Welcome"],["map","key","WB","value","Winback"]]
    },{
      "function":"__smm",
      "vtp_setDefaultValue":true,
      "vtp_input":["macro",13],
      "vtp_defaultValue":["macro",13],
      "vtp_map":["list",["map","key","AT","value","AnandTech"],["map","key","BND","value","BusinessNewsDaily"],["map","key","HS","value","Herman Street"],["map","key","LPT","value","LaptopMag"],["map","key","LS","value","LiveScience"],["map","key","NR","value","Newsarama"],["map","key","PME","value","Purch Marketplace Electronics"],["map","key","PMSH","value","Purch Marketplace Smart Home"],["map","key","PMS","value","Purch Marketplace Software"],["map","key","SP","value","Space.com"],["map","key","TGUS","value","TomsGuide.com"],["map","key","TGUFR","value","TomsGuide.fr"],["map","key","THUK","value","TomsHardware.co.uk"],["map","key","THUS","value","TomsHardware.com"],["map","key","THDE","value","TomsHardware.de"],["map","key","TIP","value","TomsITPro.com"],["map","key","TTR","value","TopTenReviews"]]
    },{
      "function":"__smm",
      "vtp_setDefaultValue":true,
      "vtp_input":["macro",15],
      "vtp_defaultValue":["macro",15],
      "vtp_map":["list",["map","key","NL","value","Newsletters"],["map","key","GPPC","value","Google PPC"],["map","key","MPPC","value","Bing PPC"]]
    },{
      "function":"__v",
      "vtp_dataLayerVersion":2,
      "vtp_setDefaultValue":false,
      "vtp_name":"event"
    },{
      "function":"__v",
      "vtp_dataLayerVersion":2,
      "vtp_setDefaultValue":false,
      "vtp_name":"button"
    },{
      "function":"__c",
      "vtp_value":"UA-72111741-27"
    },{
      "function":"__u",
      "vtp_component":"HOST"
    },{
      "function":"__f",
      "vtp_component":"URL"
    },{
      "function":"__v",
      "vtp_name":"gtm.elementTarget",
      "vtp_dataLayerVersion":1
    }],
  "tags":[{
      "function":"__ua",
      "priority":20,
      "once_per_event":true,
      "vtp_overrideGaSettings":false,
      "vtp_trackType":"TRACK_PAGEVIEW",
      "vtp_gaSettings":["macro",44],
      "vtp_enableUaRlsa":false,
      "vtp_enableUseInternalVersion":false,
      "vtp_enableFirebaseCampaignData":true,
      "tag_id":11
    },{
      "function":"__html",
      "priority":10,
      "once_per_event":true,
      "vtp_html":"\u003Cscript type=\"text\/gtmscript\"\u003EanalyticsEvent=function(b,a,c,d,e){dataLayer.push({event:\"analyticsEvent\",\"event category\":b,\"event action\":a,\"event label\":c,\"event value\":d,\"non-interaction\":e})};analyticsSocial=function(b,a,c,d){dataLayer.push({event:\"social\",\"social network\":b,\"social action\":a,\"social target\":c,\"social page path\":d})};analyticsVPV=function(b){dataLayer.push({event:\"analyticsVPV\",\"page name\":b})};analyticsClearVPV=function(){dataLayer.push({event:\"analyticsVPV\",\"page name\":void 0})};\nanalyticsForm=function(b,a){var c=b,d=a.name||a.id||a.type,e=\"interaction\";c=\"form: \"+c;d=d+\":\"+a.type;analyticsEvent(e,c,d)};\u003C\/script\u003E",
      "vtp_supportDocumentWrite":false,
      "vtp_enableIframeMode":false,
      "vtp_enableEditJsMacroBehavior":false,
      "tag_id":12
    },{
      "function":"__html",
      "priority":9,
      "once_per_event":true,
      "vtp_html":["template","\u003Cscript type=\"text\/gtmscript\"\u003Efunction trackScroll(a){function f(b,a,c){b.addEventListener?b.addEventListener(a,c):b.attachEvent\u0026\u0026b.attachEvent(\"on\"+a,c)}function e(){for(var b=0,e=a.length;b\u003Ce;b++){var c=document.documentElement;c=self.pageYOffset||c\u0026\u0026c.scrollTop||document.body.scrollTop;var d=document.documentElement;d=self.innerHeight||d\u0026\u0026d.clientHeight||document.body.clientHeight;c=(c+d)\/document.body.scrollHeight*100;d=a[b]+\"%\";0\u003Eg.indexOf(d)\u0026\u0026c\u003Ea[b]\u0026\u0026(g.push(d),analyticsEvent(\"Scroll\",d,h))}}var h=document.title,g=[];f(window,\n\"scroll\",e)}function stringToArray(a){a=a.split(\",\");for(var f=[],e=0;e\u003Ca.length;e++)f.push(a[e].trim().toLowerCase());return f}var inputValues=stringToArray(",["escape",["macro",110],8,16],");trackScroll(inputValues);\u003C\/script\u003E"],
      "vtp_supportDocumentWrite":false,
      "vtp_enableIframeMode":false,
      "vtp_enableEditJsMacroBehavior":false,
      "tag_id":6
    },{
      "function":"__ua",
      "priority":8,
      "once_per_event":true,
      "vtp_nonInteraction":false,
      "vtp_overrideGaSettings":false,
      "vtp_eventCategory":"Whitelist Modal",
      "vtp_trackType":"TRACK_EVENT",
      "vtp_gaSettings":["macro",40],
      "vtp_eventAction":["macro",103],
      "vtp_eventLabel":["macro",104],
      "vtp_enableUaRlsa":false,
      "vtp_enableUseInternalVersion":false,
      "vtp_enableFirebaseCampaignData":true,
      "vtp_trackTypeIsEvent":true,
      "tag_id":60
    },{
      "function":"__html",
      "priority":3,
      "once_per_event":true,
      "vtp_html":"\u003Cscript type=\"text\/gtmscript\"\u003E!function(){$\u0026\u0026(window.ga=window.ga||function(){(ga.q=ga.q||[]).push(arguments)},ga.l=+new Date,ga(\"create\",\"UA-72111741-7\",\"auto\",\"myTracker\"),$(\"body\").on(\"mousedown\",\".most-popular a\",function(a){a=$(this);a=a.data(\"mod\")?a.data(\"mod\"):\"LiftIgniter\";window.ga(\"myTracker.send\",\"event\",\"Click\",\"MostPopular\",a,{nonInteraction:1})}))}();\u003C\/script\u003E",
      "vtp_supportDocumentWrite":false,
      "vtp_enableIframeMode":false,
      "vtp_enableEditJsMacroBehavior":false,
      "tag_id":46
    },{
      "function":"__ua",
      "once_per_event":true,
      "vtp_nonInteraction":false,
      "vtp_overrideGaSettings":false,
      "vtp_eventCategory":"Click",
      "vtp_trackType":"TRACK_EVENT",
      "vtp_gaSettings":["macro",40],
      "vtp_eventAction":["template",["macro",5],["macro",4],["macro",10]],
      "vtp_eventLabel":["macro",18],
      "vtp_enableUaRlsa":false,
      "vtp_enableUseInternalVersion":false,
      "vtp_enableFirebaseCampaignData":true,
      "vtp_trackTypeIsEvent":true,
      "tag_id":7
    },{
      "function":"__ua",
      "once_per_event":true,
      "vtp_nonInteraction":false,
      "vtp_overrideGaSettings":false,
      "vtp_eventCategory":"Click",
      "vtp_trackType":"TRACK_EVENT",
      "vtp_gaSettings":["macro",40],
      "vtp_eventAction":["macro",10],
      "vtp_eventLabel":["macro",18],
      "vtp_enableUaRlsa":false,
      "vtp_enableUseInternalVersion":false,
      "vtp_enableFirebaseCampaignData":true,
      "vtp_trackTypeIsEvent":true,
      "tag_id":8
    },{
      "function":"__ua",
      "once_per_event":true,
      "vtp_nonInteraction":false,
      "vtp_overrideGaSettings":false,
      "vtp_eventCategory":"Click",
      "vtp_trackType":"TRACK_EVENT",
      "vtp_gaSettings":["macro",40],
      "vtp_eventAction":["macro",6],
      "vtp_eventLabel":["macro",7],
      "vtp_enableUaRlsa":false,
      "vtp_enableUseInternalVersion":false,
      "vtp_enableFirebaseCampaignData":true,
      "vtp_trackTypeIsEvent":true,
      "tag_id":10
    },{
      "function":"__ua",
      "once_per_event":true,
      "vtp_nonInteraction":false,
      "vtp_overrideGaSettings":false,
      "vtp_eventCategory":["macro",45],
      "vtp_trackType":"TRACK_EVENT",
      "vtp_gaSettings":["macro",40],
      "vtp_eventAction":["macro",46],
      "vtp_eventLabel":["macro",47],
      "vtp_enableUaRlsa":false,
      "vtp_enableUseInternalVersion":false,
      "vtp_enableFirebaseCampaignData":true,
      "vtp_trackTypeIsEvent":true,
      "tag_id":13
    },{
      "function":"__ua",
      "once_per_event":true,
      "vtp_nonInteraction":false,
      "vtp_overrideGaSettings":false,
      "vtp_eventCategory":["macro",48],
      "vtp_trackType":"TRACK_EVENT",
      "vtp_gaSettings":["macro",40],
      "vtp_eventAction":["macro",49],
      "vtp_eventLabel":["macro",50],
      "vtp_enableUaRlsa":false,
      "vtp_enableUseInternalVersion":false,
      "vtp_enableFirebaseCampaignData":true,
      "vtp_trackTypeIsEvent":true,
      "tag_id":17
    },{
      "function":"__ua",
      "once_per_event":true,
      "vtp_nonInteraction":false,
      "vtp_overrideGaSettings":false,
      "vtp_eventCategory":"Conversion",
      "vtp_trackType":"TRACK_EVENT",
      "vtp_gaSettings":["macro",40],
      "vtp_eventAction":"Store Link Click",
      "vtp_eventLabel":["macro",51],
      "vtp_enableUaRlsa":false,
      "vtp_enableUseInternalVersion":false,
      "vtp_enableFirebaseCampaignData":true,
      "vtp_trackTypeIsEvent":true,
      "tag_id":18
    },{
      "function":"__ua",
      "once_per_event":true,
      "vtp_overrideGaSettings":false,
      "vtp_trackType":"TRACK_SOCIAL",
      "vtp_socialAction":"share",
      "vtp_gaSettings":["macro",40],
      "vtp_socialActionTarget":["macro",51],
      "vtp_socialNetwork":["template",["macro",53],["macro",54]],
      "vtp_enableUaRlsa":false,
      "vtp_enableUseInternalVersion":false,
      "vtp_enableFirebaseCampaignData":true,
      "vtp_trackTypeIsSocial":true,
      "tag_id":20
    },{
      "function":"__ua",
      "once_per_event":true,
      "vtp_nonInteraction":true,
      "vtp_doubleClick":true,
      "vtp_setTrackerName":false,
      "vtp_useDebugVersion":false,
      "vtp_eventCategory":"Error 404",
      "vtp_trackType":"TRACK_EVENT",
      "vtp_enableLinkId":false,
      "vtp_eventAction":["macro",55],
      "vtp_enableEcommerce":false,
      "vtp_trackingId":["macro",39],
      "vtp_enableUaRlsa":false,
      "vtp_enableUseInternalVersion":false,
      "vtp_enableFirebaseCampaignData":true,
      "vtp_trackTypeIsEvent":true,
      "tag_id":21
    },{
      "function":"__ua",
      "once_per_event":true,
      "vtp_nonInteraction":false,
      "vtp_overrideGaSettings":false,
      "vtp_eventCategory":["macro",56],
      "vtp_trackType":"TRACK_EVENT",
      "vtp_gaSettings":["macro",40],
      "vtp_eventAction":["macro",57],
      "vtp_eventLabel":["macro",58],
      "vtp_enableUaRlsa":false,
      "vtp_enableUseInternalVersion":false,
      "vtp_enableFirebaseCampaignData":true,
      "vtp_trackTypeIsEvent":true,
      "tag_id":23
    },{
      "function":"__ua",
      "once_per_event":true,
      "vtp_nonInteraction":false,
      "vtp_overrideGaSettings":false,
      "vtp_eventCategory":"Conversion",
      "vtp_trackType":"TRACK_EVENT",
      "vtp_gaSettings":["macro",40],
      "vtp_eventAction":"RSS",
      "vtp_eventLabel":["macro",51],
      "vtp_enableUaRlsa":false,
      "vtp_enableUseInternalVersion":false,
      "vtp_enableFirebaseCampaignData":true,
      "vtp_trackTypeIsEvent":true,
      "tag_id":24
    },{
      "function":"__ua",
      "once_per_event":true,
      "vtp_nonInteraction":false,
      "vtp_overrideGaSettings":false,
      "vtp_eventCategory":"Conversion",
      "vtp_trackType":"TRACK_EVENT",
      "vtp_gaSettings":["macro",40],
      "vtp_eventAction":"Youtube",
      "vtp_eventLabel":["macro",51],
      "vtp_enableUaRlsa":false,
      "vtp_enableUseInternalVersion":false,
      "vtp_enableFirebaseCampaignData":true,
      "vtp_trackTypeIsEvent":true,
      "tag_id":25
    },{
      "function":"__ua",
      "once_per_event":true,
      "vtp_overrideGaSettings":true,
      "vtp_trackType":"TRACK_PAGEVIEW",
      "vtp_gaSettings":["macro",40],
      "vtp_trackingId":"UA-72111741-27",
      "vtp_enableUaRlsa":false,
      "vtp_enableUseInternalVersion":false,
      "vtp_enableFirebaseCampaignData":true,
      "tag_id":26
    },{
      "function":"__ua",
      "once_per_event":true,
      "vtp_nonInteraction":false,
      "vtp_overrideGaSettings":false,
      "vtp_eventCategory":"Click",
      "vtp_trackType":"TRACK_EVENT",
      "vtp_gaSettings":["macro",40],
      "vtp_eventAction":"Partner Banner",
      "vtp_eventLabel":["macro",19],
      "vtp_enableUaRlsa":false,
      "vtp_enableUseInternalVersion":false,
      "vtp_enableFirebaseCampaignData":true,
      "vtp_trackTypeIsEvent":true,
      "tag_id":27
    },{
      "function":"__ua",
      "once_per_event":true,
      "vtp_nonInteraction":false,
      "vtp_overrideGaSettings":false,
      "vtp_eventCategory":"OS - Notification",
      "vtp_trackType":"TRACK_EVENT",
      "vtp_gaSettings":["macro",40],
      "vtp_eventAction":["macro",59],
      "vtp_eventLabel":["macro",60],
      "vtp_enableUaRlsa":false,
      "vtp_enableUseInternalVersion":false,
      "vtp_enableFirebaseCampaignData":true,
      "vtp_trackTypeIsEvent":true,
      "tag_id":28
    },{
      "function":"__ua",
      "vtp_nonInteraction":["macro",62],
      "vtp_overrideGaSettings":false,
      "vtp_eventCategory":["macro",63],
      "vtp_trackType":"TRACK_EVENT",
      "vtp_gaSettings":["macro",40],
      "vtp_eventAction":["macro",61],
      "vtp_eventLabel":["macro",64],
      "vtp_enableUaRlsa":false,
      "vtp_enableUseInternalVersion":false,
      "vtp_enableFirebaseCampaignData":true,
      "vtp_trackTypeIsEvent":true,
      "tag_id":31
    },{
      "function":"__ua",
      "once_per_event":true,
      "vtp_nonInteraction":false,
      "vtp_overrideGaSettings":false,
      "vtp_eventCategory":"Countdown",
      "vtp_trackType":"TRACK_EVENT",
      "vtp_gaSettings":["macro",40],
      "vtp_eventAction":["macro",9],
      "vtp_eventLabel":["template",["macro",65]," of ",["macro",66]],
      "vtp_enableUaRlsa":false,
      "vtp_enableUseInternalVersion":false,
      "vtp_enableFirebaseCampaignData":true,
      "vtp_trackTypeIsEvent":true,
      "tag_id":32
    },{
      "function":"__ua",
      "once_per_event":true,
      "vtp_nonInteraction":false,
      "vtp_overrideGaSettings":false,
      "vtp_eventCategory":"HTTP - Notification",
      "vtp_trackType":"TRACK_EVENT",
      "vtp_gaSettings":["macro",40],
      "vtp_eventAction":["template","permission - ",["macro",67]," - ",["macro",68]],
      "vtp_eventLabel":["template",["macro",69]," ",["macro",70]],
      "vtp_enableUaRlsa":false,
      "vtp_enableUseInternalVersion":false,
      "vtp_enableFirebaseCampaignData":true,
      "vtp_trackTypeIsEvent":true,
      "tag_id":35
    },{
      "function":"__ua",
      "once_per_event":true,
      "vtp_nonInteraction":false,
      "vtp_overrideGaSettings":false,
      "vtp_eventCategory":"HTTP - Notification",
      "vtp_trackType":"TRACK_EVENT",
      "vtp_gaSettings":["macro",40],
      "vtp_eventAction":"registration",
      "vtp_eventLabel":["macro",71],
      "vtp_enableUaRlsa":false,
      "vtp_enableUseInternalVersion":false,
      "vtp_enableFirebaseCampaignData":true,
      "vtp_trackTypeIsEvent":true,
      "tag_id":36
    },{
      "function":"__ua",
      "once_per_event":true,
      "vtp_nonInteraction":false,
      "vtp_overrideGaSettings":false,
      "vtp_eventValue":["macro",72],
      "vtp_eventCategory":"HTTP - Notification",
      "vtp_trackType":"TRACK_EVENT",
      "vtp_gaSettings":["macro",40],
      "vtp_eventAction":["macro",73],
      "vtp_eventLabel":["macro",74],
      "vtp_enableUaRlsa":false,
      "vtp_enableUseInternalVersion":false,
      "vtp_enableFirebaseCampaignData":true,
      "vtp_trackTypeIsEvent":true,
      "tag_id":37
    },{
      "function":"__ua",
      "once_per_event":true,
      "vtp_nonInteraction":true,
      "vtp_overrideGaSettings":false,
      "vtp_eventCategory":"Sticky Sharethis",
      "vtp_trackType":"TRACK_EVENT",
      "vtp_gaSettings":["macro",40],
      "vtp_eventAction":["macro",55],
      "vtp_enableUaRlsa":false,
      "vtp_enableUseInternalVersion":false,
      "vtp_enableFirebaseCampaignData":true,
      "vtp_trackTypeIsEvent":true,
      "tag_id":38
    },{
      "function":"__ua",
      "once_per_event":true,
      "vtp_nonInteraction":false,
      "vtp_overrideGaSettings":false,
      "vtp_eventCategory":"Click",
      "vtp_trackType":"TRACK_EVENT",
      "vtp_gaSettings":["macro",40],
      "vtp_eventAction":"Slideshow",
      "vtp_eventLabel":["macro",75],
      "vtp_enableUaRlsa":false,
      "vtp_enableUseInternalVersion":false,
      "vtp_enableFirebaseCampaignData":true,
      "vtp_trackTypeIsEvent":true,
      "tag_id":39
    },{
      "function":"__ua",
      "once_per_event":true,
      "vtp_nonInteraction":false,
      "vtp_overrideGaSettings":false,
      "vtp_eventCategory":"Section Pages",
      "vtp_trackType":"TRACK_EVENT",
      "vtp_gaSettings":["macro",40],
      "vtp_eventAction":["macro",76],
      "vtp_eventLabel":["macro",77],
      "vtp_enableUaRlsa":false,
      "vtp_enableUseInternalVersion":false,
      "vtp_enableFirebaseCampaignData":true,
      "vtp_trackTypeIsEvent":true,
      "tag_id":40
    },{
      "function":"__ua",
      "once_per_event":true,
      "vtp_nonInteraction":false,
      "vtp_overrideGaSettings":false,
      "vtp_eventCategory":"Read More",
      "vtp_trackType":"TRACK_EVENT",
      "vtp_gaSettings":["macro",40],
      "vtp_eventAction":["macro",78],
      "vtp_eventLabel":["macro",22],
      "vtp_enableUaRlsa":false,
      "vtp_enableUseInternalVersion":false,
      "vtp_enableFirebaseCampaignData":true,
      "vtp_trackTypeIsEvent":true,
      "tag_id":41
    },{
      "function":"__ua",
      "once_per_event":true,
      "vtp_nonInteraction":false,
      "vtp_overrideGaSettings":false,
      "vtp_eventCategory":"Section Pages",
      "vtp_trackType":"TRACK_EVENT",
      "vtp_gaSettings":["macro",40],
      "vtp_eventAction":"Slideshow Options",
      "vtp_eventLabel":["macro",79],
      "vtp_enableUaRlsa":false,
      "vtp_enableUseInternalVersion":false,
      "vtp_enableFirebaseCampaignData":true,
      "vtp_trackTypeIsEvent":true,
      "tag_id":42
    },{
      "function":"__ua",
      "once_per_event":true,
      "vtp_nonInteraction":false,
      "vtp_overrideGaSettings":false,
      "vtp_eventCategory":"Facebook Messenger",
      "vtp_trackType":"TRACK_EVENT",
      "vtp_gaSettings":["macro",40],
      "vtp_eventAction":["macro",80],
      "vtp_enableUaRlsa":false,
      "vtp_enableUseInternalVersion":false,
      "vtp_enableFirebaseCampaignData":true,
      "vtp_trackTypeIsEvent":true,
      "tag_id":43
    },{
      "function":"__ua",
      "once_per_event":true,
      "vtp_nonInteraction":true,
      "vtp_overrideGaSettings":false,
      "vtp_eventCategory":["template","RAAS",["macro",81]],
      "vtp_trackType":"TRACK_EVENT",
      "vtp_gaSettings":["macro",40],
      "vtp_eventAction":"CID-5061",
      "vtp_eventLabel":"EC_pop-up_MostInteresting",
      "vtp_enableUaRlsa":false,
      "vtp_enableUseInternalVersion":false,
      "vtp_enableFirebaseCampaignData":true,
      "vtp_trackTypeIsEvent":true,
      "tag_id":45
    },{
      "function":"__ua",
      "once_per_event":true,
      "vtp_nonInteraction":true,
      "vtp_overrideGaSettings":false,
      "vtp_eventCategory":"High Impact Ad Impression",
      "vtp_trackType":"TRACK_EVENT",
      "vtp_gaSettings":["macro",40],
      "vtp_eventAction":["macro",82],
      "vtp_eventLabel":["macro",83],
      "vtp_enableUaRlsa":false,
      "vtp_enableUseInternalVersion":false,
      "vtp_enableFirebaseCampaignData":true,
      "vtp_trackTypeIsEvent":true,
      "tag_id":47
    },{
      "function":"__ua",
      "once_per_event":true,
      "vtp_nonInteraction":true,
      "vtp_overrideGaSettings":false,
      "vtp_eventCategory":"Whitelist",
      "vtp_trackType":"TRACK_EVENT",
      "vtp_gaSettings":["macro",40],
      "vtp_eventAction":["macro",84],
      "vtp_eventLabel":["macro",85],
      "vtp_enableUaRlsa":false,
      "vtp_enableUseInternalVersion":false,
      "vtp_enableFirebaseCampaignData":true,
      "vtp_trackTypeIsEvent":true,
      "tag_id":48
    },{
      "function":"__ua",
      "once_per_event":true,
      "vtp_nonInteraction":false,
      "vtp_overrideGaSettings":false,
      "vtp_eventCategory":"Countdown Mobile",
      "vtp_trackType":"TRACK_EVENT",
      "vtp_gaSettings":["macro",40],
      "vtp_eventAction":["template",["macro",86]," - ",["macro",87]],
      "vtp_eventLabel":["macro",75],
      "vtp_enableUaRlsa":false,
      "vtp_enableUseInternalVersion":false,
      "vtp_enableFirebaseCampaignData":true,
      "vtp_trackTypeIsEvent":true,
      "tag_id":49
    },{
      "function":"__ua",
      "once_per_event":true,
      "vtp_nonInteraction":false,
      "vtp_overrideGaSettings":false,
      "vtp_eventValue":["macro",88],
      "vtp_eventCategory":"Buy Button Click",
      "vtp_trackType":"TRACK_EVENT",
      "vtp_gaSettings":["macro",93],
      "vtp_eventAction":["macro",90],
      "vtp_eventLabel":["macro",91],
      "vtp_enableUaRlsa":false,
      "vtp_enableUseInternalVersion":false,
      "vtp_enableFirebaseCampaignData":true,
      "vtp_trackTypeIsEvent":true,
      "tag_id":50
    },{
      "function":"__ua",
      "once_per_event":true,
      "vtp_nonInteraction":false,
      "vtp_overrideGaSettings":false,
      "vtp_eventCategory":"Article Quiz",
      "vtp_trackType":"TRACK_EVENT",
      "vtp_gaSettings":["macro",40],
      "vtp_eventAction":["macro",86],
      "vtp_eventLabel":["macro",51],
      "vtp_enableUaRlsa":false,
      "vtp_enableUseInternalVersion":false,
      "vtp_enableFirebaseCampaignData":true,
      "vtp_trackTypeIsEvent":true,
      "tag_id":51
    },{
      "function":"__ua",
      "once_per_event":true,
      "vtp_nonInteraction":false,
      "vtp_overrideGaSettings":false,
      "vtp_eventCategory":"Email Popup",
      "vtp_trackType":"TRACK_EVENT",
      "vtp_gaSettings":["macro",40],
      "vtp_eventAction":["macro",94],
      "vtp_eventLabel":["macro",75],
      "vtp_enableUaRlsa":false,
      "vtp_enableUseInternalVersion":false,
      "vtp_enableFirebaseCampaignData":true,
      "vtp_trackTypeIsEvent":true,
      "tag_id":52
    },{
      "function":"__ua",
      "once_per_event":true,
      "vtp_nonInteraction":false,
      "vtp_overrideGaSettings":false,
      "vtp_eventCategory":"Sponsored Content",
      "vtp_trackType":"TRACK_EVENT",
      "vtp_gaSettings":["macro",40],
      "vtp_eventAction":["macro",95],
      "vtp_eventLabel":["macro",96],
      "vtp_enableUaRlsa":false,
      "vtp_enableUseInternalVersion":false,
      "vtp_enableFirebaseCampaignData":true,
      "vtp_trackTypeIsEvent":true,
      "tag_id":55
    },{
      "function":"__ua",
      "once_per_event":true,
      "vtp_nonInteraction":false,
      "vtp_overrideGaSettings":false,
      "vtp_eventCategory":"Conversion",
      "vtp_trackType":"TRACK_EVENT",
      "vtp_gaSettings":["macro",40],
      "vtp_eventAction":["macro",97],
      "vtp_eventLabel":["macro",98],
      "vtp_enableUaRlsa":false,
      "vtp_enableUseInternalVersion":false,
      "vtp_enableFirebaseCampaignData":true,
      "vtp_trackTypeIsEvent":true,
      "tag_id":56
    },{
      "function":"__ua",
      "once_per_event":true,
      "vtp_nonInteraction":false,
      "vtp_overrideGaSettings":false,
      "vtp_eventCategory":["macro",99],
      "vtp_trackType":"TRACK_EVENT",
      "vtp_gaSettings":["macro",40],
      "vtp_eventAction":["macro",100],
      "vtp_eventLabel":["macro",101],
      "vtp_enableUaRlsa":false,
      "vtp_enableUseInternalVersion":false,
      "vtp_enableFirebaseCampaignData":true,
      "vtp_trackTypeIsEvent":true,
      "tag_id":57
    },{
      "function":"__ua",
      "once_per_event":true,
      "vtp_nonInteraction":false,
      "vtp_overrideGaSettings":false,
      "vtp_eventCategory":"FB Widget",
      "vtp_trackType":"TRACK_EVENT",
      "vtp_gaSettings":["macro",40],
      "vtp_eventAction":["macro",102],
      "vtp_eventLabel":["macro",75],
      "vtp_enableUaRlsa":false,
      "vtp_enableUseInternalVersion":false,
      "vtp_enableFirebaseCampaignData":true,
      "vtp_trackTypeIsEvent":true,
      "tag_id":58
    },{
      "function":"__ua",
      "once_per_event":true,
      "vtp_overrideGaSettings":false,
      "vtp_trackType":"TRACK_SOCIAL",
      "vtp_socialAction":"Like",
      "vtp_gaSettings":["macro",40],
      "vtp_socialActionTarget":["macro",51],
      "vtp_socialNetwork":"Facebook",
      "vtp_enableUaRlsa":false,
      "vtp_enableUseInternalVersion":false,
      "vtp_enableFirebaseCampaignData":true,
      "vtp_trackTypeIsSocial":true,
      "tag_id":59
    },{
      "function":"__ua",
      "once_per_event":true,
      "vtp_nonInteraction":true,
      "vtp_overrideGaSettings":false,
      "vtp_eventCategory":"RAMPID",
      "vtp_trackType":"TRACK_EVENT",
      "vtp_gaSettings":["macro",40],
      "vtp_eventAction":["macro",26],
      "vtp_eventLabel":["macro",105],
      "vtp_enableUaRlsa":false,
      "vtp_enableUseInternalVersion":false,
      "vtp_enableFirebaseCampaignData":true,
      "vtp_trackTypeIsEvent":true,
      "tag_id":61
    },{
      "function":"__ua",
      "vtp_nonInteraction":false,
      "vtp_overrideGaSettings":false,
      "vtp_eventCategory":"Suggested Content",
      "vtp_trackType":"TRACK_EVENT",
      "vtp_gaSettings":["macro",40],
      "vtp_eventAction":["macro",106],
      "vtp_eventLabel":["macro",107],
      "vtp_enableUaRlsa":false,
      "vtp_enableUseInternalVersion":false,
      "vtp_enableFirebaseCampaignData":true,
      "vtp_trackTypeIsEvent":true,
      "tag_id":64
    },{
      "function":"__ua",
      "once_per_event":true,
      "vtp_nonInteraction":false,
      "vtp_overrideGaSettings":false,
      "vtp_eventCategory":"Latest_Mod_Click",
      "vtp_trackType":"TRACK_EVENT",
      "vtp_gaSettings":["macro",40],
      "vtp_eventAction":["macro",108],
      "vtp_enableUaRlsa":false,
      "vtp_enableUseInternalVersion":false,
      "vtp_enableFirebaseCampaignData":true,
      "vtp_trackTypeIsEvent":true,
      "tag_id":67
    },{
      "function":"__ua",
      "once_per_event":true,
      "vtp_nonInteraction":false,
      "vtp_overrideGaSettings":false,
      "vtp_eventCategory":"element_view",
      "vtp_trackType":"TRACK_EVENT",
      "vtp_gaSettings":["macro",40],
      "vtp_eventAction":["macro",109],
      "vtp_enableUaRlsa":false,
      "vtp_enableUseInternalVersion":false,
      "vtp_enableFirebaseCampaignData":true,
      "vtp_trackTypeIsEvent":true,
      "tag_id":68
    },{
      "function":"__cl",
      "tag_id":69
    },{
      "function":"__cl",
      "tag_id":70
    },{
      "function":"__lcl",
      "vtp_waitForTags":false,
      "vtp_checkValidation":true,
      "vtp_waitForTagsTimeout":"2000",
      "vtp_uniqueTriggerId":"1937581_9",
      "tag_id":71
    },{
      "function":"__lcl",
      "vtp_waitForTags":false,
      "vtp_checkValidation":true,
      "vtp_waitForTagsTimeout":"2000",
      "vtp_uniqueTriggerId":"1937581_10",
      "tag_id":72
    },{
      "function":"__cl",
      "tag_id":73
    },{
      "function":"__lcl",
      "vtp_waitForTags":false,
      "vtp_checkValidation":true,
      "vtp_waitForTagsTimeout":"2000",
      "vtp_uniqueTriggerId":"1937581_12",
      "tag_id":74
    },{
      "function":"__cl",
      "tag_id":75
    },{
      "function":"__cl",
      "tag_id":76
    },{
      "function":"__lcl",
      "vtp_waitForTags":false,
      "vtp_checkValidation":true,
      "vtp_waitForTagsTimeout":"2000",
      "vtp_uniqueTriggerId":"1937581_15",
      "tag_id":77
    },{
      "function":"__cl",
      "tag_id":78
    },{
      "function":"__lcl",
      "vtp_waitForTags":false,
      "vtp_checkValidation":true,
      "vtp_waitForTagsTimeout":"2000",
      "vtp_uniqueTriggerId":"1937581_17",
      "tag_id":79
    },{
      "function":"__cl",
      "tag_id":80
    },{
      "function":"__cl",
      "tag_id":81
    },{
      "function":"__cl",
      "tag_id":82
    },{
      "function":"__cl",
      "tag_id":83
    },{
      "function":"__cl",
      "tag_id":84
    },{
      "function":"__cl",
      "tag_id":85
    },{
      "function":"__cl",
      "tag_id":86
    },{
      "function":"__cl",
      "tag_id":87
    },{
      "function":"__cl",
      "tag_id":88
    },{
      "function":"__lcl",
      "vtp_checkValidation":true,
      "vtp_waitForTagsTimeout":"2000",
      "vtp_uniqueTriggerId":"1937581_42",
      "tag_id":89
    },{
      "function":"__evl",
      "vtp_useOnScreenDuration":false,
      "vtp_useDomChangeListener":false,
      "vtp_elementSelector":".more-editors",
      "vtp_firingFrequency":"ONCE",
      "vtp_selectorType":"CSS",
      "vtp_onScreenRatio":"75",
      "vtp_uniqueTriggerId":"1937581_192",
      "tag_id":90
    },{
      "function":"__lcl",
      "vtp_waitForTags":true,
      "vtp_checkValidation":true,
      "vtp_waitForTagsTimeout":"2000",
      "vtp_uniqueTriggerId":"1937581_195",
      "tag_id":91
    },{
      "function":"__evl",
      "vtp_useOnScreenDuration":false,
      "vtp_useDomChangeListener":false,
      "vtp_elementSelector":".purch-related",
      "vtp_firingFrequency":"ONCE",
      "vtp_selectorType":"CSS",
      "vtp_onScreenRatio":"75",
      "vtp_uniqueTriggerId":"1937581_196",
      "tag_id":92
    },{
      "function":"__evl",
      "vtp_useOnScreenDuration":false,
      "vtp_useDomChangeListener":false,
      "vtp_firingFrequency":"ONCE",
      "vtp_elementSelector":"#right-rail-newsletter",
      "vtp_selectorType":"CSS",
      "vtp_onScreenRatio":"25",
      "vtp_uniqueTriggerId":"1937581_222",
      "tag_id":93
    },{
      "function":"__html",
      "once_per_event":true,
      "vtp_html":"\u003Cscript type=\"text\/gtmscript\"\u003E0\u003Cdocument.querySelectorAll(\".ntShoppingWidget\").length\u0026\u0026function(){var a=document.createElement(\"script\");a.type=\"text\/javascript\";a.async=!0;a.src=\"https:\/\/natoms-serve.purch.com\/conf\/?key\\x3dbGl2ZXNjaWVuY2U\\x3d\";var b=document.getElementsByTagName(\"script\")[0];b.parentNode.insertBefore(a,b)}();\u003C\/script\u003E",
      "vtp_supportDocumentWrite":false,
      "vtp_enableIframeMode":false,
      "vtp_enableEditJsMacroBehavior":false,
      "tag_id":3
    },{
      "function":"__html",
      "once_per_event":true,
      "vtp_html":"\u003Cscript type=\"text\/gtmscript\"\u003Ewindow.stButtons?stButtons.locateElements():(function(){var a=document.createElement(\"script\");a.type=\"text\/javascript\";a.async=!0;a.onload=function(){try{stLight.options({publisher:\"44826d50-86b3-4690-b68d-df4ee65d1526\",doNotHash:!1,doNotCopy:!0,hashAddressBar:!1,nativeCount:!1,onhover:!1,publisherGA:\"UA-72111741-7\"})}catch(c){}var a=\"http:\/\/www.livescience.com\"+window.location.pathname;stButtons.getCount(a,\"facebook\",document.getElementById(\"fbCount\"));stButtons.getCount(a,\"twitter\",document.getElementById(\"twtrCount\"));\nstButtons.getCount(a,\"googleplus\",document.getElementById(\"gPlusCount\"));stButtons.getCount(a,\"stumbleupon\",document.getElementById(\"stumbleCount\"));stButtons.getCount(a,\"reddit\",document.getElementById(\"redditCount\"))};a.src=(\"https:\"==document.location.protocol?\"https:\/\/ws\":\"http:\/\/w\")+\".sharethis.com\/button\/buttons.js\";var b=document.getElementsByTagName(\"script\")[0];b.parentNode.insertBefore(a,b)}(),function(){var a=document.createElement(\"script\");a.type=\"text\/javascript\";a.async=!0;a.src=(\"https:\"==\ndocument.location.protocol?\"https:\/\/ss\":\"http:\/\/s\")+\".sharethis.com\/loader.js\";var b=document.getElementsByTagName(\"script\")[0];b.parentNode.insertBefore(a,b)}());\u003C\/script\u003E",
      "vtp_supportDocumentWrite":false,
      "vtp_enableIframeMode":false,
      "vtp_enableEditJsMacroBehavior":false,
      "tag_id":19
    },{
      "function":"__html",
      "once_per_event":true,
      "vtp_html":"\u003Cscript type=\"text\/gtmscript\"\u003E(function(){var a=function(){FB.Event.subscribe(\"comment.create\",function(a,b){var c=\"Article Comment\";dataLayer.push({event:\"conversion\",ConversionCategory:\"Conversion\",ConversionAction:c,ConversionLabel:a})})};if(\"undefined\"!==typeof FB)a();else if(window.fbAsyncInit){var b=window.fbAsyncInit;window.fbAsyncInit=function(){b();a()}}else window.fbAsyncInit=a})();\u003C\/script\u003E",
      "vtp_supportDocumentWrite":false,
      "vtp_enableIframeMode":false,
      "vtp_enableEditJsMacroBehavior":false,
      "tag_id":22
    },{
      "function":"__html",
      "once_per_event":true,
      "vtp_html":"\u003Cscript type=\"text\/gtmscript\"\u003Eif(\"www.livescience.com\"==document.location.hostname){var parselyDiv=document.createElement(\"div\");parselyDiv.id=\"parsely-root\";parselyDiv.style.display=\"none\";var spanElement=document.createElement(\"span\");spanElement.id=\"parsely-cfg\";spanElement.dataset.parselySite=\"livescience.com\";parselyDiv.appendChild(spanElement);document.body.appendChild(parselyDiv);var script=document.createElement(\"script\"),scriptContent=function(a,c,d){var e=d.location.protocol,f=c+\"-\"+a,b=d.getElementById(f),g=d.getElementById(c+\n\"-root\");c=\"https:\"===e?\"d1z2jf7jlzjs58.cloudfront.net\":\"static.\"+c+\".com\";b||(b=d.createElement(a),b.id=f,b.async=!0,b.src=e+\"\/\/\"+c+\"\/p.js\",g.appendChild(b))}(\"script\",\"parsely\",document);script.innerHtml=scriptContent;document.body.appendChild(script);PARSELY={onload:function(){console.log(\"Parse.ly code has loaded\");jQuery(\".carousel-countdown\")\u0026\u0026\"countdown\"==ctype\u0026\u0026jQuery(\"label.carousel-multi-css-arrow\").bind(\"click\",function(){console.log(ctype);if(jQuery(this).hasClass(\"pTracked\"))console.log(\"tracked\");\nelse{jQuery(this).addClass(\"pTracked\");var a=location.href;console.log(\"ptrack click\");console.log(\"url \"+a);console.log(\"urlref \\x3d \"+a);PARSELY.beacon.trackPageView({url:a,urlref:a,js:1})}return!0});jQuery(\".multi-wrapper\")\u0026\u0026\"image_album\"==ctype\u0026\u0026jQuery(\".next-button\").bind(\"click\",function(){console.log(ctype);var a=location.href;console.log(\"url \"+a);console.log(\"urlref \\x3d \"+a);PARSELY.beacon.trackPageView({url:a,urlref:a,js:1});return!0})}}};\u003C\/script\u003E",
      "vtp_supportDocumentWrite":false,
      "vtp_enableIframeMode":false,
      "vtp_enableEditJsMacroBehavior":false,
      "tag_id":33
    },{
      "function":"__html",
      "once_per_event":true,
      "vtp_html":"\n\u003Cscript type=\"text\/gtmscript\"\u003E!function(b,e,f,g,a,c,d){b.fbq||(a=b.fbq=function(){a.callMethod?a.callMethod.apply(a,arguments):a.queue.push(arguments)},b._fbq||(b._fbq=a),a.push=a,a.loaded=!0,a.version=\"2.0\",a.queue=[],c=e.createElement(f),c.async=!0,c.src=g,d=e.getElementsByTagName(f)[0],d.parentNode.insertBefore(c,d))}(window,document,\"script\",\"https:\/\/connect.facebook.net\/en_US\/fbevents.js\");fbq(\"init\",\"774971125895641\");fbq(\"track\",\"PageView\");\u003C\/script\u003E\n\u003Cnoscript\u003E\u003Cimg height=\"1\" width=\"1\" style=\"display:none\" src=\"https:\/\/www.facebook.com\/tr?id=774971125895641\u0026amp;ev=PageView\u0026amp;noscript=1\"\u003E\u003C\/noscript\u003E\n\n",
      "vtp_supportDocumentWrite":false,
      "vtp_enableIframeMode":false,
      "vtp_enableEditJsMacroBehavior":false,
      "tag_id":65
    }],
  "predicates":[{
      "function":"_css",
      "arg0":["macro",0],
      "arg1":".poll-wrapper *"
    },{
      "function":"_eq",
      "arg0":["macro",30],
      "arg1":"gtm.click"
    },{
      "function":"_css",
      "arg0":["macro",0],
      "arg1":".filter-list *"
    },{
      "function":"_css",
      "arg0":["macro",0],
      "arg1":"#sticky_social *"
    },{
      "function":"_css",
      "arg0":["macro",0],
      "arg1":".fig-btn *"
    },{
      "function":"_eq",
      "arg0":["macro",30],
      "arg1":"gtm.linkClick"
    },{
      "function":"_re",
      "arg0":["macro",41],
      "arg1":"(^$|((^|,)1937581_15($|,)))"
    },{
      "function":"_css",
      "arg0":["macro",0],
      "arg1":".bar *"
    },{
      "function":"_re",
      "arg0":["macro",41],
      "arg1":"(^$|((^|,)1937581_9($|,)))"
    },{
      "function":"_css",
      "arg0":["macro",0],
      "arg1":"div.subscribe_cont *"
    },{
      "function":"_re",
      "arg0":["macro",41],
      "arg1":"(^$|((^|,)1937581_10($|,)))"
    },{
      "function":"_css",
      "arg0":["macro",0],
      "arg1":"span.nav-pipe-right *"
    },{
      "function":"_css",
      "arg0":["macro",0],
      "arg1":"span.search-social *"
    },{
      "function":"_re",
      "arg0":["macro",41],
      "arg1":"(^$|((^|,)1937581_12($|,)))"
    },{
      "function":"_css",
      "arg0":["macro",0],
      "arg1":"div.social *"
    },{
      "function":"_re",
      "arg0":["macro",41],
      "arg1":"(^$|((^|,)1937581_17($|,)))"
    },{
      "function":"_css",
      "arg0":["macro",0],
      "arg1":"div.partner-banner *"
    },{
      "function":"_css",
      "arg0":["macro",0],
      "arg1":".partner-banner *"
    },{
      "function":"_re",
      "arg0":["macro",41],
      "arg1":"(^$|((^|,)1937581_42($|,)))"
    },{
      "function":"_eq",
      "arg0":["macro",30],
      "arg1":"gtm.dom"
    },{
      "function":"_eq",
      "arg0":["macro",30],
      "arg1":"refreshBeacons"
    },{
      "function":"_cn",
      "arg0":["macro",45],
      "arg1":"undefined"
    },{
      "function":"_cn",
      "arg0":["macro",46],
      "arg1":"undefined"
    },{
      "function":"_cn",
      "arg0":["macro",47],
      "arg1":"undefined"
    },{
      "function":"_eq",
      "arg0":["macro",30],
      "arg1":"analyticsEvent"
    },{
      "function":"_eq",
      "arg0":["macro",30],
      "arg1":"newsletter"
    },{
      "function":"_eq",
      "arg0":["macro",30],
      "arg1":"Newsletter: footer"
    },{
      "function":"_eq",
      "arg0":["macro",30],
      "arg1":"Newsletter: right-rail"
    },{
      "function":"_eq",
      "arg0":["macro",30],
      "arg1":"Newsletter: pop-up"
    },{
      "function":"_eq",
      "arg0":["macro",30],
      "arg1":"Newsletter: end of article"
    },{
      "function":"_re",
      "arg0":["macro",18],
      "arg1":".*store.livescience.com.*"
    },{
      "function":"_re",
      "arg0":["macro",1],
      "arg1":"st_facebook_custom fa fa-facebook|st_twitter_custom fa fa-twitter|st_googleplus_custom fa fa-google-plus|st_reddit_custom fa fa-reddit|st_stumbleupon_custom fa fa-stumbleupon",
      "ignore_case":true
    },{
      "function":"_re",
      "arg0":["macro",52],
      "arg1":"fbCount|twtrCount|gPlusCount|redditCount|strumbleCount"
    },{
      "function":"_eq",
      "arg0":["macro",9],
      "arg1":"Page Not Found - Live Science"
    },{
      "function":"_eq",
      "arg0":["macro",30],
      "arg1":"gtm.js"
    },{
      "function":"_eq",
      "arg0":["macro",30],
      "arg1":"conversion"
    },{
      "function":"_eq",
      "arg0":["macro",1],
      "arg1":"fa fa-rss"
    },{
      "function":"_eq",
      "arg0":["macro",1],
      "arg1":"fa fa-youtube"
    },{
      "function":"_eq",
      "arg0":["macro",30],
      "arg1":"Instant Article"
    },{
      "function":"_eq",
      "arg0":["macro",30],
      "arg1":"os_notification"
    },{
      "function":"_re",
      "arg0":["macro",30],
      "arg1":"OptiMonk *"
    },{
      "function":"_eq",
      "arg0":["macro",30],
      "arg1":"countdown_nav"
    },{
      "function":"_eq",
      "arg0":["macro",30],
      "arg1":"permission"
    },{
      "function":"_eq",
      "arg0":["macro",30],
      "arg1":"registration"
    },{
      "function":"_re",
      "arg0":["macro",30],
      "arg1":"onesignal_"
    },{
      "function":"_eq",
      "arg0":["macro",30],
      "arg1":"stickySharethis"
    },{
      "function":"_re",
      "arg0":["macro",1],
      "arg1":"slideNum|next-button|prev-button|slide-arrow|carousel-loadable-radio"
    },{
      "function":"_re",
      "arg0":["macro",52],
      "arg1":"cml-publishThisCarousel-label.*"
    },{
      "function":"_eq",
      "arg0":["macro",30],
      "arg1":"section_click"
    },{
      "function":"_eq",
      "arg0":["macro",30],
      "arg1":"readmore-event"
    },{
      "function":"_eq",
      "arg0":["macro",30],
      "arg1":"slideshow-options"
    },{
      "function":"_eq",
      "arg0":["macro",30],
      "arg1":"fb-button"
    },{
      "function":"_eq",
      "arg0":["macro",30],
      "arg1":"slideshow-email"
    },{
      "function":"_eq",
      "arg0":["macro",30],
      "arg1":"sign_in_popup"
    },{
      "function":"_eq",
      "arg0":["macro",30],
      "arg1":"purch.highImpactAdImp"
    },{
      "function":"_re",
      "arg0":["macro",30],
      "arg1":"whitelist-modal"
    },{
      "function":"_eq",
      "arg0":["macro",30],
      "arg1":"mobile-countdown"
    },{
      "function":"_eq",
      "arg0":["macro",30],
      "arg1":"purch.shopclick"
    },{
      "function":"_re",
      "arg0":["macro",30],
      "arg1":"article-quiz"
    },{
      "function":"_eq",
      "arg0":["macro",30],
      "arg1":"email-popup"
    },{
      "function":"_eq",
      "arg0":["macro",30],
      "arg1":"sponsored-content"
    },{
      "function":"_eq",
      "arg0":["macro",30],
      "arg1":"email-conversion"
    },{
      "function":"_eq",
      "arg0":["macro",30],
      "arg1":"generic-event"
    },{
      "function":"_re",
      "arg0":["macro",30],
      "arg1":"fb-like"
    },{
      "function":"_eq",
      "arg0":["macro",102],
      "arg1":"click"
    },{
      "function":"_eq",
      "arg0":["macro",30],
      "arg1":"fb-like"
    },{
      "function":"_eq",
      "arg0":["macro",30],
      "arg1":"Whitelist Modal"
    },{
      "function":"_eq",
      "arg0":["macro",30],
      "arg1":"paid-traffic"
    },{
      "function":"_eq",
      "arg0":["macro",30],
      "arg1":"suggested-content"
    },{
      "function":"_eq",
      "arg0":["macro",30],
      "arg1":"Latest Module Click"
    },{
      "function":"_eq",
      "arg0":["macro",30],
      "arg1":"gtm.elementVisibility"
    },{
      "function":"_re",
      "arg0":["macro",41],
      "arg1":"(^$|((^|,)1937581_192($|,)))"
    },{
      "function":"_re",
      "arg0":["macro",41],
      "arg1":"(^$|((^|,)1937581_196($|,)))"
    },{
      "function":"_re",
      "arg0":["macro",55],
      "arg1":".*"
    },{
      "function":"_eq",
      "arg0":["macro",8],
      "arg1":"true"
    },{
      "function":"_eq",
      "arg0":["macro",31],
      "arg1":"countdown"
    },{
      "function":"_cn",
      "arg0":["macro",51],
      "arg1":"qa.www"
    },{
      "function":"_cn",
      "arg0":["macro",51],
      "arg1":"staging"
    },{
      "function":"_eq",
      "arg0":["macro",33],
      "arg1":"history"
    },{
      "function":"_re",
      "arg0":["macro",51],
      "arg1":".*"
    }],
  "rules":[
    [["if",0,1],["add",5]],
    [["if",1,2],["add",5]],
    [["if",1,3],["add",5]],
    [["if",1,4],["add",5]],
    [["if",5,6],["add",6]],
    [["if",5,7,8],["add",7],["block",6]],
    [["if",5,9,10],["add",7],["block",6]],
    [["if",5,11,12,13],["add",7],["block",6]],
    [["if",5,14,15],["add",7],["block",6]],
    [["if",19],["add",0,71,73,4]],
    [["if",20],["add",0]],
    [["if",24],["unless",21,22,23],["add",8]],
    [["if",25],["add",9]],
    [["if",26],["add",9]],
    [["if",27],["add",9]],
    [["if",28],["add",9]],
    [["if",29],["add",9]],
    [["if",1,30],["add",10]],
    [["if",1,31],["add",11]],
    [["if",1,32],["add",11]],
    [["if",33,34],["add",12]],
    [["if",35],["add",13]],
    [["if",1,36],["add",14]],
    [["if",1,37],["add",15]],
    [["if",38],["add",16]],
    [["if",1,16],["add",17],["block",6]],
    [["if",39],["add",18]],
    [["if",40],["add",19]],
    [["if",41],["add",20]],
    [["if",42],["add",21]],
    [["if",43],["add",22]],
    [["if",44],["add",23]],
    [["if",45],["add",24]],
    [["if",1,46],["add",25]],
    [["if",1,47],["add",25]],
    [["if",48],["add",26]],
    [["if",49],["add",27]],
    [["if",50],["add",28]],
    [["if",51],["add",29]],
    [["if",52],["add",30]],
    [["if",53],["add",30]],
    [["if",54],["add",31]],
    [["if",55],["add",32]],
    [["if",56],["add",33]],
    [["if",57],["add",34]],
    [["if",58],["add",35]],
    [["if",59],["add",36]],
    [["if",60],["add",37]],
    [["if",61],["add",38]],
    [["if",62],["add",39]],
    [["if",63],["add",40]],
    [["if",64,65],["add",41]],
    [["if",66],["add",3]],
    [["if",67],["add",42]],
    [["if",68],["add",43]],
    [["if",69],["add",44]],
    [["if",70,71],["add",45]],
    [["if",70,72],["add",45]],
    [["if",34],["add",46,47,50,52,53,55,57,58,59,60,61,62,63,64,65,67,69,70,2,1]],
    [["if",34,73],["add",48,49,51,54,56,66,68]],
    [["if",34,74],["add",72]],
    [["if",34,79],["add",74,75]],
    [["if",5,17,18],["block",6]],
    [["if",19,75],["unless",76,77,78],["block",73]]]
},
"runtime":[
[],[]
]};

var aa=this,ea=function(){if(null===ba){var a;a:{var b=aa.document,c=b.querySelector&&b.querySelector("script[nonce]");if(c){var d=c.nonce||c.getAttribute("nonce");if(d&&da.test(d)){a=d;break a}}a=null}ba=a||""}return ba},da=/^[\w+/_-]+[=]{0,2}$/,ba=null,fa=function(a,b){function c(){}c.prototype=b.prototype;a.Ye=b.prototype;a.prototype=new c;a.prototype.constructor=a;a.He=function(a,c,f){for(var d=Array(arguments.length-2),e=2;e<arguments.length;e++)d[e-2]=arguments[e];return b.prototype[c].apply(a,
d)}};var ha=function(a,b){this.w=a;this.md=b};ha.prototype.zd=function(){return this.w};ha.prototype.getType=ha.prototype.zd;ha.prototype.getData=function(){return this.md};ha.prototype.getData=ha.prototype.getData;var ka=function(a){return"number"===typeof a&&0<=a&&isFinite(a)&&0===a%1||"string"===typeof a&&"-"!==a[0]&&a===""+parseInt(a,10)},la=function(){this.ja={};this.Ba=!1};la.prototype.get=function(a){return this.ja["dust."+a]};la.prototype.set=function(a,b){!this.Ba&&(this.ja["dust."+a]=b)};la.prototype.has=function(a){return this.ja.hasOwnProperty("dust."+a)};var ma=function(a){var b=[],c;for(c in a.ja)a.ja.hasOwnProperty(c)&&b.push(c.substr(5));return b};
la.prototype.remove=function(a){!this.Ba&&delete this.ja["dust."+a]};la.prototype.J=function(){this.Ba=!0};var g=function(a){this.ma=new la;this.h=[];a=a||[];for(var b in a)a.hasOwnProperty(b)&&(ka(b)?this.h[Number(b)]=a[Number(b)]:this.ma.set(b,a[b]))};g.prototype.toString=function(){for(var a=[],b=0;b<this.h.length;b++){var c=this.h[b];null===c||void 0===c?a.push(""):a.push(c.toString())}return a.join(",")};g.prototype.set=function(a,b){if("length"==a){if(!ka(b))throw"RangeError: Length property must be a valid integer.";this.h.length=Number(b)}else ka(a)?this.h[Number(a)]=b:this.ma.set(a,b)};
g.prototype.set=g.prototype.set;g.prototype.get=function(a){return"length"==a?this.length():ka(a)?this.h[Number(a)]:this.ma.get(a)};g.prototype.get=g.prototype.get;g.prototype.length=function(){return this.h.length};g.prototype.R=function(){for(var a=ma(this.ma),b=0;b<this.h.length;b++)a.push(b+"");return new g(a)};g.prototype.getKeys=g.prototype.R;g.prototype.remove=function(a){ka(a)?delete this.h[Number(a)]:this.ma.remove(a)};g.prototype.remove=g.prototype.remove;g.prototype.pop=function(){return this.h.pop()};
g.prototype.pop=g.prototype.pop;g.prototype.push=function(a){return this.h.push.apply(this.h,Array.prototype.slice.call(arguments))};g.prototype.push=g.prototype.push;g.prototype.shift=function(){return this.h.shift()};g.prototype.shift=g.prototype.shift;g.prototype.splice=function(a,b,c){return new g(this.h.splice.apply(this.h,arguments))};g.prototype.splice=g.prototype.splice;g.prototype.unshift=function(a){return this.h.unshift.apply(this.h,Array.prototype.slice.call(arguments))};
g.prototype.unshift=g.prototype.unshift;g.prototype.has=function(a){return ka(a)&&this.h.hasOwnProperty(a)||this.ma.has(a)};var na=function(){function a(a,b){c[a]=b}function b(){c={}}var c={},d={add:a,reset:b,create:function(d){var e={add:a,request:function(a,b){return c[a]?c[a].apply(d,Array.prototype.slice.call(arguments,1)):!1},reset:b};e.add=e.add;e.request=e.request;e.reset=e.reset;return e}};d.add=d.add;d.reset=d.reset;return d};var oa=function(){function a(a,c){if(b[a]){if(b[a].Oa+c>b[a].max)throw Error("Quota exceeded");b[a].Oa+=c}}var b={},c=void 0,d=void 0,e={Wd:function(a){c=a},Ub:function(){c&&a(c,1)},Xd:function(a){d=a},V:function(b){d&&a(d,b)},qe:function(a,c){b[a]=b[a]||{Oa:0};b[a].max=c},yd:function(a){return b[a]&&b[a].Oa||0},reset:function(){b={}},fd:a};e.onFnConsume=e.Wd;e.consumeFn=e.Ub;e.onStorageConsume=e.Xd;e.consumeStorage=e.V;e.setMax=e.qe;e.getConsumed=e.yd;e.reset=e.reset;e.consume=e.fd;return e};var pa=function(a,b,c){this.K=a;this.aa=b;this.Z=c;this.h=new la};pa.prototype.add=function(a,b){this.h.Ba||(this.K.V(("string"===typeof a?a.length:1)+("string"===typeof b?b.length:1)),this.h.set(a,b))};pa.prototype.add=pa.prototype.add;pa.prototype.set=function(a,b){this.h.Ba||(this.Z&&this.Z.has(a)?this.Z.set(a,b):(this.K.V(("string"===typeof a?a.length:1)+("string"===typeof b?b.length:1)),this.h.set(a,b)))};pa.prototype.set=pa.prototype.set;
pa.prototype.get=function(a){return this.h.has(a)?this.h.get(a):this.Z?this.Z.get(a):void 0};pa.prototype.get=pa.prototype.get;pa.prototype.has=function(a){return!!this.h.has(a)||!(!this.Z||!this.Z.has(a))};pa.prototype.has=pa.prototype.has;pa.prototype.I=function(){return this.K};pa.prototype.J=function(){this.h.J()};var qa=function(a){return"[object Array]"==Object.prototype.toString.call(Object(a))},ra=function(a,b){if(Array.prototype.indexOf){var c=a.indexOf(b);return"number"==typeof c?c:-1}for(var d=0;d<a.length;d++)if(a[d]===b)return d;return-1};var v=function(a,b){la.call(this);this.kc=a;this.wd=b};fa(v,la);var ta=function(a,b){for(var c,d=0;d<b.length&&!(c=sa(a,b[d]),c instanceof ha);d++);return c},sa=function(a,b){var c=a.get(String(b[0]));if(!(c&&c instanceof v))throw"Attempting to execute non-function "+b[0]+".";return c.m.apply(c,[a].concat(b.slice(1)))};v.prototype.toString=function(){return this.kc};v.prototype.getName=function(){return this.kc};v.prototype.getName=v.prototype.getName;v.prototype.R=function(){return new g(ma(this))};
v.prototype.getKeys=v.prototype.R;v.prototype.m=function(a,b){var c,d={B:function(){return a},evaluate:function(b){var c=a;return qa(b)?sa(c,b):b},xa:function(b){return ta(a,b)},I:function(){return a.I()},Ne:function(){c||(c=a.aa.create(d));return c}};a.I().Ub();return this.wd.apply(d,Array.prototype.slice.call(arguments,1))};v.prototype.invoke=v.prototype.m;var ua=function(){la.call(this)};fa(ua,la);ua.prototype.R=function(){return new g(ma(this))};ua.prototype.getKeys=ua.prototype.R;/*
 jQuery v1.9.1 (c) 2005, 2012 jQuery Foundation, Inc. jquery.org/license. */
var va=/\[object (Boolean|Number|String|Function|Array|Date|RegExp)\]/,wa=function(a){if(null==a)return String(a);var b=va.exec(Object.prototype.toString.call(Object(a)));return b?b[1].toLowerCase():"object"},xa=function(a,b){return Object.prototype.hasOwnProperty.call(Object(a),b)},ya=function(a){if(!a||"object"!=wa(a)||a.nodeType||a==a.window)return!1;try{if(a.constructor&&!xa(a,"constructor")&&!xa(a.constructor.prototype,"isPrototypeOf"))return!1}catch(c){return!1}for(var b in a);return void 0===
b||xa(a,b)},za=function(a,b){var c=b||("array"==wa(a)?[]:{}),d;for(d in a)if(xa(a,d)){var e=a[d];"array"==wa(e)?("array"!=wa(c[d])&&(c[d]=[]),c[d]=za(e,c[d])):ya(e)?(ya(c[d])||(c[d]={}),c[d]=za(e,c[d])):c[d]=e}return c};var Aa=function(a){if(a instanceof g){for(var b=[],c=a.length(),d=0;d<c;d++)a.has(d)&&(b[d]=Aa(a.get(d)));return b}if(a instanceof ua){for(var e={},f=a.R(),h=f.length(),k=0;k<h;k++)e[f.get(k)]=Aa(a.get(f.get(k)));return e}return a instanceof v?function(){for(var b=Array.prototype.slice.call(arguments,0),c=0;c<b.length;c++)b[c]=Ba(b[c]);var d=new pa(oa(),na());return Aa(a.m.apply(a,[d].concat(b)))}:a},Ba=function(a){if(qa(a)){for(var b=[],c=0;c<a.length;c++)a.hasOwnProperty(c)&&(b[c]=Ba(a[c]));return new g(b)}if(ya(a)){var d=
new ua,e;for(e in a)a.hasOwnProperty(e)&&d.set(e,Ba(a[e]));return d}if("function"===typeof a)return new v("",function(b){for(var c=Array.prototype.slice.call(arguments,0),d=0;d<c.length;d++)c[d]=Aa(this.evaluate(c[d]));return Ba(a.apply(a,c))});var f=typeof a;if(null===a||"string"===f||"number"===f||"boolean"===f)return a};var Da={control:function(a,b){return new ha(a,this.evaluate(b))},fn:function(a,b,c){var d=this.B(),e=this.evaluate(b);if(!(e instanceof g))throw"Error: non-List value given for Fn argument names.";var f=Array.prototype.slice.call(arguments,2);this.I().V(a.length+f.length);return new v(a,function(){return function(a){for(var b=new pa(d.K,d.aa,d),c=Array.prototype.slice.call(arguments,0),h=0;h<c.length;h++)if(c[h]=this.evaluate(c[h]),c[h]instanceof ha)return c[h];for(var n=e.get("length"),p=0;p<n;p++)p<
c.length?b.set(e.get(p),c[p]):b.set(e.get(p),void 0);b.set("arguments",new g(c));var q=ta(b,f);if(q instanceof ha)return"return"===q.w?q.getData():q}}())},list:function(a){var b=this.I();b.V(arguments.length);for(var c=new g,d=0;d<arguments.length;d++){var e=this.evaluate(arguments[d]);"string"===typeof e&&b.V(e.length?e.length-1:0);c.push(e)}return c},map:function(a){for(var b=this.I(),c=new ua,d=0;d<arguments.length-1;d+=2){var e=this.evaluate(arguments[d])+"",f=this.evaluate(arguments[d+1]),h=
e.length;h+="string"===typeof f?f.length:1;b.V(h);c.set(e,f)}return c},undefined:function(){}};var w=function(){this.K=oa();this.aa=na();this.za=new pa(this.K,this.aa)};w.prototype.T=function(a,b){var c=new v(a,b);c.J();this.za.set(a,c)};w.prototype.addInstruction=w.prototype.T;w.prototype.Tb=function(a,b){Da.hasOwnProperty(a)&&this.T(b||a,Da[a])};w.prototype.addNativeInstruction=w.prototype.Tb;w.prototype.I=function(){return this.K};w.prototype.getQuota=w.prototype.I;w.prototype.Va=function(){this.K=oa();this.za.K=this.K};w.prototype.resetQuota=w.prototype.Va;
w.prototype.ne=function(){this.aa=na();this.za.aa=this.aa};w.prototype.resetPermissions=w.prototype.ne;w.prototype.O=function(a,b){var c=Array.prototype.slice.call(arguments,0);return this.wb(c)};w.prototype.execute=w.prototype.O;w.prototype.wb=function(a){for(var b,c=0;c<arguments.length;c++){var d=sa(this.za,arguments[c]);b=d instanceof ha||d instanceof v||d instanceof g||d instanceof ua||null===d||void 0===d||"string"===typeof d||"number"===typeof d||"boolean"===typeof d?d:void 0}return b};
w.prototype.run=w.prototype.wb;w.prototype.J=function(){this.za.J()};w.prototype.makeImmutable=w.prototype.J;var Ea=function(a){for(var b=[],c=0;c<a.length();c++)a.has(c)&&(b[c]=a.get(c));return b};var Fa={ve:"concat every filter forEach hasOwnProperty indexOf join lastIndexOf map pop push reduce reduceRight reverse shift slice some sort splice unshift toString".split(" "),concat:function(a,b){for(var c=[],d=0;d<this.length();d++)c.push(this.get(d));for(d=1;d<arguments.length;d++)if(arguments[d]instanceof g)for(var e=arguments[d],f=0;f<e.length();f++)c.push(e.get(f));else c.push(arguments[d]);return new g(c)},every:function(a,b){for(var c=this.length(),d=0;d<this.length()&&d<c;d++)if(this.has(d)&&
!b.m(a,this.get(d),d,this))return!1;return!0},filter:function(a,b){for(var c=this.length(),d=[],e=0;e<this.length()&&e<c;e++)this.has(e)&&b.m(a,this.get(e),e,this)&&d.push(this.get(e));return new g(d)},forEach:function(a,b){for(var c=this.length(),d=0;d<this.length()&&d<c;d++)this.has(d)&&b.m(a,this.get(d),d,this)},hasOwnProperty:function(a,b){return this.has(b)},indexOf:function(a,b,c){var d=this.length(),e=void 0===c?0:Number(c);0>e&&(e=Math.max(d+e,0));for(var f=e;f<d;f++)if(this.has(f)&&this.get(f)===
b)return f;return-1},join:function(a,b){for(var c=[],d=0;d<this.length();d++)c.push(this.get(d));return c.join(b)},lastIndexOf:function(a,b,c){var d=this.length(),e=d-1;void 0!==c&&(e=0>c?d+c:Math.min(c,e));for(var f=e;0<=f;f--)if(this.has(f)&&this.get(f)===b)return f;return-1},map:function(a,b){for(var c=this.length(),d=[],e=0;e<this.length()&&e<c;e++)this.has(e)&&(d[e]=b.m(a,this.get(e),e,this));return new g(d)},pop:function(){return this.pop()},push:function(a,b){return this.push.apply(this,Array.prototype.slice.call(arguments,
1))},reduce:function(a,b,c){var d=this.length(),e,f;if(void 0!==c)e=c,f=0;else{if(0==d)throw"TypeError: Reduce on List with no elements.";for(var h=0;h<d;h++)if(this.has(h)){e=this.get(h);f=h+1;break}if(h==d)throw"TypeError: Reduce on List with no elements.";}for(h=f;h<d;h++)this.has(h)&&(e=b.m(a,e,this.get(h),h,this));return e},reduceRight:function(a,b,c){var d=this.length(),e,f;if(void 0!==c)e=c,f=d-1;else{if(0==d)throw"TypeError: ReduceRight on List with no elements.";for(var h=1;h<=d;h++)if(this.has(d-
h)){e=this.get(d-h);f=d-(h+1);break}if(h>d)throw"TypeError: ReduceRight on List with no elements.";}for(h=f;0<=h;h--)this.has(h)&&(e=b.m(a,e,this.get(h),h,this));return e},reverse:function(){for(var a=Ea(this),b=a.length-1,c=0;0<=b;b--,c++)a.hasOwnProperty(b)?this.set(c,a[b]):this.remove(c);return this},shift:function(){return this.shift()},slice:function(a,b,c){var d=this.length();void 0===b&&(b=0);b=0>b?Math.max(d+b,0):Math.min(b,d);c=void 0===c?d:0>c?Math.max(d+c,0):Math.min(c,d);c=Math.max(b,
c);for(var e=[],f=b;f<c;f++)e.push(this.get(f));return new g(e)},some:function(a,b){for(var c=this.length(),d=0;d<this.length()&&d<c;d++)if(this.has(d)&&b.m(a,this.get(d),d,this))return!0;return!1},sort:function(a,b){var c=Ea(this);void 0===b?c.sort():c.sort(function(c,d){return Number(b.m(a,c,d))});for(var d=0;d<c.length;d++)c.hasOwnProperty(d)?this.set(d,c[d]):this.remove(d)},splice:function(a,b,c,d){return this.splice.apply(this,Array.prototype.splice.call(arguments,1,arguments.length-1))},toString:function(){return this.toString()},
unshift:function(a,b){return this.unshift.apply(this,Array.prototype.slice.call(arguments,1))}};var x={fc:{ADD:0,AND:1,APPLY:2,ASSIGN:3,BREAK:4,CASE:5,CONTINUE:6,CONTROL:49,CREATE_ARRAY:7,CREATE_OBJECT:8,DEFAULT:9,DEFN:50,DIVIDE:10,DO:11,EQUALS:12,EXPRESSION_LIST:13,FN:51,FOR:14,FOR_IN:47,GET:15,GET_CONTAINER_VARIABLE:48,GET_INDEX:16,GET_PROPERTY:17,GREATER_THAN:18,GREATER_THAN_EQUALS:19,IDENTITY_EQUALS:20,IDENTITY_NOT_EQUALS:21,IF:22,LESS_THAN:23,LESS_THAN_EQUALS:24,MODULUS:25,MULTIPLY:26,NEGATE:27,NOT:28,NOT_EQUALS:29,NULL:45,OR:30,PLUS_EQUALS:31,POST_DECREMENT:32,POST_INCREMENT:33,PRE_DECREMENT:34,
PRE_INCREMENT:35,QUOTE:46,RETURN:36,SET_PROPERTY:43,SUBTRACT:37,SWITCH:38,TERNARY:39,TYPEOF:40,UNDEFINED:44,VAR:41,WHILE:42}},Ga="charAt concat indexOf lastIndexOf match replace search slice split substring toLowerCase toLocaleLowerCase toString toUpperCase toLocaleUpperCase trim".split(" "),Ha=new ha("break"),Ia=new ha("continue");x.add=function(a,b){return this.evaluate(a)+this.evaluate(b)};x.and=function(a,b){return this.evaluate(a)&&this.evaluate(b)};
x.apply=function(a,b,c){a=this.evaluate(a);b=this.evaluate(b);c=this.evaluate(c);if(!(c instanceof g))throw"Error: Non-List argument given to Apply instruction.";if(null===a||void 0===a)throw"TypeError: Can't read property "+b+" of "+a+".";if("boolean"==typeof a||"number"==typeof a){if("toString"==b)return a.toString();throw"TypeError: "+a+"."+b+" is not a function.";}if("string"==typeof a){if(0<=ra(Ga,b))return Ba(a[b].apply(a,Ea(c)));throw"TypeError: "+b+" is not a function";}if(a instanceof g){if(a.has(b)){var d=
a.get(b);if(d instanceof v){var e=Ea(c);e.unshift(this.B());return d.m.apply(d,e)}throw"TypeError: "+b+" is not a function";}if(0<=ra(Fa.ve,b))return e=Ea(c),e.unshift(this.B()),Fa[b].apply(a,e)}if(a instanceof v||a instanceof ua){if(a.has(b)){d=a.get(b);if(d instanceof v)return e=Ea(c),e.unshift(this.B()),d.m.apply(d,e);throw"TypeError: "+b+" is not a function";}if("toString"==b)return a instanceof v?a.getName():a.toString();if("hasOwnProperty"==b)return a.has.apply(a,Ea(c))}throw"TypeError: Object has no '"+
b+"' property.";};x.assign=function(a,b){a=this.evaluate(a);if("string"!=typeof a)throw"Invalid key name given for assignment.";var c=this.B();if(!c.has(a))throw"Attempting to assign to undefined value "+b;var d=this.evaluate(b);c.set(a,d);return d};x["break"]=function(){return Ha};x["case"]=function(a){for(var b=this.evaluate(a),c=0;c<b.length;c++){var d=this.evaluate(b[c]);if(d instanceof ha)return d}};x["continue"]=function(){return Ia};
x.nd=function(a,b,c){var d=new g;b=this.evaluate(b);for(var e=0;e<b.length;e++)d.push(b[e]);var f=[x.fc.FN,a,d].concat(Array.prototype.splice.call(arguments,2,arguments.length-2));this.B().set(a,this.evaluate(f))};x.qd=function(a,b){return this.evaluate(a)/this.evaluate(b)};x.td=function(a,b){return this.evaluate(a)==this.evaluate(b)};x.ud=function(a){for(var b,c=0;c<arguments.length;c++)b=this.evaluate(arguments[c]);return b};
x.xd=function(a,b,c){a=this.evaluate(a);b=this.evaluate(b);c=this.evaluate(c);var d=this.B();if("string"==typeof b)for(var e=0;e<b.length;e++){d.set(a,e);var f=this.xa(c);if(f instanceof ha){if("break"==f.w)break;if("return"==f.w)return f}}else if(b instanceof ua||b instanceof g||b instanceof v){var h=b.R(),k=h.length();for(e=0;e<k;e++)if(d.set(a,h.get(e)),f=this.xa(c),f instanceof ha){if("break"==f.w)break;if("return"==f.w)return f}}};x.get=function(a){return this.B().get(this.evaluate(a))};
x.cc=function(a,b){var c;a=this.evaluate(a);b=this.evaluate(b);if(void 0===a||null===a)throw"TypeError: cannot access property of "+a+".";a instanceof ua||a instanceof g||a instanceof v?c=a.get(b):"string"==typeof a&&("length"==b?c=a.length:ka(b)&&(c=a[b]));return c};x.Ad=function(a,b){return this.evaluate(a)>this.evaluate(b)};x.Bd=function(a,b){return this.evaluate(a)>=this.evaluate(b)};x.Fd=function(a,b){return this.evaluate(a)===this.evaluate(b)};x.Gd=function(a,b){return this.evaluate(a)!==this.evaluate(b)};
x["if"]=function(a,b,c){var d=[];this.evaluate(a)?d=this.evaluate(b):c&&(d=this.evaluate(c));var e=this.xa(d);if(e instanceof ha)return e};x.Od=function(a,b){return this.evaluate(a)<this.evaluate(b)};x.Pd=function(a,b){return this.evaluate(a)<=this.evaluate(b)};x.Rd=function(a,b){return this.evaluate(a)%this.evaluate(b)};x.multiply=function(a,b){return this.evaluate(a)*this.evaluate(b)};x.Sd=function(a){return-this.evaluate(a)};x.Td=function(a){return!this.evaluate(a)};
x.Ud=function(a,b){return this.evaluate(a)!=this.evaluate(b)};x["null"]=function(){return null};x.or=function(a,b){return this.evaluate(a)||this.evaluate(b)};x.uc=function(a,b){var c=this.evaluate(a);this.evaluate(b);return c};x.vc=function(a){return this.evaluate(a)};x.quote=function(a){return Array.prototype.slice.apply(arguments)};x["return"]=function(a){return new ha("return",this.evaluate(a))};
x.setProperty=function(a,b,c){a=this.evaluate(a);b=this.evaluate(b);c=this.evaluate(c);if(null===a||void 0===a)throw"TypeError: Can't set property "+b+" of "+a+".";(a instanceof v||a instanceof g||a instanceof ua)&&a.set(b,c);return c};x.ue=function(a,b){return this.evaluate(a)-this.evaluate(b)};
x["switch"]=function(a,b,c){a=this.evaluate(a);b=this.evaluate(b);c=this.evaluate(c);if(!qa(b)||!qa(c))throw"Error: Malformed switch instruction.";for(var d,e=!1,f=0;f<b.length;f++)if(e||a===this.evaluate(b[f]))if(d=this.evaluate(c[f]),d instanceof ha){var h=d.w;if("break"==h)return;if("return"==h||"continue"==h)return d}else e=!0;if(c.length==b.length+1&&(d=this.evaluate(c[c.length-1]),d instanceof ha&&("return"==d.w||"continue"==d.w)))return d};
x.we=function(a,b,c){return this.evaluate(a)?this.evaluate(b):this.evaluate(c)};x["typeof"]=function(a){a=this.evaluate(a);return a instanceof v?"function":typeof a};x.undefined=function(){};x["var"]=function(a){for(var b=this.B(),c=0;c<arguments.length;c++){var d=arguments[c];"string"!=typeof d||b.add(d,void 0)}};
x["while"]=function(a,b,c,d){var e,f=this.evaluate(d);if(this.evaluate(c)&&(e=this.xa(f),e instanceof ha)){if("break"==e.w)return;if("return"==e.w)return e}for(;this.evaluate(a);){e=this.xa(f);if(e instanceof ha){if("break"==e.w)break;if("return"==e.w)return e}this.evaluate(b)}};var Ka=function(){this.ec=!1;this.W=new w;Ja(this);this.ec=!0};Ka.prototype.Ld=function(){return this.ec};Ka.prototype.isInitialized=Ka.prototype.Ld;Ka.prototype.O=function(a){return this.W.wb(a)};Ka.prototype.execute=Ka.prototype.O;Ka.prototype.J=function(){this.W.J()};Ka.prototype.makeImmutable=Ka.prototype.J;
var Ja=function(a){function b(a,b){e.W.Tb(a,String(b))}function c(a,b){e.W.T(String(d[a]),b)}var d=x.fc,e=a;b("control",d.CONTROL);b("fn",d.FN);b("list",d.CREATE_ARRAY);b("map",d.CREATE_OBJECT);b("undefined",d.UNDEFINED);c("ADD",x.add);c("AND",x.and);c("APPLY",x.apply);c("ASSIGN",x.assign);c("BREAK",x["break"]);c("CASE",x["case"]);c("CONTINUE",x["continue"]);c("DEFAULT",x["case"]);c("DEFN",x.nd);c("DIVIDE",x.qd);c("EQUALS",x.td);c("EXPRESSION_LIST",x.ud);c("FOR_IN",x.xd);c("GET",x.get);c("GET_INDEX",
x.cc);c("GET_PROPERTY",x.cc);c("GREATER_THAN",x.Ad);c("GREATER_THAN_EQUALS",x.Bd);c("IDENTITY_EQUALS",x.Fd);c("IDENTITY_NOT_EQUALS",x.Gd);c("IF",x["if"]);c("LESS_THAN",x.Od);c("LESS_THAN_EQUALS",x.Pd);c("MODULUS",x.Rd);c("MULTIPLY",x.multiply);c("NEGATE",x.Sd);c("NOT",x.Td);c("NOT_EQUALS",x.Ud);c("NULL",x["null"]);c("OR",x.or);c("POST_DECREMENT",x.uc);c("POST_INCREMENT",x.uc);c("PRE_DECREMENT",x.vc);c("PRE_INCREMENT",x.vc);c("QUOTE",x.quote);c("RETURN",x["return"]);c("SET_PROPERTY",x.setProperty);
c("SUBTRACT",x.ue);c("SWITCH",x["switch"]);c("TERNARY",x.we);c("TYPEOF",x["typeof"]);c("VAR",x["var"]);c("WHILE",x["while"])};Ka.prototype.T=function(a,b){this.W.T(a,b)};Ka.prototype.addInstruction=Ka.prototype.T;Ka.prototype.I=function(){return this.W.I()};Ka.prototype.getQuota=Ka.prototype.I;Ka.prototype.Va=function(){this.W.Va()};Ka.prototype.resetQuota=Ka.prototype.Va;var La=function(){this.Ra={}};La.prototype.get=function(a){return this.Ra.hasOwnProperty(a)?this.Ra[a]:void 0};La.prototype.add=function(a,b){if(this.Ra.hasOwnProperty(a))throw"Attempting to add a function which already exists: "+a+".";var c=new v(a,function(){for(var a=Array.prototype.slice.call(arguments,0),c=0;c<a.length;c++)a[c]=this.evaluate(a[c]);return b.apply(this,a)});c.J();this.Ra[a]=c};La.prototype.addAll=function(a){for(var b in a)a.hasOwnProperty(b)&&this.add(b,a[b])};var y=window,A=document,Ma=navigator,Na=function(a,b){var c=y[a];y[a]=void 0===c?b:c;return y[a]},Oa=function(a,b){b&&(a.addEventListener?a.onload=b:a.onreadystatechange=function(){a.readyState in{loaded:1,complete:1}&&(a.onreadystatechange=null,b())})},B=function(a,b,c){var d=A.createElement("script");d.type="text/javascript";d.async=!0;d.src=a;Oa(d,b);c&&(d.onerror=c);ea()&&d.setAttribute("nonce",ea());var e=A.getElementsByTagName("script")[0]||A.body||A.head;e.parentNode.insertBefore(d,e);return d},
Pa=function(a,b){var c=A.createElement("iframe");c.height="0";c.width="0";c.style.display="none";c.style.visibility="hidden";var d=A.body&&A.body.lastChild||A.body||A.head;d.parentNode.insertBefore(c,d);Oa(c,b);void 0!==a&&(c.src=a);return c},Qa=function(a,b,c){var d=new Image(1,1);d.onload=function(){d.onload=null;b&&b()};d.onerror=function(){d.onerror=null;c&&c()};d.src=a},Ra=function(a,b,c,d){a.addEventListener?a.addEventListener(b,c,!!d):a.attachEvent&&a.attachEvent("on"+b,c)},Sa=function(a,b,
c){a.removeEventListener?a.removeEventListener(b,c,!1):a.detachEvent&&a.detachEvent("on"+b,c)},D=function(a){y.setTimeout(a,0)},Ua=function(a){var b=A.getElementById(a);if(b&&Ta(b,"id")!=a)for(var c=1;c<document.all[a].length;c++)if(Ta(document.all[a][c],"id")==a)return document.all[a][c];return b},Ta=function(a,b){return a&&b&&a.attributes&&a.attributes[b]?a.attributes[b].value:null},Wa=function(a){var b=a.innerText||a.textContent||"";b&&" "!=b&&(b=b.replace(/^[\s\xa0]+|[\s\xa0]+$/g,""));b&&(b=b.replace(/(\xa0+|\s{2,}|\n|\r\t)/g,
" "));return b},Xa=function(a){var b=A.createElement("div");b.innerHTML="A<div>"+a+"</div>";b=b.lastChild;for(var c=[];b.firstChild;)c.push(b.removeChild(b.firstChild));return c},Ya=function(a){Ma.sendBeacon&&Ma.sendBeacon(a)||Qa(a)};var Za=function(a,b){for(var c=a.split("&"),d=0;d<c.length;d++){var e=c[d].split("=");if(decodeURIComponent(e[0]).replace(/\+/g," ")==b)return decodeURIComponent(e.slice(1).join("=")).replace(/\+/g," ")}},G=function(a,b,c,d,e){var f,h=function(a){return a?a.replace(":","").toLowerCase():""},k=h(a.protocol)||h(y.location.protocol);b&&(b=String(b).toLowerCase());switch(b){case "protocol":f=k;break;case "host":f=(a.hostname||y.location.hostname).split(":")[0].toLowerCase();if(c){var l=/^www\d*\./.exec(f);
l&&l[0]&&(f=f.substr(l[0].length))}break;case "port":f=String(1*(a.hostname?a.port:y.location.port)||("http"==k?80:"https"==k?443:""));break;case "path":f="/"==a.pathname.substr(0,1)?a.pathname:"/"+a.pathname;var m=f.split("/");0<=ra(d||[],m[m.length-1])&&(m[m.length-1]="");f=m.join("/");break;case "query":f=a.search.replace("?","");e&&(f=Za(f,e));break;case "extension":var n=a.pathname.split(".");f=1<n.length?n[n.length-1]:"";f=f.split("/")[0];break;case "fragment":f=a.hash.replace("#","");break;
default:f=a&&a.href}return f},$a=function(a){var b="";a&&a.href&&(b=a.hash?a.href.replace(a.hash,""):a.href);return b},N=function(a){var b=A.createElement("a");a&&(b.href=a);return b};var cb=function(){this.sc=new Ka;var a=new La;a.addAll(ab());bb(this,function(b){return a.get(b)})},ab=function(){return{callInWindow:db,encodeURI:encodeURI,encodeURIComponent:encodeURIComponent,getCurrentUrl:eb,getInWindow:fb,getReferrer:gb,getUrlComponent:hb,getUrlFragment:ib,isPlainObject:jb,loadIframe:kb,loadJavaScript:lb,removeUrlFragment:nb,replaceAll:ob,sendTrackingBeacon:pb,setInWindow:qb}};cb.prototype.O=function(a){return this.sc.O(a)};cb.prototype.execute=cb.prototype.O;
var bb=function(a,b){a.sc.T("require",b)};function db(a,b){for(var c=a.split("."),d=y,e=d[c[0]],f=1;e&&f<c.length;f++)d=e,e=e[c[f]];if("function"==wa(e)){var h=[];for(f=1;f<arguments.length;f++)h.push(Aa(arguments[f]));e.apply(d,h)}}function eb(){return y.location.href}function fb(a,b,c){for(var d=a.split("."),e=y,f=0;f<d.length-1;f++)if(e=e[d[f]],void 0===e||null===e)return;b&&(void 0===e[d[f]]||c&&!e[d[f]])&&(e[d[f]]=Aa(b));return Ba(e[d[f]])}function gb(){return A.referrer}
function hb(a,b,c,d,e){var f;if(d&&d instanceof g){f=[];for(var h=0;h<d.length();h++){var k=d.get(h);"string"==typeof k&&f.push(k)}}return G(N(a),b,c,f,e)}function ib(a){return G(N(a),"fragment")}function jb(a){return a instanceof ua}function kb(a,b){var c=this.B();Pa(a,function(){b instanceof v&&b.m(c)})}var rb={};
function lb(a,b,c,d){var e=this.B(),f=function(){b instanceof v&&b.m(e)},h=function(){c instanceof v&&c.m(e)};d?rb[d]?(rb[d].onSuccess.push(f),rb[d].onFailure.push(h)):(rb[d]={onSuccess:[f],onFailure:[h]},f=function(){for(var a=rb[d].onSuccess,b=0;b<a.length;b++)D(a[b]);a.push=function(a){D(a);return 0}},h=function(){for(var a=rb[d].onFailure,b=0;b<a.length;b++)D(a[b]);rb[d]=null},B(a,f,h)):B(a,f,h)}function nb(a){return $a(N(a))}function ob(a,b,c){return a.replace(new RegExp(b,"g"),c)}
function pb(a,b,c){var d=this.B();Qa(a,function(){b instanceof v&&b.m(d)},function(){c instanceof v&&c.m(d)})}function qb(a,b,c){for(var d=a.split("."),e=y,f=0;f<d.length-1;f++)if(e=e[d[f]],void 0===e)return!1;return void 0===e[d[f]]||c?(e[d[f]]=Aa(b),!0):!1};
var sb=[],tb={"\x00":"&#0;",'"':"&quot;","&":"&amp;","'":"&#39;","<":"&lt;",">":"&gt;","\t":"&#9;","\n":"&#10;","\x0B":"&#11;","\f":"&#12;","\r":"&#13;"," ":"&#32;","-":"&#45;","/":"&#47;","=":"&#61;","`":"&#96;","\u0085":"&#133;","\u00a0":"&#160;","\u2028":"&#8232;","\u2029":"&#8233;"},ub=function(a){return tb[a]},vb=/[\x00\x22\x26\x27\x3c\x3e]/g;var zb=/[\x00\x08-\x0d\x22\x26\x27\/\x3c-\x3e\\\x85\u2028\u2029]/g,Ab={"\x00":"\\x00","\b":"\\x08","\t":"\\t","\n":"\\n","\x0B":"\\x0b",
"\f":"\\f","\r":"\\r",'"':"\\x22","&":"\\x26","'":"\\x27","/":"\\/","<":"\\x3c","=":"\\x3d",">":"\\x3e","\\":"\\\\","\u0085":"\\x85","\u2028":"\\u2028","\u2029":"\\u2029",$:"\\x24","(":"\\x28",")":"\\x29","*":"\\x2a","+":"\\x2b",",":"\\x2c","-":"\\x2d",".":"\\x2e",":":"\\x3a","?":"\\x3f","[":"\\x5b","]":"\\x5d","^":"\\x5e","{":"\\x7b","|":"\\x7c","}":"\\x7d"},Bb=function(a){return Ab[a]};
sb[8]=function(a){if(null==a)return" null ";switch(typeof a){case "boolean":case "number":return" "+a+" ";default:return"'"+String(String(a)).replace(zb,Bb)+"'"}};var Kb=/[\x00- \x22\x27-\x29\x3c\x3e\\\x7b\x7d\x7f\x85\xa0\u2028\u2029\uff01\uff03\uff04\uff06-\uff0c\uff0f\uff1a\uff1b\uff1d\uff1f\uff20\uff3b\uff3d]/g,Lb={"\x00":"%00","\u0001":"%01","\u0002":"%02","\u0003":"%03","\u0004":"%04","\u0005":"%05","\u0006":"%06","\u0007":"%07","\b":"%08","\t":"%09","\n":"%0A","\x0B":"%0B","\f":"%0C","\r":"%0D","\u000e":"%0E","\u000f":"%0F","\u0010":"%10",
"\u0011":"%11","\u0012":"%12","\u0013":"%13","\u0014":"%14","\u0015":"%15","\u0016":"%16","\u0017":"%17","\u0018":"%18","\u0019":"%19","\u001a":"%1A","\u001b":"%1B","\u001c":"%1C","\u001d":"%1D","\u001e":"%1E","\u001f":"%1F"," ":"%20",'"':"%22","'":"%27","(":"%28",")":"%29","<":"%3C",">":"%3E","\\":"%5C","{":"%7B","}":"%7D","\u007f":"%7F","\u0085":"%C2%85","\u00a0":"%C2%A0","\u2028":"%E2%80%A8","\u2029":"%E2%80%A9","\uff01":"%EF%BC%81","\uff03":"%EF%BC%83","\uff04":"%EF%BC%84","\uff06":"%EF%BC%86",
"\uff07":"%EF%BC%87","\uff08":"%EF%BC%88","\uff09":"%EF%BC%89","\uff0a":"%EF%BC%8A","\uff0b":"%EF%BC%8B","\uff0c":"%EF%BC%8C","\uff0f":"%EF%BC%8F","\uff1a":"%EF%BC%9A","\uff1b":"%EF%BC%9B","\uff1d":"%EF%BC%9D","\uff1f":"%EF%BC%9F","\uff20":"%EF%BC%A0","\uff3b":"%EF%BC%BB","\uff3d":"%EF%BC%BD"},Mb=function(a){return Lb[a]};sb[16]=function(a){return a};var Ob,Pb=[],Qb=[],Rb=[],Sb=[],Tb=[],Ub={},Vb,Wb,Xb,Yb=function(a){var b=a["function"];if(!b)throw"Error: No function name given for function call.";var c=!!Ub[b],d={},e;for(e in a)a.hasOwnProperty(e)&&0===e.indexOf("vtp_")&&(d[c?e:e.substr(4)]=a[e]);return c?Ub[b](d):Ob(b,d)},cc=function(a,b,c){c=c||[];var d={},e;for(e in a)a.hasOwnProperty(e)&&(d[e]=bc(a[e],b,c));return d},bc=function(a,b,c){if(qa(a)){var d;switch(a[0]){case "function_id":return a[1];case "list":d=[];for(var e=1;e<a.length;e++)d.push(bc(a[e],
b,c));return d;case "macro":var f=a[1];if(c[f])return;var h=Pb[f];if(!h||b(h))return;c[f]=!0;try{var k=cc(h,b,c);d=Yb(k);Xb&&(d=Xb.hd(d,k))}catch(t){d=!1}c[f]=!1;return d;case "map":d={};for(var l=1;l<a.length;l+=2)d[bc(a[l],b,c)]=bc(a[l+1],b,c);return d;case "template":d=[];for(var m=!1,n=1;n<a.length;n++){var p=bc(a[n],b,c);Wb&&(m=m||p===Wb.Ia);d.push(p)}return Wb&&m?Wb.jd(d):d.join("");case "escape":d=bc(a[1],b,c);if(Wb&&qa(a[1])&&"macro"===a[1][0]&&Wb.Md(a))return Wb.ae(d);d=String(d);for(var q=
2;q<a.length;q++)sb[a[q]]&&(d=sb[a[q]](d));return d;case "tag":var r=a[1];if(!Sb[r])throw Error("Unable to resolve tag reference "+r+".");return d={Zb:a[2],index:r};case "zb":var u=dc({"function":a[1],arg0:a[2],arg1:a[3],ignore_case:a[5]},b,c);a[4]&&(u=!u);return u;default:throw Error("Attempting to expand unknown Value type: "+a[0]+".");}}return a},dc=function(a,b,c){try{return Vb(cc(a,b,c))}catch(d){JSON.stringify(a)}return null};var ec=null,hc=function(a){function b(a){for(var b=0;b<a.length;b++)d[a[b]]=!0}var c=[],d=[];ec=fc(a);for(var e=0;e<Qb.length;e++){var f=Qb[e],h=gc(f);if(h){for(var k=f.add||[],l=0;l<k.length;l++)c[k[l]]=!0;b(f.block||[])}else null===h&&b(f.block||[])}var m=[];for(e=0;e<Sb.length;e++)c[e]&&!d[e]&&(m[e]=!0);return m},gc=function(a){for(var b=a["if"]||[],c=0;c<b.length;c++){var d=ec(b[c]);if(!d)return null===d?null:!1}var e=a.unless||[];for(c=0;c<e.length;c++){d=ec(e[c]);if(null===d)return null;if(d)return!1}return!0};
var fc=function(a){var b=[];return function(c){void 0===b[c]&&(b[c]=dc(Rb[c],a));return b[c]}};/*
 Copyright (c) 2014 Derek Brans, MIT license https://github.com/krux/postscribe/blob/master/LICENSE. Portions derived from simplehtmlparser, which is licensed under the Apache License, Version 2.0 */
var kc={},lc=null;kc.o="GTM-56QKB5";var mc=null,nc="//www.googletagmanager.com/a?id="+kc.o+"&cv=139",oc={},pc={},qc=A.currentScript?A.currentScript.src:void 0;var rc=function(){},sc=function(a){return"function"==typeof a},tc=function(a){return"string"==wa(a)},uc=function(a){return"number"==wa(a)&&!isNaN(a)},vc=function(a){return Math.round(Number(a))||0},wc=function(a){return"false"==String(a).toLowerCase()?!1:!!a},xc=function(a){var b=[];if(qa(a))for(var c=0;c<a.length;c++)b.push(String(a[c]));return b},yc=function(a){return a?a.replace(/^\s+|\s+$/g,""):""},zc=function(a,b){if(!uc(a)||!uc(b)||a>b)a=0,b=2147483647;return Math.floor(Math.random()*(b-a+1)+
a)},Ac=function(){this.prefix="gtm.";this.values={}};Ac.prototype.set=function(a,b){this.values[this.prefix+a]=b};Ac.prototype.get=function(a){return this.values[this.prefix+a]};Ac.prototype.contains=function(a){return void 0!==this.get(a)};var Bc=function(){var a=lc.sequence||0;lc.sequence=a+1;return a},Cc=function(a,b,c){return a&&a.hasOwnProperty(b)?a[b]:c},Dc=function(a){var b=!1;return function(){if(!b)try{a()}catch(c){}b=!0}};var O=function(){var a=function(a){return{toString:function(){return a}}};return{Ib:a("convert_case_to"),Jb:a("convert_false_to"),Kb:a("convert_null_to"),Lb:a("convert_true_to"),Mb:a("convert_undefined_to"),L:a("function"),Ac:a("instance_name"),Bc:a("live_only"),Cc:a("malware_disabled"),Dc:a("once_per_event"),Ob:a("once_per_load"),Pb:a("setup_tags"),Ec:a("tag_id"),Qb:a("teardown_tags")}}();var Ec=new Ac,Fc={},Ic={set:function(a,b){za(Gc(a,b),Fc)},get:function(a){return Hc(a,2)},reset:function(){Ec=new Ac;Fc={}}},Hc=function(a,b){return 2!=b?Ec.get(a):Jc(a)},Jc=function(a,b,c){var d=a.split(".");return Lc(d)},Lc=function(a){for(var b=Fc,c=0;c<a.length;c++){if(null===
b)return!1;if(void 0===b)break;b=b[a[c]]}return b};
var Nc=function(a,b){Ec.set(a,b);za(Gc(a,b),Fc)},Gc=function(a,b){for(var c={},d=c,e=a.split("."),f=0;f<e.length-1;f++)d=d[e[f]]={};d[e[e.length-1]]=b;return c};var Oc=new RegExp(/^(.*\.)?(google|youtube|blogger|withgoogle)(\.com?)?(\.[a-z]{2})?\.?$/),Pc={customPixels:["nonGooglePixels"],html:["customScripts","customPixels","nonGooglePixels","nonGoogleScripts","nonGoogleIframes"],customScripts:["html","customPixels","nonGooglePixels","nonGoogleScripts","nonGoogleIframes"],nonGooglePixels:[],nonGoogleScripts:["nonGooglePixels"],nonGoogleIframes:["nonGooglePixels"]},Qc={customPixels:["customScripts","html"],html:["customScripts"],customScripts:["html"],nonGooglePixels:["customPixels",
"customScripts","html","nonGoogleScripts","nonGoogleIframes"],nonGoogleScripts:["customScripts","html"],nonGoogleIframes:["customScripts","html","nonGoogleScripts"]},Rc=function(a,b){for(var c=[],d=0;d<a.length;d++)c.push(a[d]),c.push.apply(c,b[a[d]]||[]);return c};
var Sc=function(a){var b=Hc("gtm.whitelist");var c=b&&Rc(xc(b),Pc),d=Hc("gtm.blacklist")||Hc("tagTypeBlacklist")||[];
Oc.test(y.location&&y.location.hostname)&&(d=xc(d),d.push("nonGooglePixels","nonGoogleScripts"));var e=d&&Rc(xc(d),Qc),f={};return function(h){var k=h&&h[O.L];if(!k||"string"!=typeof k)return!0;k=k.replace(/^_*/,"");if(void 0!==f[k])return f[k];var l=pc[k]||[],m=a(k);if(b){var n;if(n=m)a:{if(0>ra(c,k))if(l&&0<l.length)for(var p=0;p<l.length;p++){if(0>ra(c,l[p])){n=!1;break a}}else{n=!1;break a}n=!0}m=n}var q=!1;if(d){var r;if(!(r=0<=
ra(e,k)))a:{for(var u=l||[],t=new Ac,z=0;z<e.length;z++)t.set(e[z],!0);for(z=0;z<u.length;z++)if(t.get(u[z])){r=!0;break a}r=!1}q=r}return f[k]=!m||q}};var Vc={hd:function(a,b){b[O.Ib]&&"string"===typeof a&&(a=1==b[O.Ib]?a.toLowerCase():a.toUpperCase());b.hasOwnProperty(O.Kb)&&null===a&&(a=b[O.Kb]);b.hasOwnProperty(O.Mb)&&void 0===a&&(a=b[O.Mb]);b.hasOwnProperty(O.Lb)&&!0===a&&(a=b[O.Lb]);b.hasOwnProperty(O.Jb)&&!1===a&&(a=b[O.Jb]);return a}};var Wc=function(a){var b=lc.zones;!b&&a&&(b=lc.zones=a());return b},Xc={active:!0,isWhitelisted:function(){return!0}};var Yc=!1,Zc=0,$c=[];function ad(a){if(!Yc){var b=A.createEventObject,c="complete"==A.readyState,d="interactive"==A.readyState;if(!a||"readystatechange"!=a.type||c||!b&&d){Yc=!0;for(var e=0;e<$c.length;e++)D($c[e])}$c.push=function(){for(var a=0;a<arguments.length;a++)D(arguments[a]);return 0}}}function bd(){if(!Yc&&140>Zc){Zc++;try{A.documentElement.doScroll("left"),ad()}catch(a){y.setTimeout(bd,50)}}}var cd=function(a){Yc?a():$c.push(a)};var dd=!1,ed=function(){return y.GoogleAnalyticsObject&&y[y.GoogleAnalyticsObject]};var fd=function(a){y.GoogleAnalyticsObject||(y.GoogleAnalyticsObject=a||"ga");var b=y.GoogleAnalyticsObject;if(!y[b]){var c=function(){c.q=c.q||[];c.q.push(arguments)};c.l=Number(new Date);y[b]=c}return y[b]},gd=function(a,b,c,d){b=String(b).replace(/\s+/g,"").split(",");var e=ed();e(a+"require","linker");e(a+"linker:autoLink",b,c,d)};
var kd=function(){return"&tc="+Sb.filter(function(a){return a}).length},ld="0.005000">Math.random(),md=function(){var a=0,b=0;return{Nd:function(){if(2>a)return!1;1E3<=(new Date).getTime()-b&&(a=0);return 2<=a},ie:function(){1E3<=(new Date).getTime()-b&&(a=0);a++;b=(new Date).getTime()}}},nd="",od=function(){nd=[nc,"&v=3&t=t","&pid="+zc(),"&rv=86"].join("")},pd={},qd="",rd=void 0,sd={},td={},ud=void 0,vd=null,wd=1E3,xd=function(){var a=rd;return void 0===a?"":[nd,
pd[a]?"":"&es=1",sd[a],kd(),qd,"&z=0"].join("")},yd=function(){ud&&(y.clearTimeout(ud),ud=void 0);void 0===rd||pd[rd]&&!qd||(td[rd]||vd.Nd()||0>=wd--?td[rd]=!0:(vd.ie(),Qa(xd()),pd[rd]=!0,qd=""))},zd=function(a,b,c){if(ld&&!td[a]&&b){a!==rd&&(yd(),rd=a);var d=c+String(b[O.L]||"").replace(/_/g,"");qd=qd?qd+"."+d:"&tr="+d;ud||(ud=y.setTimeout(yd,500));2022<=xd().length&&yd()}};function Ad(a,b,c,d,e,f){var h=Sb[a],k=Bd(a,b,c,d,e,f);if(!k)return null;var l=bc(h[O.Pb],f.Y,[]);if(l&&l.length){var m=l[0];k=Ad(m.index,b,k,1===m.Zb?e:k,e,f)}return k}
function Bd(a,b,c,d,e,f){function h(){var b=cc(k,f.Y);b.vtp_gtmOnSuccess=function(){zd(f.id,Sb[a],"5");c()};b.vtp_gtmOnFailure=function(){zd(f.id,Sb[a],"6");d()};b.vtp_gtmTagId=k.tag_id;if(k[O.Cc])d();else{zd(f.id,k,"1");try{Yb(b)}catch(z){zd(f.id,
k,"7");e()}}}var k=Sb[a];if(f.Y(k))return null;var l=bc(k[O.Qb],f.Y,[]);if(l&&l.length){var m=l[0],n=Ad(m.index,b,c,d,e,f);if(!n)return null;c=n;d=2===m.Zb?e:n}if(k[O.Ob]||k[O.Dc]){var p=k[O.Ob]?Tb:b,q=c,r=d;if(!p[a]){h=Dc(h);var u=Cd(a,p,h);c=u.S;d=u.ka}return function(){p[a](q,r)}}return h}function Cd(a,b,c){var d=[],e=[];b[a]=Dd(d,e,c);return{S:function(){b[a]=Ed;for(var c=0;c<d.length;c++)d[c]()},ka:function(){b[a]=Fd;for(var c=0;c<e.length;c++)e[c]()}}}
function Dd(a,b,c){return function(d,e){a.push(d);b.push(e);c()}}function Ed(a){a()}function Fd(a,b){b()};function Gd(a){var b=0,c=0,d=!1;return{add:function(){c++;return Dc(function(){b++;d&&b>=c&&a()})},Pc:function(){d=!0;b>=c&&a()}}}function Hd(a,b){if(!ld)return;var c=function(a){var d=b.Y(Sb[a])?"3":"4",f=bc(Sb[a][O.Pb],b.Y,[]);f&&f.length&&c(f[0].index);zd(b.id,Sb[a],d);var h=bc(Sb[a][O.Qb],b.Y,[]);h&&h.length&&c(h[0].index)};c(a);}var Id=!1;var Jd=function(a,b){var c={};c[O.L]="__"+a;for(var d in b)b.hasOwnProperty(d)&&(c["vtp_"+d]=b[d]);for(d in void 0)(void 0).hasOwnProperty(d)&&(c[d]=(void 0)[d]);Sb.push(c);return Sb.length-1};var Kd="allow_ad_personalization_signals cookie_domain cookie_expires cookie_name cookie_path custom_params event_callback event_timeout groups send_to send_page_view session_duration user_properties".split(" ");var Ld=/[A-Z]+/,Md=/\s/,Nd=function(a){if(tc(a)&&(a=a.trim(),!Md.test(a))){var b=a.indexOf("-");if(!(0>b)){var c=a.substring(0,b);if(Ld.test(c)){for(var d=a.substring(b+1).split("/"),e=0;e<d.length;e++)if(!d[e])return;return{id:a,prefix:c,containerId:c+"-"+d[0],ia:d}}}}};var Od=null,Pd={},Qd={},Rd;function Sd(){Od=Od||!lc.gtagRegistered;lc.gtagRegistered=!0;return Od}var Td=function(a,b){var c={event:a};b&&(c.eventModel=za(b,void 0),b.event_callback&&(c.eventCallback=b.event_callback),b.event_timeout&&(c.eventTimeout=b.event_timeout));return c};
function Ud(a){if(void 0===Qd[a.id]){var b;if("UA"==a.prefix)b=Jd("gtagua",{trackingId:a.id});else if("AW"==a.prefix)b=Jd("gtagaw",{conversionId:a});else if("DC"==a.prefix)b=Jd("gtagfl",{targetId:a.id});else if("GF"==a.prefix)b=Jd("gtaggf",{conversionId:a});else if("G"==a.prefix)b=Jd("get",{trackingId:a.id,isAutoTag:!0});else return;if(!Rd){var c={name:"send_to",dataLayerVersion:2},d={};d[O.L]="__v";for(var e in c)c.hasOwnProperty(e)&&(d["vtp_"+e]=c[e]);Pb.push(d);Rd=["macro",Pb.length-1]}var f={arg0:Rd,
arg1:a.id,ignore_case:!1};f[O.L]="_lc";Rb.push(f);var h={"if":[Rb.length-1],add:[b]};h["if"]&&(h.add||h.block)&&Qb.push(h);Qd[a.id]=b}}
var Wd={event:function(a){var b=a[1];if(tc(b)&&!(3<a.length)){var c;if(2<a.length){if(!ya(a[2]))return;c=a[2]}var d=Td(b,c);return d}},set:function(a){var b;2==a.length&&ya(a[1])?
b=za(a[1],void 0):3==a.length&&tc(a[1])&&(b={},b[a[1]]=a[2]);if(b)return b.eventModel=za(b,void 0),b.event="gtag.set",b._clear=!0,b},js:function(a){if(2==a.length&&a[1].getTime)return{event:"gtm.js","gtm.start":a[1].getTime()}},config:function(a){}},Vd=Dc(function(){});var Xd=!1,Yd=[];function Zd(){if(!Xd){Xd=!0;for(var a=0;a<Yd.length;a++)D(Yd[a])}};var $d=[],ae=!1,ge=function(a){var b=a.eventCallback,c=Dc(function(){sc(b)&&D(function(){b(kc.o)})}),d=a.eventTimeout;d&&y.setTimeout(c,Number(d));return c},he=function(){for(var a=!1;!ae&&0<$d.length;){ae=!0;delete Fc.eventModel;var b=$d.shift();if(sc(b))try{b.call(Ic)}catch(be){}else if(qa(b)){var c=b;if(tc(c[0])){var d=c[0].split("."),e=d.pop(),f=c.slice(1),h=Hc(d.join("."),2);if(void 0!==h&&null!==h)try{h[e].apply(h,f)}catch(be){}}}else{var k=b;if(k&&("[object Arguments]"==Object.prototype.toString.call(k)||
Object.prototype.hasOwnProperty.call(k,"callee"))){a:{var l=b;if(l.length&&tc(l[0])){var m=Wd[l[0]];if(m){b=m(l);break a}}b=void 0}if(!b){ae=!1;continue}}var n;var p=void 0,q=b,r=q._clear;for(p in q)q.hasOwnProperty(p)&&"_clear"!==p&&(r&&Nc(p,void 0),Nc(p,q[p]));var u=q.event;if(u){var t=q["gtm.uniqueEventId"];t||(t=Bc(),q["gtm.uniqueEventId"]=t,Nc("gtm.uniqueEventId",t));mc=u;var z;var I,H,C=q,P=C.event,E=C["gtm.uniqueEventId"],L=lc.zones;H=L?L.checkState(kc.o,E):Xc;if(H.active){var F=ge(C);c:{var K=
H.isWhitelisted;if("gtm.js"==P){if(Id){I=!1;break c}Id=!0}var M=E,ia=P;if(ld&&!td[M]&&rd!==M){yd();rd=M;qd="";var J=sd,R=M,S,Q=ia;S=0===Q.indexOf("gtm.")?encodeURIComponent(Q):"*";J[R]="&e="+S+"&eid="+M;ud||(ud=y.setTimeout(yd,500))}var T=Sc(K),X={id:E,name:P,ad:F||rc,Y:T,Wa:hc(T)};for(var Tc,Zb=X,ce=Gd(Zb.ad),Qf=[],$b=[],mb=0;mb<Sb.length;mb++)if(Zb.Wa[mb]){var Rf=Sb[mb];var Db=ce.add();try{var de=Ad(mb,Qf,Db,Db,Db,Zb);de?$b.push(de):(Hd(mb,Zb),Db())}catch(be){Db()}}ce.Pc();for(var Uc=0;Uc<$b.length;Uc++)$b[Uc]();Tc=0<$b.length;if("gtm.js"===P||"gtm.sync"===P)d:{}if(Tc){for(var Sf={__cl:!0,__evl:!0,__fsl:!0,__hl:!0,__jel:!0,__lcl:!0,__sdl:!0,__tl:!0,__ytl:!0},ac=0;ac<X.Wa.length;ac++)if(X.Wa[ac]){var fe=Sb[ac];if(fe&&!Sf[fe[O.L]]){I=!0;break c}}I=!1}else I=Tc}z=I?!0:!1}else z=!1;mc=null;n=z}else n=!1;a=n||a}ae=!1}return!a},ie=function(){var a=he();try{var b=y["dataLayer"].hide;if(b&&void 0!==b[kc.o]&&b.end){b[kc.o]=!1;var c=!0,d;for(d in b)if(b.hasOwnProperty(d)&&!0===
b[d]){c=!1;break}c&&(b.end(),b.end=null)}}catch(e){}return a},je=function(){var a=Na("dataLayer",[]),b=Na("google_tag_manager",{});b=b["dataLayer"]=b["dataLayer"]||{};$c.push(function(){b.gtmDom||(b.gtmDom=!0,a.push({event:"gtm.dom"}))});Yd.push(function(){b.gtmLoad||(b.gtmLoad=!0,a.push({event:"gtm.load"}))});var c=a.push;a.push=function(){var b=[].slice.call(arguments,0);c.apply(a,b);for($d.push.apply($d,b);300<this.length;)this.shift();return he()};$d.push.apply($d,a.slice(0));D(ie)};var ke={};ke.Ia=new String("undefined");ke.ab={};var le=function(a){this.resolve=function(b){for(var c=[],d=0;d<a.length;d++)c.push(a[d]===ke.Ia?b:a[d]);return c.join("")}};le.prototype.toString=function(){return this.resolve("undefined")};le.prototype.valueOf=le.prototype.toString;ke.jd=function(a){return new le(a)};var me={};ke.je=function(a,b){var c=Bc();me[c]=[a,b];return c};ke.Vb=function(a){var b=a?0:1;return function(a){var c=me[a];if(c&&"function"===typeof c[b])c[b]();me[a]=void 0}};
ke.Md=function(a){for(var b=!1,c=!1,d=2;d<a.length;d++)b=b||8===a[d],c=c||16===a[d];return b&&c};ke.ae=function(a){if(a===ke.Ia)return a;var b=Bc();ke.ab[b]=a;return'google_tag_manager["'+kc.o+'"].macro('+b+")"};ke.Fc=le;var ne=new Ac,oe=function(a,b){function c(a){var b=N(a),c=G(b,"protocol"),d=G(b,"host",!0),e=G(b,"port"),f=G(b,"path").toLowerCase().replace(/\/$/,"");if(void 0===c||"http"==c&&"80"==e||"https"==c&&"443"==e)c="web",e="default";return[c,d,e,f]}for(var d=c(String(a)),e=c(String(b)),f=0;f<d.length;f++)if(d[f]!==e[f])return!1;return!0};
function pe(a){var b=a.arg0,c=a.arg1;switch(a["function"]){case "_cn":return 0<=String(b).indexOf(String(c));case "_css":var d;a:{if(b){var e=["matches","webkitMatchesSelector","mozMatchesSelector","msMatchesSelector","oMatchesSelector"];try{for(var f=0;f<e.length;f++)if(b[e[f]]){d=b[e[f]](c);break a}}catch(u){}}d=!1}return d;case "_ew":var h,k;h=String(b);k=String(c);var l=h.length-k.length;return 0<=l&&h.indexOf(k,l)==l;case "_eq":return String(b)==String(c);case "_ge":return Number(b)>=Number(c);
case "_gt":return Number(b)>Number(c);case "_lc":var m;m=String(b).split(",");return 0<=ra(m,String(c));case "_le":return Number(b)<=Number(c);case "_lt":return Number(b)<Number(c);case "_re":var n;var p=a.ignore_case?"i":void 0;try{var q=String(c)+p,r=ne.get(q);r||(r=new RegExp(c,p),ne.set(q,r));n=r.test(b)}catch(u){n=!1}return n;case "_sw":return 0==String(b).indexOf(String(c));case "_um":return oe(b,c)}return!1};function qe(a,b,c,d){return(d||"https:"==y.location.protocol?a:b)+c}function re(a,b){for(var c=b||(a instanceof g?new g:new ua),d=a.R(),e=0;e<d.length();e++){var f=d.get(e);if(a.has(f)){var h=a.get(f);h instanceof g?(c.get(f)instanceof g||c.set(f,new g),re(h,c.get(f))):h instanceof ua?(c.get(f)instanceof ua||c.set(f,new ua),re(h,c.get(f))):c.set(f,h)}}return c}function se(){return kc.o}function te(){return(new Date).getTime()}function ue(a,b){return Ba(Hc(a,b||2))}function ve(){return mc}
function we(a){return Xa('<a href="'+a+'"></a>')[0].href}function xe(a){return vc(Aa(a))}function ye(a){return null===a?"null":void 0===a?"undefined":a.toString()}function ze(a,b){return zc(a,b)}function Ae(a,b,c){if(!(a instanceof g))return null;for(var d=new ua,e=!1,f=0;f<a.length();f++){var h=a.get(f);h instanceof ua&&h.has(b)&&h.has(c)&&(d.set(h.get(b),h.get(c)),e=!0)}return e?d:null}
var Be=function(){var a=new La;a.addAll(ab());a.addAll({buildSafeUrl:qe,decodeHtmlUrl:we,copy:re,generateUniqueNumber:Bc,getContainerId:se,getCurrentTime:te,getDataLayerValue:ue,getEventName:ve,makeInteger:xe,makeString:ye,randomInteger:ze,tableToMap:Ae});return function(b){return a.get(b)}};var Ce=new cb,De=function(){var a=data.runtime||[];Ob=function(a,b){var c=new ua,d;for(d in b)b.hasOwnProperty(d)&&c.set(d,Ba(b[d]));var e=Ce.O([a,c]);e instanceof ha&&"return"===e.w&&(e=e.getData());return Aa(e)};Vb=pe;bb(Ce,Be());for(var b=0;b<a.length;b++){var c=a[b];if(!qa(c)||3>c.length){if(0==c.length)continue;break}Ce.O(c)}};var Ee=function(a,b){var c=function(){};c.prototype=a.prototype;var d=new c;a.apply(d,Array.prototype.slice.call(arguments,1));return d};var Fe=function(a){return encodeURIComponent(a)},Ge=function(a){var b=["veinteractive.com","ve-interactive.cn"];if(!a)return!1;var c=G(N(a),"host");if(!c)return!1;for(var d=0;b&&d<b.length;d++){var e=b[d]&&b[d].toLowerCase();if(e){var f=c.length-e.length;0<f&&"."!=e.charAt(0)&&(f--,e="."+e);if(0<=f&&c.indexOf(e,f)==f)return!0}}return!1};
var U=function(a,b,c){for(var d={},e=!1,f=0;a&&f<a.length;f++)a[f]&&a[f].hasOwnProperty(b)&&a[f].hasOwnProperty(c)&&(d[a[f][b]]=a[f][c],e=!0);return e?d:null},He=function(a,b){za(a,b)},Ie=function(a){return vc(a)},Je=function(a,b){return ra(a,b)};var Ke=function(a){var b={"gtm.element":a,"gtm.elementClasses":a.className,"gtm.elementId":a["for"]||Ta(a,"id")||"","gtm.elementTarget":a.formTarget||a.target||""};b["gtm.elementUrl"]=(a.attributes&&a.attributes.formaction?a.formAction:"")||a.action||a.href||a.src||a.code||a.codebase||"";return b},Le=function(a){lc.hasOwnProperty("autoEventsSettings")||(lc.autoEventsSettings={});var b=lc.autoEventsSettings;b.hasOwnProperty(a)||(b[a]={});return b[a]},Me=function(a,b,c,d){var e=Le(a),f=Cc(e,b,d);e[b]=
c(f)},Ne=function(a,b,c){var d=Le(a);return Cc(d,b,c)};var Oe=/(^|\.)doubleclick\.net$/i,Pe=/^(www\.)?google(\.com?)?(\.[a-z]{2})?$/,Qe=function(a,b,c){for(var d=String(b||A.cookie).split(";"),e=[],f=0;f<d.length;f++){var h=d[f].split("="),k=yc(h[0]);if(k&&k==a){var l=yc(h.slice(1).join("="));l&&!1!==c&&(l=decodeURIComponent(l));e.push(l)}}return e},Re=function(a,b,c,d,e,f){f&&(b=encodeURIComponent(b));var h=a+"="+b+"; ";c&&(h+="path="+c+"; ");e&&(h+="expires="+e.toGMTString()+"; ");var k,l;if("auto"==d){var m=G(y.location,"host",!0).split(".");if(4==
m.length&&/^[0-9]*$/.exec(m[3]))l=["none"];else{for(var n=[],p=m.length-2;0<=p;p--)n.push(m.slice(p).join("."));n.push("none");l=n}}else l=[d||"none"];k=l;for(var q=A.cookie,r=0;r<k.length;r++){var u=h,t=k[r],z=c;if(Oe.test(y.location.hostname)||"/"==z&&Pe.test(t))break;"none"!=k[r]&&(u+="domain="+k[r]+";");A.cookie=u;if(q!=A.cookie||0<=ra(Qe(a),b))break}};var Se=!1;if(A.querySelectorAll)try{var Te=A.querySelectorAll(":root");Te&&1==Te.length&&Te[0]==A.documentElement&&(Se=!0)}catch(a){}var Ue=Se;var Ve=function(a){for(var b=[],c=document.cookie.split(";"),d=new RegExp("^\\s*"+a+"=\\s*(.*?)\\s*$"),e=0;e<c.length;e++){var f=c[e].match(d);f&&b.push(f[1])}return b},Ye=function(a,b,c,d){var e=We(a,d);if(1===e.length)return e[0].id;if(0!==e.length){e=Xe(e,function(a){return a.rd},b);if(1===e.length)return e[0].id;e=Xe(e,function(a){return a.Zd},c);return e[0]?e[0].id:void 0}},af=function(a,b,c,d,e){c=void 0===c?"/":c;var f=d=void 0===d?"auto":d,h=c;if(Ze.test(document.location.hostname)||"/"===
h&&$e.test(f))return!1;var k=b;k&&1200<k.length&&(k=k.substring(0,1200));b=k;var l=a+"="+b+"; path="+c+"; ";void 0!==e&&(l+="expires="+(new Date((new Date).getTime()+e)).toGMTString()+"; ");if("auto"===d){var m=!1,n;a:{var p=[],q=document.location.hostname.split(".");if(4===q.length){var r=q[q.length-1];if(parseInt(r,10).toString()===r){n=["none"];break a}}for(var u=q.length-2;0<=u;u--)p.push(q.slice(u).join("."));p.push("none");n=p}for(var t=n,z=0;z<t.length&&!m;z++)m=af(a,b,c,t[z],e);return m}d&&
"none"!==d&&(l+="domain="+d+";");var I=document.cookie;document.cookie=l;return I!=document.cookie||0<=Ve(a).indexOf(b)};function Xe(a,b,c){for(var d=[],e=[],f,h=0;h<a.length;h++){var k=a[h],l=b(k);l===c?d.push(k):void 0===f||l<f?(e=[k],f=l):l===f&&e.push(k)}return 0<d.length?d:e}function We(a,b){for(var c=[],d=Ve(a),e=0;e<d.length;e++){var f=d[e].split("."),h=f.shift();if(!b||-1!==b.indexOf(h)){var k=f.shift();k&&(k=k.split("-"),c.push({id:f.join("."),rd:1*k[0]||1,Zd:1*k[1]||1}))}}return c}
var $e=/^(www\.)?google(\.com?)?(\.[a-z]{2})?$/,Ze=/(^|\.)doubleclick\.net$/i;var bf=window,cf=document;var ff=function(a){for(var b=bf.navigator.userAgent+(cf.cookie||"")+(cf.referrer||""),c=b.length,d=bf.history.length;0<d;)b+=d--^c++;var e=1,f,h,k;if(b)for(e=0,h=b.length-1;0<=h;h--)k=b.charCodeAt(h),e=(e<<6&268435455)+k+(k<<14),f=e&266338304,e=0!=f?e^f>>21:e;var l=[Math.round(2147483647*Math.random())^e&2147483647,Math.round(Date.now()/1E3)].join("."),m=""+df(void 0),n=ef(void 0);1<n&&(m+="-"+n);return[a,m,l].join(".")},gf=function(a,b,c,d){var e=df(b);return Ye(a,e,ef(c),d)};
function df(a){if(!a)return 1;a=0===a.indexOf(".")?a.substr(1):a;return a.split(".").length}function ef(a){if(!a||"/"===a)return 1;"/"!==a[0]&&(a="/"+a);"/"!==a[a.length-1]&&(a+="/");return a.split("/").length-1};var hf=["1"],jf={},mf=function(a,b,c){b=void 0===b?"auto":b;c=void 0===c?"/":c;var d=kf(void 0===a?"_gcl":a);if(!jf[d]&&!lf(d,b,c)){var e=ff("1");af(d,e,c,b,7776E6);lf(d,b,c)}};function lf(a,b,c){var d=gf(a,b,c,hf);d&&(jf[a]=d);return d}function kf(a){return(void 0===a?"_gcl":a)+"_au"};var nf=function(a){for(var b=[],c=A.cookie.split(";"),d=new RegExp("^\\s*"+a+"=\\s*(.*?)\\s*$"),e=0;e<c.length;e++){var f=c[e].match(d);f&&b.push(f[1])}var h=[];if(!b||0==b.length)return h;for(var k=0;k<b.length;k++){var l=b[k].split(".");3==l.length&&"GCL"==l[0]&&l[1]&&h.push(l[2])}return h};var of=/^\w+$/,pf=/^[\w-]+$/,qf=/^\d+\.fls\.doubleclick\.net$/;function rf(a){return a&&"string"==typeof a&&a.match(of)?a:"_gcl"}function sf(a){if(a){if("string"==typeof a){var b=rf(a);return{va:b,sa:b,ya:b}}if(a&&"object"==typeof a)return{va:rf(a.dc),sa:rf(a.aw),ya:rf(a.gf)}}return{va:"_gcl",sa:"_gcl",ya:"_gcl"}}function tf(a){var b=N(y.location.href),c=G(b,"host",!1);if(c&&c.match(qf)){var d=G(b,"path").split(a+"=");if(1<d.length)return d[1].split(";")[0].split("?")[0]}}
function uf(a){return a.filter(function(a){return pf.test(a)})}var wf=function(a){var b=tf("gclaw");if(b)return b.split(".");var c=sf(a);if("_gcl"==c.sa){var d=vf();if(d&&(null==d.F||"aw.ds"==d.F))return[d.X]}return uf(nf(c.sa+"_aw"))},xf=function(a){var b=tf("gcldc");if(b)return b.split(".");var c=sf(a);if("_gcl"==c.va){var d=vf();if(d&&("ds"==d.F||"aw.ds"==d.F))return[d.X]}return uf(nf(c.va+"_dc"))};
function vf(){var a=N(y.location.href),b=G(a,"query",!1,void 0,"gclid"),c=G(a,"query",!1,void 0,"gclsrc");if(!b||!c){var d=G(a,"fragment");b=b||Za(d,"gclid");c=c||Za(d,"gclsrc")}return void 0!==b&&b.match(pf)?{X:b,F:c}:null}
var yf=function(){var a=tf("gac");if(a)return decodeURIComponent(a);for(var b=[],c=A.cookie.split(";"),d=/^\s*_gac_(UA-\d+-\d+)=\s*(.+?)\s*$/,e=0;e<c.length;e++){var f=c[e].match(d);f&&b.push({Ab:f[1],value:f[2]})}var h={};if(b&&b.length)for(var k=0;k<b.length;k++){var l=b[k].value.split(".");"1"==l[0]&&3==l.length&&l[1]&&(h[b[k].Ab]||(h[b[k].Ab]=[]),h[b[k].Ab].push({timestamp:l[1],X:l[2]}))}var m=[],n;for(n in h)if(h.hasOwnProperty(n)){for(var p=[],q=h[n],r=0;r<q.length;r++)p.push(q[r].X);p=uf(p);
p.length&&m.push(n+":"+p.join(","))}return m.join(";")},zf=function(a,b,c){};var Af;a:{Af="G"}var Bf={"":"n",UA:"u",AW:"a",DC:"d",G:"e",GTM:Af},Cf=function(a){var b=kc.o.split("-"),c=b[0].toUpperCase();return(Bf[c]||"i")+"86"+(a&&"GTM"===c?b[1]:"")};var Jf=!!y.MutationObserver,Kf=void 0,Lf=function(a){if(!Kf){var b=function(){var a=A.body;if(a)if(Jf)(new MutationObserver(function(){for(var a=0;a<Kf.length;a++)D(Kf[a])})).observe(a,{childList:!0,subtree:!0});else{var b=!1;Ra(a,"DOMNodeInserted",function(){b||(b=!0,D(function(){b=!1;for(var a=0;a<Kf.length;a++)D(Kf[a])}))})}};Kf=[];A.body?b():D(b)}Kf.push(a)};
var Mf=function(){var a=A.body,b=A.documentElement||a&&a.parentElement,c,d;if(A.compatMode&&"BackCompat"!==A.compatMode)c=b?b.clientHeight:0,d=b?b.clientWidth:0;else{var e=function(a,b){return a&&b?Math.min(a,b):Math.max(a,b)};c=e(b?b.clientHeight:0,a?a.clientHeight:0);d=e(b?b.clientWidth:0,a?a.clientWidth:0)}return{width:d,height:c}},Nf=function(a){var b=Mf(),c=b.height,d=b.width,e=a.getBoundingClientRect(),f=e.bottom-e.top,h=e.right-e.left;return f&&h?(1-Math.min((Math.max(0-e.left,0)+Math.max(e.right-
d,0))/h,1))*(1-Math.min((Math.max(0-e.top,0)+Math.max(e.bottom-c,0))/f,1)):0},Of=function(a){if(A.hidden)return!0;var b=a.getBoundingClientRect();if(b.top==b.bottom||b.left==b.right||!y.getComputedStyle)return!0;var c=y.getComputedStyle(a,null);if("hidden"===c.visibility)return!0;for(var d=a,e=c;d;){if("none"===e.display)return!0;var f=e.opacity,h=e.filter;if(h){var k=h.indexOf("opacity(");0<=k&&(h=h.substring(k+8,h.indexOf(")",k)),"%"==h.charAt(h.length-1)&&(h=h.substring(0,h.length-1)),f=Math.min(h,
f))}if(void 0!==f&&0>=f)return!0;(d=d.parentElement)&&(e=y.getComputedStyle(d,null))}return!1};var Pf=[],Tf=!(!y.IntersectionObserver||!y.IntersectionObserverEntry),Uf=function(a,b,c){for(var d=new y.IntersectionObserver(a,{threshold:c}),e=0;e<b.length;e++)d.observe(b[e]);for(var f=0;f<Pf.length;f++)if(!Pf[f])return Pf[f]=d,f;return Pf.push(d)-1},Vf=function(a,b,c){function d(b,c){var d={top:0,bottom:0,right:0,left:0,width:0,height:0},e={boundingClientRect:b.getBoundingClientRect(),
intersectionRatio:c,intersectionRect:d,isIntersecting:0<c,rootBounds:d,target:b,time:(new Date).getTime()};D(function(){return a(e)})}for(var e=[],f=[],h=0;h<b.length;h++)e.push(0),f.push(-1);c.sort(function(a,b){return a-b});return function(){for(var a=0;a<b.length;a++){var h=Nf(b[a]);if(h>e[a])for(;f[a]<c.length-1&&h>=c[f[a]+1];)d(b[a],h),f[a]++;else if(h<e[a])for(;0<=f[a]&&h<=c[f[a]];)d(b[a],h),f[a]--;e[a]=h}}},Wf=function(a,b,c){for(var d=0;d<c.length;d++)1<c[d]?c[d]=1:0>c[d]&&(c[d]=0);if(Tf){var e=
!1;D(function(){e||Vf(a,b,c)()});return Uf(function(b){e=!0;for(var c={Aa:0};c.Aa<b.length;c={Aa:c.Aa},c.Aa++)D(function(c){return function(){return a(b[c.Aa])}}(c))},b,c)}return y.setInterval(Vf(a,b,c),1E3)};var Yf="www.googletagmanager.com/gtm.js";
var Zf=Yf,$f=function(a,b,c,d){Ra(a,b,c,d)},ag=function(a,b){return y.setTimeout(a,b)},bg=function(a,b,c){B(a,b,c)},cg=function(){return y.location.href},dg=function(a){return G(N(a),"fragment")},V=function(a,b){return Hc(a,b||2)},eg=function(a,b,c){b&&(a.eventCallback=b,c&&(a.eventTimeout=c));return y["dataLayer"].push(a)},fg=function(a,b){y[a]=b},W=function(a,b,c){b&&(void 0===y[a]||c&&!y[a])&&(y[a]=b);return y[a]},gg=function(a,b,c){var d=b,e=c,f=sf(a);e=e||"auto";d=d||"/";var h=vf();if(null!=
h){var k=(new Date).getTime(),l=new Date(k+7776E6),m=["GCL",Math.round(k/1E3),h.X].join(".");h.F&&"aw.ds"!=h.F||Re(f.sa+"_aw",m,d,e,l,!0);"aw.ds"!=h.F&&"ds"!=h.F||Re(f.va+"_dc",m,d,e,l,!0);"gf"==h.F&&Re(f.ya+"_gf",m,d,e,l,!0)}},hg=function(a,b){var c;a:{var d;d=100;for(var e={},f=0;f<b.length;f++)e[b[f]]=!0;for(var h=a,k=0;h&&k<=d;k++){if(e[String(h.tagName).toLowerCase()]){c=h;break a}h=h.parentElement}c=null}return c},Y=function(a,b,c,d){var e=!d&&"http:"==y.location.protocol;e&&(e=2!==ig());return(e?
b:a)+c};
var jg=function(a){var b=0;b=Nf(a);return b},kg=function(a){Tf?0<=a&&a<Pf.length&&Pf[a]&&(Pf[a].disconnect(),Pf[a]=void 0):y.clearInterval(a);},lg=function(a){var b=!1;b=Of(a);return b},mg=function(a,b){var c;a:{if(a&&
qa(a))for(var d=0;d<a.length;d++)if(a[d]&&b(a[d])){c=a[d];break a}c=void 0}return c},ng=function(a,b,c,d){Me(a,b,c,d)},og=function(a,b,c){return Ne(a,b,c)},pg=function(a){return!!Ne(a,"init",!1)},qg=function(a){Le(a).init=!0};
var ig=function(){var a=Zf;if(qc){if(0===qc.toLowerCase().indexOf("https://"))return 2;if(0===qc.toLowerCase().indexOf("http://"))return 3}a=a.toLowerCase();for(var b="https://"+a,c="http://"+a,d=1,e=A.getElementsByTagName("script"),f=0;f<e.length&&100>f;f++){var h=e[f].src;if(h){h=h.toLowerCase();if(0===h.indexOf(c))return 3;1===d&&0===h.indexOf(b)&&(d=2)}}return d};var tg=function(a,b,c){var d=(void 0===c?0:c)?"www.googletagmanager.com/gtag/js":Zf;d+="?id="+encodeURIComponent(a)+"&l=dataLayer";if(b)for(var e in b)b[e]&&b.hasOwnProperty(e)&&(d+="&"+e+"="+encodeURIComponent(b[e]));var f=Y("https://","http://",d);B(f,void 0,void 0)};var vg=function(a,b,c){a instanceof ke.Fc&&(a=a.resolve(ke.je(b,c)),b=rc);return{kb:a,S:b}};var wg=function(a,b,c){this.n=a;this.t=b;this.p=c},xg=function(){this.c=1;this.e=[];this.p=null};function yg(a){var b=lc,c=b.gss=b.gss||{};return c[a]=c[a]||new xg}var zg=function(a,b){yg(a).p=b},Ag=function(a,b,c){var d=Math.floor((new Date).getTime()/1E3);yg(a).e.push(new wg(b,d,c))},Bg=function(a){};var Kg=window,Lg=document,Mg=function(a){var b=Kg._gaUserPrefs;if(b&&b.ioo&&b.ioo()||a&&!0===Kg["ga-disable-"+a])return!0;try{var c=Kg.external;if(c&&c._gaUserPrefs&&"oo"==c._gaUserPrefs)return!0}catch(m){}for(var d=[],e=Lg.cookie.split(";"),f=/^\s*AMP_TOKEN=\s*(.*?)\s*$/,h=0;h<e.length;h++){var k=e[h].match(f);k&&d.push(k[1])}for(var l=0;l<d.length;l++)if("$OPT_OUT"==decodeURIComponent(d[l]))return!0;return!1};var Pg=function(a){if(1===yg(a).c){yg(a).c=2;var b=encodeURIComponent(a);B(("http:"!=y.location.protocol?"https:":"http:")+("//www.googletagmanager.com/gtag/js?id="+b+"&l=dataLayer&cx=c"))}},Qg=function(a,b){};var Z={a:{}};
Z.a.jsm=["customScripts"],function(){(function(a){Z.__jsm=a;Z.__jsm.b="jsm";Z.__jsm.g=!0})(function(a){if(void 0!==a.vtp_javascript){var b=a.vtp_javascript;try{var c=W("google_tag_manager");return c&&c.e&&c.e(b)}catch(d){}}})}();Z.a.c=["google"],function(){(function(a){Z.__c=a;Z.__c.b="c";Z.__c.g=!0})(function(a){return a.vtp_value})}();

Z.a.e=["google"],function(){(function(a){Z.__e=a;Z.__e.b="e";Z.__e.g=!0})(function(){return mc})}();Z.a.f=["google"],function(){(function(a){Z.__f=a;Z.__f.b="f";Z.__f.g=!0})(function(a){var b=V("gtm.referrer",1)||A.referrer,c;if(b){var d;if(a.vtp_component&&"URL"!=a.vtp_component){var e=N(String(b));d=G(e,a.vtp_component,a.vtp_stripWww,a.vtp_defaultPages,a.vtp_queryKey)}else d=$a(N(String(b)));c=d}else c=String(b);return c})}();
Z.a.cl=["google"],function(){function a(a){var b=a.target;if(b){var d=Ke(b);d.event="gtm.click";eg(d)}}(function(a){Z.__cl=a;Z.__cl.b="cl";Z.__cl.g=!0})(function(b){if(!pg("cl")){var c=W("document");Ra(c,"click",a,!0);qg("cl");var d=Ne("cl","legacyTeardown",void 0);d&&d()}D(b.vtp_gtmOnSuccess)})}();
Z.a.j=["google"],function(){(function(a){Z.__j=a;Z.__j.b="j";Z.__j.g=!0})(function(a){for(var b=String(a.vtp_name).split("."),c=W(b.shift()),d=0;d<b.length;d++)c=c&&c[b[d]];return c})}();

Z.a.u=["google"],function(){var a=function(a){return{toString:function(){return a}}};(function(a){Z.__u=a;Z.__u.b="u";Z.__u.g=!0})(function(b){var c;c=(c=b.vtp_customUrlSource?b.vtp_customUrlSource:V("gtm.url",1))||cg();var d=b[a("vtp_component")],e;if(d&&"URL"!=d){var f=N(String(c));e=G(f,d,"HOST"==d?b[a("vtp_stripWww")]:void 0,"PATH"==d?b[a("vtp_defaultPages")]:void 0,"QUERY"==d?b[a("vtp_queryKey")]:void 0)}else e=$a(N(String(c)));return e})}();
Z.a.v=["google"],function(){(function(a){Z.__v=a;Z.__v.b="v";Z.__v.g=!0})(function(a){var b=a.vtp_name;if(!b||!b.replace)return!1;var c=V(b.replace(/\\\./g,"."),a.vtp_dataLayerVersion||1);return void 0!==c?c:a.vtp_defaultValue})}();
Z.a.ua=["google"],function(){var a,b=function(b){var c={},e={},f={},h={},k={};if(b.vtp_gaSettings){var l=b.vtp_gaSettings;He(U(l.vtp_fieldsToSet,"fieldName","value"),e);He(U(l.vtp_contentGroup,"index","group"),f);He(U(l.vtp_dimension,"index","dimension"),h);He(U(l.vtp_metric,"index","metric"),k);b.vtp_gaSettings=null;l.vtp_fieldsToSet=void 0;l.vtp_contentGroup=void 0;l.vtp_dimension=void 0;l.vtp_metric=void 0;var m=za(l,void 0);b=za(b,m)}He(U(b.vtp_fieldsToSet,"fieldName","value"),e);He(U(b.vtp_contentGroup,
"index","group"),f);He(U(b.vtp_dimension,"index","dimension"),h);He(U(b.vtp_metric,"index","metric"),k);var n=fd(b.vtp_functionName),p="",q="";b.vtp_setTrackerName&&"string"==typeof b.vtp_trackerName?""!==b.vtp_trackerName&&(q=b.vtp_trackerName,p=q+"."):(q="gtm"+Bc(),p=q+".");var r={name:!0,clientId:!0,sampleRate:!0,siteSpeedSampleRate:!0,alwaysSendReferrer:!0,allowAnchor:!0,allowLinker:!0,cookieName:!0,cookieDomain:!0,cookieExpires:!0,cookiePath:!0,cookieUpdate:!0,legacyCookieDomain:!0,legacyHistoryImport:!0,
storage:!0,useAmpClientId:!0,storeGac:!0},u={allowAnchor:!0,allowLinker:!0,alwaysSendReferrer:!0,anonymizeIp:!0,cookieUpdate:!0,exFatal:!0,forceSSL:!0,javaEnabled:!0,legacyHistoryImport:!0,nonInteraction:!0,useAmpClientId:!0,useBeacon:!0,storeGac:!0,allowAdFeatures:!0},t=function(a){var b=[].slice.call(arguments,0);b[0]=p+b[0];n.apply(window,b)},z=function(a,b){return void 0===b?b:a(b)},I=function(a,b){if(b)for(var c in b)b.hasOwnProperty(c)&&t("set",a+c,b[c])},H=function(){},C=function(a,b,c){var d=0;if(a)for(var e in a)if(a.hasOwnProperty(e)&&(c&&r[e]||!c&&void 0===r[e])){var f=u[e]?wc(a[e]):a[e];"anonymizeIp"!=e||f||(f=void 0);b[e]=f;d++}return d},P={name:q};C(e,P,
!0);n("create",b.vtp_trackingId||c.trackingId,P);t("set","&gtm",Cf(!0));(function(a,c){void 0!==b[c]&&t("set",a,b[c])})("nonInteraction","vtp_nonInteraction");I("contentGroup",f);I("dimension",h);I("metric",k);var E={};C(e,E,!1)&&t("set",E);var L;b.vtp_enableLinkId&&t("require","linkid","linkid.js");t("set","hitCallback",function(){var a=
e&&e.hitCallback;sc(a)&&a();b.vtp_gtmOnSuccess()});if("TRACK_EVENT"==b.vtp_trackType){b.vtp_enableEcommerce&&(t("require","ec","ec.js"),H());var F={hitType:"event",eventCategory:String(b.vtp_eventCategory||c.category),eventAction:String(b.vtp_eventAction||c.action),eventLabel:z(String,b.vtp_eventLabel||c.label),eventValue:z(Ie,b.vtp_eventValue||c.value)};C(L,F,!1);t("send",F);}else if("TRACK_SOCIAL"==
b.vtp_trackType){F={hitType:"social",socialNetwork:String(b.vtp_socialNetwork),socialAction:String(b.vtp_socialAction),socialTarget:String(b.vtp_socialActionTarget)},C(L,F,!1),t("send",F);}else if("TRACK_TRANSACTION"==b.vtp_trackType){}else if("TRACK_TIMING"==b.vtp_trackType){}else if("DECORATE_LINK"==b.vtp_trackType){}else if("DECORATE_FORM"==b.vtp_trackType){}else if("TRACK_DATA"==
b.vtp_trackType){}else{b.vtp_enableEcommerce&&(t("require","ec","ec.js"),H());if(b.vtp_doubleClick||"DISPLAY_FEATURES"==b.vtp_advertisingFeaturesType){var Q="_dc_gtm_"+String(b.vtp_trackingId).replace(/[^A-Za-z0-9-]/g,"");t("require","displayfeatures",void 0,{cookieName:Q})}"DISPLAY_FEATURES_WITH_REMARKETING_LISTS"==b.vtp_advertisingFeaturesType&&
(Q="_dc_gtm_"+String(b.vtp_trackingId).replace(/[^A-Za-z0-9-]/g,""),t("require","adfeatures",{cookieName:Q}));L?t("send","pageview",L):t("send","pageview");b.vtp_autoLinkDomains&&gd(p,b.vtp_autoLinkDomains,!!b.vtp_useHashAutoLink,!!b.vtp_decorateFormsAutoLink);}if(!a){var T=b.vtp_useDebugVersion?"u/analytics_debug.js":"analytics.js";b.vtp_useInternalVersion&&!b.vtp_useDebugVersion&&
(T="internal/"+T);a=!0;bg(Y("https:","http:","//www.google-analytics.com/"+T,e&&e.forceSSL),function(){var a=ed();a&&a.loaded||b.vtp_gtmOnFailure();},b.vtp_gtmOnFailure)}};Z.__ua=b;Z.__ua.b="ua";Z.__ua.g=!0}();

Z.a.aev=["google"],function(){var a=void 0,b="",c=0,d=void 0,e={ATTRIBUTE:"gtm.elementAttribute",CLASSES:"gtm.elementClasses",ELEMENT:"gtm.element",ID:"gtm.elementId",HISTORY_CHANGE_SOURCE:"gtm.historyChangeSource",HISTORY_NEW_STATE:"gtm.newHistoryState",HISTORY_NEW_URL_FRAGMENT:"gtm.newUrlFragment",HISTORY_OLD_STATE:"gtm.oldHistoryState",HISTORY_OLD_URL_FRAGMENT:"gtm.oldUrlFragment",TARGET:"gtm.elementTarget"},f=function(a){var b=V(e[a.vtp_varType],1);return void 0!==b?b:a.vtp_defaultValue};(function(a){Z.__aev=
a;Z.__aev.b="aev";Z.__aev.g=!0})(function(e){switch(e.vtp_varType){case "TAG_NAME":return V("gtm.element",1).tagName||e.vtp_defaultValue;case "TEXT":var h,l=V("gtm.element",1),m=V("event",1),n=Number(new Date);a===l&&b===m&&c>n-250?h=d:(d=h=l?Wa(l):"",a=l,b=m);c=n;return h||e.vtp_defaultValue;case "URL":var p=String(V("gtm.elementUrl",1)||e.vtp_defaultValue||""),q=N(p);return e.vtp_component&&"URL"!=e.vtp_component?G(q,e.vtp_component,e.vtp_stripWww,e.vtp_defaultPages,e.vtp_queryKey):p;case "ATTRIBUTE":var r;
if(void 0===e.vtp_attribute)r=f(e);else{var u=V("gtm.element",1);r=Ta(u,e.vtp_attribute)||e.vtp_defaultValue||""}return r;default:return f(e)}})}();
Z.a.gas=["google"],function(){(function(a){Z.__gas=a;Z.__gas.b="gas";Z.__gas.g=!0})(function(a){var b=za(a,void 0),c=b;c[O.L]=null;c[O.Ac]=null;var d=b=c;d.vtp_fieldsToSet=d.vtp_fieldsToSet||[];var e=d.vtp_cookieDomain;void 0!==e&&(d.vtp_fieldsToSet.push({fieldName:"cookieDomain",value:e}),delete d.vtp_cookieDomain);return b})}();
Z.a.smm=["google"],function(){(function(a){Z.__smm=a;Z.__smm.b="smm";Z.__smm.g=!0})(function(a){var b=a.vtp_input,c=U(a.vtp_map,"key","value")||{};return c.hasOwnProperty(b)?c[b]:a.vtp_defaultValue})}();



Z.a.html=["customScripts"],function(){var a=function(b,c,f,h){return function(){try{if(0<c.length){var d=c.shift(),e=a(b,c,f,h);if("SCRIPT"==String(d.nodeName).toUpperCase()&&"text/gtmscript"==d.type){var m=A.createElement("script");m.async=!1;m.type="text/javascript";m.id=d.id;m.text=d.text||d.textContent||d.innerHTML||"";d.charset&&(m.charset=d.charset);var n=d.getAttribute("data-gtmsrc");n&&(m.src=n,Oa(m,e));b.insertBefore(m,null);n||e()}else if(d.innerHTML&&0<=d.innerHTML.toLowerCase().indexOf("<script")){for(var p=
[];d.firstChild;)p.push(d.removeChild(d.firstChild));b.insertBefore(d,null);a(d,p,e,h)()}else b.insertBefore(d,null),e()}else f()}catch(q){D(h)}}};var c=function(d){if(A.body){var e=
d.vtp_gtmOnFailure,f=vg(d.vtp_html,d.vtp_gtmOnSuccess,e),h=f.kb,k=f.S;if(d.vtp_useIframe){}else d.vtp_supportDocumentWrite?b(h,k,e):a(A.body,Xa(h),k,e)()}else ag(function(){c(d)},200)};Z.__html=c;Z.__html.b="html";Z.__html.g=!0}();



Z.a.lcl=[],function(){function a(){var a=W("document"),d=0,e=function(c){var e=c.target;if(e&&3!==c.which&&(!c.timeStamp||c.timeStamp!==d)){d=c.timeStamp;e=hg(e,["a","area"]);if(!e)return c.returnValue;var f=c.defaultPrevented||!1===c.returnValue,l=Ne("lcl",f?"nv.mwt":"mwt",0),m=Ke(e);m.event="gtm.linkClick";if(f){var n=og("lcl","nv.ids",[]).join(",");if(n)m["gtm.triggers"]=n;else return}else{var p=og("lcl","ids",[]).join(",");m["gtm.triggers"]=p}if(b(c,e,a)&&!f&&l&&e.href){var q=W((e.target||"_self").substring(1)),
r=!0;if(eg(m,function(){r&&q&&(q.location.href=e.href)},l))r=!1;else return c.preventDefault&&c.preventDefault(),c.returnValue=!1}else eg(m,function(){},l||2E3);return!0}};Ra(a,"click",e,!1);Ra(a,"auxclick",e,!1)}function b(a,b,e){if(2===a.which||a.ctrlKey||a.shiftKey||a.altKey||a.metaKey)return!1;var c=b.href.indexOf("#"),d=b.target;if(d&&"_self"!==d&&"_parent"!==d&&"_top"!==d||0===c)return!1;if(0<c){var k=$a(N(b.href)),l=$a(N(e.location));return k!==l}return!0}(function(a){Z.__lcl=a;Z.__lcl.b="lcl";
Z.__lcl.g=!0})(function(b){var c=void 0===b.vtp_waitForTags?!0:b.vtp_waitForTags,e=void 0===b.vtp_checkValidation?!0:b.vtp_checkValidation,f=Number(b.vtp_waitForTagsTimeout);if(!f||0>=f)f=2E3;var h=b.vtp_uniqueTriggerId||"0";if(c){var k=function(a){return Math.max(f,a)};Me("lcl","mwt",k,0);e||Me("lcl","nv.mwt",k,0)}var l=function(a){a.push(h);return a};ng("lcl","ids",l,[]);e||ng("lcl","nv.ids",l,[]);if(!pg("lcl")){a();qg("lcl");var m=Ne("lcl","legacyTeardown",void 0);m&&m()}D(b.vtp_gtmOnSuccess)})}();

Z.a.evl=["google"],function(){function a(a,b){this.element=a;this.uid=b}function b(){var a=Number(V("gtm.start"))||0;return(new Date).getTime()-a}function c(a,c,d,l){function f(){if(!lg(a.target)){c.has(e.La)||c.set(e.La,""+b());c.has(e.$a)||c.set(e.$a,""+b());var f=0;c.has(e.Ma)&&(f=Number(c.get(e.Ma)));f+=100;c.set(e.Ma,""+f);if(f>=d){var h=Ke(a.target);h.event="gtm.elementVisibility";var k=jg(a.target);h["gtm.visibleRatio"]=Math.round(1E3*k)/10;h["gtm.visibleTime"]=d;h["gtm.visibleFirstTime"]=
Number(c.get(e.$a));h["gtm.visibleLastTime"]=Number(c.get(e.La));h["gtm.triggers"]=c.uid;eg(h);l()}}}if(!c.has(e.oa)&&(0==d&&f(),!c.has(e.ea))){var h=W("self").setInterval(f,100);c.set(e.oa,h)}}function d(a){a.has(e.oa)&&(W("self").clearInterval(Number(a.get(e.oa))),a.remove(e.oa))}var e={oa:"polling-id-",$a:"first-on-screen-",La:"recent-on-screen-",Ma:"total-visible-time-",ea:"has-fired-"};a.prototype.has=function(a){return!!this.element.getAttribute("data-gtm-vis-"+a+this.uid)};a.prototype.get=
function(a){return this.element.getAttribute("data-gtm-vis-"+a+this.uid)};a.prototype.set=function(a,b){this.element.setAttribute("data-gtm-vis-"+a+this.uid,b)};a.prototype.remove=function(a){this.element.removeAttribute("data-gtm-vis-"+a+this.uid)};(function(a){Z.__evl=a;Z.__evl.b="evl";Z.__evl.g=!0})(function(b){function f(){var b=!1,c=null;if("CSS"===l){try{c=Ue?A.querySelectorAll(m):null}catch(ia){}b=!!c&&t.length!=c.length}else if("ID"===l){var e=Ua(m);e&&(c=[e],b=1!=t.length||t[0]!==e)}c||(c=
[],b=0<t.length);if(b){for(var f=0;f<t.length;f++)d(new a(t[f],r));t=[];for(var h=0;h<c.length;h++)t.push(c[h]);0<=z&&kg(z);if(0<t.length){var n=k,p=t,u=[q],M=0;M=Wf(n,p,u);z=M}}}function k(b){var h=new a(b.target,r);b.intersectionRatio>=q?h.has(e.ea)||c(b,h,p,"ONCE"===u?function(){for(var b=0;b<t.length;b++){var c=new a(t[b],r);c.set(e.ea,"1");d(c)}kg(z);if(n&&Kf)for(var h=0;h<Kf.length;h++)Kf[h]===
f&&Kf.splice(h,1)}:function(){h.set(e.ea,"1");d(h)}):(d(h),"MANY_PER_ELEMENT"===u&&h.has(e.ea)&&(h.remove(e.ea),h.remove(e.Ma)),h.remove(e.La))}var l=b.vtp_selectorType,m;"ID"===l?m=String(b.vtp_elementId):"CSS"===l&&(m=String(b.vtp_elementSelector));var n=!!b.vtp_useDomChangeListener,p=b.vtp_useOnScreenDuration&&Number(b.vtp_onScreenDuration)||0,q=(Number(b.vtp_onScreenRatio)||50)/100,r=b.vtp_uniqueTriggerId,u=b.vtp_firingFrequency,t=[],z=-1;f();n&&Lf(f);D(b.vtp_gtmOnSuccess)})}();

var Rg={macro:function(a){if(ke.ab.hasOwnProperty(a))return ke.ab[a]}};Rg.dataLayer=Ic;Rg.onHtmlSuccess=ke.Vb(!0);Rg.onHtmlFailure=ke.Vb(!1);Rg.callback=function(a){oc.hasOwnProperty(a)&&sc(oc[a])&&oc[a]();delete oc[a]};Rg.Uc=function(){lc[kc.o]=Rg;pc=Z.a;Wb=Wb||ke;Xb=Vc};
Rg.Id=function(){lc=y.google_tag_manager=y.google_tag_manager||{};if(lc[kc.o]){var a=lc.zones;a&&a.unregisterChild(kc.o)}else{for(var b=data.resource||{},c=b.macros||[],d=0;d<c.length;d++)Pb.push(c[d]);for(var e=b.tags||[],f=0;f<e.length;f++)Sb.push(e[f]);for(var h=b.predicates||[],k=0;k<h.length;k++)Rb.push(h[k]);for(var l=b.rules||[],m=0;m<l.length;m++){for(var n=l[m],p={},q=0;q<n.length;q++)p[n[q][0]]=Array.prototype.slice.call(n[q],1);Qb.push(p)}Ub=Z;De();Rg.Uc();je();Yc=!1;Zc=0;if("interactive"==
A.readyState&&!A.createEventObject||"complete"==A.readyState)ad();else{Ra(A,"DOMContentLoaded",ad);Ra(A,"readystatechange",ad);if(A.createEventObject&&A.documentElement.doScroll){var r=!0;try{r=!y.frameElement}catch(t){}r&&bd()}Ra(y,"load",ad)}Xd=!1;"complete"===A.readyState?Zd():Ra(y,"load",Zd);a:{
if(!ld)break a;od();rd=void 0;sd={};pd={};ud=void 0;td={};qd="";vd=md();y.setInterval(od,864E5);}}};Rg.Id();

})()
