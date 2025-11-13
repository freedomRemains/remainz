<%@ page pageEncoding="UTF-8"%>

          <% if ("1000701".equals(columnMap.get("HTML_PARTS_ID"))
                 && authUtil.hasReadAuth("1000701", authList)) { // 通知一覧領域 %>
            <%@ include file="common/20080_commonNoticeList.jsp"%>
          <% } %>
