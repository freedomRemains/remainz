<%@ page pageEncoding="UTF-8"%>

            <% if (!"1000001".equals(account.get(0).get("ACCNT_ID"))) { %>
              <div class="<%=columnMap.get("HTML_PARTS_ID")%>">
                <div class="row m-2 gy-2">
                  <% var linkList = (ArrayList<LinkedHashMap<String, String>>) request.getAttribute("linkList");
                     for (LinkedHashMap<String, String> link : linkList) {
                       String url = link.get("URI_PATTERN");
                       String pageName = link.get("LNK_NAME");
                       if ("/remainz/service/error.html".equals(url)) {
                         continue;
                       }
                       if ("0".equals(link.get("IS_POST"))) { %>
                    <a class="btn btn-primary w-100 px-4 py-2" href="<%=url%>"><%=pageName%></a>
                  <%   } else { %>
                    <a class="btn btn-primary w-100 px-4 py-2" href="javascript:void(0);" onclick="submitMainForm()"><%=pageName%></a>
                  <%   } %>
                  <% } %>
                </div>
              </div>
            <% } %>
