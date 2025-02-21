package com.haprer.blogger;


import com.haprer.blogger.data.BlogPost;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

/**
 * for storing blog entries
 * written with the help of chatGPT
 */
public interface BlogPostRepository extends MongoRepository<BlogPost, String> {

    Optional<BlogPost> findByTitleAndAuthor(String title, String author);

    void deleteByTitleAndAuthor(String title, String author);


    //TODO this needs tests
    @Aggregation(pipeline = {
            "{ $unwind: '$tags' }",
            "{ $group: { _id: '$tags', count: { $sum: 1 } } }",
            "{ $sort: { count: -1 } }"
    })
    List<TagCount> findMostPopularTags();


}
