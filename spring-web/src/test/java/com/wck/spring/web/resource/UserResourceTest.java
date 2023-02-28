package com.wck.spring.web.resource;

import static java.lang.String.format;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@WebMvcTest(UserResource.class)
class UserResourceTest {

  @MockBean private UserServiceApi userService;

  @MockBean private Clock clock;

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @Test
  void findById_WHEN_id_exists_THEN_return_user() throws Exception {
    // Given
    User user =
        User.builder()
            .id("USER_ID")
            .firstName("Neil")
            .lastName("Armstrong")
            .dateOfBirth(LocalDate.of(1930, 8, 5))
            .build();

    when(userService.findById(user.id())).thenReturn(Optional.of(user));

    // When
    MvcResult mvcResult =
        mockMvc
            .perform(get("/users/{id}", user.id()).contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON_VALUE))
            .andReturn();

    // Then
    var result =
        objectMapper.readValue(mvcResult.getResponse().getContentAsString(), UserApiDto.class);

    assertThat(result.id()).isEqualTo(user.id());
    assertThat(result.firstName()).isEqualTo(user.firstName());
    assertThat(result.lastName()).isEqualTo(user.lastName());
    assertThat(result.dateOfBirth()).isEqualTo(user.dateOfBirth());
  }

  @Test
  void findById_WHEN_error_occurred_THEN_return_ApiError_404() throws Exception {
    // Given
    String userId = "USER_ID";
    Instant instant = Instant.parse("2023-02-21T15:12:00.00Z");
    ZoneId zoneId = ZoneId.of("Asia/Tokyo");

    when(userService.findById(userId)).thenReturn(Optional.empty());
    when(clock.instant()).thenReturn(instant);
    when(clock.getZone()).thenReturn(zoneId);

    // When
    MvcResult mvcResult =
        mockMvc
            .perform(get("/users/{id}", userId).contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isNotFound())
            .andReturn();

    // Then
    var result =
        objectMapper.readValue(mvcResult.getResponse().getContentAsString(), ApiError.class);

    assertThat(result.status()).isEqualTo(HttpStatus.NOT_FOUND.value());
    assertThat(result.label()).isEqualTo(HttpStatus.NOT_FOUND.getReasonPhrase());
    assertThat(result.message()).isEqualTo(format("User with id %s", userId));
    assertThat(result.path()).isEqualTo(format("/users/%s", userId));
    assertThat(result.timestamp()).isEqualTo(OffsetDateTime.now(clock));
  }

  @Test
  void findAll_THEN_return_all_users() throws Exception {
    // Given
    User user1 =
        User.builder()
            .id("USER_ID_1")
            .firstName("Neil")
            .lastName("Armstrong")
            .dateOfBirth(LocalDate.of(1930, 8, 5))
            .build();

    User user2 =
        User.builder()
            .id("USER_ID_2")
            .firstName("Edwin")
            .lastName("Aldrin")
            .dateOfBirth(LocalDate.of(1930, 1, 20))
            .build();

    var expected = Stream.of(user1, user2).map(UserMapper::mapToApi).toList();

    when(userService.findAll()).thenReturn(List.of(user1, user2));

    // When
    MvcResult mvcResult = mockMvc.perform(get("/users")).andExpect(status().isOk()).andReturn();

    // Then
    var result =
        objectMapper.readValue(
            mvcResult.getResponse().getContentAsString(), new TypeReference<List<UserApiDto>>() {});

    assertThat(result).isEqualTo(expected);
  }

  @Test
  void findAll_WHEN_unexpected_error_occurred_THEN_return_ApiError_500() throws Exception {
    // Given
    Instant instant = Instant.parse("2023-02-21T15:12:00.00Z");
    ZoneId zoneId = ZoneId.of("Asia/Tokyo");

    when(userService.findAll()).thenThrow(new RuntimeException("Unexpected error."));
    when(clock.instant()).thenReturn(instant);
    when(clock.getZone()).thenReturn(zoneId);

    // When
    MvcResult mvcResult =
        mockMvc
            .perform(get("/users").contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isInternalServerError())
            .andReturn();

    // Then
    var result =
        objectMapper.readValue(mvcResult.getResponse().getContentAsString(), ApiError.class);

    assertThat(result.status()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
    assertThat(result.label()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
    assertThat(result.message()).isEqualTo("Unexpected error.");
    assertThat(result.path()).isEqualTo("/users");
    assertThat(result.timestamp()).isEqualTo(OffsetDateTime.now(clock));
  }

  @Test
  void save_WHEN_valid_user_THEN_return_saved_user() throws Exception {
    // Given
    UserApiDto userApi =
        UserApiDto.builder()
            .firstName("Neil")
            .lastName("Armstrong")
            .dateOfBirth(LocalDate.of(1930, 8, 5))
            .build();

    User user =
        User.builder()
            .id("22496506-b296-11ed-afa1-0242ac120002")
            .firstName("Neil")
            .lastName("Armstrong")
            .dateOfBirth(LocalDate.of(1930, 8, 5))
            .build();

    when(userService.save(mapToDomain(userApi))).thenReturn(user);

    // When
    MvcResult mvcResult =
        mockMvc
            .perform(
                post("/users")
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .content(objectMapper.writeValueAsString(userApi)))
            .andExpect(status().isCreated())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON_VALUE))
            .andReturn();

    // Then
    var result =
        objectMapper.readValue(mvcResult.getResponse().getContentAsString(), UserApiDto.class);

    assertThat(result.id()).isEqualTo(user.id());
    assertThat(result.firstName()).isEqualTo(userApi.firstName());
    assertThat(result.lastName()).isEqualTo(userApi.lastName());
    assertThat(result.dateOfBirth()).isEqualTo(userApi.dateOfBirth());
  }

  @Test
  void save_WHEN_invalid_user_THEN_return_ApiError_400() throws Exception {
    // Given
    UserApiDto userApi =
        UserApiDto.builder()
            .id("UNEXPECTED_ID")
            .firstName("Neil")
            .lastName("Armstrong")
            .dateOfBirth(LocalDate.of(1930, 8, 5))
            .build();

    Instant instant = Instant.parse("2023-02-21T15:12:00.00Z");
    ZoneId zoneId = ZoneId.of("Asia/Tokyo");

    when(clock.instant()).thenReturn(instant);
    when(clock.getZone()).thenReturn(zoneId);

    // When
    MvcResult mvcResult =
        mockMvc
            .perform(
                post("/users")
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .content(objectMapper.writeValueAsString(userApi)))
            .andExpect(status().isBadRequest())
            .andReturn();

    // Then
    var result =
        objectMapper.readValue(mvcResult.getResponse().getContentAsString(), ApiError.class);

    assertThat(result.status()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    assertThat(result.label()).isEqualTo(HttpStatus.BAD_REQUEST.getReasonPhrase());
    assertThat(result.message()).containsIgnoringCase("validation failed");
    assertThat(result.path()).isEqualTo("/users");
    assertThat(result.timestamp()).isEqualTo(OffsetDateTime.now(clock));
  }

  @Test
  void save_WHEN_unexpected_error_occurred_THEN_return_ApiError_500() throws Exception {
    // Given
    UserApiDto userApi =
        UserApiDto.builder()
            .firstName("Neil")
            .lastName("Armstrong")
            .dateOfBirth(LocalDate.of(1930, 8, 5))
            .build();

    Instant instant = Instant.parse("2023-02-21T15:12:00.00Z");
    ZoneId zoneId = ZoneId.of("Asia/Tokyo");

    when(clock.instant()).thenReturn(instant);
    when(clock.getZone()).thenReturn(zoneId);

    when(userService.save(mapToDomain(userApi)))
        .thenThrow(new RuntimeException("Unexpected error."));

    // When
    MvcResult mvcResult =
        mockMvc
            .perform(
                post("/users")
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .content(objectMapper.writeValueAsString(userApi)))
            .andExpect(status().isInternalServerError())
            .andReturn();

    // Then
    var result =
        objectMapper.readValue(mvcResult.getResponse().getContentAsString(), ApiError.class);

    assertThat(result.status()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
    assertThat(result.label()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
    assertThat(result.message()).isEqualTo("Unexpected error.");
    assertThat(result.path()).isEqualTo("/users");
    assertThat(result.timestamp()).isEqualTo(OffsetDateTime.now(clock));
  }

  @Test
  void update_WHEN_valid_user_THEN_return_saved_user() throws Exception {
    // Given
    UserApiDto userApi =
        UserApiDto.builder()
            .id("22496506-b296-11ed-afa1-0242ac120002")
            .firstName("Neil")
            .lastName("Armstrong")
            .dateOfBirth(LocalDate.of(1930, 8, 5))
            .build();

    User user = mapToDomain(userApi);

    when(userService.update(user)).thenReturn(user);

    // When
    MvcResult mvcResult =
        mockMvc
            .perform(
                put("/users")
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .content(objectMapper.writeValueAsString(userApi)))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON_VALUE))
            .andReturn();

    // Then
    var result =
        objectMapper.readValue(mvcResult.getResponse().getContentAsString(), UserApiDto.class);

    assertThat(result.id()).isEqualTo(user.id());
    assertThat(result.firstName()).isEqualTo(userApi.firstName());
    assertThat(result.lastName()).isEqualTo(userApi.lastName());
    assertThat(result.dateOfBirth()).isEqualTo(userApi.dateOfBirth());
  }

  @Test
  void update_WHEN_invalid_user_THEN_return_ApiError_400() throws Exception {
    // Given
    UserApiDto userApi =
        UserApiDto.builder()
            .firstName("Neil")
            .lastName("Armstrong")
            .dateOfBirth(LocalDate.of(1930, 8, 5))
            .build();

    Instant instant = Instant.parse("2023-02-21T15:12:00.00Z");
    ZoneId zoneId = ZoneId.of("Asia/Tokyo");

    when(clock.instant()).thenReturn(instant);
    when(clock.getZone()).thenReturn(zoneId);

    // When
    MvcResult mvcResult =
        mockMvc
            .perform(
                put("/users")
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .content(objectMapper.writeValueAsString(userApi)))
            .andExpect(status().isBadRequest())
            .andReturn();

    // Then
    var result =
        objectMapper.readValue(mvcResult.getResponse().getContentAsString(), ApiError.class);

    assertThat(result.status()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    assertThat(result.label()).isEqualTo(HttpStatus.BAD_REQUEST.getReasonPhrase());
    assertThat(result.message()).containsIgnoringCase("validation failed");
    assertThat(result.path()).isEqualTo("/users");
    assertThat(result.timestamp()).isEqualTo(OffsetDateTime.now(clock));
  }

  @Test
  void update_WHEN_unexpected_error_occurred_THEN_return_ApiError_500() throws Exception {
    // Given
    UserApiDto userApi =
        UserApiDto.builder()
            .id("22496506-b296-11ed-afa1-0242ac120002")
            .firstName("Neil")
            .lastName("Armstrong")
            .dateOfBirth(LocalDate.of(1930, 8, 5))
            .build();

    Instant instant = Instant.parse("2023-02-21T15:12:00.00Z");
    ZoneId zoneId = ZoneId.of("Asia/Tokyo");

    when(clock.instant()).thenReturn(instant);
    when(clock.getZone()).thenReturn(zoneId);

    when(userService.update(mapToDomain(userApi)))
        .thenThrow(new RuntimeException("Unexpected error."));

    // When
    MvcResult mvcResult =
        mockMvc
            .perform(
                put("/users")
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .content(objectMapper.writeValueAsString(userApi)))
            .andExpect(status().isInternalServerError())
            .andReturn();

    // Then
    var result =
        objectMapper.readValue(mvcResult.getResponse().getContentAsString(), ApiError.class);

    assertThat(result.status()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
    assertThat(result.label()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
    assertThat(result.message()).isEqualTo("Unexpected error.");
    assertThat(result.path()).isEqualTo("/users");
    assertThat(result.timestamp()).isEqualTo(OffsetDateTime.now(clock));
  }

  @Test
  void delete_WHEN_success_THEN_return_204() throws Exception {
    // Given
    String validId = "22496506-b296-11ed-afa1-0242ac120002";

    // When Then
    mockMvc
        .perform(delete("/users/{id}", validId).contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().isNoContent())
        .andReturn();
  }

  @Test
  void delete_WHEN_unexpected_error_occurred_THEN_return_ApiError_500() throws Exception {
    // Given
    String id = "22496506-b296-11ed-afa1-0242ac120002";

    Instant instant = Instant.parse("2023-02-21T15:12:00.00Z");
    ZoneId zoneId = ZoneId.of("Asia/Tokyo");

    when(clock.instant()).thenReturn(instant);
    when(clock.getZone()).thenReturn(zoneId);

    doThrow(new RuntimeException("Unexpected error.")).when(userService).deleteById(id);

    // When
    MvcResult mvcResult =
        mockMvc
            .perform(delete("/users/{id}", id).contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isInternalServerError())
            .andReturn();

    // Then
    var result =
        objectMapper.readValue(mvcResult.getResponse().getContentAsString(), ApiError.class);

    assertThat(result.status()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
    assertThat(result.label()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
    assertThat(result.message()).isEqualTo("Unexpected error.");
    assertThat(result.path()).isEqualTo("/users/" + id);
    assertThat(result.timestamp()).isEqualTo(OffsetDateTime.now(clock));
  }
}
