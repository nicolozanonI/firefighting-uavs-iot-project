package it.iot.project.drone.services;

import io.dropwizard.Application;
import io.dropwizard.setup.Environment;
import it.iot.project.drone.resources.MessageResource;

public class AppService extends Application<AppConfig>{

    public static AppConfig myac = new AppConfig();
    public MessageResource messageResource = new MessageResource(myac);

    public AppService() {
    }

    public void run(AppConfig appConfig, Environment environment) throws Exception {
        appConfig = myac;
        environment.jersey().register(new MessageResource(appConfig));
    }
}
