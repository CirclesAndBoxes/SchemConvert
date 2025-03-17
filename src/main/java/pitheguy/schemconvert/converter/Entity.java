package pitheguy.schemconvert.converter;

import pitheguy.schemconvert.nbt.tags.CompoundTag;

public record Entity(String id, double x, double y, double z, CompoundTag nbt) {
}
