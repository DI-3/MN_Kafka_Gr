package com.event.jobevents;

import java.time.Duration;
import java.util.function.Function;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.avro.AvroFactory;
import com.fasterxml.jackson.dataformat.avro.AvroSchema;
import com.fasterxml.jackson.dataformat.avro.schema.AvroSchemaGenerator;
import com.github.javafaker.Faker;

import io.confluent.developer.avro.jobitem;
import lombok.NoArgsConstructor;
import net.dean.jraw.RedditClient;
import net.dean.jraw.Version;
import net.dean.jraw.http.NetworkAdapter;
import net.dean.jraw.http.OkHttpNetworkAdapter;
import net.dean.jraw.http.UserAgent;
import net.dean.jraw.models.Listing;
import net.dean.jraw.models.Submission;
import net.dean.jraw.models.SubredditSort;
import net.dean.jraw.models.TimePeriod;
import net.dean.jraw.oauth.Credentials;
import net.dean.jraw.oauth.OAuthHelper;
import net.dean.jraw.pagination.DefaultPaginator;
import net.dean.jraw.pagination.Paginator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.util.function.Tuple2;

@SpringBootApplication
public class JobeventsApplication {

	public static void main(String[] args) {
		SpringApplication.run(JobeventsApplication.class, args);
	}
}

//The Course Kafka with twitter
//
//https://learning.oreilly.com/videos/apache-kafka-series/9781789342604/9781789342604-video7_4/

// Reddit Usage Guidelines --> redditdev
//https://www.reddit.com/r/<<some_subreddit>>/hot.json  --> way to get reddits

// dev -->2k7kqUILdizXxSzuG5j25w
//	secret --> 0eGf6rVs526uRJKZVxBaF2aDEJZYEA


// prod
// reddit --> o2f3Cg4tHj5g19gEQpm0iQ
// secret --> kQqNcEGAGSU6Dp1eAOZHspkwL13jgw

@RequiredArgsConstructor
@Component
class  Producer {

	private final KafkaTemplate<Integer, jobitem>  template;
	Faker faker;

	@EventListener(ApplicationStartedEvent.class)
	public void generate(){
		faker = Faker.instance();

		final Flux<Long> interval = Flux.interval(Duration.ofMillis(1_0000));
		final Flux<String> quotes = Flux.fromStream(Stream.generate(() -> faker.hobbit().quote()));

		Flux.zip(interval, quotes).map(new Function<Tuple2<Long, String>,Object>(){
			@Override
			public Object apply(final Tuple2<Long, String> it){
				var jobItem = new jobitem(faker.random().nextInt(8), it.getT2());
//				return template.send("job-listing", faker.random().nextInt(8),it.getT2());
				return template.send("job-items", jobItem);

			}
		}).blockLast();
	}
}

//@NoArgsConstructor
//@Component
//class RedditProducer {
//
//
//	@EventListener(ApplicationStartedEvent.class)
//	public void run () throws JsonMappingException {
//
//		// You'll want to change this for your specific OAuth2 app
//		Credentials credentials = Credentials.script("mitun311", "311Reddit#", "2k7kqUILdizXxSzuG5j25w", "0eGf6rVs526uRJKZVxBaF2aDEJZYEA");
//
//		// Construct our NetworkAdapter
//		UserAgent userAgent = new UserAgent("bot", "net.dean.jraw.example.script", Version.get(), "mitun311");
//		NetworkAdapter http = new OkHttpNetworkAdapter(userAgent);
//
//		// Authenticate our client
//		RedditClient reddit = OAuthHelper.automatic(http, credentials);
//
//		// Browse through the top posts of the last month, requesting as much data as possible per request
//		DefaultPaginator<Submission> paginator = reddit.frontPage()
//				.limit(Paginator.RECOMMENDED_MAX_LIMIT)
//				.sorting(SubredditSort.TOP)
//				.timePeriod(TimePeriod.MONTH)
//				.build();
//
//		// Request the first page
//		Listing<Submission> firstPage = paginator.next();
//
//		for (Submission post : firstPage) {
//			if (post.getDomain().contains("imgur.com")) {
//				System.out.println(String.format("%s (/r/%s, %s points) - %s",
//						post.getTitle(), post.getSubreddit(), post.getScore(), post.getUrl()));
//			}
//		}
//
//		ObjectMapper mapper = new ObjectMapper(new AvroFactory());
//		AvroSchemaGenerator gen = new AvroSchemaGenerator();
//		mapper.acceptJsonFormatVisitor(Submission.class, gen);
//		AvroSchema schemaWrapper = gen.getGeneratedSchema();
//		org.apache.avro.Schema avroSchema = schemaWrapper.getAvroSchema();
//		String asJson = avroSchema.toString(true);
//	}
//}


