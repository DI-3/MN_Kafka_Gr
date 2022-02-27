package com.event.jobevents.twitter;

import com.google.common.collect.Lists;
import com.twitter.hbc.ClientBuilder;
import com.twitter.hbc.core.Client;
import com.twitter.hbc.core.Constants;
import com.twitter.hbc.core.Hosts;
import com.twitter.hbc.core.HttpHosts;
import com.twitter.hbc.core.endpoint.StatusesFilterEndpoint;
import com.twitter.hbc.core.processor.StringDelimitedProcessor;
import com.twitter.hbc.httpclient.auth.Authentication;
import com.twitter.hbc.httpclient.auth.OAuth1;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Component
public class TwitterProducer {

    String consumerKey = "LVFd11WxJSylGuQxVmvFLtwuE";
    String consumerSecret = "DhVbjy0sXn6m6EAZy2rKPepNJ1Dp96dIvmvnbtGStrYcMCwPNG";
    String token = "78708142-Inrq58gGufmAadwVEQml4kPjCDCAeU8jwK6qxu0s9";
    String tokenSecret = "bZTAodNsNwuHsigI6wWIPE6DFPG9L8kxQPPw9PuBlDcQS";
    // Message Queue
    /**
     * Set up your blocking queues: Be sure to size these properly based on expected TPS of your stream
     */
    BlockingQueue<String> msgQueue = new LinkedBlockingQueue<String>(10000);

    //@EventListener(ApplicationStartedEvent.class)
    public void run() {

        Client hosebirdClient = null;
        try {
            hosebirdClient = createTwitterClient();
            hosebirdClient.connect();
            int i = 0;
            while (!hosebirdClient.isDone() && i < 10) {
                String msg = msgQueue.take();
                System.out.println(msg);
                i++;
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public Client createTwitterClient() throws InterruptedException {

        /** Declare the host you want to connect to, the endpoint, and authentication (basic auth or oauth) */
        Hosts hosebirdHosts = new HttpHosts(Constants.STREAM_HOST);
        StatusesFilterEndpoint hosebirdEndpoint = new StatusesFilterEndpoint();

        // Optional: set up some followings and track terms
        List<String> terms = Lists.newArrayList("kafka");
        hosebirdEndpoint.trackTerms(terms);

        // These secrets should be read from a config file
        Authentication hosebirdAuth = new OAuth1(consumerKey, consumerSecret, token, tokenSecret);

        ClientBuilder builder = new ClientBuilder()
                .name("Hosebird-Client-01")                              // optional: mainly for the logs
                .hosts(hosebirdHosts)
                .authentication(hosebirdAuth)
                .endpoint(hosebirdEndpoint)
                .processor(new StringDelimitedProcessor(msgQueue));

        return  builder.build();
    }
}
