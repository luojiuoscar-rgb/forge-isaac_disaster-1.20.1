package net.luojiuoscar.isaac_disaster.helper;

import net.luojiuoscar.isaac_disaster.entity.ModEntity;
import net.luojiuoscar.isaac_disaster.entity.fireball.TimedFireball;
import net.luojiuoscar.isaac_disaster.entity.tnt.GigaBomb;
import net.luojiuoscar.isaac_disaster.entity.tnt.IsaacBomb;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class EntityHelper {
    public static void spawnBomb(Vec3 position, Player player, Vec3 tntVelocity, int fuse, int power, float scale){
        spawnBomb(position, player, tntVelocity, fuse, power, scale, player.level(), true);
    }

    public static void spawnBomb(Vec3 position, Player player, Vec3 tntVelocity, int fuse, int power, float scale, Level level){
        spawnBomb(position, player, tntVelocity, fuse, power, scale, level, true);
    }

    public static void spawnBomb(Vec3 position, Player player, Vec3 tntVelocity, int fuse, int power, float scale, Level level, boolean isOriginal){
        if (level.isClientSide()) return; // 不要在客户端生成实体

        IsaacBomb tnt = ModEntity.ISAAC_BOMB.get().create(player.level());
        if (tnt == null) return;

        tnt.moveTo(position.x, position.y, position.z, 0, 0);
        tnt.setOwner(player);
        tnt.setFuse(fuse);
        tnt.setPower(power);
        tnt.setScale(scale);
        tnt.setOriginal(isOriginal);
        tnt.setDeltaMovement(tntVelocity);

        level.addFreshEntity(tnt);
    }

    public static void spawnGigaBomb(Vec3 position, Player player, Vec3 tntVelocity, int fuse, Level level){
        if (level.isClientSide()) return; // 不要在客户端生成实体

        IsaacBomb tnt = ModEntity.GIGA_BOMB.get().create(player.level());
        if (tnt == null) return;

        tnt.moveTo(position.x, position.y, position.z, 0, 0);
        tnt.setOwner(player);
        tnt.setFuse(fuse);
        tnt.setPower(10);
        tnt.setScale(2.5f);
        tnt.setOriginal(true);
        tnt.setDeltaMovement(tntVelocity);

        level.addFreshEntity(tnt);
    }

    public static void throwGigaBomb(Player player, int fuse){
        // 获取玩家朝向向量
        Vec3 lookVec = player.getLookAngle();
        // 获取玩家当前速度
        Vec3 playerVelocity = player.getDeltaMovement();

        // 计算TNT生成位置（玩家眼睛位置略微偏移）
        Vec3 spawnPos = player.getEyePosition()
                .add(lookVec.x * 0.5, lookVec.y * 0.5, lookVec.z * 0.5);

        // 计算TNT初速度：结合玩家朝向和玩家自身速度
        double throwStrength = 1.3; // 投掷力度
        double velocityInheritance = 1.0; // 继承玩家速度的比例

        Vec3 tntVelocity = new Vec3(
                lookVec.x * throwStrength + playerVelocity.x * velocityInheritance,
                lookVec.y * throwStrength + playerVelocity.y * velocityInheritance + 0.25, // 略微向上
                lookVec.z * throwStrength + playerVelocity.z * velocityInheritance
        );

        spawnGigaBomb(spawnPos, player, tntVelocity, fuse, player.level());
    }

    public static void throwBomb(Player player, int fuse, int power) {
        throwBomb(player, fuse, power, 0.98f);
    }

    public static void throwBomb(Player player, int fuse, int power, float scale){
        // 获取玩家朝向向量
        Vec3 lookVec = player.getLookAngle();
        // 获取玩家当前速度
        Vec3 playerVelocity = player.getDeltaMovement();

        // 计算TNT生成位置（玩家眼睛位置略微偏移）
        Vec3 spawnPos = player.getEyePosition()
                .add(lookVec.x * 0.5, lookVec.y * 0.5, lookVec.z * 0.5);

        // 计算TNT初速度：结合玩家朝向和玩家自身速度
        double throwStrength = 1.3; // 投掷力度
        double velocityInheritance = 1.0; // 继承玩家速度的比例

        Vec3 tntVelocity = new Vec3(
                lookVec.x * throwStrength + playerVelocity.x * velocityInheritance,
                lookVec.y * throwStrength + playerVelocity.y * velocityInheritance + 0.25, // 略微向上
                lookVec.z * throwStrength + playerVelocity.z * velocityInheritance
        );

        spawnBomb(spawnPos, player, tntVelocity, fuse, power, scale, player.level());
    }

    public static void BomberBoy(Player player, IsaacBomb tnt, Vec3 pos, Level level){
        // 非“原始”tnt不触发效果
        if(!tnt.isOriginal()) return;
        // 巨型炸弹等不触发效果
        if(tnt instanceof GigaBomb) return;

        float offset = tnt.getPower() + 1f;

        Vec3 pos1 = pos.add(offset, 0, 0);
        spawnBomb(pos1, player, Vec3.ZERO, 0, tnt.getPower(), tnt.getScale(), level,false);

        Vec3 pos2 = pos.add(-offset, 0, 0);
        spawnBomb(pos2, player, Vec3.ZERO, 0, tnt.getPower(), tnt.getScale(), level,false);

        Vec3 pos3 = pos.add(0, 0, offset);
        spawnBomb(pos3, player, Vec3.ZERO, 0, tnt.getPower(), tnt.getScale(), level,false);

        Vec3 pos4 = pos.add(0, 0, -offset);
        spawnBomb(pos4, player, Vec3.ZERO, 0, tnt.getPower(), tnt.getScale(), level,false);
    }

    public static void ScatterBomb(Player player, IsaacBomb tnt, Vec3 pos, Level level){
        // 巨型炸弹、微型炸弹不触发效果
        if(tnt instanceof GigaBomb || tnt.getPower() <= 1) return;
        // 非“原始”不触发
        if(!tnt.isOriginal()) return;


        int power = tnt.getPower() - 3;
        float scale = 0.98f;
        if (power == 1){
            scale = 0.4f;
        }

        spawnBomb(pos, player, new Vec3(Math.random() * 0.6 - 0.3, Math.random() * 0.4, Math.random() * 0.6 - 0.3),
                30, power, scale, level);

        spawnBomb(pos, player, new Vec3(Math.random() * 0.6 - 0.3, Math.random() * 0.4, Math.random() * 0.6 - 0.3),
                30, power, scale, level);

        spawnBomb(pos, player, new Vec3(Math.random() * 0.6 - 0.3, Math.random() * 0.4, Math.random() * 0.6 - 0.3),
                30, power, scale, level);

        spawnBomb(pos, player, new Vec3(Math.random() * 0.6 - 0.3, Math.random() * 0.4, Math.random() * 0.6 - 0.3),
                30, power, scale, level);
    }

    public static void HotBomb(Player player, IsaacBomb tnt, Vec3 pos, Level level){
        // 非“原始”tnt不触发效果
        if(!tnt.isOriginal()) return;
        // 巨型炸弹等不触发效果
        if(tnt instanceof GigaBomb) return;

        int power = 0;
        if(tnt.getPower() > 4){
            power = 3;
        }else if (tnt.getPower() > 1){
            power = 5;
        }

        for (int i = 0; i < power; i++) {  // 火球数量可调
            double vx = (level.random.nextDouble() - 0.5) * 0.5;
            double vy = (level.random.nextDouble() - 0.5) * 0.5;
            double vz = (level.random.nextDouble() - 0.5) * 0.5;

            TimedFireball fireball = new TimedFireball(level, player, vx, vy, vz, power);

            fireball.setPos(pos);
            fireball.setDeltaMovement(new Vec3(vx, vy, vz)); // 设速度
            level.addFreshEntity(fireball);
        }
    }

}
