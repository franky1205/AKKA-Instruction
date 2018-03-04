package com.ruckuswireless.scg.remote.server.actor;

import akka.actor.AbstractActor;
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
public class EchoRemoteActor extends AbstractActor {

    private final LoggingAdapter logger = Logging.getLogger(getContext().getSystem(), this);

    public static Props props() {
        return Props.create(EchoRemoteActor.class);
    }

    @Override
    public Receive createReceive() {
        return this.receiveBuilder()
                .match(ActorRefRequest.class, this::onActorRefRequest)
                .match(EchoMessageRequest.class, this::onEchoMessageRequest)
                .build();
    }

    private void onActorRefRequest(ActorRefRequest actorRefRequest) {
        logger.info("Receive an ActorRefRequest from ActorRef: [{}]", this.getSender());
        this.getSender().tell(new ActorRefResponse(), this.getSelf());
    }

    private void onEchoMessageRequest(EchoMessageRequest echoMessageRequest) {
        logger.info("Receive EchoMessageRequest by requestId: [{}], message: [{}]",
                echoMessageRequest.getRequestId(), echoMessageRequest.getMessage());
        this.getSender().tell(EchoMessageResponse.builder()
                .requestId(echoMessageRequest.getRequestId())
                .message(this.getClass().getSimpleName() + " : " + echoMessageRequest.getMessage())
                .build(), this.getSelf());
    }
}
