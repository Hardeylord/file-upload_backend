package com.merging.chunks.dto;

import com.merging.chunks.model.Uploads;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class UploadDTO {
    private List<Uploads> uploadsList;
    private boolean error;
    private String message;
}
