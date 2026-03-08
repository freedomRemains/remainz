### ビルドについて

---

[TOPに戻る](../README.md)

ビルドコマンドは、次の通り。
```
rem ビルドコマンド
cd [remainzのディレクトリパス]
chcp 65001
set JAVA_HOME=[Javaのディレクトリパス]
set JAVA_OPTS=-Dfile.encoding=UTF-8
gradlew clean
gradlew build

rem サンプル
cd C:\10_local\60_github\remainz
chcp 65001
set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-21.0.6.7-hotspot
set JAVA_OPTS=-Dfile.encoding=UTF-8
gradlew clean
gradlew build
```

---

[TOPに戻る](../README.md)
