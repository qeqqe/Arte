package com.arte.apicore.dto.proto;

import java.util.List;

public record GitHubResponseDTO(
        boolean success,
        String message,
        Integer reposProcessed,
        List<String>  repoNames
) {
}
