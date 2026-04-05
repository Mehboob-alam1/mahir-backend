package com.example.demoapp.dto;

import com.example.demoapp.entity.JobStatus;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminJobPatchRequest {

    private JobStatus status;
}
