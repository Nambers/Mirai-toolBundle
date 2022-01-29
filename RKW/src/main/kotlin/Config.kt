/*
 * Copyright (c) 2020 - 2022. Eritque arcus and contributors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or any later version(in your opinion).
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package tech.eritquearcus.mirai.plugin.rkw

data class Config(
    // 是否分析文本
    var readText: Boolean? = false,
    // 是否用百度云ocr api分析图片(需要在下面配置)
    var readPic: Boolean? = false,
    // 百度云ocr配置
    val baiduSetting: BaiduSetting? = null,
    // 撤回的时候通知群主
    var notification: Boolean? = false,
    // 取消bot自己发送的消息如果超出阈值
    val recallItSelf: Boolean? = false,
    // 撤回阈值, 权值累计到多少就撤回
    val MaxBorder: Int,
    // 不处理群聊信息
    val blockGroupMessage: Boolean? = false,
    // 撤回的关键词, 每组关键词的权值=该组的下标
    var keyWords: List<List<String>>,
    // 0 = 撤回, 1 = 禁言 + 撤回, 2 = 禁言, 默认0
    val type: Int? = 0,
    // 禁言时间, 要在0s ~ 30d里面
    val muteTime: Int? = 60,
    // 全部英文转成大写字母, 默认false
    val autoUpper: Boolean? = false
){
    // 百度云OCR设置
    data class BaiduSetting(
        val APP_ID:String,
        val API_KEY:String,
        val SECRET_KEY:String
    )
}