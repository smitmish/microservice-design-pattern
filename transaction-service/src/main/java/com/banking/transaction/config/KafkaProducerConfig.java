    //package com.banking.transaction.config;
    //
    //import com.fasterxml.jackson.databind.JsonSerializer;
    //import org.apache.kafka.clients.producer.ProducerConfig;
    //import org.apache.kafka.common.serialization.StringSerializer;
    //import org.springframework.context.annotation.Bean;
    //import org.springframework.context.annotation.Configuration;
    //import org.springframework.kafka.core.DefaultKafkaProducerFactory;
    //import org.springframework.kafka.core.KafkaTemplate;
    //import org.springframework.kafka.core.ProducerFactory;
    ////import org.springframework.kafka.support.serializer.JsonSerializer;
    //
    //import java.util.HashMap;
    //import java.util.Map;
    //
    //@Configuration
    //public class KafkaProducerConfig {
    //
    //    @Bean
    //    public ProducerFactory<String, Object> producerFactory() {
    //        Map<String, Object> configProps = new HashMap<>();
    //
    //        // 1. Connection Endpoint
    //        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
    //
    //        // 2. Serialization Contract (Keys as Strings, Values as JSON bytes)
    //        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    //        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
    //
    //        // 3. Durability & Reliability (The Banking Standard)
    //        configProps.put(ProducerConfig.ACKS_CONFIG, "all"); // Wait for all replicas
    //        configProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true); // Prevent duplicate network sends
    //        configProps.put(ProducerConfig.RETRIES_CONFIG, Integer.MAX_VALUE); // Infinite retries for transient faults
    //        configProps.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 5); // Safe with idempotence enabled
    //
    //        // 4. Performance & Batching Tuning
    //        configProps.put(ProducerConfig.LINGER_MS_CONFIG, 20); // Batch messages up to 20ms for throughput
    //        configProps.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384); // 16KB batch size
    //
    //        return new DefaultKafkaProducerFactory<>(configProps);
    //    }
    //
    //    @Bean
    //    public KafkaTemplate<String, Object> kafkaTemplate() {
    //        return new KafkaTemplate<>(producerFactory());
    //    }
    //}