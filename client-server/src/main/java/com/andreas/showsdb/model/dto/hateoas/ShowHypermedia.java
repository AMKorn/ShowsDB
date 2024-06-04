package com.andreas.showsdb.model.dto.hateoas;

import com.andreas.showsdb.model.dto.ShowOutputDto;

public class ShowHypermedia extends HateoasDto<ShowOutputDto> {
    public ShowHypermedia(ShowOutputDto content) {
        super(content);
    }
}
