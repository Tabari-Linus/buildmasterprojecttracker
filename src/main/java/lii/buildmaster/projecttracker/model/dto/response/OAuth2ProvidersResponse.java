package lii.buildmaster.projecttracker.model.dto.response;

import lii.buildmaster.projecttracker.model.dto.info.OAuth2ProviderInfo;

import java.util.Map;

public record OAuth2ProvidersResponse(Map<String, OAuth2ProviderInfo> providers) {
}
