package com.sparta.testcode.domain.board.service;

import com.sparta.testcode.domain.board.dto.BoardRequestDto;
import com.sparta.testcode.domain.board.dto.BoardResponseDto;
import com.sparta.testcode.domain.board.entity.Board;
import com.sparta.testcode.domain.board.repository.BoardRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class BoardServiceTest {

    @Mock
    private BoardRepository boardRepository;

    @InjectMocks
    private BoardService boardService;

    @Test
    void createBoard() {
        // given
        BoardRequestDto requestDto = new BoardRequestDto("제목", "내용", "작성자");

        Board board = Board.create("제목", "내용", "작성자");
        // ReflectionTestUtils.setField(board, "id", 1L); // If needed

        given(boardRepository.save(any(Board.class))).willReturn(board);

        // when
        BoardResponseDto responseDto = boardService.createBoard(requestDto);

        // then
        assertThat(responseDto.getTitle()).isEqualTo("제목");
        assertThat(responseDto.getContent()).isEqualTo("내용");
        assertThat(responseDto.getAuthor()).isEqualTo("작성자");
    }

    @Test
    void getBoard() {
        // given
        Long boardId = 1L;
        Board board = Board.create("제목", "내용", "작성자");
        given(boardRepository.findById(boardId)).willReturn(Optional.of(board));

        // when
        BoardResponseDto responseDto = boardService.getBoard(boardId);

        // then
        assertThat(responseDto.getTitle()).isEqualTo("제목");
    }

    @Test
    void getAllBoards() {
        // given
        Board board1 = Board.create("제목1", "내용1", "작성자1");
        Board board2 = Board.create("제목2", "내용2", "작성자2");
        given(boardRepository.findAll()).willReturn(List.of(board1, board2));

        // when
        List<BoardResponseDto> responseDtos = boardService.getAllBoards();

        // then
        assertThat(responseDtos).hasSize(2);
        assertThat(responseDtos.get(0).getTitle()).isEqualTo("제목1");
        assertThat(responseDtos.get(1).getTitle()).isEqualTo("제목2");
    }

    @Test
    void updateBoard() {
        // given
        Long boardId = 1L;
        BoardRequestDto requestDto = new BoardRequestDto("수정된 제목", "수정된 내용", null);

        Board board = Board.create("제목", "내용", "작성자");
        given(boardRepository.findById(boardId)).willReturn(Optional.of(board));

        // when
        BoardResponseDto responseDto = boardService.updateBoard(boardId, requestDto);

        // then
        assertThat(responseDto.getTitle()).isEqualTo("수정된 제목");
        assertThat(responseDto.getContent()).isEqualTo("수정된 내용");
    }

    @Test
    void updateBoard_NotFound() {
        // given
        Long boardId = 1L;
        BoardRequestDto requestDto = new BoardRequestDto("수정된 제목", "수정된 내용", null);
        given(boardRepository.findById(boardId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> boardService.updateBoard(boardId, requestDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("보드를 찾을 수 없습니다.: " + boardId);
    }

    @Test
    void deleteBoard() {
        // given
        Long boardId = 1L;
        given(boardRepository.existsById(boardId)).willReturn(true);

        // when
        boardService.deleteBoard(boardId);

        // then
        verify(boardRepository).deleteById(boardId);
    }

    @Test
    void deleteBoard_NotFound() {
        // given
        Long boardId = 1L;
        given(boardRepository.existsById(boardId)).willReturn(false);

        // when & then
        assertThatThrownBy(() -> boardService.deleteBoard(boardId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("보드를 찾을 수 없습니다.: " + boardId);
    }
}
