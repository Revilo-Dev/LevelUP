package com.revilo.levelup.entity;

import com.revilo.levelup.api.LevelUpApi;
import com.revilo.levelup.api.LevelUpSources;
import com.revilo.levelup.registry.LevelUpEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class LevelUpXpOrbEntity extends Entity {
    private static final int LIFETIME = 6000;
    private static final int SCAN_PERIOD = 20;
    private static final int ORB_GROUPS_PER_AREA = 40;
    private static final double MERGE_DISTANCE = 0.5D;
    private static final double FOLLOW_DISTANCE_SQR = 64.0D;

    private int age;
    private int health = 5;
    private int value;
    private int count = 1;
    private Player followingPlayer;

    public LevelUpXpOrbEntity(EntityType<? extends LevelUpXpOrbEntity> type, Level level) {
        super(type, level);
    }

    public LevelUpXpOrbEntity(Level level, double x, double y, double z, int value) {
        this(LevelUpEntities.LEVEL_ORB.get(), level);
        this.setPos(x, y, z);
        this.setYRot((float) (this.random.nextDouble() * 360.0D));
        this.setDeltaMovement(
                (this.random.nextDouble() * 0.2D - 0.1D) * 2.0D,
                this.random.nextDouble() * 0.2D * 2.0D,
                (this.random.nextDouble() * 0.2D - 0.1D) * 2.0D
        );
        this.value = Math.max(1, value);
    }

    public static void award(ServerLevel level, Vec3 pos, int amount) {
        int remaining = Math.max(0, amount);
        while (remaining > 0) {
            int split = getOrbValue(remaining);
            remaining -= split;
            if (!tryMergeToExisting(level, pos, split)) {
                level.addFreshEntity(new LevelUpXpOrbEntity(level, pos.x(), pos.y(), pos.z(), split));
            }
        }
    }

    private static boolean tryMergeToExisting(ServerLevel level, Vec3 pos, int amount) {
        AABB box = AABB.ofSize(pos, 1.0D, 1.0D, 1.0D);
        int selector = level.getRandom().nextInt(ORB_GROUPS_PER_AREA);
        List<LevelUpXpOrbEntity> list = level.getEntities(
                EntityTypeTest.forClass(LevelUpXpOrbEntity.class),
                box,
                orb -> canMerge(orb, selector, amount)
        );
        if (list.isEmpty()) {
            return false;
        }
        LevelUpXpOrbEntity existing = list.getFirst();
        existing.count++;
        existing.age = 0;
        return true;
    }

    private static boolean canMerge(LevelUpXpOrbEntity orb, int selector, int otherValue) {
        return !orb.isRemoved() && (orb.getId() - selector) % ORB_GROUPS_PER_AREA == 0 && orb.value == otherValue;
    }

    private boolean canMerge(LevelUpXpOrbEntity orb) {
        return orb != this && canMerge(orb, this.getId(), this.value);
    }

    private void merge(LevelUpXpOrbEntity orb) {
        this.count += orb.count;
        this.age = Math.min(this.age, orb.age);
        orb.discard();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {}

    @Override
    protected Entity.MovementEmission getMovementEmission() {
        return Entity.MovementEmission.NONE;
    }

    @Override
    protected double getDefaultGravity() {
        return 0.03D;
    }

    @Override
    public void tick() {
        super.tick();
        this.xo = this.getX();
        this.yo = this.getY();
        this.zo = this.getZ();

        if (this.isEyeInFluid(FluidTags.WATER)) {
            this.setUnderwaterMovement();
        } else {
            this.applyGravity();
        }

        if (this.level().getFluidState(this.blockPosition()).is(FluidTags.LAVA)) {
            this.setDeltaMovement(
                    (this.random.nextFloat() - this.random.nextFloat()) * 0.2F,
                    0.2F,
                    (this.random.nextFloat() - this.random.nextFloat()) * 0.2F
            );
        }

        if (!this.level().noCollision(this.getBoundingBox())) {
            this.moveTowardsClosestSpace(this.getX(), (this.getBoundingBox().minY + this.getBoundingBox().maxY) / 2.0D, this.getZ());
        }

        if (this.tickCount % SCAN_PERIOD == 1) {
            this.scanForEntities();
        }

        if (this.followingPlayer != null && (this.followingPlayer.isSpectator() || this.followingPlayer.isDeadOrDying())) {
            this.followingPlayer = null;
        }

        if (this.followingPlayer != null) {
            Vec3 delta = new Vec3(
                    this.followingPlayer.getX() - this.getX(),
                    this.followingPlayer.getY() + this.followingPlayer.getEyeHeight() / 2.0F - this.getY(),
                    this.followingPlayer.getZ() - this.getZ()
            );
            double lenSqr = delta.lengthSqr();
            if (lenSqr < FOLLOW_DISTANCE_SQR) {
                double strength = 1.0D - Math.sqrt(lenSqr) / 8.0D;
                this.setDeltaMovement(this.getDeltaMovement().add(delta.normalize().scale(strength * strength * 0.1D)));
            }
        }

        this.move(MoverType.SELF, this.getDeltaMovement());
        float friction = 0.98F;
        if (this.onGround()) {
            BlockPos pos = getBlockPosBelowThatAffectsMyMovement();
            friction = this.level().getBlockState(pos).getFriction(this.level(), pos, this) * 0.98F;
        }

        this.setDeltaMovement(this.getDeltaMovement().multiply(friction, 0.98D, friction));
        if (this.onGround()) {
            this.setDeltaMovement(this.getDeltaMovement().multiply(1.0D, -0.9D, 1.0D));
        }

        this.age++;
        if (this.age >= LIFETIME) {
            this.discard();
        }
    }

    private void scanForEntities() {
        if (this.followingPlayer == null || this.followingPlayer.distanceToSqr(this) > FOLLOW_DISTANCE_SQR) {
            this.followingPlayer = this.level().getNearestPlayer(this, 8.0D);
        }
        if (this.level() instanceof ServerLevel) {
            for (LevelUpXpOrbEntity orb : this.level().getEntities(
                    EntityTypeTest.forClass(LevelUpXpOrbEntity.class),
                    this.getBoundingBox().inflate(MERGE_DISTANCE),
                    this::canMerge
            )) {
                this.merge(orb);
            }
        }
    }

    private void setUnderwaterMovement() {
        Vec3 delta = this.getDeltaMovement();
        this.setDeltaMovement(delta.x * 0.99F, Math.min(delta.y + 5.0E-4F, 0.06F), delta.z * 0.99F);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        tag.putShort("Health", (short) this.health);
        tag.putShort("Age", (short) this.age);
        tag.putShort("Value", (short) this.value);
        tag.putInt("Count", this.count);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        this.health = tag.getShort("Health");
        this.age = tag.getShort("Age");
        this.value = Math.max(1, tag.getShort("Value"));
        this.count = Math.max(tag.getInt("Count"), 1);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.isInvulnerableTo(source)) {
            return false;
        }
        if (this.level().isClientSide) {
            return true;
        }
        this.markHurt();
        this.health = (int) (this.health - amount);
        if (this.health <= 0) {
            this.discard();
        }
        return true;
    }

    @Override
    public void playerTouch(Player player) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        if (player.takeXpDelay != 0) {
            return;
        }
        player.takeXpDelay = 2;
        player.take(this, 1);
        LevelUpApi.awardXp(serverPlayer, this.value, LevelUpSources.ORB_PICKUP);
        this.count--;
        if (this.count <= 0) {
            this.discard();
        }
    }

    @Override
    public boolean isAttackable() {
        return false;
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket(ServerEntity entity) {
        return new ClientboundAddEntityPacket(this, entity);
    }

    @Override
    public SoundSource getSoundSource() {
        return SoundSource.AMBIENT;
    }

    public int getValue() {
        return value;
    }

    public int getIcon() {
        if (this.value >= 2477) return 10;
        if (this.value >= 1237) return 9;
        if (this.value >= 617) return 8;
        if (this.value >= 307) return 7;
        if (this.value >= 149) return 6;
        if (this.value >= 73) return 5;
        if (this.value >= 37) return 4;
        if (this.value >= 17) return 3;
        if (this.value >= 7) return 2;
        return this.value >= 3 ? 1 : 0;
    }

    public static int getOrbValue(int amount) {
        if (amount >= 2477) return 2477;
        if (amount >= 1237) return 1237;
        if (amount >= 617) return 617;
        if (amount >= 307) return 307;
        if (amount >= 149) return 149;
        if (amount >= 73) return 73;
        if (amount >= 37) return 37;
        if (amount >= 17) return 17;
        if (amount >= 7) return 7;
        return amount >= 3 ? 3 : 1;
    }
}
