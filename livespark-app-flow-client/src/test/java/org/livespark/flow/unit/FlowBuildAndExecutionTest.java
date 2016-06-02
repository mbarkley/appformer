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


package org.livespark.flow.unit;

import static org.junit.Assert.assertEquals;
import static org.livespark.flow.client.local.StepUtil.wrap;

import java.util.function.Consumer;
import java.util.function.Function;

import org.junit.Before;
import org.junit.Test;
import org.livespark.flow.api.AppFlow;
import org.livespark.flow.api.AppFlowExecutor;
import org.livespark.flow.api.AppFlowFactory;
import org.livespark.flow.api.Step;
import org.livespark.flow.api.Unit;
import org.livespark.flow.client.local.RuntimeAppFlowExecutor;
import org.livespark.flow.client.local.RuntimeAppFlowFactory;
import org.livespark.flow.util.Ref;

public class FlowBuildAndExecutionTest {

    private AppFlowFactory factory;
    private AppFlowExecutor executor;

    @Before
    public void setup() {
        factory = new RuntimeAppFlowFactory();
        executor = new RuntimeAppFlowExecutor();
    }

    @Test
    public void sequentialSteps() throws Exception {
        final Step<Unit, Integer> zero = wrap( "Produce Zero", () -> 0 );
        final Step<Integer, Integer> add10 = wrap( "Increment", (final Integer n) -> n + 10);
        final Step<Object, String> stringify = wrap( "Stringify", o -> o.toString() );
        final Step<String, String> reverse = wrap( "Reverse String", (final String s) -> new StringBuilder( s ).reverse().toString() );

        final AppFlow<Unit, String> flow = factory
            .buildFrom( zero )
            .andThen( add10 )
            .andThen( stringify )
            .andThen( reverse );

        final Ref<String> result = new Ref<>();
        executor.execute( flow, val -> { result.val = val; } );

        assertEquals( "01", result.val );
    }

    @Test
    public void simpleTransition() throws Exception {
        final Step<Unit, Boolean> t = wrap( "True", () -> true );
        final Step<Unit, Boolean> f = wrap( "False", () -> false );
        final Step<Unit, String> tString = wrap( "True String", () -> "true" );
        final Step<Unit, String> fString = wrap( "False String", () -> "false" );
        final Function<Boolean, AppFlow<Unit, String>> transition = b -> factory.buildFrom( b ? tString : fString );

        final AppFlow<Unit, String> tFlow = factory
            .buildFrom( t )
            .transition( transition );

        final AppFlow<Unit, String> fFlow = factory
            .buildFrom( f )
            .transition( transition );

        final Ref<String> res = new Ref<>();
        final Consumer<String> resConsumer = s -> { res.val = s; };

        executor.execute( tFlow, resConsumer );
        assertEquals( "true", res.val );

        executor.execute( fFlow, resConsumer );
        assertEquals( "false", res.val );
    }

    @Test
    public void sequentialStepsWithTransformationsAfter() throws Exception {
        final Step<Unit, Integer> zero = wrap( "Produce Zero", () -> 0 );
        final Function<Integer, Integer> add10 = n -> n + 10;
        final Function<Object, String> stringify = o -> o.toString();
        final Step<String, String> reverse = wrap( "Reverse String", (final String s) -> new StringBuilder( s ).reverse().toString() );

        final AppFlow<Unit, String> flow = factory
            .buildFrom( zero )
            .andThen( add10 )
            .andThen( stringify )
            .andThen( reverse );

        final Ref<String> result = new Ref<>();
        executor.execute( flow, val -> { result.val = val; } );

        assertEquals( "01", result.val );
    }

    @Test
    public void sequentialFlowsWithPreTransformation() throws Exception {
        final Step<Unit, Integer> zero = wrap( "Produce Zero", () -> 0 );
        final Function<Integer, Integer> add10 = n -> n + 10;
        final Function<Object, String> stringify = o -> o.toString();
        final Step<String, String> reverse = wrap( "Reverse String", (final String s) -> new StringBuilder( s ).reverse().toString() );

        final AppFlow<Unit, String> flow = factory
            .buildFrom( zero )
            .andThen( factory
                          .buildFrom( reverse )
                          .butFirst( stringify )
                          .butFirst( add10 ) );

        final Ref<String> result = new Ref<>();
        executor.execute( flow, val -> { result.val = val; } );

        assertEquals( "01", result.val );
    }

    @Test
    public void executeFlowInStep() throws Exception {
        final Step<Unit, Integer> zero = wrap( "Produce Zero", () -> 0 );
        final Step<Integer, Integer> add10 = wrap( "Add 10", n -> n + 10 );
        final AppFlow<Integer, Integer> add10Flow = factory.buildFrom( add10 );
        final Step<Integer, Integer> stepCallingProcess = wrap( "Step Calling Process", (n, callback) -> executor.execute( n, add10Flow, callback ) );

        final AppFlow<Unit, Integer> flow = factory
            .buildFrom( zero )
            .andThen( stepCallingProcess );

        final Ref<Integer> result = new Ref<>();
        executor.execute( flow, val -> { result.val = val; } );

        assertEquals( Integer.valueOf( 10 ), result.val );
    }

}
