package com.haprer.blogger.controllers;

import com.haprer.blogger.BlogService;
import com.haprer.blogger.controllers.data.BlogPost;
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
     * @param blogPost
     * @return
     */
    @PostMapping("/create")
    public ResponseEntity<BlogPost> createPost (@RequestBody BlogPost blogPost) {
        BlogPost createdPost = blogService.save(blogPost);
        return new ResponseEntity<>(createdPost, HttpStatus.CREATED);
    }

    /**
     * Get a blog post by title and author
     * TODO - currently not implemented.
     * @param title
     * @param author
     * @return ResponseEntity containing the blog if it exists
     *          ResponseEntity HttpStatus NOT FOUND if it does not exist. 
     */
    @GetMapping("/find")
    public ResponseEntity<Object> getBlog(@RequestParam String title, @RequestParam String author) {
        Optional<BlogPost> blog = blogService.findByTitleAndAuthor(title, author);
        if (blog.isPresent()) {
            return new ResponseEntity<>(blog.get(), HttpStatus.FOUND);
        } else {
            return new ResponseEntity<>("Blog Not Found", HttpStatus.NOT_FOUND);
        }
    }

}
