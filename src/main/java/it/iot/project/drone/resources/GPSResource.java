package it.iot.project.drone.resources;

import it.iot.project.drone.model.GeolocalizationDescriptor;

import java.util.UUID;

public class GPSResource extends SmartObjectResource<GeolocalizationDescriptor>{


    private static String RESOURCE_UNIT = "l";
    private static String RESOURCE_TYPE = "iot:sensor:gps";

    private double distance = 0.0;

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public GPSResource(GeolocalizationDescriptor value) {
        super(UUID.randomUUID().toString(), RESOURCE_UNIT, RESOURCE_TYPE);
        this.value = value;
    }


    public void takeOff() {
        this.getValue().setElevation(this.getValue().getElevation() + 2.0);
    }

    public void landing() {
        this.getValue().setElevation(this.getValue().getElevation() - 2.0);
    }

    public void updatePosition(double targetLatitude, double targetLongitude, double speed) {

        this.setDistance(Math.round(Math.sqrt(Math.pow(targetLatitude - this.getValue().getLatitude(), 2) +
                Math.pow(targetLongitude - this.getValue().getLongitude(), 2)) * 1000000.0) / 1000000.0);
        this.getValue().setLatitude(Math.round((this.getValue().getLatitude()+ (targetLatitude -
                this.getValue().getLatitude()) *
                (speed / distance)) * 1000000.0) / 1000000.0); // Calcolo avanzamento latitudine
        this.getValue().setLongitude(Math.round((this.getValue().getLongitude()+ (targetLongitude -
                this.getValue().getLongitude()) *
                (speed / distance)) * 1000000.0) / 1000000.0); // Calcolo avanzamento latitudin

    }

}
