<%@ page pageEncoding="UTF-8"%>

          <% if ("1000401".equals(columnMap.get("HTML_PARTS_ID"))
                 && authUtil.hasEditAuth("1000401", authList)) { // テーブル一覧領域 %>
            <%@ include file="common/20050_commonTableList.jsp"%>
          <% } %>
