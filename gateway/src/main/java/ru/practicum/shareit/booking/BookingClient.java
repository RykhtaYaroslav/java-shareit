package ru.practicum.shareit.booking;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.client.BaseClient;

import java.util.Map;

@Service
public class BookingClient extends BaseClient {
    private static final String API_PREFIX = "/bookings";

    @Autowired
    public BookingClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .build()
        );
    }

    public ResponseEntity<Object> create(Long userId, BookingRequestDto requestDto) {
        return post("", userId, requestDto);
    }

    public ResponseEntity<Object> approve(Long userId, Long bookingId, Boolean approved) {
        Map<String, Object> parameters = Map.of("approved", approved);
        String path = String.format("/%d?approved={approved}", bookingId);
        return patch(path, userId, parameters, null);
    }

    public ResponseEntity<Object> getBookingById(Long userId, Long bookingId) {
        String path = String.format("/%d", bookingId);
        return get(path, userId);
    }

    public ResponseEntity<Object> getUserBookings(Long userId, String state) {
        Map<String, Object> parameters = Map.of("state", state);
        return get("?state={state}", userId, parameters);
    }

    public ResponseEntity<Object> getOwnerBookings(Long ownerId, String state) {
        Map<String, Object> parameters = Map.of("state", state);
        return get("/owner?state={state}", ownerId, parameters);
    }
}