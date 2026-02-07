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

class HuntingEvent(private val plugin: ChasingtailsImpl) : Listener {

    // 1. 헌터 접근 경고: 하트 반짝임 + 심장소리 (영상 11:40 참고)
    @EventHandler
    fun onHunterApproach(event: PlayerMoveEvent) {
        val player = event.player
        val hunter = Bukkit.getOnlinePlayers().find { 
            plugin.getGamePlayer(it)?.target?.player == player 
        } ?: return

        if (player.world == hunter.world) {
            val distance = player.location.distance(hunter.location)
            if (distance <= 20.0) { // 20블록 이내 접근 시
                // 액션바에 하트 경고
                player.sendActionBar("§c❤ §4§l헌터가 근처에 있습니다! §c❤")
                // 심장소리 효과음 (노트블럭 베이스 소리)
                if (System.currentTimeMillis() % 2000 < 100) {
                    player.playSound(player.location, Sound.BLOCK_NOTE_BLOCK_BASEDRUM, 1f, 1f)
                }
            }
        }
    }

    // 2. 다이아몬드 추적기 (영상 02:17: 1개 소모하여 방향 표시)
    @EventHandler
    fun onTrackerUse(event: PlayerInteractEvent) {
        val player = event.player
        val item = event.item ?: return

        if (item.type == Material.DIAMOND && (event.action == Action.RIGHT_CLICK_AIR || event.action == Action.RIGHT_CLICK_BLOCK)) {
            val gp = plugin.getGamePlayer(player) ?: return
            val targetPlayer = gp.target?.player ?: return

            if (player.world != targetPlayer.world) {
                player.sendMessage("§e[!] 같은 세계에 있지 않습니다.") // 영상 11:55 메시지
                return
            }

            item.amount = item.amount - 1 // 다이아 1개 소모
            val dir = targetPlayer.location.toVector().subtract(player.location.toVector()).normalize()
            
            for (i in 1..10) {
                val loc = player.eyeLocation.add(dir.clone().multiply(i.toDouble()))
                player.world.spawnParticle(Particle.FIREWORK, loc, 2, 0.05, 0.05, 0.05, 0.0)
            }
            player.sendMessage("§b타겟의 방향을 추적했습니다.")
        }
    }

    // 3. 노예 시스템: 체력 한 칸 소모 후 포섭 (영상 00:14)
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
                
                // [중요] 공격자의 최대 체력을 2(하트 한 칸) 줄임
                attacker.maxHealth = attacker.maxHealth - 2.0
                attacker.sendMessage("§c[!] 하트 한 칸을 대가로 노예를 얻었습니다.")

                victimGP.color = hunterGP.color // 팀 합류
                victim.sendMessage("§c처치당했습니다! 이제 §f${attacker.name}§c의 노예입니다.")
                attacker.sendMessage("§a타겟을 노예로 포섭했습니다!")
            }
        }
    }
}
