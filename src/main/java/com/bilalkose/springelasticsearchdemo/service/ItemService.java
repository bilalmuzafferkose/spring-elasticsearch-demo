package com.bilalkose.springelasticsearchdemo.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.bilalkose.springelasticsearchdemo.dto.SearchRequestDto;
import com.bilalkose.springelasticsearchdemo.model.Item;
import com.bilalkose.springelasticsearchdemo.repository.ItemRepository;
import com.bilalkose.springelasticsearchdemo.util.ESUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ItemService {
    private final ItemRepository itemRepository;
    private final DataService dataService;
    private final ElasticsearchClient elasticsearchClient; //tüm sorgular bu client üzerinden yapılacak
    public Item createIndex(Item item) {
        return itemRepository.save(item);
    }

    public void addItemsFromJson() {
        log.info("Adding items from json..");
        List<Item> itemList = dataService.readItemsFromJson(); //veriler çekilir
        itemRepository.saveAll(itemList);
    }

    public List<Item> getAllDataFromIndex(String indexName) {
        //elasticsearch query'leri util class'da yazılabilir
        Query query = ESUtil.createMatchAllQuery();
        log.info("elasticsearch query {}", query);
        SearchResponse<Item> response = null;
        try {
            response = elasticsearchClient.search(
                    q -> q.index(indexName).query(query), Item.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        //Slf4j ile eşleşen veriyi ekrana basmak
        log.info("Elasticsearch response {}", response);
        return extractItemsFromResponse(response);
    }



    public List<Item> searchItemsByFieldAndValue(SearchRequestDto dto) {
        //elasticsearch._types.query_dsl.Query
        Supplier<Query> query = ESUtil.buildQueryForFieldAndValue(dto.getFieldName().get(0),
                dto.getSearchValue().get(0));
        log.info("elasticsearch query {}", query);
        SearchResponse<Item> response = null;
        try {
            response = elasticsearchClient.search(
                    q -> q.index("items_index").query(query.get()), Item.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        log.info("Elasticsearch response {}", response);
        return extractItemsFromResponse(response);
    }

    public List<Item> searchItemsByNameAndBrandWithQuery(String name, String brand) {
        return itemRepository.searchByNameAndBrand(name, brand);
    }

    public List<Item> boolQueryFieldAndValue(SearchRequestDto searchRequestDto) {
        try {
            var supplier = ESUtil.createBoolQuery(searchRequestDto);
            log.info("Elasticsearch query: " + supplier.get().toString());

            SearchResponse<Item> response = elasticsearchClient.search(q ->
                    q.index("items_index").query(supplier.get()), Item.class);
            log.info("Elasticsearch response: {}", response.toString());

            return extractItemsFromResponse(response);
        } catch (IOException e) {
            return Collections.emptyList();
        }
    }

    public Set<String> findSuggestedItemNames(String itemName) {
        Query autoSuggestQuery = ESUtil.buildAutoSuggestQuery(itemName);
        log.info("Elasticsearch query: {}", autoSuggestQuery.toString());

        try {
            return elasticsearchClient.search(q -> q.index("items_index").query(autoSuggestQuery), Item.class)
                    .hits()
                    .hits()
                    .stream()
                    .map(Hit::source)
                    .map(Item::getName)
                    .collect(Collectors.toSet());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<String> autoSuggestItemsByNameWithQuery(String name) {
        List<Item> items = itemRepository.customAutocompleteSearch(name);
        log.info("Elasticsearch response: {}", items.toString());
        return items
                .stream()
                .map(Item::getName)
                .collect(Collectors.toList());
    }

    public List<Item> extractItemsFromResponse(SearchResponse<Item> response) {
        return response
                .hits()
                .hits()
                .stream()
                .map(Hit::source)
                .collect(Collectors.toList());
    }
}
