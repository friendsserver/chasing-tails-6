package me.prdis.chasingtails.plugin.events

import me.prdis.chasingtails.plugin.objects.ChasingtailsImpl
import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.chat.TextComponent
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

        // 다이아몬드 우클릭 시 타겟 추적
        if (item.type == Material.DIAMOND && (event.action == Action.RIGHT_CLICK_AIR || event.action == Action.RIGHT_CLICK_BLOCK)) {
            val gamePlayer = plugin.getGamePlayer(player) ?: return
            val target = gamePlayer.target ?: return
            val targetPlayer = target.player ?: return

            // 다른 세계에 있을 때 액션바 메시지
            if (player.world != targetPlayer.world) {
                player.spigot().sendMessage(
                    ChatMessageType.ACTION_BAR, 
                    TextComponent("§e* 이 플레이어는 다른 세계에 있습니다 *")
                )
                return
            }

            // 타겟 방향으로 파란색 연기(WATER_WAKE) 생성
            val startLocation = player.eyeLocation
            val direction: Vector = targetPlayer.location.toVector().subtract(startLocation.toVector()).normalize()
            
            for (i in 1..3) {
                val particleLoc = startLocation.clone().add(direction.clone().multiply(i.toDouble()))
                player.world.spawnParticle(Particle.WATER_WAKE, particleLoc, 3, 0.05, 0.05, 0.05, 0.0)
            }
            player.sendMessage("§b타겟의 방향을 추적합니다.")
        }
    }

    @EventHandler
    fun onHunterKill(event: EntityDamageByEntityEvent) {
        val victim = event.entity as? Player ?: return
        val attacker = event.damager as? Player ?: return

        // 타겟을 처치했을 때 포섭(팀 변경)
        if (victim.health - event.finalDamage <= 0) {
            val hunter = plugin.getGamePlayer(attacker)
            val prey = plugin.getGamePlayer(victim)

            if (hunter?.target == prey) {
                event.isCancelled = true 
                victim.health = 20.0 

                // 팀 포섭 로직
                prey?.color = hunter?.color
                
                victim.sendMessage("§c처치당했습니다! 이제 §f${attacker.name}§c의 팀입니다.")
                attacker.sendMessage("§a타겟 포섭 완료! 팀원이 늘어났습니다.")
            }
        }
    }
}
