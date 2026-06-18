package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentRequestDto {
    private String text;

    public static Comment mapToModel(CommentRequestDto request, Item item, User author, LocalDateTime created) {
        return Comment.builder()
                .text(request.getText())
                .author(author)
                .item(item)
                .created(created)
                .build();
    }
}
