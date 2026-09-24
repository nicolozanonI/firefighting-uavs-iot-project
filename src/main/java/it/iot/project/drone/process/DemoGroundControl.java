package it.iot.project.drone.process;

import it.iot.project.drone.conf.MqttConfParameters;
import it.iot.project.drone.message.AlarmMessage;
import it.iot.project.drone.message.AvailabilityMessage;
import it.iot.project.drone.message.PositionMessage;
import it.iot.project.drone.services.AppService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import com.google.gson.Gson;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class DemoGroundControl {

    private final static Logger controlLogger = LoggerFactory.getLogger(DemoGroundControl.class);
    public static double emergencyLatitude;
    public  static double emergencyLongitude;
    private static boolean missionStatus;
    private static String missionDateTime = "null";
    private static final LinkedList<String> droneList = new LinkedList<String>();
    private static final HashMap<String, Double[]> sensorMap = new HashMap<String, Double[]>();
    private static final HashMap<String, String> missionMap = new HashMap<String, String>();

    private static final ArrayList<Double[]> refillPoints = new ArrayList<Double[]>();

    public static void main(String[] args) throws Exception{

        try{

            AppService appService = new AppService();
            appService.run("server", "configuration.yml");
            appService.messageResource.appConfig.getMessageManager().addNewMissionId(missionDateTime);
            appService.messageResource.appConfig.getMessageManager().addNewMission(missionDateTime,
                    new ArrayList<Object>());

            String mqttGroundControlClientId = UUID.randomUUID().toString();

            MqttClientPersistence persistence = new MemoryPersistence();

            IMqttClient mqttGroundCOntrolClient = new MqttClient(String.format("tcp://%s:%d",
                    MqttConfParameters.BROKER_ADDRESS, MqttConfParameters.BROKER_PORT),
                    mqttGroundControlClientId, persistence);

            MqttConnectOptions options = new MqttConnectOptions();
            options.setUserName(MqttConfParameters.MQTT_USERNAME);
            options.setPassword(new String(MqttConfParameters.MQTT_PASSWORD).toCharArray());
            options.setAutomaticReconnect(true);
            options.setCleanSession(true);
            options.setConnectionTimeout(10);

            mqttGroundCOntrolClient.connect(options);

            controlLogger.info("Connected - Client Id; {}", mqttGroundControlClientId);

            refillPoints.add(MqttConfParameters.refillPoint1);
            refillPoints.add(MqttConfParameters.refillPoint2);
            refillPoints.add(MqttConfParameters.refillPoint3);
            refillPoints.add(MqttConfParameters.refillPoint4);

            mqttGroundCOntrolClient.subscribe(MqttConfParameters.BASE_TOPIC+MqttConfParameters.FLEET_TOPIC
                    +"/+"+MqttConfParameters.CONTROL_AVAILABILITY, (topic, msg) -> {

                        byte[] payload = msg.getPayload();
                        AvailabilityMessage avaiabilityMessage = parseAvailabilityJsonMessage(payload);
                        if(avaiabilityMessage != null) {
                            controlLogger.info("AVAILABILITY MESSAGE RECEIVED {} Data -> Drone ID: {}, " +
                                            "Type: {}, Timestamp: {}, " +
                                            "Drone status: {}, Tank status {}",
                                    topic,
                                    avaiabilityMessage.getId(),
                                    avaiabilityMessage.getType(),
                                    avaiabilityMessage.getTimestamp(),
                                    avaiabilityMessage.isDroneStatus(),
                                    avaiabilityMessage.isTankStatus());
                            appService.messageResource.appConfig.getMessageManager()
                                    .addNewAvailabilityMessage(missionDateTime, avaiabilityMessage);

                            if (avaiabilityMessage.isDroneStatus()) {
                                droneList.add(avaiabilityMessage.getId());
                                for (String i : droneList) {
                                    controlLogger.info("Drone id: ---> {}", i);
                                }

                                for (String i : sensorMap.keySet()) {
                                    controlLogger.info("Sensor ---> Sensor Id: {}, Position: {}", i,
                                            sensorMap.get(i));
                                }
                                for (String i : missionMap.keySet()) {
                                    controlLogger.info("Mission ---> Sensor Id: {}, Drone ID: {}", i,
                                            missionMap.get(i));
                                }

                                if (!sensorMap.isEmpty()) {
                                    for (String i : sensorMap.keySet()) {
                                        if (!missionMap.containsKey(i)){
                                            emergencyLatitude = sensorMap.get(i)[0];
                                            emergencyLongitude = sensorMap.get(i)[1];
                                            String emergencyPositionString = buildJsonMessage("Receiver: "+
                                                            avaiabilityMessage.getId(),
                                                    MqttConfParameters.START_MISSION,
                                                    emergencyLatitude, emergencyLongitude);

                                            if (emergencyPositionString != null) {

                                                publishData(mqttGroundCOntrolClient, MqttConfParameters.BASE_TOPIC
                                                        + MqttConfParameters.FLEET_TOPIC +"/" + avaiabilityMessage.getId()+
                                                        "/control", emergencyPositionString);
                                                controlLogger.info("Message correctly published");
                                                byte[] apiPayload = emergencyPositionString.getBytes();
                                                PositionMessage apiPositionMessage = parseJsonMessage(apiPayload);
                                                appService.messageResource.appConfig.getMessageManager()
                                                        .addNePositionMessage(missionDateTime, apiPositionMessage);
                                                missionMap.put(i, avaiabilityMessage.getId());
                                                for (String k : missionMap.keySet()) {
                                                    controlLogger.info("Mission ---> Sensor Id: {}, Drone ID: {}", i,
                                                            missionMap.get(i));
                                                }
                                                droneList.remove(avaiabilityMessage.getId());
                                            } else {
                                                controlLogger.error("Emergency message NULL");
                                            }
                                            break;

                                        }
                                    }
                                } else if (sensorMap.isEmpty()) {
                                    String basePositionString = buildJsonMessage("Receiver: " + avaiabilityMessage.getId(),
                                            MqttConfParameters.BASE, 44.974768, 10.564354);

                                    if (basePositionString != null) {

                                        publishData(mqttGroundCOntrolClient, MqttConfParameters.BASE_TOPIC +
                                                MqttConfParameters.FLEET_TOPIC+ "/" + avaiabilityMessage.getId() +
                                                "/control", basePositionString);
                                        byte[] apiPayload = basePositionString.getBytes();
                                        PositionMessage apiPositionMessage = parseJsonMessage(apiPayload);
                                        appService.messageResource.appConfig.getMessageManager()
                                                .addNePositionMessage(missionDateTime, apiPositionMessage);
                                    }
                                }

                            } else if (!avaiabilityMessage.isDroneStatus()) {
                                String sKey = getKeyByValue(missionMap, avaiabilityMessage.getId());
                                if (sKey != null) {
                                    missionMap.remove(sKey);
                                }

                                if (!avaiabilityMessage.isTankStatus()) {
                                    double refillDistanceMin =
                                            Math.round(Math.sqrt(Math.pow(avaiabilityMessage.getLatitude() -
                                                    MqttConfParameters.basePoint[0], 2) +
                                            Math.pow(avaiabilityMessage.getLongitude() -
                                                    MqttConfParameters.basePoint[1], 2)) * 1000000.0) / 1000000.0;
                                    double refillDistance;
                                    double refillCoordinates[] = {MqttConfParameters.basePoint[0],
                                            MqttConfParameters.basePoint[1]};

                                    for(Double[] i : refillPoints) {
                                        refillDistance = Math.round(Math.sqrt(Math.pow(avaiabilityMessage.getLatitude()
                                                - i[0], 2) +
                                                Math.pow(avaiabilityMessage.getLongitude() - i[1], 2)) * 1000000.0) / 1000000.0;
                                        if (refillDistance < refillDistanceMin) {
                                            refillDistanceMin = refillDistance;
                                            refillCoordinates[0] = i[0];
                                            refillCoordinates[1] = i[1];
                                        }

                                    }

                                    String rechargeString = buildJsonMessage("Receiver: "+avaiabilityMessage.getId(),
                                            MqttConfParameters.RECHARGE_POINT,
                                            refillCoordinates[0], refillCoordinates[1]);
                                    if (rechargeString != null) {

                                        publishData(mqttGroundCOntrolClient, MqttConfParameters.BASE_TOPIC +
                                                MqttConfParameters.FLEET_TOPIC + "/" +
                                                avaiabilityMessage.getId()+"/control", rechargeString);
                                        controlLogger.info("Data correctly published");
                                        byte[] apiPayload = rechargeString.getBytes();
                                        PositionMessage apiPositionMessage = parseJsonMessage(apiPayload);
                                        appService.messageResource.appConfig.getMessageManager()
                                                .addNePositionMessage(missionDateTime, apiPositionMessage);

                                        for (String i : missionMap.keySet()) {
                                            controlLogger.info("Mission ---> Sensor Id: {}, Drone ID: {}", i, missionMap.get(i));
                                        }
                                    } else {
                                        controlLogger.error("Emergency message NULL");
                                    }
                                }
                            }
                        }
                        else{
                            controlLogger.info("Message Received {} Message Received: {}", topic, new String(payload));}
                    }
            );

            mqttGroundCOntrolClient.subscribe(MqttConfParameters.BASE_TOPIC + MqttConfParameters.SENSOR_TOPIC+ "/+" +
                    MqttConfParameters.CONTROL_ALARM_TOPIC, (topic, msg) -> {

                        byte[] payload = msg.getPayload();
                        AlarmMessage alarmMessage = parseAlarmJsonMessage(payload);

                        if(alarmMessage != null) {
                            controlLogger.info("ALARM MESSAGE RECEIVED {} Data -> Sensor ID: {} Timestamp: {}, " +
                                            "Type: {}, Latitude: {}, Longitude {}, sensor status: {}",
                                    topic,
                                    alarmMessage.getId(),
                                    alarmMessage.getTimestamp(),
                                    alarmMessage.getType(),
                                    alarmMessage.getLatitude(),
                                    alarmMessage.getLongitude(),
                                    alarmMessage.isSensorStatus());

                            if (alarmMessage.isSensorStatus()) {


                                if (sensorMap.isEmpty()) {
                                    missionStatus = true;
                                    LocalDateTime myDateObj = LocalDateTime.now();
                                    DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("dd-MM-yyyy-HH-mm-ss");
                                    missionDateTime = myDateObj.format(myFormatObj);
                                    controlLogger.info("MISSION STARTED, (mission active: {}), Date and time: {}", missionStatus, missionDateTime);
                                    appService.messageResource.appConfig.getMessageManager().addNewMissionId(missionDateTime);
                                    appService.messageResource.appConfig.getMessageManager().addNewMission(missionDateTime, new ArrayList<Object>());
                                    appService.messageResource.appConfig.getMessageManager().addNeAlarmMessage(missionDateTime, alarmMessage);

                                }

                                sensorMap.put(alarmMessage.getId(), new Double[]{alarmMessage.getLatitude(),
                                        alarmMessage.getLongitude()});

                                for (String i : sensorMap.keySet()) {
                                    controlLogger.info(" Sensor Id: {}, Position: {} {}", i, sensorMap.get(i)[0],
                                            sensorMap.get(i)[1]);
                                }

                                if (droneList.peekLast() != null) {
                                    for (String i : droneList) {
                                        controlLogger.info("Drone list Id {}", i);
                                    }

                                    emergencyLatitude = alarmMessage.getLatitude();
                                    emergencyLongitude = alarmMessage.getLongitude();
                                    String emergencyPositionString = buildJsonMessage("Receiver: "+
                                                    droneList.peekLast(),"START_MISSION",emergencyLatitude,
                                            emergencyLongitude);


                                    if (emergencyPositionString != null) {
                                        publishData(mqttGroundCOntrolClient, MqttConfParameters.BASE_TOPIC +
                                                        MqttConfParameters.FLEET_TOPIC + "/" + droneList.peekLast()+
                                                        "/control",
                                                emergencyPositionString);
                                        controlLogger.info("Message correctly published");
                                        missionMap.put(alarmMessage.getId(), droneList.peekLast());
                                        byte[] apiPayload = emergencyPositionString.getBytes();
                                        PositionMessage apiPositionMessage = parseJsonMessage(apiPayload);
                                        appService.messageResource.appConfig.getMessageManager()
                                                .addNePositionMessage(missionDateTime, apiPositionMessage);


                                        for (String i : missionMap.keySet()) {
                                            controlLogger.info("Mission ---> Sensor Id: {}, Drone ID: {}", i,
                                                    missionMap.get(i));
                                        }
                                        //sensorMap.remove(alarmMessage.getId());
                                        droneList.remove(missionMap.get(alarmMessage.getId()));
                                    } else {
                                        controlLogger.error("Emergency message NULL");
                                    }
                                }
                            } else if (!alarmMessage.isSensorStatus()) {

                                if (missionMap.containsKey(alarmMessage.getId())) {
                                    droneList.add(missionMap.get(alarmMessage.getId()));
                                }
                                sensorMap.remove(alarmMessage.getId());
                                if (sensorMap.isEmpty()) {
                                    missionStatus = false;

                                    controlLogger.info("MISSION ENDED, (mission active: {})", missionStatus);
                                    String basePositionString = buildJsonMessage("Receiver: all",
                                            MqttConfParameters.END_MISSION, 44.974768, 10.564354);

                                    if (basePositionString != null) {
                                        for (String i : droneList) {
                                            if (i != null) {
                                                publishData(mqttGroundCOntrolClient, MqttConfParameters.BASE_TOPIC
                                                        + MqttConfParameters.FLEET_TOPIC+ "/" + i + "/control", basePositionString);
                                                byte[] apiPayload = basePositionString.getBytes();
                                                PositionMessage apiPositionMessage = parseJsonMessage(apiPayload);
                                                appService.messageResource.appConfig.getMessageManager()
                                                        .addNePositionMessage(missionDateTime, apiPositionMessage);

                                            }
                                        }
                                    }
                                    if (missionMap.containsKey(alarmMessage.getId())) {
                                        missionMap.remove(alarmMessage.getId());
                                    }
                                    missionDateTime = "null";

                                } else if (!sensorMap.isEmpty()) {
                                    if (missionMap.containsKey(alarmMessage.getId())) {
                                        missionMap.remove(alarmMessage.getId());
                                    }


                                    for (String i : sensorMap.keySet()) {
                                        if (droneList.peekLast() != null) {
                                            if (!missionMap.containsKey(i)) {
                                                missionMap.put(i, droneList.peekLast());
                                                emergencyLatitude = sensorMap.get(i)[0];
                                                emergencyLongitude = sensorMap.get(i)[1];
                                                String emergencyPositionString = buildJsonMessage("Receiver: " +
                                                                missionMap.get(i), MqttConfParameters.ALARM_MESSAGE_TYPE,
                                                        emergencyLatitude, emergencyLongitude);

                                                if (emergencyPositionString != null) {

                                                    publishData(mqttGroundCOntrolClient, MqttConfParameters.BASE_TOPIC
                                                            + "/" + MqttConfParameters.FLEET_TOPIC +
                                                            missionMap.get(i) + "/control", emergencyPositionString);
                                                    controlLogger.info("Message correctly published");
                                                    missionMap.put(i, missionMap.get(i));
                                                    byte[] apiPayload = emergencyPositionString.getBytes();
                                                    PositionMessage apiPositionMessage = parseJsonMessage(apiPayload);
                                                    appService.messageResource.appConfig.getMessageManager()
                                                            .addNePositionMessage(missionDateTime, apiPositionMessage);
                                                    for (String k : missionMap.keySet()) {
                                                        controlLogger.info("Mission ---> Sensor Id: {}, Drone ID: {}", i,
                                                                missionMap.get(i));
                                                    }
                                                    //sensorMap.remove(alarmMessage.getId());
                                                    droneList.remove(missionMap.get(i));
                                                } else {
                                                    controlLogger.error("Emergency message NULL");
                                                }

                                            }
                                        }
                                    }
                                }
                            }
                        }
                        else{
                            controlLogger.info("Message Received {} Message Received: {}", topic, new String(payload));}
                    }
            );

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static String buildJsonMessage(String id, String msgType, double controlTargetLatitude,
                                          double controlTargetLongitude){

        try {

            Gson gson = new Gson();
            PositionMessage fireSensorPositionMessage = new PositionMessage(id, msgType,
                    System.currentTimeMillis(), controlTargetLatitude, controlTargetLongitude);
            return gson.toJson(fireSensorPositionMessage);
        }catch(Exception e) {
            controlLogger.error("Error creating json payload ! Message: {}", e.getLocalizedMessage());
            return null;
        }
    }

    public static void publishData(IMqttClient mqttClient, String topic, String msgString) throws MqttException {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    controlLogger.info("Publishing to Topic: {} Data: {}", topic, msgString);
                    if (mqttClient.isConnected() && msgString != null && topic != null) {

                        MqttMessage msg = new MqttMessage(msgString.getBytes());
                        msg.setQos(1);
                        msg.setRetained(false);
                        mqttClient.publish(topic, msg);

                    } else {
                        controlLogger.error("Error: Topic or Msg = Null or MQTT Client is not Connected !");
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
            return gson.fromJson(new String(payload), PositionMessage.class);

        }catch (Exception e) {
            controlLogger.error("Parsing error");
            return null;
        }

    }

    public static AlarmMessage parseAlarmJsonMessage(byte[] payload) {

        try {
            Gson gson = new Gson();
            return gson.fromJson(new String(payload), AlarmMessage.class);
        }catch (Exception e) {
            controlLogger.error("Parsing error");
            return null;
        }

    }
    public static AvailabilityMessage parseAvailabilityJsonMessage(byte[] payload) {

        try {
            Gson gson = new Gson();
            return gson.fromJson(new String(payload), AvailabilityMessage.class);
        }catch (Exception e) {
            controlLogger.error("Parsing error");
            return null;
        }

    }

    public static <T, E> T getKeyByValue(HashMap<T, E> map, E value) {
        for (Map.Entry<T, E> entry : map.entrySet()) {
            if (Objects.equals(value, entry.getValue())) {
                return entry.getKey();
            }
        }
        return null;
    }

}
