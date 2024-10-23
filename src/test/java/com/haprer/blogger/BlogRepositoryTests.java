package com.haprer.blogger;

import com.haprer.blogger.controllers.data.BlogPost;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 *
 * These tests are for the MongoDB Blog database
 *
 * written with the help of chatGPT
 *
 */
@SpringBootTest
@ActiveProfiles("test") //configure spring to use database defined in test-application.properties
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)  // Reset the context after each test
public class BlogRepositoryTests {

    @Autowired
    BlogPostRepository blogPostRepository;

    @Autowired
    BlogService blogService;

    // Clear the database before each test
    @BeforeEach
    public void setup() {
        blogPostRepository.deleteAll();  // Remove all blog posts to ensure clean state
    }

    // (Optionally) clear the database after each test, if needed
    @AfterEach
    public void tearDown() {
        blogPostRepository.deleteAll();  // Clean up after each test to ensure isolation
    }


    //----------------------tests--------------------------------------------

    @Test
    void autoConfigLoads(@Autowired final MongoTemplate mongoTemplate) {
        Assertions.assertThat(mongoTemplate.getDb()).isNotNull();
    }

    @Test
    public void createBlogPost() {
        Assertions.assertThat(blogPostRepository.count()).isEqualTo(0);

        //create and save a new post
        BlogPost post = new BlogPost("Test Title", "Test Content", "Test Author", List.of("test", "spring"));
        blogPostRepository.save(post);

        Assertions.assertThat(blogPostRepository.count()).isEqualTo(1);
    }

    @Test
    public void createReadAndDestroyBlogPost() {

        //test data
        String author = "Test Author";
        String title = "Test Title";
        String content = "Test Content";
        List<String> tags = List.of("test","spring");

        //save post
        BlogPost post = new BlogPost(title, author, content, tags);
        blogPostRepository.save(post);
        Assertions.assertThat(blogPostRepository.count()).isEqualTo(1);

        //check that can find post and all post data is the same
        Optional<BlogPost> searchPost = blogPostRepository.findByTitleAndAuthor(title, author);
        assertTrue(searchPost.isPresent());
        BlogPost foundPost = searchPost.get();
        Assertions.assertThat(foundPost.getAuthor()).isEqualTo(author);
        Assertions.assertThat(foundPost.getTitle()).isEqualTo(title);
        Assertions.assertThat(foundPost.getContent()).isEqualTo(content);
        Assertions.assertThat(foundPost.getTags()).isEqualTo(tags);

        //remove the blog post
        blogPostRepository.deleteByTitleAndAuthor(title, author);
        Assertions.assertThat(blogPostRepository.count()).isEqualTo(0);
        Optional<BlogPost> searchTwo = blogPostRepository.findByTitleAndAuthor(title, author);
        Assertions.assertThat(searchTwo).isEmpty();

    }

    @Test
    public void cannotFindNonexistentBlog(){
        BlogPost post = new BlogPost("A", "B", "Test Content", List.of("test", "spring"));
        blogPostRepository.save(post);

        Assertions.assertThat(blogPostRepository.count()).isEqualTo(1);

        Optional<BlogPost> foundPost = blogPostRepository.findByTitleAndAuthor("DNE", "DNE");
        assertTrue(foundPost.isEmpty());
    }

    @Test
    public void cannotUpdateNonexistentPost() {
        Assertions.assertThat(blogPostRepository.count()).isEqualTo(0);
        Assertions.assertThat(blogService.updateTitleByTitleAndAuthor("Old Title", "Author", "New Title")).isFalse();
    }

}
