<%@ page pageEncoding="UTF-8"%>

            <div class="<%=columnMap.get("HTML_PARTS_ID")%>">
              <div class="partsArea">
                <% var tableDefListEdit = (ArrayList<LinkedHashMap<String, String>>) request.getAttribute("tableDefList"); %>
                <label><span class="genericLabel"><%=tableDefListEdit.get(0).get("TABLE_LOGICAL_NAME") + "(" + tableDefListEdit.get(0).get("TABLE_NAME") + ")"%></span><br /></label>
                <table>
                  <tbody>
                    <% var columnList = (ArrayList<LinkedHashMap<String, String>>) request.getAttribute("columnList");
                       var targetRecordEdit = (ArrayList<LinkedHashMap<String, String>>) request.getAttribute("targetRecord");
                       var tableNameEdit = tableDefListEdit.get(0).get("TABLE_NAME");
                       if (columnList != null && columnList.size() > 0) { // 新規レコード追加の場合
                         for (LinkedHashMap<String, String> column : columnList) {
                           if (column.get("FIELD_NAME").equals(tableNameEdit + "_ID")
                               || "VERSION".equals(column.get("FIELD_NAME")) || "IS_DELETED".equals(column.get("FIELD_NAME"))
                               || "CREATED_BY".equals(column.get("FIELD_NAME")) || "CREATED_AT".equals(column.get("FIELD_NAME"))
                               || "UPDATED_BY".equals(column.get("FIELD_NAME")) || "UPDATED_AT".equals(column.get("FIELD_NAME"))) {
                             continue;
                           }
                           String fieldName = column.get("FIELD_NAME");
                           String fieldLogicalName = column.get("FIELD_LOGICAL_NAME"); %>
                      <tr>
                        <td><%=fieldLogicalName%></td>
                        <td><input id="<%=fieldName%>" name="<%=fieldName%>" size="50"></td>
                      </tr>
                    <%   } %>
                    <% } else { // 削除もしくは編集の場合
                         for (Map.Entry<String, String> entry : targetRecordEdit.get(0).entrySet()) {
                           String disabled = "";
                           if (entry.getKey().equals(tableNameEdit + "_ID")
                               || "VERSION".equals(entry.getKey()) || "IS_DELETED".equals(entry.getKey())
                               || "CREATED_BY".equals(entry.getKey()) || "CREATED_AT".equals(entry.getKey())
                               || "UPDATED_BY".equals(entry.getKey()) || "UPDATED_AT".equals(entry.getKey())) {
                             disabled = "disabled";
                           }
                           String fieldName = entry.getKey();
                           String fieldLogicalName = "";
                           for (LinkedHashMap<String, String> tableDef : tableDefListEdit) {
                             if (fieldName.equals(tableDef.get("FIELD_NAME"))) {
                               fieldLogicalName = tableDef.get("FIELD_LOGICAL_NAME");
                             }
                           } %>
                      <tr>
                        <td><%=fieldLogicalName%></td>
                        <td><input id="<%=fieldName%>" name="<%=fieldName%>" value="<%=entry.getValue()%>" size="50" <%=disabled%>></td>
                      </tr>
                    <%   } %>
                    <% } %>
                  </tbody>
                </table>
              </div>
            </div>
            <% if (columnList == null || columnList.size() == 0) { // 削除もしくは編集の場合
                 for (Map.Entry<String, String> entry : targetRecordEdit.get(0).entrySet()) {
                   if (entry.getKey().equals(tableNameEdit + "_ID")
                       || "VERSION".equals(entry.getKey()) || "IS_DELETED".equals(entry.getKey())
                       || "CREATED_BY".equals(entry.getKey()) || "CREATED_AT".equals(entry.getKey())
                       || "UPDATED_BY".equals(entry.getKey()) || "UPDATED_AT".equals(entry.getKey())) {
                     String fieldName = entry.getKey();
                     String fieldValue = entry.getValue(); %>
              <input type="hidden" name="<%=fieldName%>" value="<%=fieldValue%>">
            <%     } %>
            <%   } %>
            <% } %>
            <input type="hidden" name="tableName" value="<%=tableNameEdit%>">
