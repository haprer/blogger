package com.haprer.blogger.data;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@Getter
@Document()
public class BlogPost {

    @Id
    private String id;

    @Setter private String title;
    @Setter private String author;
    @Setter private List<String> tags;
    @Setter private String content;


     private Instant timestamp;


    public BlogPost(String title, String author, String content, List<String> tags, Instant timestamp) {
        this.title = title;
        this.author = author;
        this.content = content;
        this.tags = tags;
        this.timestamp = Instant.now(); // Always set to current time
    }
    private BlogPost() {
        this.timestamp = Instant.now();
    }
    public BlogPost(String title, String author, String content, List<String> tags) {
        this.title = title;
        this.author = author;
        this.content = content;
        this.tags = tags;
        this.timestamp = null; // Always set to current time
    }

}
