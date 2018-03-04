package com.ruckuswireless.scg.remote.client.actor;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.ruckuswireless.scg.remote.core.EchoMessageRequest;
import com.ruckuswireless.scg.remote.core.EchoMessageResponse;
import scala.concurrent.duration.FiniteDuration;

import java.util.concurrent.TimeUnit;

/**
 * Created by Frankie on 2018/3/4.
 */
public class EchoClientActor extends AbstractActor {

    public static Props props(String remoteActorPath) {
        return Props.create(EchoClientActor.class, remoteActorPath);
    }

    private final LoggingAdapter logger = Logging.getLogger(getContext().getSystem(), this);

    private final String remoteActorPath;

    private ActorRef remoteActorRef;

    public EchoClientActor(String remoteActorPath) {
        this.remoteActorPath = remoteActorPath;
    }

    @Override
    public void preStart() throws Exception {
        logger.info("EchoClientActor is going to be started with the remote actor path: [{}]", this.remoteActorPath);
        ActorSelection remoteActorSelection = this.getContext().actorSelection(this.remoteActorPath);
        remoteActorSelection.resolveOneCS(new FiniteDuration(5, TimeUnit.SECONDS))
                .thenAccept(remoteActorRef -> this.remoteActorRef = remoteActorRef);
    }

    @Override
    public void postStop() throws Exception {
        logger.info("EchoClientActor is stopped.");
    }

    @Override
    public Receive createReceive() {
        return this.receiveBuilder()
                .match(EchoMessageRequest.class, this::onEchoMessageRequest)
                .match(EchoMessageResponse.class, this::onEchoMessageResponse)
                .build();
    }

    private void onEchoMessageRequest(EchoMessageRequest echoMessageRequest) {
        this.remoteActorRef.tell(echoMessageRequest, this.getSelf());
    }

    private void onEchoMessageResponse(EchoMessageResponse echoMessageResponse) {
        logger.info("Receive Remote Echo Actor response: [{}]-[{}]",
                echoMessageResponse.getRequestId(), echoMessageResponse.getMessage());
    }
}
