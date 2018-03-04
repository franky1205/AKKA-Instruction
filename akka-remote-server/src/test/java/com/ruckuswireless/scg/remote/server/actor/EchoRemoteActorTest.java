package com.ruckuswireless.scg.remote.server.actor;

import akka.actor.ActorRef;
import com.ruckuswireless.scg.remote.core.EchoMessageRequest;
import com.ruckuswireless.scg.remote.core.EchoMessageResponse;
import com.ruckuswireless.scg.remote.server.AbstractActorTest;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by Frankie on 2018/3/4.
 */
public class EchoRemoteActorTest extends AbstractActorTest {

    @Test
    public void testWithEchoMessageRequest() {
        ActorRef echoRemoteActor = this.system.actorOf(EchoRemoteActor.props(), "echo-remote");
        final String echoMessage = "EchoMessage";
        echoRemoteActor.tell(EchoMessageRequest.builder().requestId(1L).message(echoMessage).build(), this.probe.getRef());
        EchoMessageResponse echoMessageResponse = this.probe.expectMsgClass(EchoMessageResponse.class);
        assertEquals(1L, echoMessageResponse.getRequestId());
        assertEquals("EchoRemoteActor : " + echoMessage, echoMessageResponse.getMessage());
    }
}
