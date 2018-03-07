package com.ruckuswireless.scg.remote.client;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Address;
import akka.actor.Deploy;
import akka.remote.RemoteScope;
import com.ruckuswireless.scg.remote.client.actor.MessageLoggingActor;
import com.ruckuswireless.scg.remote.core.actor.ClusterServiceActor;

import java.util.Scanner;

/**
 * Created by Frankie on 2018/3/6.
 */
public class ScgClusterClientMain {

    private static final String CLUSTER_NAME = "Frankie-Akka-Cluster";

    public static void main(String[] args) {
        final ActorSystem system = ActorSystem.create("scg-cluster-client");

        try {
            final ActorRef clusterServerActorRef = createClusterServerActor(system, CLUSTER_NAME);
            final ActorRef messageLoggingActor = system.actorOf(MessageLoggingActor.props(), "message-logger");

            System.out.println(">>> ENTER query command or 'exit' to quit <<<");
            Scanner scanner = new Scanner(System.in);
            while (true) {
                String queryCommand = scanner.nextLine();
                if ("exit".equalsIgnoreCase(queryCommand)) {
                    break;
                }
                clusterServerActorRef.tell(queryCommand, messageLoggingActor);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            system.terminate();
        }
    }

    private static ActorRef createClusterServerActor(ActorSystem system, String clusterName) {
        Address remoteActorRef = new Address("akka.tcp",
                "scg-cluster-server", "127.0.0.1", 5250);
        return system.actorOf(ClusterServiceActor.props(clusterName)
                .withDeploy(new Deploy(new RemoteScope(remoteActorRef))), "cluster-service");
    }
}
