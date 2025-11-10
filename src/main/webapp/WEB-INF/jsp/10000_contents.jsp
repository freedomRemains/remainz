<%@ page language="java" contentType="text/html; charset=UTF-8"%>
<%@ page import="com.jc.param.GenericParam" %>
<%@ page import="com.jw.util.AuthUtil" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.LinkedHashMap" %>
<%@ page import="java.util.Map" %>
<% String contextPath = request.getContextPath();
   var authUtil = new AuthUtil();
   var account = (ArrayList<LinkedHashMap<String, String>>) request.getAttribute("account");
   var authList = (ArrayList<LinkedHashMap<String, String>>) request.getAttribute("authList");
   var systemName = (ArrayList<LinkedHashMap<String, String>>) request.getAttribute("systemName");
   var htmlPage = (ArrayList<LinkedHashMap<String, String>>) request.getAttribute("htmlPage"); %>
<!doctype html>
<html>
  <head>
    <script type="text/javascript" src="<%=contextPath%>/js/jwscript.js"></script>
    <link rel="stylesheet" type="text/css" href="<%=contextPath%>/css/jwstyle.css">
    <link rel="icon" type="image/png" href="<%=contextPath%>/img/favicon.png">
    <title><%=htmlPage.get(0).get("PAGE_NAME")%></title>
  </head>
  <body>
    <div id="mainArea" class="mainArea">
      <form id="mainForm" method="POST">

        <% ArrayList<String> partsInPageIdList = new ArrayList<String>();
           for (LinkedHashMap<String, String> columnMap : htmlPage) {
             if (partsInPageIdList.contains(columnMap.get("PARTS_IN_PAGE_ID"))) {
               continue; // 同一のページ内パーツIDを持つものが複数あっても、表示するのは1つだけ
             }
             partsInPageIdList.add(columnMap.get("PARTS_IN_PAGE_ID")); %>

          <%@ include file="10010_header.jsp"%>
          <%@ include file="10020_error.jsp"%>
          <%@ include file="10030_login.jsp"%>
          <%@ include file="10040_linkList.jsp"%>
          <%@ include file="10050_tableList.jsp"%>
          <%@ include file="10060_tableDefList.jsp"%>
          <%@ include file="10070_recordList.jsp"%>
          <%@ include file="10080_noticeList.jsp"%>
          <%@ include file="10090_recordEdit.jsp"%>
          <%@ include file="10100_recordRef.jsp"%>
          <%@ include file="10110_relatedTableList.jsp"%>
          <%@ include file="10120_errMsgList.jsp"%>

        <% } %>

      </form>
    </div>
  </body>
</html>
