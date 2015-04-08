package com.boundary.metrics;


public class Measure {
    private final String name;
    private final double value;

    public Measure(String name, double value) {

        this.name = name;
        this.value = value;
    }

    @Override
    public String toString() {
        return "Measure{" +
                "name='" + name + '\'' +
                ", value=" + value +
                '}';
    }


    public String getName() {
        return name;
    }

    public double getValue() {
        return value;
    }

}
