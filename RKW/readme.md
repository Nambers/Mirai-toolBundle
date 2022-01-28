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
    // 取消bot自己发送的消息如果超出阈值
    val recallItSelf: Boolean?,
    // 撤回阈值, 权值累计到多少就撤回
    val MaxBorder: Int,
    // 不处理群聊信息
    val blockGroupMessage: Boolean?,
    // 撤回的关键词, 每组关键词的权值=该组的下标
    var keyWords: List<List<String>>,
    // 0 = 撤回, 1 = 禁言 + 撤回, 2 = 禁言, 默认0
    val type: Int?,
    // 禁言时间, 要在0s ~ 30d里面
    val muteTime: Int?
) {
    // 百度云OCR设置
    data class BaiduSetting(
        val APP_ID: String,
        val API_KEY: String,
        val SECRET_KEY: String
    )
}
```
示例配置文件:
```
// 下面的 `//` 只是解释作用, 实际上的json不能出现注释
{
  "readText": true, // 检查文本信息
  "notification": false, // 不提醒群主
  "MaxBorder": 5, // 权值累计超过5就撤回
  "keyWords": [
    [a, b, c], // 权重为1的关键词组
    [dddadas] // 权值为2
  ]
}
```
## 依赖
[ToolGood.Words - StringSearchEx2](https://github.com/toolgood/ToolGood.Words) 提取关键词 -  Apache-2.0 License

[gson](https://github.com/google/gson) - Apache-2.0 License