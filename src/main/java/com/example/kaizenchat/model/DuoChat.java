package com.example.kaizenchat.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
@Getter
public class DuoChat {
    protected final long id;
    protected final String name;

    public Map<String, Object> map() {
        return new HashMap<>(Map.of(
                "id", id,
                "name", name
        ));
    }
}