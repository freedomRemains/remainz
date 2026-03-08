<%@ page pageEncoding="UTF-8"%>

          <% if ("1000201".equals(columnMap.get("HTML_PARTS_ID"))
                 && AuthUtil.hasEditAuth("1000201", authList)) { // ログイン情報入力領域 %>
            <%@ include file="common/20030_commonLogin.jsp"%>
          <% } %>
