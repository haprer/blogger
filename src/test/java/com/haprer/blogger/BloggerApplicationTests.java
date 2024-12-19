package com.haprer.blogger;

import com.haprer.blogger.configurations.JacksonConfig;
import com.haprer.blogger.data.BlogPost;
import com.haprer.blogger.services.BlogService;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import com.fasterxml.jackson.databind.ObjectMapper;


import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;



/**
 */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@Import(JacksonConfig.class)  // Import your configuration if necessary
@ActiveProfiles("test")    //set spring to use test database - in test-application.properties
class BloggerApplicationTests {

	//this is the docker container that will run the test
	@Container
	private static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:latest");

	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private BlogService blogService;

	@Autowired
	private ObjectMapper objectMapper;


	//default values for testing a new blog post
	private final String title = "Test Title";
	private final String author = "Test Author";
	private final String content = "Test content";
	private final List<String> tags = List.of("Spring", "Test");

	@BeforeEach
	void beforeEach() {
		blogService.deleteAll();
	}

	/**
	 * Dynamically provide MongoDB connection properties from the running container
	 */
	@DynamicPropertySource
	static void setMongoDbProperties(DynamicPropertyRegistry registry) {
		// Dynamically set MongoDB connection string provided by Testcontainers
		registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
	}

	//------------------------------------tests------------------------------------------------

	@Test
	void contextLoads() {}




	@Test
	void findBlogByTitleAndAuthor() throws Exception {

		BlogPost post = new BlogPost(title, author, content, tags);
		String postJson = objectMapper.writeValueAsString(post);
		System.out.println("Starting timestamp: " + post.getTimestamp());

		// Perform the POST request to /save
		mockMvc.perform(post("/save")
				.contentType(MediaType.APPLICATION_JSON)
				.content(postJson))
				.andExpect(status().isCreated())  // Expect HTTP 201 CREATED status
				.andExpect(jsonPath("$.title", is(title)))
				.andExpect(jsonPath("$.author", is(author)))
				.andExpect(jsonPath("$.content", is(content)))
				.andExpect(jsonPath("$.tags[0]", is(tags.get(0))))
				.andExpect(jsonPath("$.tags[1]", is(tags.get(1))))
				;


		mockMvc.perform(get("/find")
				.param("title", title)
				.param("author", author)
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isFound())  // Expect HTTP 302 FOUND status
				.andExpect(jsonPath("$.title", is(title)))
				.andExpect(jsonPath("$.author", is(author)))
				.andExpect(jsonPath("$.content", is(content)))
				.andExpect(jsonPath("$.tags[0]", is(tags.get(0))))
				.andExpect(jsonPath("$.tags[1]", is(tags.get(1))))
				.andExpect(jsonPath("$.timestamp", not(post.getTimestamp()))).andDo((result)-> {
					System.out.println(postJson);
					System.out.println(result.getResponse().getContentAsString());
				})
		;



	}

	@Test
	void cannotFindIncorrectTitleAndAuthor() throws Exception {
		BlogPost post = new BlogPost(title, author, content, tags);
		blogService.save(post);


		mockMvc.perform(get("/find")
						.param("title", "Incorrect Title")
						.param("author", "Incorrect Author")
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isNotFound());  // Expect HTTP NOT FOUND status
	}

	@Test
	void timestampsAreOverwritten() throws Exception {
		BlogPost testPost = new BlogPost(title, author, content, tags, Instant.now());
		Thread.sleep(1000);
		// Perform the POST request to /save
		mockMvc.perform(post("/save")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(testPost)))
				.andExpect(status().isCreated())  // Expect HTTP 201 CREATED status
				.andExpect(jsonPath("$.timestamp").exists())
				.andExpect(result -> {
					BigDecimal returnedTimestampBigDecimal = JsonPath.read(result.getResponse().getContentAsString(), "$.timestamp");
					Instant returnedTimestamp = Instant.ofEpochMilli(returnedTimestampBigDecimal.longValue());

					//TODO This test passes when it should fail I think because of rounding errors

					// Get the original timestamp from testPost
					Instant originalTimestamp = testPost.getTimestamp();

					// Assert that the timestamps are different
					assertNotEquals(originalTimestamp, returnedTimestamp);
				});

	}

	@Test
	void findRangeByTime() {

	}

}
