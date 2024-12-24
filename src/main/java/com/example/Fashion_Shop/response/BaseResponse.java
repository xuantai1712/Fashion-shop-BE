package com.example.Fashion_Shop.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BaseResponse {
    @JsonProperty("create_at")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd,MM,yyyy")
    private LocalDateTime createAt;

    @JsonProperty("update_at")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd,MM,yyyy")
    private LocalDateTime updateAt;
}
