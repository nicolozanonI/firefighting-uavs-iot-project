package it.iot.project.drone.resources;

import java.util.Random;
import java.util.UUID;

public class ObjectDetectionSensor extends SmartObjectResource<Boolean>{

    private static String RESOURCE_UNIT = "Boolean";
    private static String RESOURCE_TYPE = "iot:switch:detection";

    Random random = new Random();

    public ObjectDetectionSensor(boolean value) {
        super(UUID.randomUUID().toString(), RESOURCE_UNIT, RESOURCE_TYPE);
        this.value = value;
    }

    public void detection(){
        this.setValue(random.nextInt(10) == 0);
    }

}
