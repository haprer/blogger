package com.haprer.blogger;

import com.haprer.blogger.controllers.data.BlogPost;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import com.fasterxml.jackson.databind.ObjectMapper;


import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")    //set spring to use test database - in test-application.properties
class BloggerApplicationTests {

	//this is the docker container that will run the test
	@Container
	private static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:latest");

	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private BlogService blogService;
	private final ObjectMapper objectMapper = new ObjectMapper();


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
		blogService.save(post);


		mockMvc.perform(get("/find")
						.param("title", title)
						.param("author", author)
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isFound())  // Expect HTTP 302 FOUND status
				.andExpect(content().json(objectMapper.writeValueAsString(post)));

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
	void findRangeByTime() {

	}

}
