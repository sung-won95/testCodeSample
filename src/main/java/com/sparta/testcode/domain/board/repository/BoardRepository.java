package com.sparta.testcode.domain.board.repository;

import com.sparta.testcode.domain.board.entity.Board;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardRepository extends JpaRepository<Board, Long> {
}
