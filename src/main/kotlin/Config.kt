package tech.eritquearcus.mirai.plugin.kwr

data class Config(
    var readText: Boolean?,
    var readPic: Boolean?,
    val baiduSetting: BaiduSetting?,
    var notification: Boolean?,
    val MaxBorder: Int,
    var keyWords: List<List<String>>
){
    data class BaiduSetting(
        val APP_ID:String,
        val API_KEY:String,
        val SECRET_KEY:String
    )
}