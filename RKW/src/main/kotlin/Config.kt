package tech.eritquearcus.mirai.plugin.rkw

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