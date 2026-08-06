package com.hlysine.create_connected.foundation.registrate;

/**
 * The slice of Registrate's {@code Builder<R, T, P, S>} that {@code FeatureToggle} actually used:
 * the owning mod id and the entry name, read to build the feature's {@code Identifier} before
 * registration completes. Registrate's four type parameters existed for its own builder hierarchy
 * and carried nothing Connected needed, so the toggle transforms are generic over this instead.
 */
public interface NamedBuilder {
    String getModid();

    String getName();
}
