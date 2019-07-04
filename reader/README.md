## BookReader 文本阅读器
### 权限
```
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
```
### 混淆配置(可选)：
```
    -keep class gapp.season.reader.** {*;}
```
### 接入方法
```
    先将SDK的aar包导入工程；(打aar包：执行AS的Gradle工具中找到对应Module-Tasks-build-assembleRelease命令)
    Application的onCreate方法中初始化一些配置项： BookReader.config(isdev, pageTheme, bookDir); (可选)
    阅读书籍：BookReader.readBook(context, bookFilePath); (bookFilePath允许传空)
```

#### 存在的问题及解决方法
```
UI：界面UI待优化
搜索：搜索功能待增加
书签：字体大小和行首缩进的改变会影响书签定位的准确性（记录书签字符index替换掉记录行index，而这样会牺牲一些性能）
性能：对于特大文件和单行文字数量特多的情景，会有性能问题（限制文件大小20M，限制单行字数20000）
备注：android:background="?attr/selectableItemBackground" 用低版本编译时会报错（换用自定义drawable）
```
