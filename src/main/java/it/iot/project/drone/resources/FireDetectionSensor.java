package it.iot.project.drone.resources;

import java.util.UUID;

public class FireDetectionSensor extends SmartObjectResource<Boolean>{

    private static String UNIT_VALUE = "Boolean";

    private static String TYPE_VALUE = "iot:switch:firesensor";

    public FireDetectionSensor(boolean value) {
        super(UUID.randomUUID().toString(), UNIT_VALUE, TYPE_VALUE);
        this.value = value;
    }

}
