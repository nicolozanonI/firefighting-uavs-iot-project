package it.iot.project.drone.services;

import io.dropwizard.Configuration;
import it.iot.project.drone.message.MessageManager;

public class AppConfig extends Configuration{

    private MessageManager messageManager = null;

    public MessageManager getMessageManager() {

        if (messageManager == null)
            this.messageManager = new MessageManager();

        return this.messageManager;
    }
}

