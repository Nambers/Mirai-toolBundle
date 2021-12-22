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

import com.baidu.aip.ocr.AipOcr
import com.google.gson.Gson
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.contact.PermissionDeniedException
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.globalEventChannel
import net.mamoe.mirai.message.code.MiraiCode
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.Image.Key.queryUrl
import net.mamoe.mirai.message.data.MessageSource.Key.recall
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.source
import org.json.JSONArray
import org.json.JSONObject
import toolgood.words.StringSearchEx2
import java.io.File
import java.net.URL
import java.util.zip.GZIPInputStream


object Ocr {
    //设置APPID/AK/SK
    internal var APP_ID = ""
    internal var API_KEY = ""
    internal var SECRET_KEY = ""

    // 初始化一个AipOcr
    private val client by lazy{ AipOcr(APP_ID, API_KEY, SECRET_KEY) }

    // 可选：设置网络连接参数
//    client.setConnectionTimeoutInMillis(2000)
//    client.setSocketTimeoutInMillis(60000)
    @JvmStatic
    fun main(image:String): String {
        // 传入可选参数调用接口
        val options = HashMap<String, String>()
        options["detect_direction"] = "true"
        options["probability"] = "true"

        // 参数为本地图片路径
        val res = this.client.basicGeneral(image, options)
        val obj = JSONObject(res.toString(2))
        var temp = ""
        val it: JSONArray = obj["words_result"] as JSONArray
        for (i in 0 until it.length()){
            temp +=  (it.get(i) as JSONObject).getString("words")
        }
        return temp
    }
}

//下载图片
fun downloadImage(url: String, file: File): File {
    val openConnection = URL(url).openConnection()
    //防止某些网站跳转到验证界面
    openConnection.addRequestProperty("user-agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3729.169 Safari/537.36")
    //如果图片是采用gzip压缩
    val bytes = if (openConnection.contentEncoding == "gzip") {
        GZIPInputStream(openConnection.getInputStream()).readBytes()
    } else {
        openConnection.getInputStream().readBytes()
    }
    file.writeBytes(bytes)
    return file
}

object PluginMain : KotlinPlugin(
    JvmPluginDescription(
        id = "tech.eritquearcus.RKW",
        name="RecallKeyWords",
        version = "1.3.0"
    )
) {
    private var seachers: ArrayList<StringSearchEx2> = ArrayList()

    //图片结果缓存
    private var imgCache: Map<String, String> = mapOf()
    override fun onEnable() {
        logger.info("Keywords recall plugin loaded!")
        val f = File(dataFolder.absolutePath + "/config.json").let {
            if(!it.isFile || !it.exists()) {
                logger.error("配置文件(${it.absolutePath})不存在, 自动生成并结束加载插件")
                it.writeText(Gson().toJson(Config(true, false, null, false, 5, listOf(emptyList()))))
                return
            } else
                it
        }
        val config = Gson().fromJson(f.readText(), Config::class.java)
        if(config.readPic == null)
            config.readPic = false
        if(config.readText == null)
            config.readText = false
        if(config.notification == null)
            config.notification = false
        if(config.readPic!!)
            if(config.baiduSetting == null){
                logger.error("百度ocr未设置, 读取图片开关关闭")
                config.readPic = false
            }else{
                Ocr.API_KEY = config.baiduSetting.API_KEY
                Ocr.APP_ID = config.baiduSetting.APP_ID
                Ocr.SECRET_KEY = config.baiduSetting.SECRET_KEY
            }
        logger.info("配置文件路径${dataFolder.absolutePath}/config.txt")
        logger.info("文字识别开关${config.readText}")
        logger.info("图片识别开关${config.readPic}")
        logger.info("撤回边界值${config.MaxBorder}")
        logger.info("目前关键词有:${config.keyWords}")
        for(a in config.keyWords){
            val tmp = StringSearchEx2()
            tmp.SetKeywords(a)
            seachers.add(tmp)
        }
        if(!File(dataFolder.absolutePath + "/Imgcache/").exists())
            File(dataFolder.absolutePath + "/Imgcache/").mkdir()
        globalEventChannel().subscribeAlways<GroupMessageEvent> {
            var plainAll = ""
            var picAll = ""
            message.forEach {
                if (it is PlainText && config.readText!!) plainAll += it.contentToString()
                if (it is Image && config.readPic!!) {
                    //图片处理
                    val id = it.imageId.split(".")[0]
                    val value = imgCache[id]
                    if (value != null) {
                        picAll += value
                        logger.info(picAll)
                    } else {
                        val url = it.queryUrl()
                        logger.info("取到图片${url}")
                        //用BinaryTest.Excute()来二值化
                        val temp: File = downloadImage(url, File(dataFolder.absolutePath + "/Imgcache/$id.jpg"))
                        val tempa = Ocr.main(dataFolder.absolutePath + "/Imgcache/$id.jpg")
                            .replace("\n", "")//取消换行
                            .replace(" ", "")//取消空格
                        logger.info("结果$tempa")
                        imgCache = imgCache.plus(mapOf(id to tempa))
                        picAll += tempa
                        //自动删除图片缓存
                        temp.delete()
                    }
                }
            }
            var allp = 0
            if (config.readText!! || config.readPic!!) {
                var i = 0
                for (sc in seachers) {
                    i += 1
                    allp += sc.FindAll(plainAll).size * i + sc.FindAll(picAll).size * i
                    if (allp > config.MaxBorder) {
                        try {
                            message.source.recall()
                        } catch (e: PermissionDeniedException) {
                            logger.warning("撤回失败:机器人无权限")
                        } catch (e: IllegalStateException) {
                            logger.warning("撤回失败:消息已撤回或对方权限比bot还高")
                        }
                        if(config.notification!!)
                            this.group.owner.sendMessage(MiraiCode.deserializeMiraiCode("[群${this.group.id}]撤回违规信息[${this.message.serializeToMiraiCode()}]来自群成员[${this.sender.id}]"))
                        return@subscribeAlways
                    }
                }
            }
        }
    }
}
