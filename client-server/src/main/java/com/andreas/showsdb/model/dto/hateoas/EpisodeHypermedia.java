package com.andreas.showsdb.model.dto.hateoas;

import com.andreas.showsdb.model.dto.EpisodeOutputDto;

public class EpisodeHypermedia extends HateoasDto<EpisodeOutputDto> {
    public EpisodeHypermedia(EpisodeOutputDto content) {
        super(content);
    }
}
