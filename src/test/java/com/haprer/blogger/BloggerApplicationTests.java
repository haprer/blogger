package com.haprer.blogger;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;


@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")    //set spring to use test database - in test-application.properties
class BloggerApplicationTests {


	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;  // Used for converting objects to JSON

	@Autowired
	private BlogPostRepository blogPostRepository;



	@Test
	void context_loads() {
	}

	@Test
	void get_nonexistent_blog_returns_not_found() {

	}

	@Test
	void create_and_get_blog_post_returns_correct_post() {

	}

}
