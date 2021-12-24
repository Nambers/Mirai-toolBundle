/*
 * Copyright (c) 2020 - 2021. Eritque arcus and contributors.
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
    var keyWords: List<List<String>>
){
    // 百度云OCR设置
    data class BaiduSetting(
        val APP_ID:String,
        val API_KEY:String,
        val SECRET_KEY:String
    )
}