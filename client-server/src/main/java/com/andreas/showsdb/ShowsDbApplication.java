package com.andreas.showsdb;

import com.andreas.showsdb.statemachine.StateMachineConfig.ShowsEvent;
import com.andreas.showsdb.statemachine.StateMachineConfig.ShowsState;
import com.andreas.showsdb.util.Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.statemachine.StateMachine;
import reactor.core.publisher.Mono;

@Slf4j
@SpringBootApplication
public class ShowsDbApplication implements CommandLineRunner {

    @Autowired
    private StateMachine<ShowsState, ShowsEvent> stateMachine;

    public static void main(String[] args) {
        SpringApplication.run(ShowsDbApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        Utils.sendMachineStateEvent(stateMachine, ShowsEvent.AIRS);
        Utils.sendMachineStateEvent(stateMachine, ShowsEvent.FINISH);
    }
}
