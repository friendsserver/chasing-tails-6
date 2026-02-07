package me.prdis.chasingtails.plugin.events

import me.prdis.chasingtails.plugin.objects.ChasingtailsImpl
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerMoveEvent
import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.chat.TextComponent

class HuntingEvent(private val plugin: ChasingtailsImpl) : Listener {

    // 1. 헌터 접근 경고: 하트 반짝임 + 심장소리
    @EventHandler
    fun onHunterApproach(event: PlayerMoveEvent) {
        val player = event.player
        val hunter = Bukkit.getOnlinePlayers().find { 
            plugin.getGamePlayer(it)?.target?.player == player 
        } ?: return

        if (player.world == hunter.world) {
            val distance = player.location.distance(hunter.location)
            if (distance <= 20.0) {
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent("§c❤ §4§l헌터가 근처에 있습니다! §c❤"))
                if (System.currentTimeMillis() % 2000 < 100) {
                    player.playSound(player.location, Sound.BLOCK_NOTE_BLOCK_BASEDRUM, 1f, 1f)
                }
            }
        }
    }

    // 2. 다이아몬드 추적기 (영상 방식)
    @EventHandler
    fun onTrackerUse(event: PlayerInteractEvent) {
        val player = event.player
        val item = event.item ?: return

        if (item.type == Material.DIAMOND && (event.action == Action.RIGHT_CLICK_AIR || event.action == Action.RIGHT_CLICK_BLOCK)) {
            val gp = plugin.getGamePlayer(player) ?: return
            val targetPlayer = gp.target?.player ?: return

            if (player.world != targetPlayer.world) {
                player.sendMessage("§e[!] 타겟이 다른 세계에 있어 추적할 수 없습니다.")
                return
            }

            item.amount = item.amount - 1
            val dir = targetPlayer.location.toVector().subtract(player.location.toVector()).normalize()
            
            for (i in 1..10) {
                val loc = player.eyeLocation.add(dir.clone().multiply(i.toDouble()))
                player.world.spawnParticle(Particle.CRIT, loc, 5, 0.1, 0.1, 0.1, 0.0)
            }
            player.sendMessage("§b타겟의 방향으로 입자가 날아갑니다.")
        }
    }

    // 3. 노예 시스템: 체력 한 칸 소모 후 포섭
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
                
                // 최대 체력 감소 (하트 한 칸)
                val newMax = (attacker.maxHealth - 2.0).coerceAtLeast(2.0)
                attacker.maxHealth = newMax
                attacker.sendMessage("§c[!] 하트 한 칸을 소모하여 노예를 얻었습니다.")

                victimGP.color = hunterGP.color
                victim.sendMessage("§c처치당했습니다! 이제 §f${attacker.name}§c의 노예입니다.")
                attacker.sendMessage("§a타겟을 노예로 포섭했습니다!")
            }
        }
    }
}
