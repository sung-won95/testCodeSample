package com.sparta.testcode.domain.chat.integration;

import com.sparta.testcode.domain.chat.dto.ChatMessageDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * WebSocket 통합 테스트
 *
 * 핵심 개념:
 * - @SpringBootTest(webEnvironment = RANDOM_PORT): 실제 서버를 랜덤 포트로 띄움
 * - WebSocketStompClient: STOMP 프로토콜을 사용하는 WebSocket 클라이언트
 * - BlockingQueue: 비동기 메시지를 동기적으로 검증하기 위한 자료구조
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class WebSocketIntegrationTest {

    @LocalServerPort
    private int port;

    private String wsUrl;
    private WebSocketStompClient stompClient;

    @BeforeEach
    void setUp() {
        wsUrl = "http://localhost:" + port + "/ws-stomp";

        // SockJS 클라이언트 설정
        SockJsClient sockJsClient = new SockJsClient(
                List.of(new WebSocketTransport(new StandardWebSocketClient()))
        );

        // STOMP 클라이언트 설정
        stompClient = new WebSocketStompClient(sockJsClient);
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());
    }

    @Test
    @DisplayName("WebSocket 연결 성공 테스트")
    void testWebSocketConnection() throws Exception {
        // given
        StompSession session = stompClient
                .connectAsync(wsUrl, new StompSessionHandlerAdapter() {})
                .get(3, TimeUnit.SECONDS);

        // then
        assertThat(session.isConnected()).isTrue();

        // cleanup
        session.disconnect();
    }

    @Test
    @DisplayName("채팅방 입장 메시지 전송 및 수신 테스트")
    void testChatRoomEnterMessage() throws Exception {
        // given
        String roomId = "test-room-1";
        BlockingQueue<ChatMessageDto> messageQueue = new LinkedBlockingQueue<>();

        // STOMP 세션 연결
        StompSession session = stompClient
                .connectAsync(wsUrl, new StompSessionHandlerAdapter() {})
                .get(3, TimeUnit.SECONDS);

        // 채팅방 구독
        session.subscribe("/topic/chat/room/" + roomId, new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return ChatMessageDto.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                messageQueue.offer((ChatMessageDto) payload);
            }
        });

        // when - 입장 메시지 전송
        ChatMessageDto enterMessage = new ChatMessageDto();
        enterMessage.setType(ChatMessageDto.MessageType.ENTER);
        enterMessage.setRoomId(roomId);
        enterMessage.setSender("테스트유저");
        enterMessage.setMessage("");

        session.send("/app/chat/message", enterMessage);

        // then - 메시지 수신 대기 (최대 3초)
        ChatMessageDto receivedMessage = messageQueue.poll(3, TimeUnit.SECONDS);

        assertThat(receivedMessage).isNotNull();
        assertThat(receivedMessage.getType()).isEqualTo(ChatMessageDto.MessageType.ENTER);
        assertThat(receivedMessage.getRoomId()).isEqualTo(roomId);
        assertThat(receivedMessage.getSender()).isEqualTo("테스트유저");
        assertThat(receivedMessage.getMessage()).isEqualTo("테스트유저님이 입장하셨습니다.");

        // cleanup
        session.disconnect();
    }

    @Test
    @DisplayName("채팅 메시지 전송 및 수신 테스트")
    void testChatMessage() throws Exception {
        // given
        String roomId = "test-room-2";
        String testMessage = "안녕하세요!";
        BlockingQueue<ChatMessageDto> messageQueue = new LinkedBlockingQueue<>();

        StompSession session = stompClient
                .connectAsync(wsUrl, new StompSessionHandlerAdapter() {})
                .get(3, TimeUnit.SECONDS);

        session.subscribe("/topic/chat/room/" + roomId, new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return ChatMessageDto.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                messageQueue.offer((ChatMessageDto) payload);
            }
        });

        // when
        ChatMessageDto chatMessage = new ChatMessageDto();
        chatMessage.setType(ChatMessageDto.MessageType.TALK);
        chatMessage.setRoomId(roomId);
        chatMessage.setSender("홍길동");
        chatMessage.setMessage(testMessage);

        session.send("/app/chat/message", chatMessage);

        // then
        ChatMessageDto receivedMessage = messageQueue.poll(3, TimeUnit.SECONDS);

        assertThat(receivedMessage).isNotNull();
        assertThat(receivedMessage.getType()).isEqualTo(ChatMessageDto.MessageType.TALK);
        assertThat(receivedMessage.getMessage()).isEqualTo(testMessage);
        assertThat(receivedMessage.getSender()).isEqualTo("홍길동");

        // cleanup
        session.disconnect();
    }

    @Test
    @DisplayName("여러 클라이언트가 동일 채팅방 구독 시 모두 메시지 수신")
    void testMultipleSubscribers() throws Exception {
        // given
        String roomId = "test-room-3";
        BlockingQueue<ChatMessageDto> queue1 = new LinkedBlockingQueue<>();
        BlockingQueue<ChatMessageDto> queue2 = new LinkedBlockingQueue<>();

        // 첫 번째 클라이언트
        StompSession session1 = stompClient
                .connectAsync(wsUrl, new StompSessionHandlerAdapter() {})
                .get(3, TimeUnit.SECONDS);

        session1.subscribe("/topic/chat/room/" + roomId, new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return ChatMessageDto.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                queue1.offer((ChatMessageDto) payload);
            }
        });

        // 두 번째 클라이언트
        StompSession session2 = stompClient
                .connectAsync(wsUrl, new StompSessionHandlerAdapter() {})
                .get(3, TimeUnit.SECONDS);

        session2.subscribe("/topic/chat/room/" + roomId, new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return ChatMessageDto.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                queue2.offer((ChatMessageDto) payload);
            }
        });

        // when - 메시지 전송
        ChatMessageDto message = new ChatMessageDto();
        message.setType(ChatMessageDto.MessageType.TALK);
        message.setRoomId(roomId);
        message.setSender("발신자");
        message.setMessage("테스트 메시지");

        session1.send("/app/chat/message", message);

        // then - 두 클라이언트 모두 메시지 수신
        ChatMessageDto received1 = queue1.poll(3, TimeUnit.SECONDS);
        ChatMessageDto received2 = queue2.poll(3, TimeUnit.SECONDS);

        assertThat(received1).isNotNull();
        assertThat(received2).isNotNull();
        assertThat(received1.getMessage()).isEqualTo("테스트 메시지");
        assertThat(received2.getMessage()).isEqualTo("테스트 메시지");

        // cleanup
        session1.disconnect();
        session2.disconnect();
    }

    @Test
    @DisplayName("잘못된 목적지로 메시지 전송 시 수신되지 않음")
    void testInvalidDestination() throws Exception {
        // given
        String correctRoomId = "room-1";
        String wrongRoomId = "room-2";
        BlockingQueue<ChatMessageDto> messageQueue = new LinkedBlockingQueue<>();

        StompSession session = stompClient
                .connectAsync(wsUrl, new StompSessionHandlerAdapter() {})
                .get(3, TimeUnit.SECONDS);

        // room-1 구독
        session.subscribe("/topic/chat/room/" + correctRoomId, new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return ChatMessageDto.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                messageQueue.offer((ChatMessageDto) payload);
            }
        });

        // when - room-2로 메시지 전송
        ChatMessageDto message = new ChatMessageDto();
        message.setType(ChatMessageDto.MessageType.TALK);
        message.setRoomId(wrongRoomId);
        message.setSender("테스터");
        message.setMessage("이 메시지는 수신되지 않아야 함");

        session.send("/app/chat/message", message);

        // then - 메시지가 수신되지 않아야 함 (타임아웃)
        ChatMessageDto receivedMessage = messageQueue.poll(2, TimeUnit.SECONDS);
        assertThat(receivedMessage).isNull();

        // cleanup
        session.disconnect();
    }
}
