package mrmathami.thegame.entity.enemy;

import mrmathami.thegame.Config;
import mrmathami.thegame.GameEntities;
import mrmathami.thegame.GameField;
import mrmathami.thegame.LoadedAudio;
import mrmathami.thegame.entity.*;
import mrmathami.thegame.entity.Explosion;
import mrmathami.thegame.entity.tile.Road;

import javax.annotation.Nonnull;
import java.util.Collection;

public abstract class AbstractEnemy extends AbstractEntity implements UpdatableEntity, EffectEntity, LivingEntity, DestroyListener {
	private static final double SQRT_2 = Math.sqrt(2) / 2.0;
	private static final double[][] DELTA_DIRECTION_ARRAY = {
			{0.0, -1.0}, {0.0, 1.0}, {-1.0, 0.0}, {1.0, 0.0},
			{-SQRT_2, -SQRT_2}, {SQRT_2, SQRT_2}, {SQRT_2, -SQRT_2}, {-SQRT_2, SQRT_2},
	};

	private long health;
	private long armor;
	private double speed;
	private long reward;
	public boolean badEffect = false;

	protected AbstractEnemy(long createdTick, double posX, double posY, double size, long health, long armor, double speed, long reward) {
		super(createdTick, posX, posY, size, size);
		this.health = health;
		this.armor = armor;
		this.speed = speed;
		this.reward = reward;
	}

	private static double evaluateDistance(@Nonnull Collection<GameEntity> overlappableEntities,
			@Nonnull GameEntity sourceEntity, double posX, double posY, double width, double height) {
		double distance = 0.0;
		double sumArea = 0.0;
		for (GameEntity entity : GameEntities.getOverlappedEntities(overlappableEntities, posX, posY, width, height)) {
			if (sourceEntity != entity && GameEntities.isCollidable(sourceEntity.getClass(), entity.getClass())) return Double.NaN;
			if (entity instanceof Road) {
				final double entityPosX = entity.getPosX();
				final double entityPosY = entity.getPosY();
				final double area = (Math.min(posX + width, entityPosX + entity.getWidth()) - Math.max(posX, entityPosX))
						* (Math.min(posY + height, entityPosY + entity.getHeight()) - Math.max(posY, entityPosY));
				sumArea += area;
				distance += area * ((Road) entity).getDistance();
			}
		}
		return distance / sumArea;
	}

	@Override
	public final void onUpdate(@Nonnull GameField field) {
		final double enemyPosX = getPosX();
		final double enemyPosY = getPosY();
		final double enemyWidth = getWidth();
		final double enemyHeight = getHeight();
		final Collection<GameEntity> overlappableEntities = GameEntities.getOverlappedEntities(field.getEntities(),
				getPosX() - speed, getPosY() - speed, speed + getWidth() + speed, speed + getHeight() + speed);
		double minimumDistance = Double.MAX_VALUE;
		double newPosX = enemyPosX;
		double newPosY = enemyPosY;
		for (double realSpeed = speed * 0.125; realSpeed <= speed; realSpeed += realSpeed) {
			for (double[] deltaDirection : DELTA_DIRECTION_ARRAY) {
				final double currentPosX = enemyPosX + deltaDirection[0] * realSpeed;
				final double currentPosY = enemyPosY + deltaDirection[1] * realSpeed;
				final double currentDistance = evaluateDistance(overlappableEntities, this, currentPosX, currentPosY, enemyWidth, enemyHeight);
				if (currentDistance < minimumDistance || (currentDistance == minimumDistance && Math.random() > 0.8)) {
					minimumDistance = currentDistance;
					newPosX = currentPosX;
					newPosY = currentPosY;
				}
			}
		}
		setPosX(newPosX);
		setPosY(newPosY);
		if (badEffect && field.getTickCount() % Config.GAME_TPS == 0) health -= Math.round(0.1 * health);
	}

	@Override
	public final void onDestroy(@Nonnull GameField field) {
		// TODO: reward ... Done!
		field.credit += reward;
		if (Config.sfx) LoadedAudio.enemyDestroy(this.getClass()).play();
		field.doSpawn(new Explosion(0, this));
	}

	@Override
	public final boolean onEffect(@Nonnull GameField field, @Nonnull LivingEntity livingEntity) {
		// TODO: harm the target ... Done!
		livingEntity.doEffect(-health);
		this.health = Long.MIN_VALUE;
		if (livingEntity instanceof  AbstractEntity)
			field.doSpawn(new Explosion(0, (AbstractEntity) livingEntity));
		return false;
	}

	@Override
	public final long getHealth() {
		return health;
	}

	@Override
	public final void doEffect(long value) {
		if (health != Long.MIN_VALUE && (value < -armor || value > 0)) this.health += value;
	}

	public final void doTimerEffect(){
		speed = Math.max(speed * 0.8, 1 - getWidth());
		badEffect = true;
	}
	@Override
	public final void doDestroy() {
		this.health = Long.MIN_VALUE;
	}

	@Override
	public final boolean isDestroyed() {
		return health <= 0L;
	}

}
