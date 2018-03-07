package com.ruckuswireless.scg.remote.server;

import akka.actor.ActorSystem;

/**
 * Created by Frankie on 2018/3/6.
 */
public class ScgClusterServerMain {

    public static void main(String[] args) {
        final ActorSystem system = ActorSystem.create("scg-cluster-server");

        try {
            System.out.println(">>> Press ENTER to exit <<<");
            System.in.read();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            system.terminate();
        }
    }
}
