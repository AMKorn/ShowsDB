package com.andreas.showsdb.model.dto.hateoas;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.RepresentationModel;

@Getter
@RequiredArgsConstructor
abstract class HateoasDto<T> extends RepresentationModel<HateoasDto<T>> {
    private final T content;
}

