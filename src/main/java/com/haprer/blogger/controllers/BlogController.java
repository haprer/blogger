package com.haprer.blogger.controllers;

import com.haprer.blogger.BlogService;
import com.haprer.blogger.data.BlogPost;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
public class BlogController {


    @Autowired
    private BlogService blogService;


    /**
     * create a new blog request
     * @param blogPost blog post
     * @return the saved blog post
     */
    @PostMapping("/save")
    public ResponseEntity<BlogPost> save (@RequestBody BlogPost blogPost) {
        BlogPost createdPost = blogService.save(blogPost);
        return new ResponseEntity<>(createdPost, HttpStatus.CREATED);
    }

    /**
     * Get a blog post by title and author
     * @param title
     * @param author
     * @return ResponseEntity containing the blog if it exists
     *          ResponseEntity HttpStatus NOT FOUND if it does not exist. 
     */
    @GetMapping("/find")
    public ResponseEntity<Object> findByTitleAndAuthor(@RequestParam String title, @RequestParam String author) {
        Optional<BlogPost> blog = blogService.findByTitleAndAuthor(title, author);
        return blog.<ResponseEntity<Object>>map(blogPost -> new ResponseEntity<>(blogPost, HttpStatus.FOUND))
                .orElseGet(() -> new ResponseEntity<>("Blog Not Found", HttpStatus.NOT_FOUND));
    }

}
