package com.andreas.showsdb.controller.graphql;

import com.andreas.showsdb.exception.NotFoundException;
import com.andreas.showsdb.model.dto.ShowInputDto;
import com.andreas.showsdb.model.dto.ShowOutputDto;
import com.andreas.showsdb.service.ShowsService;
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

    @GraphQlExceptionHandler
    public GraphQLError handle(NotFoundException e) {
        return GraphQLError.newError().errorType(ErrorType.NOT_FOUND).message(e.getMessage()).build();
    }
}
