package com.haprer.blogger;

import com.haprer.blogger.controllers.data.BlogPost;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

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


	//default values for testing a new blog post
	private final String title = "Test Title";
	private final String author = "Test Author";
	private final String content = "Test content";
	private final List<String> tags = List.of("Spring", "Test");

	@BeforeEach
	void beforeEach() {
		blogService.deleteAll();
	}

	@Test
	void contextLoads() {}

	@Test
	void findBlogByTitleAndAuthor() throws Exception {



		BlogPost post = new BlogPost(title, author, content, tags);
		blogService.save(post);


		mockMvc.perform(get("/find")
						.param("title", "Test Title")
						.param("author", "Test Author")
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isFound())  // Expect HTTP 302 FOUND status
				.andExpect(content().json("{'title':'Test Title','author':'Test Author','content':'Test Content'}"));

	}

}
