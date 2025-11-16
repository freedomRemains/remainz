<%@ page pageEncoding="UTF-8"%>

            <nav class="navbar navbar-expand-md navbar-dark bg-secondary">
              <a class="navbar-brand" href="#"><%=systemName.get(0).get("GNR_VAL")%></a>
              <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarMenu">
                <span class="navbar-toggler-icon"></span>
              </button>
              <div class="collapse navbar-collapse" id="navbarMenu">
                <ul class="navbar-nav ms-auto">
                  <% var urlLinkList = (ArrayList<LinkedHashMap<String, String>>) request.getAttribute("urlLink");
                     if (urlLinkList != null) {
                       for (LinkedHashMap<String, String> urlLink : urlLinkList) {
                         String link = urlLink.get("URI_PATTERN");
                         String pageName = urlLink.get("PAGE_NAME");
                         if ("/remainz/service/error.html".equals(link)) {
                           continue;
                         } %>
                    <li class="nav-item"><a class="nav-link" href="<%=link%>"><%=pageName%></a></li>
                  <%   } %>
                  <% } %>
                  <li class="nav-item"><a class="nav-link" href="#"><%=account.get(0).get("ACCOUNT_NAME")%></a></li>
                </ul>
              </div>
            </nav>
