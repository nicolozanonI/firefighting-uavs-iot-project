package it.iot.project.drone.conf;

public class MqttConfParameters {
    public static String BROKER_ADDRESS = "<broker-address";
    public static int BROKER_PORT = 1883;
    public static final String MQTT_USERNAME = "<username>";
    public static final String MQTT_PASSWORD = "<password>";

    public static final String BASE_TOPIC = "<base-topic>";
    public static final String CONTROL_ALARM_TOPIC = "/alarm";
    public static final String CONTROL_AVAILABILITY = "/availability";
    public static final String FLEET_TOPIC = "/firefighter/fleet/drone";
    public static final String SENSOR_TOPIC = "/firefighter/sensor";
    public static final String ALARM_MESSAGE_TYPE = "ALARM";
    public static final String START_MISSION = "START_MISSION";
    public static final String END_MISSION = "BASE";

    public static final String BASE = "BASE";
    public static final String RECHARGE_POINT = "REFILL";
    public static final String TANK_EMPTY_MESSAGE = "TANK_EMPTY";
    public static final String AVAILABILITY_MESSAGE = "AVAILABILITY";

    public static final Double refillPoint1[] = {45.427865, 10.728326};

    public static final Double refillPoint2[] = {44.627782, 10.275632};
    public static final Double refillPoint3[] = {45.8245837, 10.875284};
    public static final Double refillPoint4[] = {46.0792384, 10.929327};

    public static final Double basePoint[] = {45.054768, 10.564354};

}
