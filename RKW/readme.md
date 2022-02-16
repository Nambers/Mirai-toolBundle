# 关键词撤回插件

配置几组不同权值的关键词和撤回阈值, 当一条消息权值累计超过(出现多个关键词相加大于或者重复出现多个关键词每个都算)阈值就撤回 因为是很早前写的了, 可能维护不会太频繁

第一组关键词权值是1, 第二组是2, 以此类推

比如关键词是: `[["a"], ["b"]]`, 撤回阈值是5 会撤回`aaaaaa`, 6个`a`(权值是6 = 6 * 1), `bbb`, 3个`b`(权值是6 = 3 * 2), 但是不会撤回`aaaaa`, 5个a(5 = 5 * 1)

下载: [Release](https://github.com/Nambers/Mirai-toolBundle/releases)
## 配置文件

```kotlin
data class Config(
    // 是否分析文本
    var readText: Boolean?,
    // 是否用百度云ocr api分析图片(需要在下面配置)
    var readPic: Boolean?,
    // 百度云ocr配置
    val baiduSetting: BaiduSetting?,
    // 撤回的时候通知群主, 默认false
    var notification: Boolean?,
    // 取消bot自己发送的消息如果超出阈值, 默认false
    val recallItSelf: Boolean?,
    // 撤回阈值, 权值累计到多少就撤回
    val MaxBorder: Int,
    // 不处理群聊信息, 默认false
    val blockGroupMessage: Boolean?,
    // 撤回的关键词, 每组关键词的权值=该组的下标
    var keyWords: List<List<String>>,
    // 0 = 撤回, 1 = 禁言 + 撤回, 2 = 禁言, 默认0, 其他不操作
    val type: Int?,
    // 禁言时间, 要在0s ~ 30d里面, 默认60s, 单位秒
    val muteTime: Int?,
    // 自动把全部英文转换成大写字母处理(包括关键词), 默认false
    val autoUpper: Boolean?,
    // 延时做出操作, 单位毫秒, 默认0
    val delay: Long? = 0L,
    // 撤回后随机取出其中之一发送
    val hints: List<String>? = emptyList()
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
  "MaxBorder":4, // 撤回阈值
  "blockGroupMessage":false, // 不屏蔽群聊信息 [可选配置]
  "keyWords":[
    ["啊"] // 撤回关键词, 权值为 1
  ],
  "notification":true, // 撤回是通知群主 [可选配置]
  "readPic":false, // 不检查图片 [可选配置] 默认 false
  "readText":true, // 检查文字 [可选配置] 默认false
  "recallItSelf":true, // 撤回机器人自己的信息如果超过撤回阈值 [可选配置]
  "autoUpper": true, // 自动把英文变成大写处理 [可选配置]
  "type": 0, // 仅撤回 [可选配置]
  "hints":["testA", "testB"] // 撤回后发送 [可选]
}
```
## 依赖
[ToolGood.Words - StringSearchEx2](https://github.com/toolgood/ToolGood.Words) 提取关键词 -  Apache-2.0 License

[gson](https://github.com/google/gson) - Apache-2.0 License
