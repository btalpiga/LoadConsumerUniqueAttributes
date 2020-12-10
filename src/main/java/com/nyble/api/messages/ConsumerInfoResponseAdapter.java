package com.nyble.api.messages;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Field;

public class ConsumerInfoResponseAdapter extends TypeAdapter<ConsumerInfoResponse> {
    final static Logger logger = LoggerFactory.getLogger(ConsumerInfoResponse.class);
    final static Gson localGson = new Gson();

    @Override
    public void write(JsonWriter jsonWriter, ConsumerInfoResponse consumerInfoResponse) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public ConsumerInfoResponse read(JsonReader jsonReader) throws IOException {
        ConsumerInfoResponse rsp;
        JsonToken token = jsonReader.peek();
        String json = readerToString(jsonReader);
        try{
            if(token.equals(JsonToken.BEGIN_ARRAY)){
                json = String.format("{\"consumers\": %s}", json);
                logger.debug("{}", json);
                rsp = localGson.fromJson(json, ConsumerInfoResponse.class);
            }else{
                logger.error("Error: {}", json);
                rsp = new ConsumerInfoResponse();
                rsp.consumers = new ConsumerInfoResponse.ConsumerInfo[0];
                rsp.hasError = true;
            }
            rsp.raw = json;
            return rsp;
        }catch(Exception e){
            throw new RuntimeException(json, e);
        }
    }

    private String readerToString(JsonReader jsonReader) throws IOException {
        StringBuilder json = new StringBuilder();
        while(jsonReader.peek() != JsonToken.END_DOCUMENT){
            JsonToken next = jsonReader.peek();
            if(next.equals(JsonToken.BEGIN_ARRAY)){
                jsonReader.beginArray();
                if(json.length()>0 && (json.charAt(json.length()-1)==']'||json.charAt(json.length()-1)=='}')){
                    json.append(",");
                }
                json.append("[");
            }else if(next.equals(JsonToken.BEGIN_OBJECT)){
                jsonReader.beginObject();
                if(json.length()>0 && (json.charAt(json.length()-1)==']'||json.charAt(json.length()-1)=='}')){
                    json.append(",");
                }
                json.append("{");
            }else if(next.equals(JsonToken.END_ARRAY)){
                jsonReader.endArray();
                json.append("]");
            }else if(next.equals(JsonToken.END_OBJECT)){
                jsonReader.endObject();
                json.append("}");
            }else if(next.equals(JsonToken.NAME)){
                if(json.length()>0 && !(json.charAt(json.length()-1)=='['||json.charAt(json.length()-1)=='{')){
                    json.append(",");
                }
                String nextTag = jsonReader.nextName();
                json.append("\"").append(nextTag).append("\":");
            }else if(next.equals(JsonToken.STRING)){
                String nextString = jsonReader.nextString().replace("\"", "\\\"");
                json.append("\"").append(nextString).append("\"");
            }else if(next.equals(JsonToken.NUMBER)){
                json.append(jsonReader.nextLong());
            }else if(next.equals(JsonToken.BOOLEAN)){
                json.append(jsonReader.nextBoolean());
            }else if(next.equals(JsonToken.NULL)){
                jsonReader.nextNull();
                json.append("null");
            }else{
                throw new RuntimeException("Unknown token type");
            }
        }
        return json.toString();
    }
}
