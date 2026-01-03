# 実行可能jarの作成方法について

---

[TOPに戻る](../README.md)

ロジックの全部もしくは一部を抽出して実行可能jarとする可能性があるため、AI回答を記録する。

Spring Boot なしで「ダブルクリックで実行できる実行可能 JAR」を作る方法はいくつかあります。昔ながらの Java アプリの作り方に近いので、POJO プログラムでも簡単にできます。

以下では **Gradle** と **Maven** の両方での作り方を、できるだけシンプルにまとめます。

---

# 🚀 実行可能 JAR（Executable JAR）の作り方（Spring Boot なし）

## 1. 必須条件：`Main-Class` を MANIFEST に書く
Java の実行可能 JAR は、`META-INF/MANIFEST.MF` に次のような記述が必要です。

```
Main-Class: com.example.Main
```

これがあると、JAR をダブルクリックしたときに Java がどのクラスを起動すればよいか分かります。

---

# 🟦 Gradle（Java プラグイン）で作る方法

## ① `build.gradle` に manifest を設定する

```gradle
plugins {
    id 'java'
}

jar {
    manifest {
        attributes(
            'Main-Class': 'com.example.Main'
        )
    }

    // 依存ライブラリを JAR にまとめたい場合（fat jar）
    from {
        configurations.runtimeClasspath.collect { it.isDirectory() ? it : zipTree(it) }
    }
}
```

### 📌 ポイント
- `Main-Class` を指定するだけで実行可能 JAR になる  
- 依存ライブラリがある場合は `fat jar`（いわゆる Uber JAR）にする必要がある  
- `zipTree` を使う方法は昔ながらの定番

---

## ② 実行用の Main クラスを書く

```java
package com.example;

public class Main {
    public static void main(String[] args) {
        System.out.println("Hello Executable JAR!");
    }
}
```

---

## ③ ビルド

```
./gradlew jar
```

生成物：

```
build/libs/yourapp.jar
```

---

## ④ ダブルクリックで実行
Windows / macOS なら JAR をダブルクリックで起動できます。

CLI なら：

```
java -jar build/libs/yourapp.jar
```

---

# 🟧 Maven で作る方法

Maven なら `maven-jar-plugin` を使います。

```xml
<build>
  <plugins>
    <plugin>
      <groupId>org.apache.maven.plugins</groupId>
      <artifactId>maven-jar-plugin</artifactId>
      <version>3.3.0</version>
      <configuration>
        <archive>
          <manifest>
            <mainClass>com.example.Main</mainClass>
          </manifest>
        </archive>
      </configuration>
    </plugin>

    <!-- 依存ライブラリをまとめる場合（fat jar） -->
    <plugin>
      <groupId>org.apache.maven.plugins</groupId>
      <artifactId>maven-assembly-plugin</artifactId>
      <version>3.6.0</version>
      <configuration>
        <descriptorRefs>
          <descriptorRef>jar-with-dependencies</descriptorRef>
        </descriptorRefs>
        <archive>
          <manifest>
            <mainClass>com.example.Main</mainClass>
          </manifest>
        </archive>
      </configuration>
      <executions>
        <execution>
          <id>make-assembly</id>
          <phase>package</phase>
          <goals>
            <goal>single</goal>
          </goals>
        </execution>
      </executions>
    </plugin>
  </plugins>
</build>
```

ビルド：

```
mvn package
```

生成物：

```
target/yourapp-jar-with-dependencies.jar
```

---

# 🟩 依存ライブラリがない POJO ならもっと簡単

もし依存ライブラリが一切ないなら、manifest に `Main-Class` を書くだけで OK。

Gradle:

```gradle
jar {
    manifest {
        attributes 'Main-Class': 'com.example.Main'
    }
}
```

これだけで実行可能 JAR になります。

---

# 🟪 さらに簡単にしたい場合：Shadow Plugin（Gradle）

Gradle なら **Shadow Plugin** を使うと fat jar が超簡単。

```gradle
plugins {
    id 'java'
    id 'com.github.johnrengelman.shadow' version '8.1.1'
}

shadowJar {
    manifest {
        attributes 'Main-Class': 'com.example.Main'
    }
}
```

ビルド：

```
./gradlew shadowJar
```

生成物：

```
build/libs/yourapp-all.jar
```

---

# 🎯 まとめ

| 方法 | 特徴 |
|------|------|
| **Gradle + jar タスク** | 最もシンプル。POJO ならこれで十分 |
| **Gradle + Shadow Plugin** | 依存ライブラリが多い場合に便利 |
| **Maven + maven-jar-plugin** | Maven 標準の方法 |
| **Maven + assembly plugin** | fat jar を作る定番 |

---

# サーブレットプログラムを実行可能jarとする方法

結論から言うと **サーブレットのように main() を持たないプログラムは、そのままでは実行可能 JAR にはできません**。  
理由はとてもシンプルで、**Java の JAR をダブルクリックして起動するには、必ず Main-Class が必要だから**です。

ただし、サーブレットが動く仕組みを理解すると「なぜ Spring Boot の Web アプリには main があるのか」もスッキリ腑に落ちます。

---

# 🔍 なぜサーブレットは main() がないのに動くのか

サーブレットは **Java アプリケーションではなく、Web サーバ（Servlet Container）にロードされる “プラグイン”** だからです。

つまり：

- Tomcat / Jetty / WildFly などの **サーバ側に main() がある**
- サーブレットは **サーバに読み込まれて動く部品**

という構造になっています。

図にするとこうです：

```
[あなたのサーブレット]  ← main()なし
        ↑
[Tomcat/Jetty の main()] が起動してロードする
```

だからサーブレット単体では実行できず、Tomcat などにデプロイする必要があるわけです。

---

