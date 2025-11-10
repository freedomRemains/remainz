<%@ page pageEncoding="UTF-8"%>

            <% if (!"1000001".equals(account.get(0).get("ACCNT_ID"))) { %>
              <div class="<%=columnMap.get("HTML_PARTS_ID")%>">
                <div class="linkArea">
                  <% var linkList = (ArrayList<LinkedHashMap<String, String>>) request.getAttribute("linkList");
                     for (LinkedHashMap<String, String> link : linkList) {
                       String url = link.get("URI_PATTERN");
                       String pageName = link.get("LNK_NAME");
                       if ("/jl/service/error.html".equals(url)) {
                         continue;
                       }
                       if ("0".equals(link.get("IS_POST"))) { %>
                    <div class="largePadding"><a class="linkButton" href="<%=url%>"><%=pageName%></a></div>
                  <%   } else { %>
                    <div class="largePadding"><a class="linkButton" href="javascript:void(0);" onclick="submitMainForm()"><%=pageName%></a></div>
                  <%   } %>
                  <% } %>
                </div>
              </div>
            <% } %>
