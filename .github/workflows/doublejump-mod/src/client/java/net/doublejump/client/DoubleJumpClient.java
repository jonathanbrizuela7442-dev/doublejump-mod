package net.doublejump.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.Vec3d;

/**
 * A purely client-side "double jump" mod.
 *
 * It works by watching the jump key on the client render/tick loop and giving
 * the local player one extra upward boost while airborne. Because it only
 * ever touches the client's own player entity, it is naturally inert unless
 * the client itself is authoritative over movement - which is only true for
 * singleplayer worlds (and worlds you host over LAN). On a real multiplayer
 * server the server is authoritative, so the boost is skipped entirely to
 * avoid being flagged as movement cheating / anti-cheat violations.
 */
public class DoubleJumpClient implements ClientModInitializer {

	/** Upward velocity applied on the extra jump. Vanilla's normal jump is ~0.42. */
	private static final double DOUBLE_JUMP_VELOCITY = 0.45;

	/** Small forward push in the direction the player is looking/moving. */
	private static final double FORWARD_BOOST = 0.15;

	private boolean hasDoubleJumped = false;
	private boolean wasOnGround = true;

	@Override
	public void onInitializeClient() {
		ClientTickEvents.END_CLIENT_TICK.register(this::onClientTick);
	}

	private void onClientTick(MinecraftClient client) {
		ClientPlayerEntity player = client.player;
		if (player == null) {
			return;
		}

		// Only allow the double jump in singleplayer / worlds you are hosting
		// (e.g. "Open to LAN"). This is false when connected to any remote
		// dedicated or third-party server.
		if (!client.isInSingleplayer()) {
			hasDoubleJumped = false;
			wasOnGround = true;
			return;
		}

		boolean onGround = player.isOnGround();

		if (onGround) {
			// Reset the charge as soon as we touch the ground again.
			hasDoubleJumped = false;
		} else if (wasOnGround != onGround) {
			// Just left the ground via a normal jump; nothing to do yet,
			// but don't let a jump-key press that is still being consumed
			// from the initial jump trigger a double jump immediately.
		}

		// wasPressed() consumes a single "press" edge from the key binding,
		// which is exactly what we want for a one-shot action per press.
		boolean jumpPressed = client.options.jumpKey.wasPressed();

		if (!onGround && jumpPressed && !hasDoubleJumped && !player.isTouchingWater() && !player.isClimbing()) {
			performDoubleJump(player);
			hasDoubleJumped = true;
		}

		wasOnGround = onGround;
	}

	private void performDoubleJump(ClientPlayerEntity player) {
		Vec3d velocity = player.getVelocity();

		// Cancel any existing downward motion and apply the boost.
		double newY = Math.max(velocity.y, 0.0) + DOUBLE_JUMP_VELOCITY;

		// Add a bit of forward momentum in whatever direction the player is
		// currently moving, so the double jump feels like a "leap" rather
		// than a pure vertical hop.
		double dx = velocity.x;
		double dz = velocity.z;
		double horizontalSpeed = Math.sqrt(dx * dx + dz * dz);
		if (horizontalSpeed > 0.001) {
			dx += (dx / horizontalSpeed) * FORWARD_BOOST;
			dz += (dz / horizontalSpeed) * FORWARD_BOOST;
		}

		player.setVelocity(dx, newY, dz);
		player.velocityModified = true;

		// Fall damage is normally waived for the tick right after a jump;
		// resetting fall distance keeps a double jump from causing fall
		// damage on landing.
		player.fallDistance = 0.0f;

		MinecraftClient client = MinecraftClient.getInstance();
		if (client.world != null) {
			client.world.playSound(
					player,
					player.getX(), player.getY(), player.getZ(),
					net.minecraft.sound.SoundEvents.ENTITY_ENDER_DRAGON_FLAP,
					net.minecraft.sound.SoundCategory.PLAYERS,
					0.5f,
					1.6f
			);
		}
	}
}
