package com.arte.jobhunter.dto;

import java.util.List;

public record FetchJobRequest(
        String location,
        List<String> skills
) {

}
