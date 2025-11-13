<%@ page pageEncoding="UTF-8"%>

          <% if ("1001001".equals(columnMap.get("HTML_PARTS_ID"))
                 && authUtil.hasEditAuth("1001001", authList)) { // 関連テーブル一覧 %>
            <%@ include file="common/20110_commonRelatedTableList.jsp"%>
          <% } %>
