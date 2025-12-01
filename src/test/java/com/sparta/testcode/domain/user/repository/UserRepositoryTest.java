package com.sparta.testcode.domain.user.repository;

import com.sparta.testcode.domain.user.entity.User;
import com.sparta.testcode.domain.user.entity.UserRoleEnum;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void findByUsername() {
        // given
        User user = User.create("테스트유저", "비밀번호123", UserRoleEnum.USER);
        userRepository.save(user);

        // when
        User foundUser = userRepository.findByUsername("테스트유저").orElse(null);

        // then
        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getUsername()).isEqualTo("테스트유저");
        assertThat(foundUser.getRole()).isEqualTo(UserRoleEnum.USER);
    }
}
