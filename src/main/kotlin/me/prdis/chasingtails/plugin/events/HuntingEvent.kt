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
import org.bukkit.util.Vector

class HuntingEvent(private val plugin: ChasingtailsImpl) : Listener {

    @EventHandler
    fun onTrackerUse(event: PlayerInteractEvent) {
        val player = event.player
        val item = event.item ?: return

        if (item.type == Material.DIAMOND && (event.action == Action.RIGHT_CLICK_AIR || event.action == Action.RIGHT_CLICK_BLOCK)) {
            val gamePlayer = plugin.getGamePlayer(player) ?: return
            val targetEntry = gamePlayer.target ?: return
            val targetPlayer = targetEntry.player ?: return

            // 월드가 다를 때 알림
            if (player.world != targetPlayer.world) {
                player.sendMessage("§e[!] 타겟이 다른 세계(월드)에 있습니다.")
                return
            }

            val startLocation = player.eyeLocation
            val direction: Vector = targetPlayer.location.toVector().subtract(startLocation.toVector()).normalize()
            
            for (i in 1..3) {
                val particleLoc = startLocation.clone().add(direction.clone().multiply(i.toDouble()))
                player.world.spawnParticle(Particle.CLOUD, particleLoc, 5, 0.1, 0.1, 0.1, 0.0)
            }
            player.sendMessage("§b타겟의 방향으로 연기가 피어오릅니다.")
        }
    }

    @EventHandler
    fun onHunterKill(event: EntityDamageByEntityEvent) {
        val victim = event.entity as? Player ?: return
        val attacker = event.damager as? Player ?: return

        if (victim.health - event.finalDamage <= 0) {
            val hunter = plugin.getGamePlayer(attacker) ?: return
            val prey = plugin.getGamePlayer(victim) ?: return

            if (hunter.target == prey) {
                event.isCancelled = true 
                victim.health = 20.0 

                // 팀 포섭 로직
                prey.color = hunter.color
                
                victim.sendMessage("§c처치당했습니다! 이제 §f${attacker.name}§c의 팀입니다.")
                attacker.sendMessage("§a타겟을 우리 팀으로 포섭했습니다!")
            }
        }
    }
}
