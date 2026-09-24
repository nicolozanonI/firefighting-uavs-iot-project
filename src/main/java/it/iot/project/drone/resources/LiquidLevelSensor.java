package it.iot.project.drone.resources;

import java.util.UUID;

public class LiquidLevelSensor extends SmartObjectResource<Double>{

    private static String RESOURCE_UNIT = "l";

    private static String RESOURCE_TYPE = "iot:sensor:liquidlevel";

    public LiquidLevelSensor(double value) {
        super(UUID.randomUUID().toString(), RESOURCE_UNIT, RESOURCE_TYPE);
        this.value = value;
    }


    public void emptyTank() { this.setValue(this.getValue() - 1.5); }

    public void rechargeTank() {
        this.setValue(this.getValue() + 1.5);
    }

}
