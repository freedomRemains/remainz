<%@ page pageEncoding="UTF-8"%>

            <div id="headerArea" class="headerArea">
              <h1 class="headerTitle"><%=systemName.get(0).get("GNR_VAL")%></h1>
              <div id="loginUserArea" class="loginUserArea">
                <table>
                  <tbody>
                    <tr>
                      <td><%=account.get(0).get("ACCOUNT_NAME")%></td>
                    </tr>
                  </tbody>
                </table>
              </div>
              <div class="flexButton">
                <% var urlLinkList = (ArrayList<LinkedHashMap<String, String>>) request.getAttribute("urlLink");
                   if (urlLinkList != null) {
                     for (LinkedHashMap<String, String> urlLink : urlLinkList) {
                       String link = urlLink.get("URI_PATTERN");
                       String pageName = urlLink.get("PAGE_NAME");
                       if ("/jl/service/error.html".equals(link)) {
                         continue;
                       } %>
                  <div class="largePadding"><a class="linkButton" href="<%=link%>"><%=pageName%></a></div>
                <%   } %>
                <% } %>
              </div>
            </div>
