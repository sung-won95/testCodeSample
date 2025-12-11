package com.sparta.testcode.domain.chat.controller;

import com.sparta.testcode.domain.chat.dto.ChatRoomDto;
import com.sparta.testcode.domain.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat")
public class ChatRoomController {

    private final ChatService chatService;

    @PostMapping("/rooms")
    public ChatRoomDto createRoom(@RequestParam String name) {
        return chatService.createRoom(name);
    }

    @GetMapping("/rooms")
    public List<ChatRoomDto> findAllRooms() {
        return chatService.findAllRoom();
    }

    @GetMapping("/rooms/{roomId}")
    public ChatRoomDto findRoom(@PathVariable String roomId) {
        return chatService.findRoomById(roomId);
    }
}
