<%@ page pageEncoding="UTF-8"%>

            <% if ("1000001".equals(account.get(0).get("ACCNT_ID"))) { %>
              <div class="<%=columnMap.get("HTML_PARTS_ID")%>">
                <table>
                  <tbody>
                    <tr>
                      <td class=" px-4 py-2">メールアドレス</td>
                      <td class=" px-4 py-2"><input id="MAIL_ADDRESS" name="MAIL_ADDRESS" value="grandmaster@account.com"></td>
                    </tr>
                    <tr>
                      <td class=" px-4 py-2">パスワード</td>
                      <td class=" px-4 py-2"><input id="PASSWORD" type="password" name="PASSWORD" value="password"></td>
                    </tr>
                  </tbody>
                </table>
                <button class="btn btn-primary px-4 py-2" onclick="submitMainForm()">サインイン</button>
              </div>
            <% } %>
