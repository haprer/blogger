package com.haprer.blogger.controllers.data;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document()
public class BlogPost {

    @Id
    private String id;

    private String title;
    private String author;
    private List<String> tags;
    private String content;


    public BlogPost(String title, String author, String content, List<String> tags) {
        this.title = title;
        this.author = author;
        this.content = content;
        this.tags = tags; 
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
