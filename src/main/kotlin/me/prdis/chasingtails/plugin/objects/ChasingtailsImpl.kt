package me.prdis.chasingtails.plugin.objects

import org.bukkit.entity.Player
import java.util.*

class ChasingtailsImpl {
    private val gamePlayers = mutableMapOf<UUID, GamePlayer>()

    fun getGamePlayer(player: Player): GamePlayer? {
        return gamePlayers.getOrPut(player.uniqueId) { GamePlayer(player) }
    }

    inner class GamePlayer(val player: Player) {
        var target: GamePlayer? = null
        var color: String = "§f" // 기본 흰색
    }
}
