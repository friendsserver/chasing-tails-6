package me.prdis.chasingtails.plugin.events

import me.prdis.chasingtails.plugin.objects.ChasingtailsImpl
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.player.PlayerInteractEvent

class HuntingEvent(private val plugin: ChasingtailsImpl) : Listener {

    @EventHandler
    fun onTrackerUse(event: PlayerInteractEvent) {
        val player = event.player
        val item = event.item ?: return

        if (item.type == Material.DIAMOND && (event.action == Action.RIGHT_CLICK_AIR || event.action == Action.RIGHT_CLICK_BLOCK)) {
            val gp = plugin.getGamePlayer(player) ?: return
            val targetGP = gp.target ?: return
            val targetPlayer = targetGP.player ?: return

            if (player.world != targetPlayer.world) {
                player.sendMessage("§e[!] 타켓이 다른 세계에 있습니다.")
                return
            }

            val dir = targetPlayer.location.toVector().subtract(player.location.toVector()).normalize()
            for (i in 1..3) {
                val loc = player.eyeLocation.add(dir.clone().multiply(i.toDouble()))
                player.world.spawnParticle(Particle.WATER_WAKE, loc, 5, 0.1, 0.1, 0.1, 0.0)
            }
            player.sendMessage("§b타겟의 방향을 추적합니다.")
        }
    }

    @EventHandler
    fun onHunterKill(event: EntityDamageByEntityEvent) {
        val victim = event.entity as? Player ?: return
        val attacker = event.damager as? Player ?: return

        if (victim.health - event.finalDamage <= 0) {
            val hunterGP = plugin.getGamePlayer(attacker) ?: return
            val victimGP = plugin.getGamePlayer(victim) ?: return

            if (hunterGP.target == victimGP) {
                event.isCancelled = true
                victim.health = 20.0
                
                // 포섭 방식: 팀 색깔을 사냥꾼과 동일하게 변경
                victimGP.color = hunterGP.color
                
                victim.sendMessage("§c처치당했습니다! 이제 §f${attacker.name}§c의 팀입니다.")
                attacker.sendMessage("§a타겟을 포섭했습니다!")
            }
        }
    }
}
