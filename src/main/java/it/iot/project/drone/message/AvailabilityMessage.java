package it.iot.project.drone.message;

public class AvailabilityMessage extends GenericMessage{


    private boolean tankStatus;

    private boolean droneStatus;


    public AvailabilityMessage() {
    }

    public AvailabilityMessage(String id, String type, long timestamp, double latitude, double longitude,
                               boolean tankStatus, boolean droneStatus) {
        super(id,type, timestamp, latitude, longitude);
        this.tankStatus = tankStatus;
        this.droneStatus = droneStatus;
    }

    public boolean isTankStatus() {
        return tankStatus;
    }

    public void setTankStatus(boolean tankStatus) {
        this.tankStatus = tankStatus;
    }

    public boolean isDroneStatus() {
        return droneStatus;
    }

    public void setDroneStatus(boolean droneStatus) {
        this.droneStatus = droneStatus;
    }

}
