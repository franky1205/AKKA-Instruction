package com.lightbend.akka.sample;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;

/**
 * Created by Frankie on 2018/2/17.
 */
public class ActorHierarchyExperiment {

    private static String messageFormat = "[%3$s]-%1$10s Ref: %2$s";

    public static void main(String[] args) throws Exception {
        ActorSystem rootSystem = ActorSystem.create("RootSystem");

        ActorRef topLevelRef = rootSystem.actorOf(Props.create(ParentLevelActor.class), "TopLevelActor");
        topLevelRef.tell("print", ActorRef.noSender());
        topLevelRef.tell("nextPrint", ActorRef.noSender());
        topLevelRef.tell("nextPrint", ActorRef.noSender());
        topLevelRef.tell("nextPrint", ActorRef.noSender());
        topLevelRef.tell("printChildren", ActorRef.noSender());

        Thread.sleep(2000L);

        try {
            System.in.read();
        } finally {
            rootSystem.terminate();
        }
    }

    private static class ParentLevelActor extends AbstractActor {

        private int childIndex = 0;

        @Override
        public void preStart() throws Exception {
            System.out.println(String.format(messageFormat, "Parent-S", this.getSelf(), Thread.currentThread().getName()));
        }

        @Override
        public void postStop() throws Exception {
            System.out.println(String.format(messageFormat, "Parent-E", this.getSelf(), Thread.currentThread().getName()));
        }

        @Override
        public Receive createReceive() {
            return this.receiveBuilder()
                    .matchEquals("print", message -> {
                        System.out.println(String.format(messageFormat, "Current", this.getSelf(), Thread.currentThread().getName()));
                    })
                    .matchEquals("nextPrint", message -> {
                        int nextChildIndex = childIndex++;
                        ActorRef secondActorRef = this.getContext().actorOf(
                                Props.create(ChildLevelActor.class, nextChildIndex), "ChildLevelActor" + nextChildIndex);
                        System.out.println(String.format(messageFormat, "Next", secondActorRef, Thread.currentThread().getName()));
                    })
                    .matchEquals("printChildren", message -> {
                        for (ActorRef childActorRef : this.getContext().getChildren()) {
                            System.out.println(String.format(messageFormat, "Child", childActorRef, Thread.currentThread().getName()));
                        }
                    })
                    .build();
        }
    }

    private static class ChildLevelActor extends AbstractActor {

        private Integer index;

        private ChildLevelActor(Integer index) {
            this.index = index;
        }

        @Override
        public void preStart() throws Exception {
            System.out.println(String.format(messageFormat, "Child-S", this.getSelf(), Thread.currentThread().getName()));
        }

        @Override
        public void postStop() throws Exception {
            System.out.println(String.format(messageFormat, "Child-E", this.getSelf(), Thread.currentThread().getName()));
        }

        @Override
        public Receive createReceive() {
            return emptyBehavior();
        }
    }
}
