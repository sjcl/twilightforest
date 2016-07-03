package twilightforest.entity.boss;

import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.entity.projectile.EntityLargeFireball;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class EntityTFUrGhastFireball extends EntityLargeFireball {

	public EntityTFUrGhastFireball(World worldObj, EntityTFUrGhast entityTFTowerBoss, double x, double y, double z) 
	{
		super(worldObj, entityTFTowerBoss, x, y, z);
	}

    @Override
    protected void onImpact(RayTraceResult par1MovingObjectPosition)
    {
        if (!this.worldObj.isRemote && !(par1MovingObjectPosition.entityHit instanceof EntityFireball))
        {
            if (par1MovingObjectPosition.entityHit != null)
            {
                par1MovingObjectPosition.entityHit.attackEntityFrom(DamageSource.causeFireballDamage(this, this.shootingEntity), 16);
            }

            this.worldObj.newExplosion((Entity)null, this.posX, this.posY, this.posZ, (float)this.field_92057_e, true, this.worldObj.getGameRules().getGameRuleBooleanValue("mobGriefing"));
            this.setDead();
        }
    }
}
