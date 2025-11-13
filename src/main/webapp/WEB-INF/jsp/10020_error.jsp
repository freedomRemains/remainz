<%@ page pageEncoding="UTF-8"%>

          <% if ("1000101".equals(columnMap.get("HTML_PARTS_ID"))
                 && authUtil.hasReadAuth("1000101", authList)) { // エラー表示領域
               String errMsg = (String) request.getAttribute("errMsg");
               String stackTrace = (String) request.getAttribute("stackTrace");
               var errPageMsg = (ArrayList<LinkedHashMap<String, String>>) request.getAttribute("errPageMsg"); %>
            <%@ include file="common/20020_commonError.jsp"%>
          <% } %>
