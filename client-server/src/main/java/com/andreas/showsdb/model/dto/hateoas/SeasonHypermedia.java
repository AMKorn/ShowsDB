package com.andreas.showsdb.model.dto.hateoas;

import com.andreas.showsdb.model.dto.SeasonOutputDto;

public class SeasonHypermedia extends HateoasDto<SeasonOutputDto> {
    public SeasonHypermedia(SeasonOutputDto content) {
        super(content);
    }
}
