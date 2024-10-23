package com.haprer.blogger;


import com.haprer.blogger.controllers.data.BlogPost;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

/**
 * for storing blog entries
 * written with the help of chatGPT
 */
public interface BlogPostRepository extends MongoRepository<BlogPost, String> {

    Optional<BlogPost> findByTitleAndAuthor(String title, String author);

    void deleteByTitleAndAuthor(String title, String author);

}
