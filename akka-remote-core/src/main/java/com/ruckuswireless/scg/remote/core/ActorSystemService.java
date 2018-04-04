package com.ruckuswireless.scg.remote.core;

import akka.actor.ActorRef;
import akka.actor.Address;
import akka.actor.Props;

/**
 * ActorSystemService is an interface used to operate ActorSystem with AKKA APIs.
 *
 * @author Frankie Chao on 2018/3/20.
 */
public interface ActorSystemService {

    void init(String systemName);

    ActorRef newLocalActor(Props actorProps, String actorName);

    ActorRef newRemoteActor(Address address, Props actorProps, String actorName);
}
