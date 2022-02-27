package com.event.jobevents;

import io.confluent.developer.avro.jobitem;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Component
public class JobListingListener {

    @KafkaListener(topics="job-listing", groupId = "mn-consumer-app")
    public void listen(String message) {
        //System.out.println(message.getJobDesc());
        System.out.println("MN Consumer" + " : " + message);
    }


    @KafkaListener(topics="job-items", groupId = "mn-eater-app")
    public void listen1(ConsumerRecord<String, jobitem> message) {
        //System.out.println(message.getJobDesc());
        System.out.println("Eater" + " : " + message.toString());
    }
}
