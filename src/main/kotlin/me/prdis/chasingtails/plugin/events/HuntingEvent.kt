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

        // 다이아몬드 우클릭 감지
        if (item.type == Material.DIAMOND && (event.action == Action.RIGHT_CLICK_AIR || event.action == Action.RIGHT_CLICK_BLOCK)) {
            val gp = plugin.getGamePlayer(player) ?: return
            val targetGP = gp.target ?: return
            val targetPlayer = targetGP.player ?: return

            // 월드 체크 및 안내
            if (player.world != targetPlayer.world) {
                player.sendMessage("§e[!] 타겟이 다른 세계에 있습니다.")
                return
            }

            // 타겟 방향으로 연기 소환
            val dir = targetPlayer.location.toVector().subtract(player.location.toVector()).normalize()
            for (i in 1..3) {
                val loc = player.eyeLocation.add(dir.multiply(i.toDouble()))
                player.world.spawnParticle(Particle.CLOUD, loc, 5, 0.1, 0.1, 0.1, 0.0)
            }
            player.sendMessage("§b타겟의 방향을 감지했습니다.")
        }
    }

    @EventHandler
    fun onHunterKill(event: EntityDamageByEntityEvent) {
        val victim = event.entity as? Player ?: return
        val attacker = event.damager as? Player ?: return

        if (victim.health - event.finalDamage <= 0) {
            val hunterGP = plugin.getGamePlayer(attacker) ?: return
            val victimGP = plugin.getGamePlayer(victim) ?: return

            // 타겟 처치 시 팀 포섭 (색상 변경)
            if (hunterGP.target == victimGP) {
                event.isCancelled = true
                victim.health = 20.0
                
                // 팀 색상 강제 일치
                victimGP.color = hunterGP.color
                
                victim.sendMessage("§c처치당했습니다! 이제 §f${attacker.name}§c의 팀입니다.")
                attacker.sendMessage("§a타겟을 포섭하여 팀이 되었습니다!")
            }
        }
    }
}
