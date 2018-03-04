package com.ruckuswireless.scg.remote.server;

import akka.actor.ActorSystem;
import com.ruckuswireless.scg.remote.server.actor.EchoRemoteActor;

/**
 * Created by Frankie on 2018/3/4.
 */
public class AkkaRemoteServerMain {

    public static void main(String[] args) {
        final ActorSystem system = ActorSystem.create("akka-remote-server");

        try {
            system.actorOf(EchoRemoteActor.props(), "echo-remote");

            System.out.println(">>> Press ENTER to exit <<<");
            System.in.read();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            system.terminate();
        }
    }
}
