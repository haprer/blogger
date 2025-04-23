package com.haprer.blogger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.haprer.blogger.configurations.JacksonConfig;
import com.haprer.blogger.data.BlogPost;
import com.haprer.blogger.services.BlogService;
import com.jayway.jsonpath.JsonPath;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import org.testcontainers.shaded.com.fasterxml.jackson.core.type.TypeReference;


import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.ArrayList;

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

	/**
	 * Helper function to save the post and check that the title/author/content is correct
	 * @param post - a blog post
	 */
	void save(BlogPost post) throws Exception {

		String postJson = objectMapper.writeValueAsString(post);

		// Perform the POST request to /save
		mockMvc.perform(post("/save")
						.contentType(MediaType.APPLICATION_JSON)
						.content(postJson))
				.andExpect(status().isCreated())  // Expect HTTP 201 CREATED status
				.andExpect(jsonPath("$.title", is(post.getTitle())))
				.andExpect(jsonPath("$.author", is(post.getAuthor())))
				.andExpect(jsonPath("$.content", is(post.getContent())))
		;
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
		this.save(post);


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
					String returnedTimeStamp = JsonPath.read(result.getResponse().getContentAsString(), "$.timestamp");
					//Instant returnedTimestamp = Instant.ofEpochMilli(returnedTimestampBigDecimal.longValue());

					// Get the original timestamp from testPost
					Instant originalTimestamp = testPost.getTimestamp();

					// Assert that the timestamps are different
					assertNotEquals(originalTimestamp.toString(), returnedTimeStamp);
				});

	}

	@Test
	void findFirstTwoPages() throws Exception {

		//posts have tags of all previous numbers
		List<String> tags = new ArrayList<String>();
		for (int i = 0; i < 20; i ++) {
			tags.add("" + i);
			BlogPost post = new BlogPost("title " + i, "author " + i, "content " + i, new ArrayList<>(tags));
			this.save(post);
		}

		//perform get for first page ---> this does not specify a page because first page is default
		MvcResult res =  mockMvc.perform(get("/blogposts")
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn();

		//extract the result (BlogPost[]) from the response
		JsonNode jsonNode = objectMapper.readTree(res.getResponse().getContentAsString());
		BlogPost[] posts =  objectMapper.readValue(jsonNode.get("content").toString(), BlogPost[].class);


		//Assertions for page 1

		Assertions.assertThat(posts.length).isEqualTo(10);
		//check blogs
		for (int i = 0; i < 10; i ++) {
			Assertions.assertThat(posts[i].getTitle()).isEqualTo("title " + i);
			//todo check other stats
		}



		//get page two
		MvcResult res2 =  mockMvc.perform(get("/blogposts")                
						.param("page", "1")  // Page index starts from 0
						.param("size", "10") // Optional: Set the number of items per page
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn();  // Expect HTTP NOT FOUND status

		//extract the result (BlogPost[]) from the response
		JsonNode jsonNode2 = objectMapper.readTree(res2.getResponse().getContentAsString());
		BlogPost[] posts2 =  objectMapper.readValue(jsonNode2.get("content").toString(), BlogPost[].class);


		//Assertions for page 2

		Assertions.assertThat(posts2.length).isEqualTo(10);
		//check blogs
		for (int i = 0; i < 10; i ++) {
			Assertions.assertThat(posts2[i].getTitle()).isEqualTo("title " + (i + 10));
			//todo check other stats
		}

	}

}
