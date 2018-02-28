package com.lightbend.akka.sample.iot;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Created by Frankie on 2018/2/28.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({DeviceActorTest.class,
        DeviceGroupActorTest.class,
        DeviceGroupQueryActorTest.class,
        DeviceManagerActorTest.class})
public class IotTestSuite {

}
