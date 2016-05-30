/*
 * Copyright (C) 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.livespark.process.client.local;

import java.util.Deque;
import java.util.LinkedList;
import java.util.Optional;
import java.util.function.Consumer;

class ProcessContext {

    private Optional<Object> input = Optional.empty(),
                             output = Optional.empty();
    private final DefaultProcessFlow<?, ?> process;
    private Optional<ProcessNode<?, ?>> currentNode = Optional.empty();

    private final Deque<Consumer<Object>> callbacks = new LinkedList<>();


    ProcessContext( final DefaultProcessFlow<?, ?> process ) {
        this.process = process;
    }

    Optional<Object> pollInput() {
        return input;
    }

    Optional<Object> pollOutput() {
        return output;
    }

    @SuppressWarnings( { "unchecked", "rawtypes" } )
    void pushOutput(final Object value) {
        input = output;
        output = Optional.of( value );
        currentNode = (Optional) currentNode.flatMap( node -> node.next );
    }

    void start() {
        if ( isStarted() ) {
            throw new RuntimeException( "Process has already been started." );
        }

        currentNode = Optional.of( process.start );
    }

    boolean isStarted() {
        return !( currentNode.isPresent() || output.isPresent() );
    }

    boolean isFinished() {
        return !currentNode.isPresent() && output.isPresent();
    }

    Optional<ProcessNode<?, ?>> getCurrentNode() {
        return currentNode;
    }

    DefaultProcessFlow<?, ?> getProcess() {
        return process;
    }

    public void pushCallback( final Consumer<Object> callback ) {
        callbacks.push( callback );
    }

}
