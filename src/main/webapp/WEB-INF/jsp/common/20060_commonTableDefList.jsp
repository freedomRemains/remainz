<%@ page pageEncoding="UTF-8"%>

            <div class="<%=columnMap.get("HTML_PARTS_ID")%>">
              <div class="p-2">
                <% var tableDefList = (ArrayList<LinkedHashMap<String, String>>) request.getAttribute("tableDefList");
                   var tableName = tableDefList.get(0).get("TABLE_NAME"); %>
                <div class="p-2">
                  <label><span class="form-label fw-bold"><%=tableDefList.get(0).get("TABLE_LOGICAL_NAME") + "(" + tableDefList.get(0).get("TABLE_NAME") + ")"%></span><br /></label>
                </div>
                <table class="table table-bordered table-striped table-hover table-responsive text-nowrap">
                  <thead class="table-success">
                    <tr>
                      <th class="px-1 py-0">DB項目論理名</th>
                      <th class="px-1 py-0">DB項目物理名</th>
                      <th class="px-1 py-0">型</th>
                    </tr>
                  </thead>
                  <tbody>
                    <% var columnList = (ArrayList<LinkedHashMap<String, String>>) request.getAttribute("columnList");
                       for (LinkedHashMap<String, String> column : columnList) {
                         String fieldName = column.get("FIELD_NAME");
                         String fieldLogicalName = column.get("FIELD_LOGICAL_NAME");
                         String typeName = column.get("TYPE_NAME"); %>
                      <tr>
                        <td class="px-1 py-0"><%=fieldLogicalName%></td>
                        <td class="px-1 py-0"><%=fieldName%></td>
                        <td class="px-1 py-0"><%=typeName%></td>
                      </tr>
                    <% } %>
                  </tbody>
                </table>
              </div>
            </div>
            <input type="hidden" name="tableName" value="<%=tableName%>">
