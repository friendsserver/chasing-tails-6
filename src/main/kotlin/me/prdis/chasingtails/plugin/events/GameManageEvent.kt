package me.prdis.chasingtails.plugin.events
import org.bukkit.Bukkit
import org.bukkit.event.Listener
class GameManageEvent : Listener {
    fun checkStart() {
        if (Bukkit.getOnlinePlayers().size == 6) { /* 6명 시작 */ }
    }
}
