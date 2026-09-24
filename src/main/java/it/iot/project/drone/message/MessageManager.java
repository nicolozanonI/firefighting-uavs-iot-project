package it.iot.project.drone.message;


import java.util.HashMap;

import java.util.ArrayList;


public class MessageManager {

    private HashMap<String,ArrayList<Object>> msgMap;

    private ArrayList<String> missionList;

    public MessageManager() {
        this.missionList = new ArrayList<String>();
        this.msgMap = new HashMap<String, ArrayList<Object>>();
    }


    public String addNewMissionId(String missionId){
        this.missionList.add(missionId);
        return missionId;
    }

    public void addNewMission(String missionId, ArrayList<Object> missioneMessageList) {
        msgMap.put(missionId, missioneMessageList);
    }

    public AvailabilityMessage addNewAvailabilityMessage(String missionId, AvailabilityMessage
            availabilityMessage){

        this.msgMap.get(missionId).add(availabilityMessage);
        return availabilityMessage;
    }

    public PositionMessage addNePositionMessage(String missionId,PositionMessage positionMessage){

        this.msgMap.get(missionId).add(positionMessage);
        return positionMessage;
    }

    public AlarmMessage addNeAlarmMessage(String missionId, AlarmMessage alarmMessage){

        this.msgMap.get(missionId).add(alarmMessage);
        return alarmMessage;
    }

    public ArrayList<String> getMissionsList() {
        return this.missionList;
    }

    public Object getMessageList(String msnId){
        return this.msgMap.get(msnId);
    }


}
