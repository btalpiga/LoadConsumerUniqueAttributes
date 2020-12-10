package com.nyble.api;

import com.nyble.api.messages.ConsumerInfoResponse;
import com.nyble.api.messages.GetConsumerInfoRequest;
import feign.Headers;
import feign.RequestLine;

import java.net.URI;

public interface ConsumersAPI {

    @RequestLine("POST /consumer/get-consumers")
    @Headers("Content-Type: application/json")
    ConsumerInfoResponse getConsumers(URI base, GetConsumerInfoRequest request);
}
