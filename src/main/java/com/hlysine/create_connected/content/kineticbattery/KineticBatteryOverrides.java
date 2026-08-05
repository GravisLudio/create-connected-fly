package com.hlysine.create_connected.content.kineticbattery;

import com.hlysine.create_connected.CreateConnected;
import net.minecraft.resources.Identifier;

/**
 * Identifier for the kinetic battery's charge-level model property.
 *
 * <h2>Currently inert</h2>
 * This class used to do two things, and 26.2 removed the basis for both:
 * <ul>
 *   <li>{@code registerModelOverridesClient} called {@code ItemProperties.register} to expose the
 *       charge as a model property. {@code ItemProperties} no longer exists; item model properties
 *       are registered through the item model definition system now.</li>
 *   <li>{@code addOverrideModels} generated the six level models and their {@code overrides}
 *       entries at datagen time. {@code overrides} was removed from the model format, and the
 *       models are already committed, so there is nothing to generate.</li>
 * </ul>
 * The identifier is kept because a replacement needs the same property name, and the six
 * {@code kinetic_battery_level_N} models are still present.
 * <p>
 * Consequence: the battery renders as empty at every charge level. See the TODO in
 * {@code assets/create_connected/items/kinetic_battery.json}.
 */
public class KineticBatteryOverrides {

    public static final Identifier ID = CreateConnected.asResource("kinetic_battery_level");
}
