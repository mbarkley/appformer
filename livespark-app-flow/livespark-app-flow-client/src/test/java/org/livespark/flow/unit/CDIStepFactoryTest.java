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

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.junit.Before;
import org.junit.Test;
import org.livespark.flow.api.Step;
import org.livespark.flow.cdi.api.FlowInput;
import org.livespark.flow.cdi.api.FlowOutput;
import org.livespark.flow.client.local.CDIFlowFactory;
import org.livespark.flow.client.local.CDIFlowFactory.BaseFlowIO;
import org.livespark.flow.util.Ref;

public class CDIStepFactoryTest {

    private CDIFlowFactory factory;

    @Before
    public void setup() {
        factory = new CDIFlowFactory();
    }

    @Test
    public void sequentialStepsInputsAndOutputs() throws Exception {
        final List<Integer> inputs = new ArrayList<>();
        final List<Integer> outputs = new ArrayList<>();

        final Step<Integer, Integer> step =
                factory.createStep( () -> new Object(),
                                       o -> {
                                           final FlowInput<Integer> flowInput = factory.createInput();
                                           final FlowOutput<Integer> flowOutput = factory.createOutput();
                                           setKey( o, flowInput );
                                           setKey( o, flowOutput );
                                           inputs.add( flowInput.get() );
                                           final int output = flowInput.get() + 1;
                                           outputs.add( output );
                                           flowOutput.submit( output );
                                       },
                                       o -> {}, "Adder" );

        step.execute( 0, n1 -> {
            step.execute( n1, n2 -> {
                step.execute( n2, n3 -> {} );
            } );
        } );

        assertEquals( asList( 0, 1, 2 ), inputs );
        assertEquals( asList( 1, 2, 3 ), outputs );
    }

    @Test
    public void nestedStepsInputsAndOutputs() throws Exception {
        final List<Integer> inputs = new ArrayList<>();
        final List<Integer> outputs = new ArrayList<>();

        final Function<Function<Integer, Integer>, Step<Integer, Integer>> stepFunc =
                consumer ->
            factory.createStep( () -> new Object(),
                                   o -> {
                                       final FlowInput<Integer> flowInput = factory.createInput();
                                       final FlowOutput<Integer> flowOutput = factory.createOutput();
                                       setKey( o, flowInput );
                                       setKey( o, flowOutput );
                                       final Integer beforeInput = flowInput.get();
                                       inputs.add( beforeInput );
                                       final int intermediateOutput = beforeInput + 1;
                                       final Integer finalOutput = consumer.apply( intermediateOutput );
                                       assertEquals( beforeInput, flowInput.get() );
                                       outputs.add( finalOutput );
                                       flowOutput.submit( finalOutput );
                                   },
                                   o -> {}, "Adder" );

        stepFunc.apply( n1 -> {
            final Ref<Integer> ref1 = new Ref<>();
            stepFunc.apply( n2 -> {
                final Ref<Integer> ref2 = new Ref<>();
                stepFunc.apply( n3 -> n3 + 1 ).execute( n2, val -> { ref2.val = val; } );
                return ref2.val;
            } ).execute( n1, val -> { ref1.val = val; } );
            return ref1.val;
        } ).execute( 0, val -> {} );

        assertEquals( asList( 0, 1, 2 ), inputs );
        assertEquals( asList( 4, 4, 4 ), outputs );
    }

    @Test
    public void parallelStepsInputsAndOutputs() throws Exception {
        final List<FlowInput<Integer>> flowInputs = new ArrayList<>();
        final List<FlowOutput<Integer>> flowOutputs = new ArrayList<>();
        final List<Integer> submittedOutputs = new ArrayList<>();

        final Step<Integer, Integer> nonTerminatingStep =
                factory.createStep(
                                      () -> new Object(),
                                      o -> {
                                          final FlowInput<Integer> flowInput = factory.createInput();
                                          final FlowOutput<Integer> flowOutput = factory.createOutput();
                                          setKey( o, flowInput );
                                          setKey( o, flowOutput );
                                          flowInputs.add( flowInput );
                                          flowOutputs.add( flowOutput );
                                      },
                                      o -> {}, "Adder" );

        nonTerminatingStep.execute( 0, output -> submittedOutputs.add( output ) );
        assertEquals( 1, flowInputs.size() );
        assertEquals( 1, flowOutputs.size() );
        assertEquals( Integer.valueOf( 0 ), flowInputs.get( 0 ).get() );

        nonTerminatingStep.execute( 1, output -> submittedOutputs.add( output ) );
        assertEquals( 2, flowInputs.size() );
        assertEquals( 2, flowOutputs.size() );
        assertEquals( Integer.valueOf( 0 ), flowInputs.get( 0 ).get() );
        assertEquals( Integer.valueOf( 1 ), flowInputs.get( 1 ).get() );

        flowOutputs.get( 1 ).submit( 2 );
        assertEquals( 1, submittedOutputs.size() );
        assertEquals( Integer.valueOf( 2 ), submittedOutputs.get( 0 ) );
        assertEquals( Integer.valueOf( 0 ), flowInputs.get( 0 ).get() );

        flowOutputs.get( 0 ).submit( 3 );
        assertEquals( 2, submittedOutputs.size() );
        assertEquals( Integer.valueOf( 3 ), submittedOutputs.get( 1 ) );
    }

    private void setKey( final Object key, final FlowInput<?> flowInput ) {
        setKey( key, (BaseFlowIO) flowInput );
    }

    private void setKey( final Object key, final FlowOutput<?> flowOutput ) {
        setKey( key, (BaseFlowIO) flowOutput );
    }

    private void setKey( final Object key, final BaseFlowIO flowIO ) {
        flowIO.setKey( key );
    }

}
