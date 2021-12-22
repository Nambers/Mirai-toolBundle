package tech.eritquearcus.mirai.plugin.pfs

import com.google.gson.Gson
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.contact.PermissionDeniedException
import net.mamoe.mirai.event.GlobalEventChannel
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.MessageSource
import net.mamoe.mirai.message.data.MessageSource.Key.recall
import java.io.File
import java.util.*
import java.util.Calendar



object PluginMain : KotlinPlugin(
    JvmPluginDescription(
        id = "tech.eritquearcus.pfs",
        name = "防刷屏",
        version = "1.3.0"
    )
) {
    data class Msg(
        val hash: Int,
        var num: Int,
        val source: ArrayList<MessageSource> = ArrayList()
    )
    // short term cache
    private val STCache:MutableMap<Long, Msg> = mutableMapOf()
    // long term cache
    val LTCache = ArrayList<Msg>()
    fun getTime(hour:Int, min:Int, sec:Int): Date {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, min)
        calendar.set(Calendar.SECOND, sec)
        return calendar.time
    }
    override fun onEnable() {
        val file = File("${dataFolder.absolutePath}\\config.json")
        logger.info("配置文件目录${dataFolder.absolutePath}\\config.json")
        val config = if(file.isFile && file.exists())
            Gson().fromJson(file.readText(), Config::class.java)
        else {
            logger.warning("找不到配置文件, 使用默认文件")
            Config(null, null, null, null, null)
        }
        val n = config.max ?: 4
        val h = config.hour ?: 20
        val m = config.min ?: 0
        val s = config.sec ?: 0
        val period:Long = 86400000
        Timer().schedule(object:TimerTask(){
            override fun run() {
                logger.warning("警告:清空长缓存")
                LTCache.clear()
            }
        }, getTime(h, m, s), period)
        logger.info("清空缓存任务启动于${getTime(h, m, s)}，每隔${period}ms执行一次")
        logger.info("撤回阈值为$n")
        GlobalEventChannel.subscribeAlways<GroupMessageEvent> {
            val msg = this.message.serializeToMiraiCode().hashCode()
            for(l in LTCache){
                //在长缓存中
                if(l.hash == msg){
                    //匹配到信息
                    try{
                        this.message.recall()
                    }catch (e: PermissionDeniedException){
                        logger.error("没有权限")
                    }catch(e: IllegalStateException ){
                        logger.error("该消息已被撤回")
                    }
                    l.num += 1
                    return@subscribeAlways
                }
            }
            //不在长缓存，那就在短缓存
            if(!STCache.containsKey(this.group.id)){
                //如果没该群的记录，新建
                STCache[this.group.id] = Msg(msg, 1)
                STCache[this.group.id]!!.source.add(this.message[MessageSource]!!)
                return@subscribeAlways
            }else{
                //如果在
                val temp = STCache[this.group.id]!!
                //如果匹配
                if(temp.hash == msg) {
                    if (temp.num == n - 1) {
                        if(config.notification == true)
                            this.group.owner.sendMessage("[QQ群${this.group.id}]开始刷屏[${this.message.serializeToMiraiCode()}]")
                        //撤回后几个
                        try {
                            //循环撤回
                            for(i in 1 until n - 1){
                                try {
                                    temp.source[i].recall()
                                } catch (e: PermissionDeniedException) {
                                    logger.error("没有权限")
                                } catch (e: IllegalStateException) {
                                    logger.error("该消息已被撤回")
                                }
                            }
                            this.message.recall()
                        } catch (e: PermissionDeniedException) {
                            logger.error("没有权限")
                        } catch (e: IllegalStateException) {
                            logger.error("该消息已被撤回")
                        }
                        //进入长缓存
                        LTCache.add(Msg(msg,n))
                        return@subscribeAlways
                    }else{
                        //如果< n - 1
                        temp.num += 1
                        temp.source.add(this.message[MessageSource]!!)
                        return@subscribeAlways
                    }
                }else{
                    //如果不匹配就覆盖
                    STCache[this.group.id] = Msg(msg,1)
                    STCache[this.group.id]!!.source.add(this.message[MessageSource]!!)
                    return@subscribeAlways
                }
            }
        }
    }
}