# 🟦 Spring Boot の Web アプリに main() がある理由

Spring Boot は **Tomcat（または Jetty）をアプリ内に組み込んでしまう** という発想で作られています。

つまり：

```
あなたのアプリ
 └─ main()
      └─ Spring Boot が組み込み Tomcat を起動
            └─ サーブレットやコントローラをロード
```

この構造により、**Web アプリなのに単体で実行できる JAR** が作れるわけです。

---

# 🟥 main() がないサーブレットを「実行可能 JAR」にできるか？

## ❌ そのままでは不可能

理由：

- 実行可能 JAR には **Main-Class が必須**
- サーブレットは **サーバにロードされる前提** で作られている

---

# 🟩 ではどうすれば実行可能にできるのか？

## ✔️ 方法1：自分で “小さな main()” を書いて組み込みサーバを起動する

Spring Boot を使わずに、**自前で Jetty や Tomcat を組み込む**方法があります。

### 例：組み込み Jetty を使う（最小構成）

```java
public class Main {
    public static void main(String[] args) throws Exception {
        Server server = new Server(8080);

        ServletContextHandler handler = new ServletContextHandler();
        handler.addServlet(MyServlet.class, "/hello");

        server.setHandler(handler);
        server.start();
        server.join();
    }
}
```

これを JAR にすれば、ダブルクリックでサーブレットが動きます。

---

## ✔️ 方法2：組み込み Tomcat を使う

```java
public class Main {
    public static void main(String[] args) throws Exception {
        Tomcat tomcat = new Tomcat();
        tomcat.setPort(8080);

        Context ctx = tomcat.addContext("", new File(".").getAbsolutePath());
        Tomcat.addServlet(ctx, "myServlet", new MyServlet());
        ctx.addServletMappingDecoded("/hello", "myServlet");

        tomcat.start();
        tomcat.getServer().await();
    }
}
```

これも実行可能 JAR にできます。

---

# 🟩 まとめ：サーブレットを実行可能 JAR にするには？

| 方法 | main() 必要？ | 説明 |
|------|--------------|------|
| **サーブレット単体** | ❌ | サーバにロードされる前提なので不可 |
| **Spring Boot** | ✔️ | main() が組み込みサーバを起動する |
| **自作 main() + Jetty/Tomcat** | ✔️ | Spring Boot の簡易版を自作するイメージ |
| **WAR を Tomcat にデプロイ** | ❌ | 実行可能 JAR ではなくサーバ側が main() を持つ |

---

# new Tomcat(); でTomcatを起動するために必要な依存関係ライブラリの取り込み方法

`new Tomcat()` や `new Server()`（Jetty）といった「組み込みサーバを自前で new する」コードを書くには、**そのサーバのライブラリを依存関係として追加する必要があります**。

ここを押さえると、Spring Boot が裏で何をやっているのかもよりクリアになります。

---

# 🟦 1. 組み込み Tomcat を使う場合の依存関係

`new Tomcat()` を使うには **Tomcat の embedded 版** を入れます。

## Gradle

```gradle
dependencies {
    implementation 'org.apache.tomcat.embed:tomcat-embed-core:10.1.0'
    implementation 'org.apache.tomcat.embed:tomcat-embed-jasper:10.1.0' // JSP使う場合
    implementation 'jakarta.servlet:jakarta.servlet-api:6.0.0'
}
```

### 必要な理由
- `tomcat-embed-core` → `org.apache.catalina.startup.Tomcat` が入っている  
- `jakarta.servlet-api` → `HttpServlet` などサーブレット API が入っている  
- `tomcat-embed-jasper` → JSP を使う場合のみ必要

---

# 🟧 2. 組み込み Jetty を使う場合の依存関係

`new Server()` を使うには Jetty の core を入れます。

## Gradle

```gradle
dependencies {
    implementation 'org.eclipse.jetty:jetty-server:11.0.15'
    implementation 'org.eclipse.jetty:jetty-servlet:11.0.15'
    implementation 'jakarta.servlet:jakarta.servlet-api:6.0.0'
}
```

### 必要な理由
- `jetty-server` → `Server` クラスが入っている  
- `jetty-servlet` → `ServletContextHandler` などが入っている  
- `jakarta.servlet-api` → サーブレット API

---

# 🟩 3. なぜ Spring Boot は依存関係を意識させないのか？

Spring Boot の Web Starter（`spring-boot-starter-web`）には、以下がすべて含まれています：

- 組み込み Tomcat（または Jetty/Undertow）
- サーブレット API
- WebMVC の初期化コード
- 自動設定（AutoConfiguration）

つまり Spring Boot は **「Web サーバを new するための依存関係」＋「起動コード」** を全部まとめて提供しているわけです。

---

# 🟪 4. 依存関係を入れれば、サーブレットを自前で動かせる

依存関係さえ入れれば、次のようなコードが普通に動きます。

### Tomcat 例

```java
Tomcat tomcat = new Tomcat();
tomcat.setPort(8080);

Context ctx = tomcat.addContext("", new File(".").getAbsolutePath());
Tomcat.addServlet(ctx, "myServlet", new MyServlet());
ctx.addServletMappingDecoded("/hello", "myServlet");

tomcat.start();
tomcat.getServer().await();
```

---

# 🎯 まとめ

| サーバ | 必要な依存関係 | 備考 |
|--------|----------------|------|
| **Tomcat Embedded** | `tomcat-embed-core` + `jakarta.servlet-api` | JSP 使うなら `tomcat-embed-jasper` |
| **Jetty Embedded** | `jetty-server` + `jetty-servlet` + `jakarta.servlet-api` | 軽量でシンプル |
| **Spring Boot** | 依存関係を全部まとめて提供 | main() で組み込みサーバを起動 |

---

[TOPに戻る](../README.md)
