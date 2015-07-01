package com.boundary.metrics;

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

        String masked = null;
        for(String mask: masks) {
            if(name.startsWith(mask)) {
                masked = name.substring(mask.length());
                if (masked.startsWith(DELIMITER)) {
                    masked = masked.substring(1);
                }
                break;
            }
        }
        return masked == null ? name: masked;
    }

    public String name(String name, MetricExtension extension) {
        return JOINER.join(prefix, mask(name), extension.getName());
    }
}
