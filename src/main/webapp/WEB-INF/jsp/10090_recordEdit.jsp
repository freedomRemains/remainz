<%@ page pageEncoding="UTF-8"%>

          <% if ("1000801".equals(columnMap.get("HTML_PARTS_ID"))
                 && authUtil.hasEditAuth("1000801", authList)) { // レコード詳細領域 %>
            <%@ include file="common/20090_commonRecordEdit.jsp"%>
          <% } %>
