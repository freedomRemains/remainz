<%@ page pageEncoding="UTF-8"%>

            <% if ("1000001".equals(account.get(0).get("ACCNT_ID"))) { %>
              <div class="<%=columnMap.get("HTML_PARTS_ID")%>">
                <table>
                  <tbody>
                    <tr>
                      <td>メールアドレス</td>
                      <td><input id="MAIL_ADDRESS" name="MAIL_ADDRESS" value="grandmaster@account.com"></td>
                    </tr>
                    <tr>
                      <td>パスワード</td>
                      <td><input id="PASSWORD" type="password" name="PASSWORD" value="password"></td>
                    </tr>
                  </tbody>
                </table>
                <input type="button" name="doPost" value="送信" onclick="submitMainForm()">
              </div>
            <% } %>
