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

import com.baidu.aip.ocr.AipOcr
import kotlinx.coroutines.delay
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.permission.PermissionId
import net.mamoe.mirai.console.permission.PermissionService
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.PermissionDeniedException
import net.mamoe.mirai.event.GlobalEventChannel
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MessagePostSendEvent
import net.mamoe.mirai.event.events.MessagePreSendEvent
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.message.data.MessageSource.Key.recall
import org.json.JSONArray
import org.json.JSONObject
import toolgood.words.StringSearchEx2
import java.io.File
import java.util.*
import kotlin.collections.ArrayDeque


object Ocr {
    //设置APPID/AK/SK
    internal var APP_ID = ""
    internal var API_KEY = ""
    internal var SECRET_KEY = ""

    // 初始化一个AipOcr
    private val client by lazy { AipOcr(APP_ID, API_KEY, SECRET_KEY) }

    // 可选：设置网络连接参数
//    client.setConnectionTimeoutInMillis(2000)
//    client.setSocketTimeoutInMillis(60000)
    @JvmStatic
    fun main(image: String): String {
        // 传入可选参数调用接口
        val options = HashMap<String, String>()
        options["detect_direction"] = "true"
        options["probability"] = "true"

        // 参数为本地图片路径
        val res = this.client.basicGeneral(image, options)
        val obj = JSONObject(res.toString(2))
        var temp = ""
        val it: JSONArray = obj["words_result"] as JSONArray
        for (i in 0 until it.length()) {
            temp += (it.get(i) as JSONObject).getString("words")
        }
        return temp
    }
}

object PluginMain : KotlinPlugin(JvmPluginDescription(
    id = "tech.eritquearcus.RKW", name = "RecallKeyWords", version = "1.4.3"
) {
    author("Eritque arcus")
}) {
    var seachers: ArrayList<StringSearchEx2> = ArrayList()
    private val unFilterMsg: ArrayDeque<Message> = ArrayDeque()

    @OptIn(ConsoleExperimentalApi::class)
    val perm: PermissionId by lazy {
        PermissionService.INSTANCE.allocatePermissionIdForPlugin(PluginMain, "ch")
    }

    //图片结果缓存
    var imgCache: Map<String, String> = mapOf()
    override fun onEnable() {
        logger.info("Keywords recall plugin loaded!")
        if (Config.readPic) {
            if (Config.baiduSetting.APP_ID.isBlank()) {
                logger.error("百度ocr未设置, 读取图片开关关闭")
                Config.readPic = false
            } else {
                Ocr.API_KEY = Config.baiduSetting.API_KEY
                Ocr.APP_ID = Config.baiduSetting.APP_ID
                Ocr.SECRET_KEY = Config.baiduSetting.SECRET_KEY
            }
        }
        // register
        perm
        Recall.register()
        logger.info("撤回权限名: ${perm.name}")
        logger.info("配置文件路径${dataFolder.absolutePath}/Config.txt")
        logger.info("文字识别开关${Config.readText}")
        logger.info("图片识别开关${Config.readPic}")
        logger.info("撤回边界值${Config.MaxBorder}")
        logger.info("目前关键词有:${Config.keyWords}")
        logger.info("处理模式:${if (Config.type == 1) "撤回 + 禁言" else if (Config.type == 0) "撤回" else if (Config.type == 2) "禁言" else "不处理"}")
        logger.info("自动大写:${Config.autoUpper}")
        for (a in Config.keyWords) {
            val tmp = StringSearchEx2()
            if (Config.autoUpper) {
                val b = mutableListOf<String>()
                a.forEach { b.add(it.uppercase(Locale.getDefault())) }
                tmp.SetKeywords(b)
            } else tmp.SetKeywords(a)
            seachers.add(tmp)
        }
        if (!File(dataFolder.absolutePath + "/Imgcache/").exists()) File(dataFolder.absolutePath + "/Imgcache/").mkdir()
        if (Config.recallItSelf) GlobalEventChannel.subscribeAlways<MessagePreSendEvent> {
            if (unFilterMsg.isNotEmpty() && Config.notification && this.message in unFilterMsg) {
                // 如果在自己发的不被撤回的list中
                unFilterMsg.remove(this.message)
            } else if ((Config.readText || Config.readPic) && this.message.toMessageChain().toText()
                    .excessBorder()
            ) {
                logger.info((if (Config.delay != 0L) "在${Config.delay}ms后" else "") + "取消:${this.message.contentToString()}的发送(如果是0s可能下面会抛出异常)")
                if (Config.delay == 0L) this.cancel()
                else
                    GlobalEventChannel.subscribeOnce<MessagePostSendEvent<Contact>> {
                        delayRecall(this.receipt, this@subscribeAlways.target, this.target)
                    }
            }
        }
        if (!Config.blockGroupMessage) GlobalEventChannel.subscribeAlways<GroupMessageEvent> {
            if ((Config.readText || Config.readPic) && this.message.toText().excessBorder()) {
                delay(Config.delay)
                if (Config.type == 0 || Config.type == 1) try {
                    message.source.recall()
                } catch (e: PermissionDeniedException) {
                    logger.warning("撤回失败:机器人无权限")
                } catch (e: IllegalStateException) {
                    logger.warning("撤回失败:消息已撤回")
                }
                if (Config.type == 1 || Config.type == 2) try {
                    sender.mute(Config.muteTime)
                } catch (e: PermissionDeniedException) {
                    logger.warning("禁言失败:机器人无权限")
                } catch (e: IllegalStateException) {
                    logger.error("禁言失败:禁言时间需要在0s ~ 30d, 当前是:${Config.muteTime}s")
                }
                if (Config.hints.isNotEmpty()) {
                    val msg = MessageChainBuilder()
                        .append(this.sender.at())
                        .append(PlainText(Config.hints.random()))
                        .build()
                    unFilterMsg.addFirst(msg)
                    this.group.sendMessage(msg)
                }
                if (Config.notification) {
                    val msg = buildForwardMessage(this.group) {
                        this.add(bot, PlainText("[群${this@subscribeAlways.group.id}]撤回违规信息"))
                        this.add(this@subscribeAlways.sender, this@subscribeAlways.message)
                        this.add(bot, PlainText("来自群成员[${this@subscribeAlways.sender.id}]"))
                    }
                    unFilterMsg.addFirst(msg)
                    this.group.owner.sendMessage(msg)
                }
            }
        }
    }
}
