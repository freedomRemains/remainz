<%@ page pageEncoding="UTF-8"%>

            <div class="<%=columnMap.get("HTML_PARTS_ID")%>">
              <div class="p-2">
                <% var noticeList = (ArrayList<LinkedHashMap<String, String>>) request.getAttribute("noticeList");
                   if (noticeList != null && noticeList.size() > 0) {
                     for (LinkedHashMap<String, String> notice : noticeList) { %>
                  <div><span><%=notice.get("GNR_VAL")%></span></div>
                <%   } %>
                <% } %>
              </div>
            </div>
