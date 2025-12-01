package com.sparta.testcode.domain.board.repository;

import com.sparta.testcode.domain.board.entity.Board;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class BoardRepositoryTest {

    @Autowired
    private BoardRepository boardRepository;

    @Test
    void saveBoard() {
        // given
        Board board = Board.create("제목", "내용", "작성자");

        // when
        Board savedBoard = boardRepository.save(board);

        // then
        assertThat(savedBoard.getId()).isNotNull();
        assertThat(savedBoard.getTitle()).isEqualTo("제목");
        assertThat(savedBoard.getContent()).isEqualTo("내용");
        assertThat(savedBoard.getAuthor()).isEqualTo("작성자");
    }

    @Test
    void findAllBoards() {
        // given
        Board board1 = Board.create("제목1", "내용1", "작성자1");
        Board board2 = Board.create("제목2", "내용2", "작성자2");
        boardRepository.save(board1);
        boardRepository.save(board2);

        // when
        var boards = boardRepository.findAll();

        // then
        assertThat(boards).hasSize(2);
    }

    @Test
    void findBoardById() {
        // given
        Board board = Board.create("제목", "내용", "작성자");
        Board savedBoard = boardRepository.save(board);

        // when
        var foundBoard = boardRepository.findById(savedBoard.getId()).orElse(null);

        // then
        assertThat(foundBoard).isNotNull();
        assertThat(foundBoard.getTitle()).isEqualTo("제목");
    }

    @Test
    void deleteBoard() {
        // given
        Board board = Board.create("제목", "내용", "작성자");
        Board savedBoard = boardRepository.save(board);

        // when
        boardRepository.delete(savedBoard);

        // then
        var foundBoard = boardRepository.findById(savedBoard.getId()).orElse(null);
        assertThat(foundBoard).isNull();
    }
}
