package com.andreas.showsdb.model.dto.hateoas;

import com.andreas.showsdb.model.dto.ActorOutputDto;

public class ActorHypermedia extends HateoasDto<ActorOutputDto> {
    public ActorHypermedia(ActorOutputDto content) {
        super(content);
    }
}
