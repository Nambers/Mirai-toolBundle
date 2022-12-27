package tech.eritquearcus.mirai.plugin.rkw

import net.mamoe.mirai.console.command.*
import net.mamoe.mirai.console.permission.PermissionService.Companion.hasPermission
import net.mamoe.mirai.message.data.MessageSource.Key.recall
import net.mamoe.mirai.message.data.QuoteReply
import tech.eritquearcus.mirai.plugin.rkw.PluginMain.save


object Recall : CompositeCommand(
    PluginMain, "RKW", description = "RKW指令"
) {
    @SubCommand("ch")
    private suspend fun ch(context: CommandContext) {
        val sender = context.sender
        val msg = context.originalMessage
        if (sender !is MemberCommandSenderOnMessage) {
            sender.sendMessage("请在群内使用")
            return
        }
        if (!msg.contains(QuoteReply)) {
            sender.sendMessage("请引用消息再撤回")
            return
        }
        if (!sender.hasPermission(PluginMain.perm)) {
            sender.sendMessage("你没有被赋予权限, 权限名: ${PluginMain.perm.name}")
            return
        }
        if (sender.user.permission >= sender.group.botPermission) {
            sender.sendMessage("撤回对象权限大于等于机器人")
            return
        }
        try {
            msg[QuoteReply]!!.source.recall()
        } catch (e: IllegalStateException) {
            sender.sendMessage("撤回失败, 这条信息可能已经被撤回")
        }
    }

    @SubCommand("keyWords")
    private suspend fun showAllKeyWords(context: CommandContext) {
        val sender = context.sender
        if (!sender.hasPermission(PluginMain.perm)) {
            sender.sendMessage("你没有被赋予权限, 权限名: ${PluginMain.perm.name}")
            return
        }
        sender.sendMessage("当前关键词: ${Config.keyWords}")
    }

    @SubCommand("addKeyWord")
    private suspend fun addKeyWord(context: CommandContext, keyWord: String, level: Int) {
        val sender = context.sender
        if (!sender.hasPermission(PluginMain.perm)) {
            sender.sendMessage("你没有被赋予权限, 权限名: ${PluginMain.perm.name}")
            return
        }
        if (Config.keyWords.size < level) {
            for (i in Config.keyWords.size until level) {
                Config.keyWords.add(emptyList<String>().toMutableList())
            }
        }
        Config.keyWords[level - 1].add(keyWord)
        Config.save()
        sender.sendMessage("添加成功")
    }

    @SubCommand("delKeyWord")
    private suspend fun delKeyWord(context: CommandContext, keyWord: String, level: Int) {
        val sender = context.sender
        if (!sender.hasPermission(PluginMain.perm)) {
            sender.sendMessage("你没有被赋予权限, 权限名: ${PluginMain.perm.name}")
            return
        }
        if (Config.keyWords.size < level) {
            sender.sendMessage("关键词等级不存在")
            return
        }
        if (!Config.keyWords[level - 1].contains(keyWord)) {
            sender.sendMessage("关键词不存在")
            return
        }
        Config.keyWords[level - 1].remove(keyWord)
        Config.save()
        sender.sendMessage("删除成功")
    }
}