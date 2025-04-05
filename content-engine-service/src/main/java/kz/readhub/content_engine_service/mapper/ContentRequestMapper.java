package kz.readhub.content_engine_service.mapper;

import kz.readhub.content_engine_service.dto.ContentSearchRequestDto;
import kz.readhub.content_engine_service.model.SearchRequest;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ContentRequestMapper {

    private final ModelMapper modelMapper;

    public SearchRequest toModel(ContentSearchRequestDto request) {
        return this.modelMapper.map(request, SearchRequest.class);
    }
}
