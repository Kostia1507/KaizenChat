package com.example.kaizenchat.model;

import lombok.Getter;

import java.util.Map;

@Getter
public class GroupChat extends DuoChat {

    private final int membersCount;
    private final int membersLimit;

    public GroupChat(long id, String name, int membersCount, int membersLimit) {
        super(id, name);
        this.membersCount = membersCount;
        this.membersLimit = membersLimit;
    }

    @Override
    public Map<String, Object> map() {
        var map = super.map();
        map.put("membersCount", membersCount);
        map.put("membersLimit", membersLimit);
        return map;
    }
}