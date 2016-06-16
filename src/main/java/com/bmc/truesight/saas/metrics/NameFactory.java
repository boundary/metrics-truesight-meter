package com.bmc.truesight.saas.metrics;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;

import java.util.List;

import static java.util.Objects.requireNonNull;

public class NameFactory {

    private static final String DELIMITER = ".";
    private static final Joiner JOINER = Joiner.on(DELIMITER);

    private final String prefix;
    private final ImmutableList<String> masks;

    public NameFactory(String prefix, List<String> masks) {
        this.prefix = requireNonNull(prefix);
        this.masks = ImmutableList.copyOf(masks);
    }

    public String name(String name) {
        return JOINER.join(prefix, mask(name));
    }

    private String mask(String name) {

        return masks
                .stream()
                .filter(name::startsWith)
                .map(mask -> name.substring(mask.length()))
                .map(masked ->
                        masked.startsWith(DELIMITER) ? masked.substring(1) : masked
                )
                .findFirst()
                .orElse(name);
    }

    public String name(String name, MetricExtension extension) {
        return JOINER.join(prefix, mask(name), extension.getName());
    }
}
