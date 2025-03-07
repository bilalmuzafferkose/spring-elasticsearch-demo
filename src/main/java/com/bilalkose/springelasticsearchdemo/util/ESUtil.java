package com.bilalkose.springelasticsearchdemo.util;

import co.elastic.clients.elasticsearch._types.query_dsl.*;
import co.elastic.clients.util.ObjectBuilder;
import com.bilalkose.springelasticsearchdemo.dto.SearchRequestDto;
import lombok.experimental.UtilityClass;

import java.util.function.Function;
import java.util.function.Supplier;

@UtilityClass
public class ESUtil {
    public static Query createMatchAllQuery() { //elasticsearch._types.query_dsl.Query
        return Query.of(q -> q.matchAll(new MatchAllQuery.Builder().build())); //herhangi bir şart olmadan veriyi olduğu gibi getir
    }


    public static Supplier<Query> buildQueryForFieldAndValue(String fieldName, String searchValue) {
        return () -> Query.of( q -> q.match(buildMatchQueryForFieldAndValue(fieldName, searchValue)) );
    }

    private static MatchQuery buildMatchQueryForFieldAndValue(String fieldName, String searchValue) {
        return new MatchQuery.Builder()
                .field(fieldName)
                .query(searchValue)
                .build();
    }

    public static Supplier<Query> createBoolQuery(SearchRequestDto dto) {
        return () -> Query.of(q -> q.bool(boolQuery(dto.getFieldName().get(0).toString(), dto.getSearchValue().get(0),
                dto.getFieldName().get(1).toString(), dto.getSearchValue().get(1))));
    }

    public static BoolQuery boolQuery(String key1, String value1, String key2, String value2) {
        return new BoolQuery.Builder()
                .filter(termQuery(key1.toString(), value1))
                .must(termQuery(key2.toString(), value2))
                .build();
    }

    public static Query termQuery(String field, String value) {
        return Query.of(q -> q.term(new TermQuery.Builder()
                .field(field)
                .value(value)
                .build()));
    }

    public static Query matchQuery(String field, String value) {
        return Query.of(q -> q.match(new MatchQuery.Builder()
                .field(field)
                .query(value)
                .build()));
    }

    public static Query buildAutoSuggestQuery(String name) {
        return Query.of(q -> q.match(createAutoSuggestMatchQuery(name)));
    }

    public static MatchQuery createAutoSuggestMatchQuery(String name) {
        return new MatchQuery.Builder()
                .field("name")
                .query(name)
                .analyzer("custom_index")
                .build();
    }
}
