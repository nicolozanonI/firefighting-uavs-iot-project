package it.iot.project.drone.resources;

import java.util.UUID;

public class BatteryLevelSensor extends SmartObjectResource<Double>{

    private static String RESOURCE_UNIT = "%";

    private static String RESOURCE_TYPE = "iot:sensor:battery";

    public BatteryLevelSensor(double value) {
        super(UUID.randomUUID().toString(), RESOURCE_UNIT, RESOURCE_TYPE);
        this.value = value;
    }

    public void dischargeBattery() { this.setValue(this.getValue() - 0.05); }

    public void chargeBattery() {
        this.setValue(this.getValue() + 5.0);
    }



}
