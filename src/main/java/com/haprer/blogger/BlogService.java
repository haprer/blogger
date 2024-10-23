package com.haprer.blogger;


import com.haprer.blogger.controllers.data.BlogPost;
import lombok.experimental.Delegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.util.Optional;

/**
 * Service for blog database access
 * Uses Lombok to wrap all BlogPostRepository methods
 *
 * created with the help of chatGPT
 */
@Service
public class BlogService {

    //WARNING This feature is experimental in lombok and not fully supported
    @Delegate    //this means this class automatically overrides and wraps all methods from blogPostRepository
    private final BlogPostRepository blogPostRepository;

    @Autowired
    public BlogService(BlogPostRepository blogPostRepository) {
        this.blogPostRepository = blogPostRepository;
    }

    /**
     *
     * @param title - the current title
     * @param author - the author
     * @param newTitle - what the title should be updated to
     * @return true if the title was changed, false if the post was not present.
     */
    public boolean updateTitleByTitleAndAuthor(String title, String author, String newTitle) {
        Optional<BlogPost> post = findByTitleAndAuthor(title, author);
        if (post.isEmpty()) {
            return false;
        }
        BlogPost b = post.get();
        b.setTitle(newTitle);
        blogPostRepository.save(b);
        return true;
    }
}
