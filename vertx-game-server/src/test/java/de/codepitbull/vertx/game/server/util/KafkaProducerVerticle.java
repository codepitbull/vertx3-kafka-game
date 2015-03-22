package de.codepitbull.vertx.game.server.util;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.impl.LoggerFactory;
import io.vertx.rxjava.core.AbstractVerticle;
import io.vertx.rxjava.core.eventbus.MessageConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Properties;

import static org.apache.commons.lang3.Validate.notEmpty;

/**
 * A simple verticle to
 */
public class KafkaProducerVerticle extends AbstractVerticle {
    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaProducerVerticle.class);

    private KafkaProducer<String, String> producer;

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        notEmpty("bootstrap.server not set", config().getString("bootstrap.server"));
        Properties props = new Properties();
        props.put("bootstrap.servers", config().getString("bootstrap.server"));
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("producer.type", "async");
        props.put("request.required.acks", "1");
        props.put("batch.size", "0");

        producer = new KafkaProducer<>(props);

        MessageConsumer<JsonObject> outgoing = vertx.eventBus().<JsonObject>localConsumer("outgoing");
        outgoing.bodyStream().handler(jsonObject ->
                        producer.send
                                (new ProducerRecord<>(
                                                jsonObject.getString("topic"), jsonObject.getString("msg")),
                                        (result, exception) -> {
                                            LOGGER.info("Wrote to "+result.topic()+" at "+result.partition()+" with offset "+result.offset()+" and exception "+exception);
                                        })
        );

        outgoing.completionHandler(complete -> startFuture.complete());

    }

    @Override
    public void stop() throws Exception {
        producer.close();
    }
}
