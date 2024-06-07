package com.andreas.showsdb.statemachine;

import com.andreas.showsdb.exception.ShowStateMachineException;
import com.andreas.showsdb.model.Show;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineBuilder;
import org.springframework.statemachine.config.StateMachineBuilder.Builder;
import org.springframework.statemachine.listener.StateMachineListener;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;
import reactor.core.publisher.Mono;

import java.util.EnumSet;

import static com.andreas.showsdb.model.Show.Event.*;
import static com.andreas.showsdb.model.Show.State.*;

@Slf4j
public class ShowStateMachine {
    StateMachine<Show.State, Show.Event> stateMachine;

    public static StateMachine<Show.State, Show.Event> create() throws ShowStateMachineException {
        return create(UNRELEASED);
    }

    public static StateMachine<Show.State, Show.Event> create(Show.State state) throws ShowStateMachineException {
        try {
            Builder<Show.State, Show.Event> builder = StateMachineBuilder.builder();
            builder.configureStates()
                    .withStates()
                    .initial(state)
                    .states(EnumSet.allOf(Show.State.class));
            builder.configureTransitions().withExternal()
                    .source(UNRELEASED).target(AIRING).event(AIRS)
                    .and().withExternal()
                    .source(AIRING).target(FINISHED).event(FINISH)
                    .and().withExternal()
                    .source(AIRING).target(CANCELLED).event(CANCELLATION)
                    .and().withExternal()
                    .source(CANCELLED).target(AIRING).event(RENEWED)
                    .and().withExternal()
                    .source(FINISHED).target(AIRING).event(RENEWED);
            builder.configureConfiguration().withConfiguration()
                    .autoStartup(true)
                    .listener(listener());
            return builder.build();
        } catch (Exception e) {
            throw new ShowStateMachineException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public static StateMachineListener<Show.State, Show.Event> listener() {
        return new StateMachineListenerAdapter<>() {
            @Override
            public void stateChanged(State<Show.State, Show.Event> from, State<Show.State, Show.Event> to) {
                log.info("State change to {}", to.getId());
            }
        };
    }

    public static void sendMachineStateEvent(StateMachine<Show.State, Show.Event> stateMachine, Show.Event event) {
        Message<Show.Event> message = MessageBuilder.withPayload(event).build();
        Mono<Message<Show.Event>> messageMono = Mono.just(message);
        stateMachine.sendEvent(messageMono).subscribe();
    }
}
