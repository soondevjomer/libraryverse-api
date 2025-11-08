package com.soondevjomer.libraryverse.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CustomerDto {

    private Long id;
    private String address;
    private String contactNumber;
    private String email;
    private String name;
    private String image;
    private String imageThumbnail;
    private LocalDateTime createdDate;
}
