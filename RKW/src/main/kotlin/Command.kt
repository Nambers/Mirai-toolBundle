package tech.eritquearcus.mirai.plugin.rkw

import net.mamoe.mirai.console.command.CommandContext
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.MemberCommandSenderOnMessage
import net.mamoe.mirai.console.command.SimpleCommand
import net.mamoe.mirai.console.permission.PermissionService.Companion.hasPermission
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.MessageSource.Key.recall
import net.mamoe.mirai.message.data.QuoteReply


object Recall : SimpleCommand(
    PluginMain, "RKW"
) {
    private suspend fun ch(sender: CommandSender, msg: MessageChain) {
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

    private suspend fun showAllKeyWrods(sender: CommandSender) {
        if (!sender.hasPermission(PluginMain.perm)) {
            sender.sendMessage("你没有被赋予权限, 权限名: ${PluginMain.perm.name}")
            return
        }
        sender.sendMessage("当前关键词: ${PluginMain.config.keyWords}")
    }

    @Handler
    suspend fun handler(context: CommandContext, arg: String) {
        val sender = context.sender
        val msg = context.originalMessage
        if (arg == "ch")
            ch(sender, msg)
        if (arg == "keyWords")
            showAllKeyWrods(sender)
    }
}