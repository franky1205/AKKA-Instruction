package com.ruckuswireless.scg.remote.client.actor;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.ruckuswireless.scg.remote.core.ActorRefRequest;
import com.ruckuswireless.scg.remote.core.ActorRefResponse;
import com.ruckuswireless.scg.remote.core.EchoMessageRequest;
import com.ruckuswireless.scg.remote.core.EchoMessageResponse;

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
        remoteActorSelection.tell(new ActorRefRequest(), this.getSelf());
    }

    @Override
    public void postStop() throws Exception {
        logger.info("EchoClientActor is stopped.");
    }

    @Override
    public Receive createReceive() {
        return this.receiveBuilder()
                .match(ActorRefResponse.class, this::onActorRefResponse)
                .match(EchoMessageRequest.class, this::onEchoMessageRequest)
                .match(EchoMessageResponse.class, this::onEchoMessageResponse)
                .build();
    }

    private void onActorRefResponse(ActorRefResponse actorRefResponse) {
        logger.info("Receive ActorRefResponse from sender: [{}]", this.getSender());
        this.remoteActorRef = this.getSender();
    }

    private void onEchoMessageRequest(EchoMessageRequest echoMessageRequest) {
        this.remoteActorRef.tell(echoMessageRequest, this.getSelf());
    }

    private void onEchoMessageResponse(EchoMessageResponse echoMessageResponse) {
        logger.info("Receive Remote Echo Actor response: [{}]-[{}]",
                echoMessageResponse.getRequestId(), echoMessageResponse.getMessage());
    }
}
