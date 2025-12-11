package com.sparta.testcode.domain.chat.service;

import com.sparta.testcode.domain.chat.dto.ChatRoomDto;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ChatService {
    private Map<String, ChatRoomDto> chatRooms = new LinkedHashMap<>();

    public List<ChatRoomDto> findAllRoom() {
        return new ArrayList<>(chatRooms.values());
    }

    public ChatRoomDto findRoomById(String roomId) {
        return chatRooms.get(roomId);
    }

    public ChatRoomDto createRoom(String name) {
        ChatRoomDto room = ChatRoomDto.create(name);
        chatRooms.put(room.getRoomId(), room);
        return room;
    }
}
