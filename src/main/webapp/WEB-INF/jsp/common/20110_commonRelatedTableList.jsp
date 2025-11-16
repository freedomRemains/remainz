<%@ page pageEncoding="UTF-8"%>

            <div class="<%=columnMap.get("HTML_PARTS_ID")%>">
              <div class="p-2">
                <hr>
                <label><span class="form-label fw-bold">このレコードを使用している外部テーブル</span><br /></label>
                <% var relatedTableList = (ArrayList<LinkedHashMap<String, String>>) request.getAttribute("relatedTableList");
                   var tableDataMainteUri = ((ArrayList<LinkedHashMap<String, String>>) request.getAttribute("tableDataMainteUri")).get(0).get("URI_PATTERN");
                   for (LinkedHashMap<String, String> relatedTable : relatedTableList) {
                     String relatedTableName = relatedTable.get("TABLE_NAME");
                     String relatedTableLogicalName = relatedTable.get("TABLE_LOGICAL_NAME"); %>
                  <hr>
                  <label><a href="<%=tableDataMainteUri%>?tableName=<%=relatedTableName%>"><span class="form-label fw-bold"><%=relatedTableLogicalName%>(<%=relatedTableName%>)</span><br /></a></label>
                  <table class="table table-bordered table-striped table-hover table-responsive text-nowrap w-auto">
                    <thead class="table-success">
                      <tr>
                        <% var foreignTableDefList = (ArrayList<LinkedHashMap<String, String>>) request.getAttribute("foreignTableDefList_" + relatedTableName);
                           String foreignTableName = foreignTableDefList.get(0).get("TABLE_NAME");
                           String foreignTablePriKeyField = foreignTableName + "_ID";
                           String foreignTableDescField = foreignTableDefList.get(0).get("DESC_FIELD");
                           for (LinkedHashMap<String, String> foreignTableDef : foreignTableDefList) {
                             if (foreignTableDef.get("FIELD_NAME").equals(foreignTablePriKeyField)
                                 || foreignTableDef.get("FIELD_NAME").equals(foreignTableDescField)) { %>
                          <th class="px-2 py-1"><%=foreignTableDef.get("FIELD_LOGICAL_NAME")%><br /><%=foreignTableDef.get("FIELD_NAME")%></th>
                        <%   } %>
                        <% } %>
                      </tr>
                    </thead>
                    <tbody>
                      <% var foreignTableRecordList = (ArrayList<LinkedHashMap<String, String>>) request.getAttribute("foreignTableRecordList_" + relatedTableName);
                         var editRecordUri = ((ArrayList<LinkedHashMap<String, String>>) request.getAttribute("editRecordUri")).get(0).get("URI_PATTERN");
                         for (LinkedHashMap<String, String> foreignTableRecord : foreignTableRecordList) { %>
                        <tr>
                          <td class="px-2 py-1"><a href="<%=editRecordUri%>?tableName=<%=foreignTableName%>&recordId=<%=foreignTableRecord.get(foreignTablePriKeyField)%>"><%=foreignTableRecord.get(foreignTablePriKeyField)%></a></td>
                          <td class="px-2 py-1"><%=foreignTableRecord.get(foreignTableDescField)%></td>
                        </tr>
                      <% } %>
                      <tr>
                        <td class="px-2 py-1"></td>
                      </tr>
                    </tbody>
                  </table>
                <% } %>
              </div>
            </div>
