承知しました。
ここでは、**Eclipseでレトロな（Servlet 2.x〜3.x 時代の）サーブレットアプリをGradle対応で作成する方法**を、初心者にも分かりやすく、丁寧に説明しますね。

---

## 🧭 ゴール

Eclipse上で以下のようなプロジェクトを作成します。

* プロジェクト構成：`Gradle + Java + Servlet`
* ビルドツール：Gradle（Eclipse Buildshipでサポート）
* 実行サーバー：Tomcat（例：Tomcat 9）
* サーブレットを`http://localhost:8080/hello`などで動かせる

---

## 🪜 手順概要

1. **Eclipse環境の準備**
2. **Gradleプロジェクトの作成**
3. **Java EE（Servlet）ライブラリをGradleで追加**
4. **サーブレットクラスと`web.xml`を作成**
5. **Tomcat設定と実行**

---

## ① Eclipse環境の準備

以下を確認してください：

| 項目              | 推奨バージョン                                                 | 備考                |
| --------------- | ------------------------------------------------------- | ----------------- |
| **Eclipse IDE** | Eclipse IDE for Enterprise Java and Web Developers（最新版） | 「Gradleサポート」入り    |
| **Java JDK**    | JDK 11 〜 17                                             | Tomcat 9に合う範囲     |
| **Tomcat**      | 9.x                                                     | Servlet 4.0対応、安定版 |
| **Gradle**      | Eclipseが自動でラップするため、ローカルにあってもOK                          | 不要でも動作可能          |

---

## ② Gradleプロジェクトを作成

1. Eclipseを起動
2. メニューから：

   ```
   File → New → Gradle Project
   ```
3. 「Project name」例：`retro-servlet-demo`
4. 「Use default location」にチェック
5. 「Finish」

これで、`build.gradle`を持つGradleプロジェクトができます。

---

## ③ build.gradle の設定

`build.gradle`を以下のように編集します。

```gradle
plugins {
    id 'java'
    id 'war'
    id 'eclipse'
}

group = 'com.example'
version = '1.0-SNAPSHOT'
sourceCompatibility = '11'

repositories {
    mavenCentral()
}

dependencies {
    // Servlet API（コンパイル時のみ必要。Tomcat実行時に提供される）
    providedCompile 'javax.servlet:javax.servlet-api:4.0.1'

    // JSTL（JSPで必要なら）
    implementation 'javax.servlet:jstl:1.2'
}

tasks.withType(JavaCompile) {
    options.encoding = 'UTF-8'
}

war {
    archiveFileName = 'retro-servlet-demo.war'
}
```

📌 **ポイント**

* `providedCompile` は「Tomcatに同梱されるので、実行時は含めない」指定。
* `war` プラグインを使うことでWARファイルが出力されます。

---

## ④ webapp 構成を追加

Gradleではデフォルトで以下の構造が想定されています。

```
src
 └ main
    ├ java
    │   └ com.example
    │       └ HelloServlet.java
    ├ resources
    └ webapp
        └ WEB-INF
            └ web.xml
```

以下を手動で作成してください。

---

### 📄 `HelloServlet.java`

`src/main/java/com/example/HelloServlet.java`

```java
package com.example;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class HelloServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("text/html;charset=UTF-8");
        PrintWriter out = resp.getWriter();
        out.println("<html><body>");
        out.println("<h2>Hello, Servlet!</h2>");
        out.println("</body></html>");
    }
}
```

---

### 📄 `web.xml`

`src/main/webapp/WEB-INF/web.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<web-app xmlns="http://xmlns.jcp.org/xml/ns/javaee"
         version="3.1">
  
  <servlet>
    <servlet-name>helloServlet</servlet-name>
    <servlet-class>com.example.HelloServlet</servlet-class>
  </servlet>

  <servlet-mapping>
    <servlet-name>helloServlet</servlet-name>
    <url-pattern>/hello</url-pattern>
  </servlet-mapping>

</web-app>
```

---

## ⑤ Tomcat設定と実行

1. Eclipseのメニューから：

   ```
   Window → Preferences → Server → Runtime Environments → Add
   ```
2. `Apache Tomcat 9`を選び、Tomcatのインストールフォルダを指定
3. `OK`

次に：

1. プロジェクトを右クリック → **Run As → Run on Server**
2. 「Tomcat 9」を選択して起動

---

## ⑥ 動作確認

ブラウザで以下にアクセス：

```
http://localhost:8080/retro-servlet-demo/hello
```

→ 「Hello, Servlet!」と表示されれば成功です 🎉

---

## 💡 補足

* JSPも使いたい場合は、`src/main/webapp/` に `index.jsp` を置けばTomcatで自動処理されます。
* `web.xml`を省略したい場合はServlet 3.x以降のアノテーション (`@WebServlet`) でもOKです。

  ```java
  @WebServlet("/hello")
  public class HelloServlet extends HttpServlet { ... }
  ```

---

ご希望があれば、
次のステップとして「このプロジェクトをGradleコマンドでビルド・WAR作成・Tomcatに自動デプロイする方法」もご紹介できます。

続けて知りたいですか？
