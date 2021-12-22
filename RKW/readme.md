# 关键词撤回插件
配置几组不同权值的关键词和撤回阈值, 当一条消息权值累计超过(出现多个关键词相加大于或者重复出现多个关键词每个都算)阈值就撤回 因为是很早前写的了, 可能维护不会太频繁
## 配置文件
```kotlin
data class Config(
    // 是否分析文本
    var readText: Boolean?,
    // 是否用百度云ocr api分析图片(需要在下面配置)
    var readPic: Boolean?,
    // 百度云ocr配置
    val baiduSetting: BaiduSetting?,
    // 撤回的时候通知群主
    var notification: Boolean?,
    // 撤回阈值, 权值累计到多少就撤回
    val MaxBorder: Int,
    // 撤回的关键词, 每组关键词的权值=该组的下标
    var keyWords: List<List<String>>
){
    // 百度云OCR设置
    data class BaiduSetting(
        val APP_ID:String,
        val API_KEY:String,
        val SECRET_KEY:String
    )
}
```
## 依赖
[ToolGood.Words - StringSearchEx2](https://github.com/toolgood/ToolGood.Words) 提取关键词 -  Apache-2.0 License

[gson](https://github.com/google/gson) - Apache-2.0 License