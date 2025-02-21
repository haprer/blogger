package com.haprer.blogger;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TagCount {


    @Field("_id")       //"_id" is assigned the tag value in the unwind process in blogpostrepository.java
    private String tag;
    private long count;
}