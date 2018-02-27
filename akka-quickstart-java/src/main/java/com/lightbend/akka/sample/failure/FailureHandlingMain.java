package com.lightbend.akka.sample.failure;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;

import java.io.IOException;

/**
 * Created by Frankie on 2018/2/19.
 */
public class FailureHandlingMain {

    public static void main(String[] args) throws IOException {
        ActorSystem failureRoot = ActorSystem.create("failureRoot");

        ActorRef supervisor = failureRoot.actorOf(Props.create(Supervisor.class), "Supervisor");

        supervisor.tell("failedChild", ActorRef.noSender());

        System.out.println(Thread.currentThread().getName() + " Supervisor Path: " + supervisor);
        try {
            System.in.read();
        } finally {
            failureRoot.terminate();
        }
    }
}
