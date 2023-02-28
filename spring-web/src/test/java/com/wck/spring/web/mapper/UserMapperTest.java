package com.wck.spring.web.mapper;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class UserMapperTest {

  @Test
  void mapToApi_map_from_Domain_to_Api() {
    User user =
        User.builder()
            .id("22496506-b296-11ed-afa1-0242ac120002")
            .firstName("Neil")
            .lastName("Armstrong")
            .dateOfBirth(LocalDate.of(1930, 8, 5))
            .build();

    var result = mapToApi(user);

    assertThat(result.id()).isEqualTo(user.id());
    assertThat(result.firstName()).isEqualTo(user.firstName());
    assertThat(result.lastName()).isEqualTo(user.lastName());
    assertThat(result.dateOfBirth()).isEqualTo(user.dateOfBirth());
  }

  @Test
  void mapToDomain_map_from_Api_to_Domain() {
    UserApiDto userApi =
        UserApiDto.builder()
            .id("22496506-b296-11ed-afa1-0242ac120002")
            .firstName("Neil")
            .lastName("Armstrong")
            .dateOfBirth(LocalDate.of(1930, 8, 5))
            .build();

    var result = mapToDomain(userApi);

    assertThat(result.id()).isEqualTo(userApi.id());
    assertThat(result.firstName()).isEqualTo(userApi.firstName());
    assertThat(result.lastName()).isEqualTo(userApi.lastName());
    assertThat(result.dateOfBirth()).isEqualTo(userApi.dateOfBirth());
  }
}
