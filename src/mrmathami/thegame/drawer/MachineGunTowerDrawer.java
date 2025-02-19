package mrmathami.thegame.drawer;

import javafx.scene.canvas.GraphicsContext;
import mrmathami.thegame.Config;
import mrmathami.thegame.LoadedImage;
import mrmathami.thegame.entity.GameEntity;
import mrmathami.thegame.entity.tile.tower.MachineGunTower;

import javax.annotation.Nonnull;

public final class MachineGunTowerDrawer implements EntityDrawer {
	@Override
	public void draw(long tickCount, @Nonnull GraphicsContext graphicsContext, @Nonnull GameEntity entity, double screenPosX, double screenPosY, double screenWidth, double screenHeight, double zoom) {
//		graphicsContext.setFill(Color.DARKRED);
//		graphicsContext.fillOval(screenPosX, screenPosY, screenWidth, screenHeight);
		if (entity.getClass().equals(MachineGunTower.class)) {
			MachineGunTower tower = (MachineGunTower) entity;
			graphicsContext.save();
			graphicsContext.translate((tower.getPosX() + tower.getWidth() / 2) * Config.TILE_SIZE, (tower.getPosY() + tower.getHeight() / 2) * Config.TILE_SIZE);
			graphicsContext.rotate(tower.getRotation());
//			graphicsContext.setEffect(new Glow(tower.getTickDown() / Config.MACHINE_GUN_TOWER_SPEED));
			graphicsContext.drawImage(LoadedImage.MACHINE_GUN_TOWER, -screenWidth / 2, -screenHeight / 2, screenWidth, screenHeight);
			graphicsContext.restore();
		}
	}
}
