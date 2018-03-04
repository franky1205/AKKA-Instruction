package com.ruckuswireless.scg.remote.client;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import com.ruckuswireless.scg.remote.client.actor.EchoClientActor;
import com.ruckuswireless.scg.remote.core.EchoMessageRequest;

import java.util.Scanner;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by Frankie on 2018/3/4.
 */
public class AkkaRemoteClientMain {

    public static void main(String[] args) {
        final ActorSystem system = ActorSystem.create("akka-remote-client");

        try {
            ActorRef echoClientRef = system.actorOf(EchoClientActor.props(
                    "akka.tcp://akka-remote-server@127.0.0.1:5250/user/echo-remote"), "echo-client");

            AtomicLong requestId = new AtomicLong();
            Scanner scanner = new Scanner(System.in);
            System.out.println(">>> ENTER echo message or 'exit' to quit <<<");
            while (true) {
                String inputMessage = scanner.nextLine();
                if ("exit".equalsIgnoreCase(inputMessage)) {
                    break;
                }
                long currentRequestId = requestId.getAndIncrement();
                echoClientRef.tell(EchoMessageRequest.builder()
                        .requestId(currentRequestId)
                        .message(inputMessage)
                        .build(), ActorRef.noSender());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            system.terminate();
        }
    }
}
