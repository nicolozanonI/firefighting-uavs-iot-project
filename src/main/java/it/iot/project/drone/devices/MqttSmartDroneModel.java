package it.iot.project.drone.devices;

import it.iot.project.drone.conf.MqttConfParameters;
import it.iot.project.drone.model.DroneDescriptor;
import it.iot.project.drone.model.GeolocalizationDescriptor;
import it.iot.project.drone.resources.*;
import it.iot.project.drone.message.AvailabilityMessage;
import it.iot.project.drone.message.PositionMessage;
import it.iot.project.drone.resources.*;

import com.google.gson.Gson;
import org.eclipse.paho.client.mqttv3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MqttSmartDroneModel extends DroneDescriptor {

    //private String id;

    private TankStatusSwitchResource tankStatusSwitchResource = new TankStatusSwitchResource(true);
    private TankActionSwitchResource tankActionSwitchResource = new TankActionSwitchResource(false);
    private GPSResource gpsResource = new GPSResource(new GeolocalizationDescriptor(44.974768,
            10.564354, 0.0));
    private LiquidLevelSensor liquidLevelSensor = new LiquidLevelSensor(30.0);

    private BatteryLevelSensor batteryLevelSensor = new BatteryLevelSensor(100.0);

    private ObjectDetectionSensor objectDetectionSensor = new ObjectDetectionSensor(false);


    private IMqttClient mqttClient;
    private double diagonal;


    private final static Logger droneLogger = LoggerFactory.getLogger(MqttSmartDroneModel.class);
     //Base Topic

    public static final String CONTROL_DRONE_TOPIC = "/control";   //Publish To

    public static final String MISSION_TOPIC = "/drone"; // +Id Drone, Subscrobe To
    private static final String CONTROL_AVAILABILITY = "/availability";
    //private static final double DRONE_SPEED = 0.01;
    private double targetLatitude;
    private double targetLongitude;
    private String messageType;

    public MqttSmartDroneModel() {
    }

    public MqttSmartDroneModel(String id,String manifacturer, String type, String softwareVersion,
                               double speed,IMqttClient mqttClient) {
        this.id = id;
        this.speed = speed;
        this.mqttClient = mqttClient;
        this.type = type;
        this.manifacturer = manifacturer;
        this.softwareVersion = softwareVersion;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public IMqttClient getMqttClient() {
        return mqttClient;
    }

    public void setMqttClient(IMqttClient mqttClient) {
        this.mqttClient = mqttClient;
    }

    public double getTargetLatitude() {
        return targetLatitude;
    }

    public void setTargetLatitude(double targetLatitude) {
        this.targetLatitude = targetLatitude;
    }

    public double getTargetLongitude() {
        return targetLongitude;
    }

    public void setTargetLongitude(double targetLongitude) {
        this.targetLongitude = targetLongitude;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }


    public void connectDroneToBroker(){

        try{

            MqttConnectOptions options = new MqttConnectOptions();
            options.setUserName(MqttConfParameters.MQTT_USERNAME);
            options.setPassword(new String(MqttConfParameters.MQTT_PASSWORD).toCharArray());
            options.setAutomaticReconnect(true);
            options.setCleanSession(true);
            options.setConnectionTimeout(10);
            this.mqttClient.connect(options);

            droneLogger.info("Connected - Client Id {}", this.mqttClient);
            droneLogger.info("Drone Id {}, type: {}, manifacturer: {} software version: {}, speed: {}", this.id,
                    this.type, this.manifacturer, this.softwareVersion, this.speed);


        }catch (Exception e){
            e.printStackTrace();
        }

    }


    public void subscribeDroneToTopics(){

        try{


            String avaiabilitymsg = buildAvaiabilityJsonMessage(this.id, MqttConfParameters.AVAILABILITY_MESSAGE,
                    this.gpsResource.getValue().getLatitude(), this.gpsResource.getValue().getLongitude(),
                    this.tankStatusSwitchResource.getValue(),
                    true);
            if (avaiabilitymsg != null) {
                publishData(mqttClient,MqttConfParameters.BASE_TOPIC + MqttConfParameters.FLEET_TOPIC + "/" +
                        this.getId() + CONTROL_AVAILABILITY, avaiabilitymsg);
                droneLogger.info("Data correctly published");
            } else {
                droneLogger.error("Recharge message NULL");
            }

            mqttClient.subscribe(MqttConfParameters.BASE_TOPIC + MqttConfParameters.FLEET_TOPIC + "/" + this.getId()
                    + "/control", (topic, msg) -> {

                        byte[] payload = msg.getPayload();

                        PositionMessage positionMessage = parseJsonMessage(payload);

                        if(positionMessage != null) {
                            droneLogger.info("NEW MESSAGE RECEIVED  from {} Data -> Timestamp: {}, Type: {}, " +
                                            "Latitude: {}, " + "Longitude {}",
                                    topic,
                                    positionMessage.getTimestamp(),
                                    positionMessage.getType(),
                                    positionMessage.getLatitude(),
                                    positionMessage.getLongitude());
                            setMessageType(positionMessage.getType());
                            setTargetLatitude(positionMessage.getLatitude());
                            setTargetLongitude(positionMessage.getLongitude());
                            /*droneLogger.info("Message type: {}, Target latitude: {}, Target longitude: {}",
                                    this.getMessageType(), this.getTargetLatitude(), this.getTargetLongitude());*/

                        }
                        else{
                            droneLogger.info("Message Received {} Message Received: {}", topic, new String(payload));}
                    }
            );

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void startTest() {

        try {

            this.messageType = "NO MESSAGE";
            //this.tankActionSwitchResource.setValue(false);

            while (true) {

                this.batteryLevelSensor.dischargeBattery();

                if (this.tankStatusSwitchResource.getValue()) {
                    switch (this.getMessageType()) {
                        case MqttConfParameters.START_MISSION -> {

                            if ((Math.abs(this.targetLatitude - this.gpsResource.getValue().getLatitude()) > 0.03) ||
                                    (Math.abs(this.targetLongitude -
                                    this.gpsResource.getValue().getLongitude()) > 0.03)) {
                                if (this.gpsResource.getValue().getElevation() < 10.0) {
                                    this.gpsResource.takeOff();

                                } else {
                                    this.objectDetectionSensor.detection();
                                    if (this.objectDetectionSensor.getValue()) {
                                        this.gpsResource.updatePosition(targetLatitude*(-2),
                                                targetLongitude*(-5), this.speed);
                                        droneLogger.info("OBJECT DETECTED! Deviation to position: {} {}",
                                                this.gpsResource.getValue().getLatitude(),
                                                this.gpsResource.getValue().getLongitude());
                                    } else {
                                        this.gpsResource.updatePosition(targetLatitude, targetLongitude, this.speed);
                                    }

                            }
                                droneLogger.info("{} GPS: {} {} elv: {}, Tank: FULL --> {} --- OPEN --> {}, " +
                                                "Battery: {} %", this.getId(), this.gpsResource.getValue().getLatitude(),
                                        this.gpsResource.getValue().getLongitude(),
                                        this.gpsResource.getValue().getElevation(),
                                        this.tankStatusSwitchResource.getValue(),
                                        this.tankActionSwitchResource.getValue(),
                                        (Math.round(this.batteryLevelSensor.getValue() * 100.0) / 100.0));

                            } else if ((this.targetLatitude - this.gpsResource.getValue().getLatitude() <= 0.03) ||
                                    (this.targetLongitude -
                                    this.gpsResource.getValue().getLongitude() <= 0.03)) {
                                this.gpsResource.getValue().setLatitude(this.targetLatitude);
                                this.gpsResource.getValue().setLongitude(this.targetLongitude);
                                this.tankActionSwitchResource.setValue(true);
                                this.liquidLevelSensor.emptyTank();
                                droneLogger.info("{} ON TARGET ({} {}), liquid level = {} l, tank->  OPEN: {}",
                                        this.getId(),this.gpsResource.getValue().getLatitude(),
                                        this.gpsResource.getValue().getLongitude(),this.liquidLevelSensor.getValue(),
                                        this.tankActionSwitchResource.getValue());
                                if (this.liquidLevelSensor.getValue() <= 3.0){
                                    this.tankStatusSwitchResource.setValue(false);
                                    this.tankActionSwitchResource.setValue(false);

                                    droneLogger.info("Tank: FULL -> {} -- OPEN -> {} ",
                                            this.tankStatusSwitchResource.getValue(),
                                            this.tankActionSwitchResource.getValue());
                                    String availabilityMessage = buildAvaiabilityJsonMessage(this.id,
                                            MqttConfParameters.TANK_EMPTY_MESSAGE,
                                            this.gpsResource.getValue().getLatitude(),
                                            this.gpsResource.getValue().getLongitude(),
                                            this.tankStatusSwitchResource.getValue(),
                                            false);
                                    if (availabilityMessage != null) {
                                        publishData(mqttClient,MqttConfParameters.BASE_TOPIC +
                                                MqttConfParameters.FLEET_TOPIC +"/" + this.getId() +
                                                CONTROL_AVAILABILITY, availabilityMessage);
                                        droneLogger.info("Message correctly published");
                                    } else {
                                        droneLogger.error("Recharge message NULL");
                                    }

                                }
                            }
                        }
                        case MqttConfParameters.BASE -> {
                            if ((Math.abs(this.targetLatitude - this.gpsResource.getValue().getLatitude()) > 0.03) ||
                                    (Math.abs(this.targetLongitude -
                                    this.gpsResource.getValue().getLongitude()) > 0.03)) {
                                if (this.gpsResource.getValue().getElevation() < 10.0) {
                                    this.gpsResource.takeOff();
                                } else {
                                    this.objectDetectionSensor.detection();
                                    if (this.objectDetectionSensor.getValue()) {
                                        this.gpsResource.updatePosition(targetLatitude*(-2),
                                                targetLongitude*(-5), this.speed);
                                        droneLogger.info("OBJECT DETECTED! Deviation to position: {} {}",
                                                this.gpsResource.getValue().getLatitude(),
                                                this.gpsResource.getValue().getLongitude());
                                    } else {
                                        this.gpsResource.updatePosition(targetLatitude, targetLongitude, this.speed);
                                    }
                                }
                                droneLogger.info("{} GPS: {} {} elv: {}, Tank: FULL --> {} --- OPEN --> {}, " +
                                                "Battery: {} %", this.getId(),
                                        this.gpsResource.getValue().getLatitude(),
                                        this.gpsResource.getValue().getLongitude(),
                                        this.gpsResource.getValue().getElevation(),
                                        this.tankStatusSwitchResource.getValue(),
                                        this.tankActionSwitchResource.getValue(),
                                        (Math.round(this.batteryLevelSensor.getValue() * 100.0)/100.0));
                            } else if ((this.targetLatitude - this.gpsResource.getValue().getLatitude() <= 0.03) ||
                                    (this.targetLongitude -
                                    this.gpsResource.getValue().getLongitude() <= 0.03)) {
                                if (this.gpsResource.getValue().getElevation() > 0.0) {
                                    this.gpsResource.landing();
                                    droneLogger.info("{} GPS: {} {} elv: {}, Tank: FULL --> {} --- OPEN --> {}," +
                                                    " Battery: {} %", this.getId(),
                                            this.gpsResource.getValue().getLatitude(),
                                            this.gpsResource.getValue().getLongitude(),
                                            this.gpsResource.getValue().getElevation(),
                                            this.tankStatusSwitchResource.getValue(),
                                            this.tankActionSwitchResource.getValue(),
                                            (Math.round(this.batteryLevelSensor.getValue() * 100.0)/100.0));
                                }
                            }
                        }
                    }

                } else if (!this.tankStatusSwitchResource.getValue()) {
                    switch (this.getMessageType()){
                        case MqttConfParameters.RECHARGE_POINT -> {
                            if ((Math.abs(this.targetLatitude - this.gpsResource.getValue().getLatitude()) > 0.03) ||
                                    (Math.abs(this.targetLongitude -
                                    this.gpsResource.getValue().getLongitude()) > 0.03)) {
                                if (this.gpsResource.getValue().getElevation() < 10.0) {
                                    this.gpsResource.takeOff();
                                } else {
                                    this.objectDetectionSensor.detection();
                                    if (this.objectDetectionSensor.getValue()) {
                                        this.gpsResource.updatePosition(targetLatitude*(-2),
                                                targetLongitude*(-5), this.speed);
                                        droneLogger.info("OBJECT DETECTED! Deviation to position: {} {}",
                                                this.gpsResource.getValue().getLatitude(),
                                                this.gpsResource.getValue().getLongitude());
                                    }
                                    this.gpsResource.updatePosition(targetLatitude, targetLongitude, this.speed);
                                }

                                droneLogger.info("{} GPS: {} {}, Tank: FULL -> {} OPEN -> {}, Battery: {} %",
                                        this.getId(),
                                        this.gpsResource.getValue().getLatitude(),
                                        this.gpsResource.getValue().getLongitude(),
                                        this.tankStatusSwitchResource.getValue(),
                                        this.tankActionSwitchResource.getValue(),
                                        (Math.round(this.batteryLevelSensor.getValue() * 100.0)/100.0));
                            } else if ((this.targetLatitude - this.gpsResource.getValue().getLatitude() <= 0.03) ||
                                    (this.targetLongitude -
                                    this.gpsResource.getValue().getLongitude() <= 0.03)) {
                                if (this.gpsResource.getValue().getElevation() > 0.0) {
                                    this.gpsResource.landing();
                                    droneLogger.info("{} GPS: {} {} elv: {}, Tank: FULL --> {} --- OPEN --> {}, " +
                                                    "Battery: {} %", this.getId(),
                                            this.gpsResource.getValue().getLatitude(),
                                            this.gpsResource.getValue().getLongitude(),
                                            this.gpsResource.getValue().getElevation(),
                                            this.tankStatusSwitchResource.getValue(),
                                            this.tankActionSwitchResource.getValue(),
                                            (Math.round(this.batteryLevelSensor.getValue() * 100.0)/100.0));

                                } else {
                                    this.gpsResource.getValue().setLatitude(this.targetLatitude);
                                    this.gpsResource.getValue().setLongitude(this.targetLongitude);
                                    this.liquidLevelSensor.rechargeTank();
                                    droneLogger.info("{} Recharging, liquid level = {} l", this.getId(),
                                            this.liquidLevelSensor.getValue());
                                    if (this.liquidLevelSensor.getValue() >= 29.0) {
                                        this.liquidLevelSensor.setValue(30.0);
                                        this.tankStatusSwitchResource.setValue(true);
                                        String availabilityMessage = buildAvaiabilityJsonMessage(this.id,
                                                MqttConfParameters.AVAILABILITY_MESSAGE,
                                                this.gpsResource.getValue().getLatitude(),
                                                this.gpsResource.getValue().getLongitude(),
                                                this.tankStatusSwitchResource.getValue(),
                                                true);
                                        if (availabilityMessage != null) {
                                            publishData(mqttClient, MqttConfParameters.BASE_TOPIC +
                                                    MqttConfParameters.FLEET_TOPIC + "/" + this.getId() +
                                                    CONTROL_AVAILABILITY, availabilityMessage);
                                            droneLogger.info("Message correctly published");
                                        } else {
                                            droneLogger.error("Recharge message NULL");
                                        }

                                    }
                                }

                            }
                        }
                    }
                }
                Thread.sleep(650);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    public static String buildAvaiabilityJsonMessage(String id, String msgType,double latitude, double longitude,
                                                     boolean tankStatus, boolean droneStatus){

        try {

            Gson gson = new Gson();

            AvailabilityMessage avaiabilityMessage = new AvailabilityMessage(id, msgType,
                    System.currentTimeMillis(), latitude, longitude,  tankStatus, droneStatus);

            return gson.toJson(avaiabilityMessage);

        }catch(Exception e) {
            droneLogger.error("Error creating json payload ! Message: {}", e.getLocalizedMessage());
            return null;
        }

    }


    public static void publishData(IMqttClient mqttClient, String topic, String msgString) throws MqttException {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    droneLogger.info("Publishing to Topic: {} Data: {}", topic, msgString);

                    if (mqttClient.isConnected() && msgString != null && topic != null) {


                        MqttMessage msg = new MqttMessage(msgString.getBytes());
                        msg.setQos(2);
                        msg.setRetained(false);
                        mqttClient.publish(topic, msg);
                        //droneLogger.info("Data Correctly Published !");
                    } else {
                        droneLogger.error("Error: Topic or Msg = Null or MQTT Client is not Connected !");
                    }
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        }).start();

    }


    public static PositionMessage parseJsonMessage(byte[] payload) {

        try {
            Gson gson = new Gson();
            //droneLogger.info("parsed");
            return gson.fromJson(new String(payload), PositionMessage.class);
        }catch (Exception e) {
            droneLogger.error("Parsing error");
            return null;
        }

    }


    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("MqttSmartDroneModel{");
        sb.append("id='").append(id).append('\'');
        //sb.append(", latitude=").append(latitude);
        //sb.append(", longitude=").append(longitude);
        //sb.append(", tankAction=").append(tankAction);
        //sb.append(", liquidQuantity=").append(liquidQuantity);
        sb.append('}');
        return sb.toString();
    }


}
