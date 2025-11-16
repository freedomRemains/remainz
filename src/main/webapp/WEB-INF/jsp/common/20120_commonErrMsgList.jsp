<%@ page pageEncoding="UTF-8"%>

            <div class="<%=columnMap.get("HTML_PARTS_ID")%>">
              <div class="partsArea">
                <% var errMsgList = (ArrayList<LinkedHashMap<String, String>>) request.getAttribute("errMsgList");
                   if (errMsgList != null && errMsgList.size() > 0) {
                     for (LinkedHashMap<String, String> errMsg : errMsgList) { %>
                  <div><h3><span><font color="red"><%=errMsg.get("ERR_MSG")%></font></span></h3></div>
                <%   } %>
                <% } %>
              </div>
            </div>
