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

import java.util.function.Consumer;
import java.util.function.Function;

import javax.enterprise.context.ApplicationScoped;

import org.livespark.process.api.ProcessExecutor;
import org.livespark.process.api.ProcessFlow;
import org.livespark.process.api.Step;
import org.livespark.process.api.Unit;

@ApplicationScoped
public class DefaultProcessExecutor implements ProcessExecutor {

    @Override
    public void execute( final ProcessFlow<?, ?> process, final Consumer<Object> callback ) {
        if ( !(process instanceof DefaultProcessFlow) ) {
            throw new RuntimeException( "This " + ProcessExecutor.class.getSimpleName() + " can only execute a " + DefaultProcessFlow.class.getSimpleName() );
        }

        executeDefaultProcess( (DefaultProcessFlow<?, ?>) process, callback );
    }

    private void executeDefaultProcess( final DefaultProcessFlow<?, ?> process, final Consumer<Object> callback ) {
        final ProcessContext context = new ProcessContext( process );
        context.start();
        context.pushCallback( callback );
        continueProcess( context );
    }

    @SuppressWarnings( { "unchecked", "rawtypes" } )
    private void continueProcess( final ProcessContext context ) {
        while ( !context.isFinished() ) {
            final ProcessNode<?, ?> curNode = getCurrentNode( context );
            if ( curNode instanceof TransformationNode ) {
                final TransformationNode<?, ?> node = (TransformationNode< ? , ? >) curNode;
                final Object newInput = pollOutput( context );
                final Object newOutput = applyTransformation( node.transformation, newInput );
                context.pushOutput( newOutput );
            } else if ( curNode instanceof StepNode ) {
                final StepNode<?, ?> node = (StepNode<?, ?>) curNode;
                final Object newInput = pollOutput( context );
                executeStep( newInput, node.step, context );
                return;
            } else if ( curNode instanceof TransitionNode ) {
                final TransitionNode<?, ?, ?> node = (TransitionNode<?, ?, ?>) curNode;
                final Object newInput = pollOutput( context );
                executeTransition( newInput, (Function) node.transition, context );
                return;
            } else {
                throw new RuntimeException( "Unrecognized " + ProcessNode.class.getSimpleName() + " subtype: " + curNode.getClass().getName() );
            }
        }
    }

    private void executeTransition( final Object newInput,
                                    final Function<Object, ProcessFlow<?, ?>> transition,
                                    final ProcessContext context ) {
        try {
            final ProcessFlow<?, ?> newProcess = transition.apply( newInput );
            execute( newProcess, output -> {
                context.pushOutput( output );
                continueProcess( context );
            } );
        } catch ( final Throwable t ) {
            throw new RuntimeException( "An error occurred while executing a transition process.", t );
        }
    }

    @SuppressWarnings( "unchecked" )
    private void executeStep( final Object newInput, @SuppressWarnings( "rawtypes" ) final Step step, final ProcessContext context ) {
        try {
            step.execute( newInput, output -> {
                context.pushOutput( output );
                continueProcess( context );
            } );
        } catch ( final Throwable t ) {
            throw new RuntimeException( "An error occurred while executing the " + (step == null ? "null" : step.getName()) + " step.", t);
        }
    }

    private static ProcessNode<?, ?> getCurrentNode( final ProcessContext context ) {
        final ProcessNode<?, ?> curNode = context.getCurrentNode().orElseThrow( () -> new IllegalStateException( "There was not current node even though the process has not finished." ) );
        return curNode;
    }

    private static Object pollOutput( final ProcessContext context ) {
        final Object newInput = context.pollOutput().orElse( Unit.INSTANCE );
        return newInput;
    }

    @SuppressWarnings( "unchecked" )
    private static Object applyTransformation( @SuppressWarnings( "rawtypes" ) final Function transformation,
                                        final Object newInput) {
        try {
            return transformation.apply( newInput );
        } catch ( final ClassCastException e ) {
            throw new RuntimeException( "Failed to apply a transformation.", e );
        }
    }

}
