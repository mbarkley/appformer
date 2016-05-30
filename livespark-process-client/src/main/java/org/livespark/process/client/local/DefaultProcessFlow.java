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

import java.util.Optional;
import java.util.function.Function;

import org.livespark.process.api.ProcessFlow;
import org.livespark.process.api.Step;

class DefaultProcessFlow<INPUT, OUTPUT> implements ProcessFlow<INPUT, OUTPUT> {

    ProcessNode<INPUT, ?> start;
    ProcessNode<?, OUTPUT> end;

    public DefaultProcessFlow( final Step<INPUT, OUTPUT> step ) {
        start = new StepNode<>( step );
        end = new StepNode<>( step );
    }

    @SuppressWarnings( "unchecked" )
    @Override
    public <T> ProcessFlow<INPUT, T> andThen( final Step<OUTPUT, T> nextStep ) {
        addLast( new StepNode<>( nextStep ) );

        return (ProcessFlow<INPUT, T>) this;
    }

    @SuppressWarnings( "unchecked" )
    @Override
    public <T> ProcessFlow<INPUT, T> andThen( final Function<OUTPUT, T> transformation ) {
        addLast( new TransformationNode<>( transformation ) );

        return (ProcessFlow<INPUT, T>) this;
    }

    @SuppressWarnings( "unchecked" )
    @Override
    public <T> ProcessFlow<T, OUTPUT> butFirst( final Function<T, INPUT> transformation ) {
        addFirst( new TransformationNode<>( transformation ) );

        return (ProcessFlow<T, OUTPUT>) this;
    }

    @SuppressWarnings( "unchecked" )
    @Override
    public <T> ProcessFlow<T, OUTPUT> butFirst( final Step<T, INPUT> prevStep ) {
        addFirst( new StepNode<>( prevStep ) );

        return (ProcessFlow<T, OUTPUT>) this;
    }

    @SuppressWarnings( "unchecked" )
    @Override
    public <T> ProcessFlow<INPUT, T> transition( final Function<OUTPUT, ProcessFlow<INPUT, T>> transition ) {
        addLast( new TransitionNode<>( transition ) );

        return (ProcessFlow<INPUT, T>) this;
    }

    @SuppressWarnings( "unchecked" )
    private void addLast( final ProcessNode<OUTPUT, ?> node ) {
        node.prev = Optional.of( end );
        end.next = Optional.of( node );
        end = (ProcessNode< ? , OUTPUT>) node;
    }

    @SuppressWarnings( "unchecked" )
    private void addFirst( final ProcessNode<?, INPUT> node ) {
        node.next = Optional.of( start );
        start.prev = Optional.of( node );
        start = (ProcessNode<INPUT, ? >) node;
    }

}
