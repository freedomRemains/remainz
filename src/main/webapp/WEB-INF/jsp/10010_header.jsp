<%@ page pageEncoding="UTF-8"%>

          <% if ("1000001".equals(columnMap.get("HTML_PARTS_ID"))
                 && authUtil.hasReadAuth("1000001", authList)) { // システム名、共通ヘッダ %>
            <%@ include file="common/20010_commonHeader.jsp"%>
          <% } %>
