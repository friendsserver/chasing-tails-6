package me.prdis.chasingtails.plugin.events

import me.prdis.chasingtails.plugin.objects.ChasingtailsImpl
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import kotlin.random.Random

class GameManageEvent(private val plugin: ChasingtailsImpl) : Listener {

    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        if (Bukkit.getOnlinePlayers().size == 6) {
            startTailGame()
        }
    }

    private fun startTailGame() {
        val players = Bukkit.getOnlinePlayers().toList()
        
        for (i in players.indices) {
            val hunter = plugin.getGamePlayer(players[i]) ?: continue
            val nextIndex = if (i == players.size - 1) 0 else i + 1
            hunter.target = plugin.getGamePlayer(players[nextIndex])

            val loc = getRandomLoc(players[i])
            players[i].teleport(loc)
        }
        Bukkit.broadcastMessage("§6[게임 시작] §f6명의 꼬리잡기가 시작되었습니다!")
    }

    private fun getRandomLoc(player: Player): Location {
        val x = if (Random.nextBoolean()) Random.nextDouble(50.0, 5000.0) else -Random.nextDouble(50.0, 5000.0)
        val z = if (Random.nextBoolean()) Random.nextDouble(50.0, 5000.0) else -Random.nextDouble(50.0, 5000.0)
        val y = player.world.getHighestBlockYAt(x.toInt(), z.toInt()).toDouble()
        return Location(player.world, x, y + 1.0, z)
    }
}
