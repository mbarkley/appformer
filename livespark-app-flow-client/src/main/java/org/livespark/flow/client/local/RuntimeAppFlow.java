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


package org.livespark.flow.client.local;

import java.util.Optional;
import java.util.function.Function;

import org.livespark.flow.api.AppFlow;
import org.livespark.flow.api.Step;

class RuntimeAppFlow<INPUT, OUTPUT> implements AppFlow<INPUT, OUTPUT> {

    FlowNode<INPUT, ?> start;
    FlowNode<?, OUTPUT> end;

    public RuntimeAppFlow( final Step<INPUT, OUTPUT> step ) {
        final StepNode<INPUT, OUTPUT> stepNode = new StepNode<>( step );
        start = stepNode;
        end = stepNode;
    }

    @SuppressWarnings( "unchecked" )
    @Override
    public <T> AppFlow<INPUT, T> andThen( final Step<OUTPUT, T> nextStep ) {
        addLast( new StepNode<>( nextStep ) );

        return (AppFlow<INPUT, T>) this;
    }

    @SuppressWarnings( "unchecked" )
    @Override
    public <T> AppFlow<INPUT, T> andThen( final Function<OUTPUT, T> transformation ) {
        addLast( new TransformationNode<>( transformation ) );

        return (AppFlow<INPUT, T>) this;
    }

    @SuppressWarnings( "unchecked" )
    @Override
    public <T> AppFlow<T, OUTPUT> butFirst( final Function<T, INPUT> transformation ) {
        addFirst( new TransformationNode<>( transformation ) );

        return (AppFlow<T, OUTPUT>) this;
    }

    @SuppressWarnings( "unchecked" )
    @Override
    public <T> AppFlow<T, OUTPUT> butFirst( final Step<T, INPUT> prevStep ) {
        addFirst( new StepNode<>( prevStep ) );

        return (AppFlow<T, OUTPUT>) this;
    }

    @SuppressWarnings( "unchecked" )
    @Override
    public <T> AppFlow<INPUT, T> transition( final Function<OUTPUT, AppFlow<INPUT, T>> transition ) {
        addLast( new TransitionNode<>( transition ) );

        return (AppFlow<INPUT, T>) this;
    }

    @SuppressWarnings( "unchecked" )
    private void addLast( final FlowNode<OUTPUT, ?> node ) {
        node.prev = Optional.of( end );
        end.next = Optional.of( node );
        end = (FlowNode< ? , OUTPUT>) node;
    }

    @SuppressWarnings( "unchecked" )
    private void addFirst( final FlowNode<?, INPUT> node ) {
        node.next = Optional.of( start );
        start.prev = Optional.of( node );
        start = (FlowNode<INPUT, ? >) node;
    }

}
