package it.iot.project.drone.message;

public class AlarmMessage extends GenericMessage{


    private boolean sensorStatus;

    public AlarmMessage() {
    }

    public AlarmMessage(String id, String type, long timestamp, double latitude, double longitude,
                        boolean sensorStatus) {
        super(id,type, timestamp, latitude, longitude);
        this.sensorStatus = sensorStatus;
    }



    public boolean isSensorStatus() {
        return sensorStatus;
    }

    public void setSensorStatus(boolean sensorStatus) {
        this.sensorStatus = sensorStatus;
    }

}
