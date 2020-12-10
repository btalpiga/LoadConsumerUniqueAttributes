package com.nyble.api.messages;

public class GetConsumerInfoRequest {
    private String consumer_id;
    private String key;
    private String external_system_id;

    public GetConsumerInfoRequest(String consumer_id, String key, String external_system_id) {
        this.consumer_id = consumer_id;
        this.key = key;
        this.external_system_id = external_system_id;
    }
}
