package it.iot.project.drone.process;

import it.iot.project.drone.conf.MqttConfParameters;

import it.iot.project.drone.devices.MqttSmartFireDetectorModel;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class MqttFireDetectorProcess {

    private final static Logger firesensorlogger = LoggerFactory.getLogger(MqttSmartFireDetectorModel.class);
    private static final String SENSOR_ONE_ID = "sensor_1";
    private static final String SENSOR_TWO_ID = "sensor_2";

    private static final String SENSOR_TTHREE_ID = "sensor-3";

    public static void main(String[] args){


        try {

            String idFireSensor1 = SENSOR_ONE_ID;
            MqttClientPersistence persistence1 = new MemoryPersistence();
            IMqttClient mqttFireSensorClientOne = generateMqttClient(idFireSensor1, persistence1);
            MqttSmartFireDetectorModel sensor1 = new MqttSmartFireDetectorModel(mqttFireSensorClientOne, idFireSensor1,
                    45.393847, 10.796394, false);
            sensor1.connectToBroker();
            String idFireSensor2 = SENSOR_TWO_ID;
            MqttClientPersistence persistence2 = new MemoryPersistence();
            IMqttClient mqttFireSensorClientTwo = generateMqttClient(idFireSensor2, persistence2);
            MqttSmartFireDetectorModel sensor2 = new MqttSmartFireDetectorModel(mqttFireSensorClientTwo, idFireSensor2,
                    45.463847,
                    10.852564, false);
            sensor2.connectToBroker();

            String idFireSensor3 = SENSOR_TTHREE_ID;
            MqttClientPersistence persistence3 = new MemoryPersistence();
            IMqttClient mqttFireSensorClientThree = generateMqttClient(idFireSensor3, persistence3);
            MqttSmartFireDetectorModel sensor3 = new MqttSmartFireDetectorModel(mqttFireSensorClientThree, idFireSensor3,
                    45.323543,
                    10.684572, false);
            sensor3.connectToBroker();

            Thread.sleep(10000);
            sensor1.turnAlarmOn();
            Thread.sleep(5000);
            sensor2.turnAlarmOn();
            Thread.sleep(90000);
            sensor3.turnAlarmOn();

            Thread.sleep(30000);
            sensor1.turnAlarmOff();
            Thread.sleep(15000);
            sensor2.turnAlarmOff();
            Thread.sleep(40000);
            sensor3.turnAlarmOff();


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static IMqttClient generateMqttClient(String mqttId,MqttClientPersistence persistence){

        try {

            IMqttClient mqttFireSensorClient = new MqttClient(String.format("tcp://%s:%d",
                    MqttConfParameters.BROKER_ADDRESS, MqttConfParameters.BROKER_PORT),
                    mqttId, persistence);
            return mqttFireSensorClient;

        } catch (Exception e){
            e.printStackTrace();
            return  null;
        }
    }
}
