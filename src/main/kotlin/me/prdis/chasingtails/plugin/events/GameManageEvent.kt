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
        // 6명이 모이면 게임 시작
        if (Bukkit.getOnlinePlayers().size == 6) {
            startZombieGame()
        }
    }

    private fun startZombieGame() {
        Bukkit.getOnlinePlayers().forEach { player ->
            // 랜덤 스폰 위치 결정 (0,0 기준 50~5000 범위)
            val loc = getRandomLocation(player)
            player.teleport(loc)
            player.sendMessage("§6[게임 시작] §f광활한 지형으로 흩어졌습니다. 타겟을 찾아 포섭하세요!")
        }
    }

    private fun getRandomLocation(player: Player): Location {
        val world = player.world
        
        fun getCoord(): Double {
            val dist = Random.nextDouble(50.0, 5000.0)
            return if (Random.nextBoolean()) dist else -dist
        }

        val x = getCoord()
        val z = getCoord()
        val y = world.getHighestBlockYAt(x.toInt(), z.toInt()).toDouble()

        return Location(world, x, y + 1.0, z)
    }
}
