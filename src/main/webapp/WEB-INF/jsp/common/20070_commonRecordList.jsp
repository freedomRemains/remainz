<%@ page pageEncoding="UTF-8"%>

            <div class="<%=columnMap.get("HTML_PARTS_ID")%>">
              <div class="partsArea">
                <% var tableDefList = (ArrayList<LinkedHashMap<String, String>>) request.getAttribute("tableDefList");
                   var dbRecordList = (ArrayList<LinkedHashMap<String, String>>) request.getAttribute("dbRecordList");
                   var newRecordUri = ((ArrayList<LinkedHashMap<String, String>>) request.getAttribute("newRecordUri")).get(0).get("URI_PATTERN");
                   var deleteRecordUri = ((ArrayList<LinkedHashMap<String, String>>) request.getAttribute("deleteRecordUri")).get(0).get("URI_PATTERN");
                   var editRecordUri = ((ArrayList<LinkedHashMap<String, String>>) request.getAttribute("editRecordUri")).get(0).get("URI_PATTERN");
                   var recordRefUri = ((ArrayList<LinkedHashMap<String, String>>) request.getAttribute("recordRefUri")).get(0).get("URI_PATTERN");
                   var recordListUri = ((ArrayList<LinkedHashMap<String, String>>) request.getAttribute("recordListUri")).get(0).get("URI_PATTERN");
                   var tableName = tableDefList.get(0).get("TABLE_NAME");
                   var limitList = ((ArrayList<LinkedHashMap<String, String>>) request.getAttribute("limitList"));
                   String currentLimit = (String) request.getAttribute("currentLimit");
                   if (currentLimit == null) {
                     currentLimit = "10";
                   }
                   int currentLimitInt = Integer.parseInt(currentLimit);
                   String totalRecord = ((ArrayList<LinkedHashMap<String, String>>) request.getAttribute("totalRecord")).get(0).get("COUNT(*)");
                   if (totalRecord == null) {
                     totalRecord = Integer.toString(dbRecordList.size());
                   }
                   String currentPage = (String) request.getAttribute("currentPage");
                   if (currentPage == null) {
                     currentPage = "1";
                   }
                   int currentPageInt = Integer.parseInt(currentPage);
                   String currentPageOffset = Integer.toString(currentLimitInt * (currentPageInt - 1));
                   int lastPageInt = Integer.parseInt(totalRecord) / currentLimitInt;
                   if (Integer.parseInt(totalRecord) % currentLimitInt > 0) {
                     lastPageInt++;
                   }
                   String lastPage = Integer.toString(lastPageInt);
                   String lastPageOffset = Integer.toString(currentLimitInt * (lastPageInt - 1));
                   String prevPage = "1";
                   int prevPageInt = currentPageInt - 1;
                   if (prevPageInt > 1) {
                     prevPage = Integer.toString(prevPageInt);
                   }
                   String prevPageOffset = Integer.toString(currentLimitInt * (prevPageInt - 1));
                   String nextPage = "1";
                   int nextPageInt = currentPageInt + 1;
                   if (nextPageInt > lastPageInt) {
                     nextPage = Integer.toString(lastPageInt);
                   } else {
                     nextPage = Integer.toString(nextPageInt);
                   }
                   String nextPageOffset = Integer.toString(currentLimitInt * (Integer.parseInt(nextPage) - 1));
                   %>
                <div class="littlePadding">
                  <label><span class="genericLabel"><%=tableDefList.get(0).get("TABLE_LOGICAL_NAME") + "(" + tableDefList.get(0).get("TABLE_NAME") + ")"%></span><br /></label>
                  <% if (authUtil.hasEditAuth("1000601", authList)) { %>
                    <a href="<%=newRecordUri%>?tableName=<%=tableName%>">新規レコード追加</a>
                    <input type="button" name="doPost" value="一括削除" onclick="submitMainForm()">
                  <% } %>
                </div>
                <div class="scrollArea">
                  <table class="dbRecordTable">
                    <thead>
                      <tr>
                        <% if (authUtil.hasEditAuth("1000601", authList)) { %>
                          <th>操作</th>
                          <th>選択</th>
                        <% } %>
                        <% for (LinkedHashMap<String, String> tableDef : tableDefList) { %>
                          <th><%=tableDef.get("FIELD_LOGICAL_NAME")%><br /><%=tableDef.get("FIELD_NAME")%></th>
                        <% } %>
                      </tr>
                    </thead>
                    <tbody>
                      <% for (LinkedHashMap<String, String> dbRecord : dbRecordList) { %>
                        <tr>
                          <% for (Map.Entry<String, String> entry : dbRecord.entrySet()) {
                               if (entry.getKey().equals(tableName + "_ID") &&
                                   authUtil.hasEditAuth("1000601", authList)) { %>
                              <td>
                                  <a href="<%=deleteRecordUri%>?tableName=<%=tableName%>&recordId=<%=entry.getValue()%>">削除</a>
                                  <a href="<%=editRecordUri%>?tableName=<%=tableName%>&recordId=<%=entry.getValue()%>">編集</a>
                              </td>
                              <td>
                                <input type="checkbox" name="<%=entry.getValue()%>">
                              </td>
                          <%   } %>
                          <%   String foreignTable = "";
                               for (LinkedHashMap<String, String> tableDef : tableDefList) {
                                 if (entry.getKey().equals(tableDef.get("FIELD_NAME"))) {
                                   foreignTable = tableDef.get("FOREIGN_TABLE");
                                   break;
                                 }
                               }
                               if (foreignTable != null && foreignTable.length() > 0) { %>
                            <td><a href="<%=recordRefUri%>?tableName=<%=foreignTable%>&recordId=<%=entry.getValue()%>" target="_blank" rel="noopener noreferrer"><%=entry.getValue()%></a></td>
                          <%   } else { %>
                            <td><%=entry.getValue()%></td>
                          <%   } %>
                          <% } %>
                        </tr>
                      <% } %>
                    </tbody>
                  </table>
                </div>
                <div class="littlePadding">
                  <% if (lastPageInt > 1) { %>
                    <% if (currentPageInt > 1) { %>
                      <a href="<%=recordListUri%>?tableName=<%=tableName%>&limit=<%=currentLimit%>&offset=0">最初のページ(1)</a>　
                    <% } %>
                    <% if (prevPageInt > 1) { %>
                      <a href="<%=recordListUri%>?tableName=<%=tableName%>&limit=<%=currentLimit%>&offset=<%=prevPageOffset%>">前のページ(<%=prevPage%>)</a>　
                    <% } %>
                    <% if (nextPageInt < lastPageInt) { %>
                      <a href="<%=recordListUri%>?tableName=<%=tableName%>&limit=<%=currentLimit%>&offset=<%=nextPageOffset%>">次のページ(<%=nextPage%>)</a>　
                    <% } %>
                    <% if (currentPageInt < lastPageInt) { %>
                      <a href="<%=recordListUri%>?tableName=<%=tableName%>&limit=<%=currentLimit%>&offset=<%=lastPageOffset%>">最後のページ(<%=lastPage%>)</a>　
                    <% } %>
                  <% } %>
                  <label><span class="genericLabel"><%=currentPage%>／<%=lastPage%>　表示件数</span></label>
                  <select id="selectLimit" name="limit" onchange="changeLimit('<%=recordListUri%>?tableName=<%=tableName%>&limit=', '&offset=<%=currentPageOffset%>')">
                    <% for (LinkedHashMap<String, String> limitInList : limitList) {
                         String limitNum = limitInList.get("GNR_VAL");
                         if (limitNum.equals(currentLimit)) { %>
                      <option value="<%=limitNum%>" selected><%=limitNum%>件</option>
                    <%   } else { %>
                      <option value="<%=limitNum%>"><%=limitNum%>件</option>
                    <%   }%>
                    <% } %>
                  </select>
                </div>
              </div>
            </div>
            <input type="hidden" name="tableName" value="<%=tableName%>">
            <input type="hidden" name="currentLimit" value="<%=currentLimit%>">
            <input type="hidden" name="totalRecord" value="<%=totalRecord%>">
            <input type="hidden" name="currentPage" value="<%=currentPage%>">
