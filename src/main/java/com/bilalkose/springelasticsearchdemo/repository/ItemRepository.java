package com.bilalkose.springelasticsearchdemo.repository;

import com.bilalkose.springelasticsearchdemo.model.Item;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;


public interface ItemRepository extends ElasticsearchRepository<Item, String> {
    //match query ve bool query kullanacağız.
    //bool-> 2 veya daha fazla query birleşebilir
    //must -> && (and)
    // ?0 -> String name | ?1 -> String brand
    @Query("{\"bool\": {\"must\": [{\"match\": {\"name\": \"?0\"}}, {\"match\": {\"brand\": \"?1\"}}]}}")
    List<Item> searchByNameAndBrand(String name, String brand);

    @Query("{\"bool\": {\"must\": {\"match_phrase_prefix\": {\"name\": \"?0\"}}}}")
    List<Item> customAutocompleteSearch(String input);
}
