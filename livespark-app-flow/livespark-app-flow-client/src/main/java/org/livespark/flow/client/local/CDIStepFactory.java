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

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import org.jboss.errai.ioc.client.container.Factory;
import org.livespark.flow.api.Step;
import org.livespark.flow.cdi.api.FlowInput;
import org.livespark.flow.cdi.api.FlowOutput;

@ApplicationScoped
public class CDIStepFactory {

    public static abstract class BaseFlowIO {
        private Object key;
        public void setKey( final Object key ) {
            this.key = key;
        }
        public boolean hasKey() {
            return key != null;
        }
        public Object getKey() {
            if ( !hasKey() ) {
                throw new IllegalStateException( "Cannot access key for FlowInput/FlowOutput before it is set." );
            }
            return key;
        }
    }

    private final class FlowOutputImpl<T> extends BaseFlowIO implements FlowOutput<T> {
        @Override
        public void submit( final T output ) {
            consumeOutput( getKey(), output );
        }
    }

    private final class FlowInputImpl<T> extends BaseFlowIO implements FlowInput<T> {
        @Override
        @SuppressWarnings( "unchecked" )
        public T get() {
            return (T) getInput( getKey() );
        }
    }

    private static class StepFrame {
        final Object instance;
        final Object input;
        final Consumer<?> callback;
        final Consumer<?> closer;
        StepFrame( final Object instance, final Object input, final Consumer<?> callback, final Consumer<?> closer ) {
            this.instance = instance;
            this.input = input;
            this.callback = callback;
            this.closer = closer;
        }
    }

    private final Map<Object, StepFrame> frames = new IdentityHashMap<>();

    public <T, INPUT, OUTPUT> Step<INPUT, OUTPUT> createCdiStep( final Supplier<T> instanceSupplier,
                                                                 final Consumer<T> starter,
                                                                 final Consumer<T> closer,
                                                                 final String name ) {
        return new Step<INPUT, OUTPUT>() {

            @Override
            public void execute( final INPUT input,
                                 final Consumer<OUTPUT> callback ) {
                final T instance = Factory.maybeUnwrapProxy( instanceSupplier.get() );
                final StepFrame frame = new StepFrame( instance, input, callback, closer );
                storeFrame( instance, frame );
                starter.accept( instance );
            }

            @Override
            public String getName() {
                return name;
            }
        };
    }

    protected void storeFrame( final Object key, final StepFrame frame ) {
        if ( frames.containsKey( key ) ) {
            throw new IllegalStateException( "Attempted to store StepFrame for [" + key + "] when one already existed." );
        }
        frames.put( key, frame );
    }

    private StepFrame removeFrame( final Object key ) {
        final StepFrame removed = frames.get( key );
        frames.remove( key );
        if ( removed == null ) {
            throw new IllegalStateException( "Attempted to remove StepFrame for [" + key + "] but none existed." );
        }

        return removed;
    }

    private StepFrame getFrame( final Object key ) {
        return frames.get( key );
    }

    @SuppressWarnings( { "unchecked", "rawtypes" } )
    private void consumeOutput( final Object key, final Object output ) {
        final StepFrame frame = removeFrame( key );
        ((Consumer) frame.closer).accept( frame.instance );
        ((Consumer) frame.callback).accept( output );
    }

    private Object getInput( final Object key ) {
        return getFrame( key ).input;
    }

    @Produces
    public <T> FlowInput<T> createInput() {
        return new FlowInputImpl<T>();
    }

    @Produces
    public <T> FlowOutput<T> createOutput() {
        return new FlowOutputImpl<T>();
    }
}
