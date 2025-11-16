<%@ page pageEncoding="UTF-8"%>

            <div class="container">
              <div class="<%=columnMap.get("HTML_PARTS_ID")%>">
                <div class="partsArea">
                  <table class="buttonTable">
                    <tbody>
                      <% var tableList = (ArrayList<LinkedHashMap<String, String>>) request.getAttribute("tableList");
                         for (LinkedHashMap<String, String> table : tableList) {
                           String tableName = table.get("TABLE_NAME");
                           String tableLogicalName = table.get("TABLE_LOGICAL_NAME"); %>
                        <tr>
                          <td><%=tableLogicalName%>(<%=tableName%>)</td>
                          <td><a class="btn btn-primary px-4 py-2" href="tableDefRef.html?tableName=<%=tableName%>" target="_blank" rel="noopener noreferrer">定義参照</a></td>
                          <td><a class="btn btn-primary px-4 py-2" href="tableDataMainte.html?tableName=<%=tableName%>">データ編集</a></td>
                        </tr>
                      <% } %>
                    </tbody>
                  </table>
                </div>
              </div>
            </div>
