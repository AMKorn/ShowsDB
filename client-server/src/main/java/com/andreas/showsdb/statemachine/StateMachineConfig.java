package com.andreas.showsdb.statemachine;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.listener.StateMachineListener;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;

import java.util.EnumSet;

@Slf4j
@Configuration
@EnableStateMachine
public class StateMachineConfig extends EnumStateMachineConfigurerAdapter<StateMachineConfig.ShowsState, StateMachineConfig.ShowsEvent> {
    public enum ShowsState {
        UNRELEASED, AIRING, CANCELLED, FINISHED
    }

    public enum ShowsEvent {
        AIRS, CANCELLATION, FINISH
    }

    @Override
    public void configure(StateMachineConfigurationConfigurer<ShowsState, ShowsEvent> config) throws Exception {
        config.withConfiguration().autoStartup(true).listener(listener());
    }

    @Override
    public void configure(StateMachineStateConfigurer<ShowsState, ShowsEvent> states) throws Exception {
        states.withStates()
                .initial(ShowsState.UNRELEASED)
                .states(EnumSet.allOf(ShowsState.class));
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<ShowsState, ShowsEvent> transitions) throws Exception {
        transitions.withExternal()
                .source(ShowsState.UNRELEASED).target(ShowsState.AIRING).event(ShowsEvent.AIRS)
                .and().withExternal()
                .source(ShowsState.AIRING).target(ShowsState.FINISHED).event(ShowsEvent.FINISH)
                .and().withExternal()
                .source(ShowsState.AIRING).target(ShowsState.CANCELLED).event(ShowsEvent.CANCELLATION);
    }

    public StateMachineListener<ShowsState, ShowsEvent> listener() {
        return new StateMachineListenerAdapter<>() {
            @Override
            public void stateChanged(State<ShowsState, ShowsEvent> from, State<ShowsState, ShowsEvent> to) {
                log.info("State change to {}", to.getId());
            }
        };
    }
}
