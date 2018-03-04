package com.ruckuswireless.scg.remote.core;

import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;

/**
 * Created by Frankie on 2018/3/4.
 */
@Builder
public class EchoMessageRequest implements Serializable {

    @Getter
    private long requestId;

    @Getter
    private String message;
}
