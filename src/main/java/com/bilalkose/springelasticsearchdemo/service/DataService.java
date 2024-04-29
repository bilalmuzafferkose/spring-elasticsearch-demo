package com.bilalkose.springelasticsearchdemo.service;

import com.bilalkose.springelasticsearchdemo.model.Item;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DataService {
    private final ObjectMapper objectMapper;

    public List<Item> readItemsFromJson() {
        try {
            ClassPathResource resource = new ClassPathResource("data/items.json"); //resource/data
            InputStream inputStream = resource.getInputStream();
            return objectMapper.readValue(inputStream, new TypeReference<List<Item>>() {
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
