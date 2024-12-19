package com.haprer.blogger;

import com.haprer.blogger.data.BlogPost;
import com.haprer.blogger.services.BlogService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test") //configure spring to use database defined in test-application.properties
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)  // Reset the context after each test
public class BlogServiceTests {


    //this is the docker container that will run the test
    @Container
    private static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:latest");

    /**
     * Dynamically provide MongoDB connection properties from the running container
     */
    @DynamicPropertySource
    static void setMongoDbProperties(DynamicPropertyRegistry registry) {
        // Dynamically set MongoDB connection string provided by Testcontainers
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @Autowired
    BlogService blogService;

    @BeforeEach
    public void beforeEach() {
        blogService.deleteAll();
    }
    @AfterEach
    public void afterEach() {
        blogService.deleteAll();
    }






    //-------------------------------------tests----------------------------------------

    @Test
    public void createReadUpdateDestroy() {

        //test data
        String author = "Test Author";
        String title = "Test Title";
        String content = "Test Content";
        List<String> tags = List.of("test","spring");

        //save post
        BlogPost post = new BlogPost(title, author, content, tags);
        blogService.save(post);
        Assertions.assertThat(blogService.count()).isEqualTo(1);

        //check that can find post and all post data is correct
        Optional<BlogPost> searchPost = blogService.findByTitleAndAuthor(title, author);
        assertTrue(searchPost.isPresent());
        BlogPost foundPost = searchPost.get();
        Assertions.assertThat(foundPost.getAuthor()).isEqualTo(author);
        Assertions.assertThat(foundPost.getTitle()).isEqualTo(title);
        Assertions.assertThat(foundPost.getContent()).isEqualTo(content);
        Assertions.assertThat(foundPost.getTags()).isEqualTo(tags);

        //Update the title
        String newTitle = "New Title";
        blogService.updateTitleByTitleAndAuthor(title, author, newTitle);

        //check that the new post is correct
        Optional<BlogPost> newPost = blogService.findByTitleAndAuthor(newTitle, author);
        assertTrue(newPost.isPresent());
        BlogPost nPost = newPost.get();
        Assertions.assertThat(nPost.getAuthor()).isEqualTo(author);
        Assertions.assertThat(nPost.getTitle()).isEqualTo(newTitle);
        Assertions.assertThat(nPost.getContent()).isEqualTo(content);
        Assertions.assertThat(nPost.getTags()).isEqualTo(tags);

        //check that the old post is gone
        Optional<BlogPost> oldPost = blogService.findByTitleAndAuthor(title, author);
        Assertions.assertThat(oldPost).isEmpty();


        //remove the blog post
        blogService.deleteByTitleAndAuthor(newTitle, author);
        Assertions.assertThat(blogService.count()).isEqualTo(0);
        Optional<BlogPost> deletedPost = blogService.findByTitleAndAuthor(title, author);
        Assertions.assertThat(deletedPost).isEmpty();
    }

    @Test
    void getPopularTags() {
        List<BlogPost> posts = new ArrayList<>();

        List<String> tags = new ArrayList<>();
        // Each tag is going to appear on the posts less than or equal to it.
        for (int i = 0; i < 10; i++) {
            tags.add("" + i);  // Adding tag i to the list
            posts.add(new BlogPost("title" + i, "author" + i, "content" + i, new ArrayList<>(tags)));
        }

        // Save all the blog posts
        blogService.saveAll(posts);

        // Verify that posts are saved by querying the repository
        List<BlogPost> savedPosts = blogService.findAll();
        System.out.println("Saved Posts: " + savedPosts.size());  // Debugging statement to verify data is saved
        Assertions.assertThat(savedPosts.size()).isEqualTo(10);


        // Now call the method to get popular tags
        List<TagCount> tagCounts = blogService.findMostPopularTags();

        // Add some debugging to verify the result
        System.out.println("Popular Tags: " + tagCounts);  // Debugging statement to check the popular tags

        // Assertions
        Assertions.assertThat(tagCounts.size()).isEqualTo(10);  // There should be 10 popular tags
        Assertions.assertThat(tagCounts.getFirst().getTag()).isEqualTo("0");  // The most popular tag should be "0"
    }


}
