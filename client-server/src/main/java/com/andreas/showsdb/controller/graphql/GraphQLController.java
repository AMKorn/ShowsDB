package com.andreas.showsdb.controller.graphql;

import com.andreas.showsdb.exception.NotFoundException;
import com.andreas.showsdb.model.dto.*;
import com.andreas.showsdb.service.*;
import graphql.GraphQLError;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.GraphQlExceptionHandler;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class GraphQLController {

    private final ShowsService showsService;
    private final SeasonsService seasonsService;
    private final EpisodesService episodesService;
    private final ActorsService actorsService;
    private final MainCastService mainCastService;

    @QueryMapping
    List<ShowOutputDto> shows() {
        return showsService.findAll();
    }

    @QueryMapping
    ShowOutputDto showById(@Argument Long id) {
        try {
            return showsService.findById(id);
        } catch (NotFoundException e) {
            return null;
        }
    }

    @MutationMapping
    ShowOutputDto addShow(@Argument ShowInputDto show) {
        return showsService.save(show);
    }

    @QueryMapping
    List<SeasonOutputDto> seasons(@Argument Long showId) {
        return seasonsService.findByShow(showId);
    }

    @QueryMapping
    SeasonOutputDto season(@Argument Long showId, @Argument Integer seasonNumber) {
        try {
            return seasonsService.findByShowAndNumber(showId, seasonNumber);
        } catch (NotFoundException e) {
            return null;
        }
    }

    @QueryMapping
    List<EpisodeOutputDto> episodes(@Argument Long showId, @Argument Integer seasonNumber) {
        return episodesService.findBySeason(showId, seasonNumber);
    }

    @QueryMapping
    EpisodeOutputDto episode(@Argument Long showId, @Argument Integer seasonNumber, @Argument Integer episodeNumber) {
        try {
            return episodesService.findByShowAndSeasonAndEpisodeNumbers(showId, seasonNumber, episodeNumber);
        } catch (NotFoundException e) {
            return null;
        }
    }

    @QueryMapping
    List<ActorOutputDto> actors() {
        return actorsService.findAll();
    }

    @QueryMapping
    ActorOutputDto actorById(@Argument Long id) {
        try {
            return actorsService.findById(id);
        } catch (NotFoundException e) {
            return null;
        }
    }

    @QueryMapping
    List<MainCastDto> mainCasts() {
        return mainCastService.findAll();
    }

    @QueryMapping
    List<MainCastDto> actorShows(@Argument Long actorId) {
        return mainCastService.findByActor(actorId);
    }

    @QueryMapping
    List<MainCastDto> showActors(@Argument Long showId) {
        return mainCastService.findByShow(showId);
    }

    @QueryMapping
    MainCastDto character(@Argument Long actorId, @Argument Long showId) {
        try {
            return mainCastService.findByActorAndShow(actorId, showId);
        } catch (NotFoundException e) {
            return null;
        }
    }

    @GraphQlExceptionHandler
    public GraphQLError handle(NotFoundException e) {
        return GraphQLError.newError().errorType(ErrorType.NOT_FOUND).message(e.getMessage()).build();
    }
}
