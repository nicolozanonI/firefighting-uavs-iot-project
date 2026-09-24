package it.iot.project.drone.process;

import it.iot.project.drone.conf.MqttConfParameters;

import it.iot.project.drone.devices.MqttSmartDroneModel;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MqttSmartDroneProcess {

    private static final Logger droneProcessLogger = LoggerFactory.getLogger(MqttSmartDroneModel.class);
    private static final String DRONE_ONE_ID = "drone-1";




    public static void main(String[] args) {

        try {
            // Creating Drone 1: id, persistence, mqttClient
            String idDrone1 = DRONE_ONE_ID;
            MqttClientPersistence persistence1 = new MemoryPersistence();
            IMqttClient mqttDroneClientOne = generateMqttClient(idDrone1, persistence1);
            // Create drone 1 and connect to broker
            MqttSmartDroneModel drone1 = new MqttSmartDroneModel(idDrone1, "DJI",
                    "firefighter-drone", "v5.0.0.2", 0.01,mqttDroneClientOne);
            drone1.connectDroneToBroker();
            drone1.subscribeDroneToTopics();

            new Thread(new Runnable() {
                @Override
                public void run() {
                    drone1.startTest();
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
