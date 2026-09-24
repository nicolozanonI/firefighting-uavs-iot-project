package it.iot.project.drone.devices;

import com.google.gson.Gson;
import it.iot.project.drone.conf.MqttConfParameters;
import it.iot.project.drone.resources.FireDetectionSensor;
import it.iot.project.drone.message.AlarmMessage;
import org.eclipse.paho.client.mqttv3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MqttSmartFireDetectorModel {

    public static final String RESOURCE_TYPE = "fire_detection_sensor";

    private String id;

    public double latitude;

    public double longitude;

    //private boolean value;

    private FireDetectionSensor fireDetectionSensor = new FireDetectionSensor(false);

    private IMqttClient mqttClient;

    private final static Logger logger = LoggerFactory.getLogger(MqttSmartFireDetectorModel.class);

    public static final String ALARM_MESSAGE_TYPE = "ALARM";

    public static final String END_MISSION = "END_MISSION";

    public MqttSmartFireDetectorModel() {
    }

    public MqttSmartFireDetectorModel(IMqttClient mqttClient, String id, double latitude, double longitude,
                                      boolean value) {
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
        //this.value = value;
        this.mqttClient = mqttClient;

    }

    /*public boolean isValue() {
        return value;
    }*/

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
/*
    public void setValue(boolean value) {
        this.value = value;
    }*/

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setMqttClient(IMqttClient mqttClient) {
        this.mqttClient = mqttClient;
    }

    public IMqttClient getMqttClient() {
        return mqttClient;
    }


    public void connectToBroker(){

        try{

            MqttConnectOptions options = new MqttConnectOptions();
            options.setUserName(MqttConfParameters.MQTT_USERNAME);
            options.setPassword(new String(MqttConfParameters.MQTT_PASSWORD).toCharArray());
            options.setAutomaticReconnect(true);
            options.setCleanSession(true);
            options.setConnectionTimeout(10);
            this.mqttClient.connect(options);
            logger.info("Connected! Sensor Id {}, Sensor Status: {}, Latitude: {}, Longitude {}, mqtt client: {}",
                    this.id, this.fireDetectionSensor.getValue(),
                    this.latitude, this.longitude, this.mqttClient);

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void publishData(IMqttClient mqttClient, String topic, String msgString) throws MqttException {

        logger.debug("Publishing to Topic: {} Data: {}", topic, msgString);

        if (mqttClient.isConnected() && msgString != null && topic != null){

            MqttMessage msg = new MqttMessage(msgString.getBytes());
            msg.setQos(2);
            msg.setRetained(false);
            mqttClient.publish(topic,msg);

        }else{
            logger.error("Error: Topic or Msg = Null or MQTT Client is not Connected !");
        }
    }

    public static String buildAlarmJsonMessage(String sensorId, String msgType, double FireSensorLatitude,
                                               double FireSensorLongitude, boolean sensorStatus){

        try {

            Gson gson = new Gson();

            AlarmMessage fireSensorAlarmMessage = new AlarmMessage(sensorId, msgType,
                    System.currentTimeMillis(), FireSensorLatitude, FireSensorLongitude, sensorStatus);

            return gson.toJson(fireSensorAlarmMessage);

        }catch(Exception e) {
            logger.error("Error creating json payload ! Message: {}", e.getLocalizedMessage());
            return null;
        }

    }

    public void turnAlarmOn() {

        try {

            this.fireDetectionSensor.setValue(true);
            String payloadString = buildAlarmJsonMessage(this.id, ALARM_MESSAGE_TYPE,latitude, longitude,
                    this.fireDetectionSensor.getValue());
            if (payloadString != null) {
                publishData(mqttClient, MqttConfParameters.BASE_TOPIC + MqttConfParameters.SENSOR_TOPIC + "/" +
                        this.getId() + MqttConfParameters.CONTROL_ALARM_TOPIC , payloadString);
                logger.info("FIRE ALARM ON ({} status changed in {})", id, this.fireDetectionSensor.getValue());
            }
            else
                logger.error("Skipping message send due to NULL Payload !");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void turnAlarmOff() {

        try {

            this.fireDetectionSensor.setValue(false);
            String payloadString = buildAlarmJsonMessage(this.id, ALARM_MESSAGE_TYPE,0.0,
                    0.0, this.fireDetectionSensor.getValue());
            if (payloadString != null) {
                publishData(mqttClient, MqttConfParameters.BASE_TOPIC + MqttConfParameters.SENSOR_TOPIC + "/"+
                        this.getId()+ MqttConfParameters.CONTROL_ALARM_TOPIC , payloadString);
                logger.info("FIRE ALARM OFF ( {} status changed in {})", id, this.fireDetectionSensor.getValue());
            }
            else
                logger.error("Skipping message send due to NULL Payload !");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("MqttSmartFireDetector{");
        sb.append("id=").append(id);
        sb.append(", latitude=").append(latitude);
        sb.append(", longitude=").append(longitude);
        sb.append('}');
        return sb.toString();
    }

}
