package com.arte.jobhunter.dto;

import com.arte.jobhunter.type.Result;

import java.util.List;

public record SearchResponse(
        List<Result> result
) {
}
