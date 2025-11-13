<%@ page pageEncoding="UTF-8"%>

          <% if ("1001101".equals(columnMap.get("HTML_PARTS_ID"))
                 && authUtil.hasReadAuth("1001101", authList)) { // エラーメッセージ一覧領域 %>
            <%@ include file="common/20120_commonErrMsgList.jsp"%>
          <% } %>
