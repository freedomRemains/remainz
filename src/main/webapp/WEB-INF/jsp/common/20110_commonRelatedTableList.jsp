<%@ page pageEncoding="UTF-8"%>

            <div class="<%=columnMap.get("HTML_PARTS_ID")%>">
              <div class="partsArea">
                <hr>
                <label><span class="genericLabel">このレコードを使用している外部テーブル</span><br /></label>
                <% var relatedTableList = (ArrayList<LinkedHashMap<String, String>>) request.getAttribute("relatedTableList");
                   var tableDataMainteUri = ((ArrayList<LinkedHashMap<String, String>>) request.getAttribute("tableDataMainteUri")).get(0).get("URI_PATTERN");
                   for (LinkedHashMap<String, String> relatedTable : relatedTableList) {
                     String relatedTableName = relatedTable.get("TABLE_NAME");
                     String relatedTableLogicalName = relatedTable.get("TABLE_LOGICAL_NAME"); %>
                  <hr>
                  <label><a href="<%=tableDataMainteUri%>?tableName=<%=relatedTableName%>"><span class="genericLabel"><%=relatedTableLogicalName%>(<%=relatedTableName%>)</span><br /></a></label>
                  <table class="dbColumnTable">
                    <thead>
                      <tr>
                        <% var foreignTableDefList = (ArrayList<LinkedHashMap<String, String>>) request.getAttribute("foreignTableDefList_" + relatedTableName);
                           String foreignTableName = foreignTableDefList.get(0).get("TABLE_NAME");
                           String foreignTablePriKeyField = foreignTableName + "_ID";
                           String foreignTableDescField = foreignTableDefList.get(0).get("DESC_FIELD");
                           for (LinkedHashMap<String, String> foreignTableDef : foreignTableDefList) {
                             if (foreignTableDef.get("FIELD_NAME").equals(foreignTablePriKeyField)
                                 || foreignTableDef.get("FIELD_NAME").equals(foreignTableDescField)) { %>
                          <th><%=foreignTableDef.get("FIELD_LOGICAL_NAME")%><br /><%=foreignTableDef.get("FIELD_NAME")%></th>
                        <%   } %>
                        <% } %>
                      </tr>
                    </thead>
                    <tbody>
                      <% var foreignTableRecordList = (ArrayList<LinkedHashMap<String, String>>) request.getAttribute("foreignTableRecordList_" + relatedTableName);
                         var editRecordUri = ((ArrayList<LinkedHashMap<String, String>>) request.getAttribute("editRecordUri")).get(0).get("URI_PATTERN");
                         for (LinkedHashMap<String, String> foreignTableRecord : foreignTableRecordList) { %>
                        <tr>
                          <td><a href="<%=editRecordUri%>?tableName=<%=foreignTableName%>&recordId=<%=foreignTableRecord.get(foreignTablePriKeyField)%>"><%=foreignTableRecord.get(foreignTablePriKeyField)%></a></td>
                          <td><%=foreignTableRecord.get(foreignTableDescField)%></td>
                        </tr>
                      <% } %>
                      <tr>
                        <td></td>
                      </tr>
                    </tbody>
                  </table>
                <% } %>
              </div>
            </div>
