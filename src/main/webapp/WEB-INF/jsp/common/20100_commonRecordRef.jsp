<%@ page pageEncoding="UTF-8"%>

            <div class="<%=columnMap.get("HTML_PARTS_ID")%>">
              <div class="partsArea">
                <% var targetRecordRef = (ArrayList<LinkedHashMap<String, String>>) request.getAttribute("targetRecord");
                   var tableDefListRef = (ArrayList<LinkedHashMap<String, String>>) request.getAttribute("tableDefList");
                   var tableNameRef = tableDefListRef.get(0).get("TABLE_NAME"); %>
                <label><span class="genericLabel"><%=tableDefListRef.get(0).get("TABLE_LOGICAL_NAME") + "(" + tableNameRef + ")"%></span><br /></label>
                <table>
                  <tbody>
                    <% for (Map.Entry<String, String> entry : targetRecordRef.get(0).entrySet()) {
                         String disabled = "disabled";
                         String fieldName = entry.getKey();
                         String fieldLogicalName = "";
                         for (LinkedHashMap<String, String> tableDef : tableDefListRef) {
                           if (fieldName.equals(tableDef.get("FIELD_NAME"))) {
                             fieldLogicalName = tableDef.get("FIELD_LOGICAL_NAME");
                           }
                         } %>
                      <tr>
                        <td><%=fieldLogicalName%></td>
                        <td><input id="<%=fieldName%>" name="<%=fieldName%>" value="<%=entry.getValue()%>" size="50" <%=disabled%>></td>
                      </tr>
                    <% } %>
                  </tbody>
                </table>
              </div>
            </div>
            <input type="hidden" name="tableName" value="<%=tableNameRef%>">
