package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoCreateRequest;
import ru.practicum.shareit.booking.model.BookingStatus;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BookingController.class)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private BookingService bookingService;

    private static final String USER_HEADER = "X-Sharer-User-Id";

    private BookingDto bookingDto;
    private BookingDtoCreateRequest createRequest;

    @BeforeEach
    void setUp() {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(2);

        BookingDto.BookerShortDto booker = BookingDto.BookerShortDto.builder()
                .id(2L)
                .build();

        BookingDto.ItemShortDto item = BookingDto.ItemShortDto.builder()
                .id(10L)
                .name("Item Name")
                .build();

        bookingDto = BookingDto.builder()
                .id(1L)
                .start(start)
                .end(end)
                .status(BookingStatus.WAITING.toString())
                .booker(booker)
                .item(item)
                .build();

        createRequest = BookingDtoCreateRequest.builder()
                .itemId(10L)
                .start(start)
                .end(end)
                .build();
    }

    @DisplayName("create(): Успешное создание запроса на бронирование")
    @Test
    void shouldCreateBooking() throws Exception {
        when(bookingService.create(eq(2L), any(BookingDtoCreateRequest.class))).thenReturn(bookingDto);

        mockMvc.perform(post("/bookings")
                        .header(USER_HEADER, 2L)
                        .content(mapper.writeValueAsString(createRequest))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(bookingDto.getId()))
                .andExpect(jsonPath("$.status").value(BookingStatus.WAITING.toString()))
                .andExpect(jsonPath("$.item.id").value(bookingDto.getItem().getId()))
                .andExpect(jsonPath("$.booker.id").value(bookingDto.getBooker().getId()));
    }

    @DisplayName("approve(): Успешное подтверждение бронирования владельцем")
    @Test
    void shouldApproveBooking() throws Exception {
        bookingDto.setStatus(BookingStatus.APPROVED.toString());
        when(bookingService.approve(1L, 1L, true)).thenReturn(bookingDto);

        mockMvc.perform(patch("/bookings/1")
                        .header(USER_HEADER, 1L)
                        .param("approved", "true")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value(BookingStatus.APPROVED.toString()));
    }

    @DisplayName("getBookingById(): Успешное получение деталей бронирования по ID")
    @Test
    void shouldReturnBookingById() throws Exception {
        when(bookingService.getBookingById(2L, 1L)).thenReturn(bookingDto);

        mockMvc.perform(get("/bookings/1")
                        .header(USER_HEADER, 2L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(bookingDto.getId()))
                .andExpect(jsonPath("$.item.name").value(bookingDto.getItem().getName()));
    }

    @DisplayName("getUserBookings(): Успешное получение списка бронирований арендатора")
    @Test
    void shouldReturnBookingsByUser() throws Exception {
        when(bookingService.getUserBookings(eq(2L), anyString())).thenReturn(List.of(bookingDto));

        mockMvc.perform(get("/bookings")
                        .header(USER_HEADER, 2L)
                        .param("state", "ALL")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].id").value(bookingDto.getId()));
    }

    @DisplayName("getOwnerBookings(): Успешное получение списка бронирований владельца вещей")
    @Test
    void shouldReturnBookingsByOwner() throws Exception {
        when(bookingService.getOwnerBookings(eq(1L), anyString())).thenReturn(List.of(bookingDto));

        mockMvc.perform(get("/bookings/owner")
                        .header(USER_HEADER, 1L)
                        .param("state", "ALL")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].id").value(bookingDto.getId()));
    }
}