package com.sparta.testcode.domain.board.service;

import com.sparta.testcode.domain.board.dto.BoardRequestDto;
import com.sparta.testcode.domain.board.dto.BoardResponseDto;
import com.sparta.testcode.domain.board.entity.Board;
import com.sparta.testcode.domain.board.repository.BoardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;

    @Transactional
    public BoardResponseDto createBoard(BoardRequestDto requestDto) {
        Board board = Board.create(requestDto.getTitle(), requestDto.getContent(), requestDto.getAuthor());
        Board savedBoard = boardRepository.save(board);
        return new BoardResponseDto(savedBoard);
    }

    @Transactional(readOnly = true)
    public BoardResponseDto getBoard(Long id) {
        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("보드를 찾을 수 없습니다.: " + id));
        return new BoardResponseDto(board);
    }

    @Transactional(readOnly = true)
    public List<BoardResponseDto> getAllBoards() {
        return boardRepository.findAll().stream()
                .map(BoardResponseDto::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public BoardResponseDto updateBoard(Long id, BoardRequestDto requestDto) {
        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("보드를 찾을 수 없습니다.: " + id));
        board.update(requestDto.getTitle(), requestDto.getContent());

        return new BoardResponseDto(board);
    }

    @Transactional
    public void deleteBoard(Long id) {
        if (!boardRepository.existsById(id)) {
            throw new IllegalArgumentException("보드를 찾을 수 없습니다.: " + id);
        }
        boardRepository.deleteById(id);
    }
}
