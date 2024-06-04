package com.andreas.showsdb.model.dto.hateoas;

import com.andreas.showsdb.model.dto.MainCastDto;

public class MainCastHypermedia extends HateoasDto<MainCastDto> {
    public MainCastHypermedia(MainCastDto content) {
        super(content);
    }
}
