package com.example.demoapp.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.example.demoapp.entity.JobStatus;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class AdminJobPatchRequest {

    private JobStatus status;

    @JsonAlias({"jobTitle", "job_title"})
    private String title;

    @JsonAlias({"jobDescription", "job_description"})
    private String description;

    @JsonAlias({"hidden", "adminHidden", "jobHidden", "job_hidden"})
    private Boolean hiddenFromPublic;

    @JsonAlias({"moderationBlocked", "moderation_blocked", "jobBlocked", "job_blocked", "blocked"})
    private Boolean moderationBlocked;

    /** When false, job is hidden from public feeds (same as hiddenFromPublic = true). */
    private Boolean feedVisible;

    /** When false, job is hidden from public feeds. */
    private Boolean publiclyVisible;
}
