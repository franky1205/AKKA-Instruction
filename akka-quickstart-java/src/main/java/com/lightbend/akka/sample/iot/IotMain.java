package com.lightbend.akka.sample.iot;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;

import java.io.IOException;

/**
 * Created by Frankie on 2018/2/19.
 */
public class IotMain {

    public static void main(String[] args) throws IOException {
        ActorSystem iotSystem = ActorSystem.create("iot-system");

        try {
            ActorRef supervisor = iotSystem.actorOf(IotSupervisor.props(), "iot-supervisor");

            System.out.println("Press ENTER to exit the system");
            System.in.read();
        } finally {
            iotSystem.terminate();
        }
    }
}
