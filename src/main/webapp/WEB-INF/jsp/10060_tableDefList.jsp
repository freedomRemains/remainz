<%@ page pageEncoding="UTF-8"%>

          <% if ("1000501".equals(columnMap.get("HTML_PARTS_ID"))
                 && AuthUtil.hasEditAuth("1000501", authList)) { // テーブル情報入力領域 %>
            <%@ include file="common/20060_commonTableDefList.jsp"%>
          <% } %>
