package it.iot.project.drone.process;

import it.iot.project.drone.conf.MqttConfParameters;

import it.iot.project.drone.devices.MqttSmartDroneModel;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MqttSmartDroneProccesTwo {

    private static final Logger droneProcessLogger = LoggerFactory.getLogger(MqttSmartDroneModel.class);

    //private static final String DRONE_ONE_ID = "drone_1";

    private static final String DRONE_TWO_ID = "drone-2";


    public static void main(String[] args) {

        try {

            String idDrone2 = DRONE_TWO_ID;
            MqttClientPersistence persistence2 = new MemoryPersistence();
            IMqttClient mqttDroneClientOne = generateMqttClient(idDrone2, persistence2);
            MqttSmartDroneModel drone2 = new MqttSmartDroneModel(idDrone2, "DJI", "firefighter-drone",
                    "v5.0.0.2", 0.01, mqttDroneClientOne);
            drone2.connectDroneToBroker();
            drone2.subscribeDroneToTopics();

            new Thread(new Runnable() {
                @Override
                public void run() {
                    drone2.startTest();
                }
            }).start();

        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public static IMqttClient generateMqttClient(String mqttId, MqttClientPersistence persistence) {

        try {

            IMqttClient mqttDroneClient = new MqttClient(String.format("tcp://%s:%d",
                    MqttConfParameters.BROKER_ADDRESS, MqttConfParameters.BROKER_PORT),
                    mqttId, persistence);

            return mqttDroneClient;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
