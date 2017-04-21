/*
 * Copyright (C) 2017 Red Hat, Inc. and/or its affiliates.
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


package org.kie.appformer.flowset.interpeter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.kie.appformer.flow.api.CrudOperation.CREATE;
import static org.kie.appformer.flow.api.CrudOperation.DELETE;
import static org.kie.appformer.flow.api.CrudOperation.UPDATE;
import static org.kie.appformer.flow.api.FormOperation.CANCEL;
import static org.kie.appformer.flow.api.FormOperation.NEXT;
import static org.kie.appformer.flow.api.FormOperation.PREVIOUS;
import static org.kie.appformer.flow.api.FormOperation.SUBMIT;
import static org.kie.appformer.flowset.interpeter.InterpeterTest.Choice.ONE;
import static org.kie.appformer.flowset.interpeter.InterpeterTest.Choice.TWO;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;

import org.junit.Before;
import org.junit.Test;
import org.kie.appformer.flow.api.AppFlow;
import org.kie.appformer.flow.api.AppFlowExecutor;
import org.kie.appformer.flow.api.AppFlowFactory;
import org.kie.appformer.flow.api.Command;
import org.kie.appformer.flow.api.CrudOperation;
import org.kie.appformer.flow.api.FormOperation;
import org.kie.appformer.flow.api.Unit;
import org.kie.appformer.flow.impl.RuntimeAppFlowExecutor;
import org.kie.appformer.flow.impl.RuntimeAppFlowFactory;
import org.kie.appformer.flowset.api.definition.DataStep;
import org.kie.appformer.flowset.api.definition.DecisionGateway;
import org.kie.appformer.flowset.api.definition.FormStep;
import org.kie.appformer.flowset.api.definition.JoinGateway;
import org.kie.appformer.flowset.api.definition.MatcherStep;
import org.kie.appformer.flowset.api.definition.MultiStep;
import org.kie.appformer.flowset.api.definition.RootStep;
import org.kie.appformer.flowset.api.definition.SequenceFlow;
import org.kie.appformer.flowset.api.definition.StartNoneEvent;
import org.kie.appformer.flowset.api.definition.property.gateway.MatchedOperation;
import org.kie.appformer.flowset.api.definition.property.general.Name;
import org.kie.appformer.flowset.api.definition.property.general.Type;
import org.kie.appformer.flowset.interpeter.res.Address;
import org.kie.appformer.flowset.interpeter.res.User;
import org.kie.appformer.flowset.interpeter.util.TestDisplayer;
import org.kie.appformer.flowset.interpeter.util.TestFormView;
import org.kie.appformer.flowset.interpeter.util.TestModelOracle;
import org.kie.appformer.flowset.interpeter.util.TestUIComponent;
import org.kie.appformer.flowset.interpreter.Interpreter;
import org.kie.appformer.flowset.interpreter.ModelOracle;
import org.kie.workbench.common.stunner.core.graph.Graph;
import org.kie.workbench.common.stunner.core.graph.Node;
import org.kie.workbench.common.stunner.core.graph.content.relationship.Child;
import org.kie.workbench.common.stunner.core.graph.content.view.View;
import org.kie.workbench.common.stunner.core.graph.content.view.ViewImpl;
import org.kie.workbench.common.stunner.core.graph.impl.EdgeImpl;
import org.kie.workbench.common.stunner.core.graph.impl.GraphImpl;
import org.kie.workbench.common.stunner.core.graph.impl.NodeImpl;
import org.kie.workbench.common.stunner.core.graph.store.GraphNodeStore;
import org.kie.workbench.common.stunner.core.graph.store.GraphNodeStoreImpl;

public class InterpeterTest {

    private final Random rand = new Random( 1234 );

    private final AppFlowFactory flowFactory = new RuntimeAppFlowFactory();
    private final AppFlowExecutor executor = new RuntimeAppFlowExecutor();

    private Graph<?, Node> graph;
    private GraphNodeStore<Node> nodeStore;

    private Map<String, AppFlow<?, ?>> flowParts;
    private Map<String, TestUIComponent<?>> formSteps;
    private TestDisplayer<TestFormView<?>> displayer;
    TestModelOracle modelOracle;
    private Interpreter<TestFormView<?>> interpreter;

    @Before
    public void setup() {
        nodeStore = new GraphNodeStoreImpl();
        graph = new GraphImpl<>( "graph", nodeStore );
        flowParts = new HashMap<>();
        formSteps = new HashMap<>();
        displayer = new TestDisplayer<>();
        final HashMap<Class<?>, ModelOracle> oraclesByType = new HashMap<>();
        oraclesByType.put( User.class, new User.UserOracle() );
        oraclesByType.put( org.kie.appformer.flowset.interpeter.res.Name.class,
                           new org.kie.appformer.flowset.interpeter.res.Name.NameOracle() );
        oraclesByType.put( Address.class, new Address.AddressOracle() );
        modelOracle = new TestModelOracle( oraclesByType );
        final Set<Class<? extends Enum<?>>> enums = new HashSet<>( Arrays.asList( FormOperation.class,
                                                                                  CrudOperation.class,
                                                                                  Choice.class ) );
        interpreter = new Interpreter<>( flowParts,
                                         name -> Optional.ofNullable( formSteps.get( name ) ),
                                         displayer,
                                         modelOracle,
                                         enums,
                                         flowFactory );
    }

    @Test
    public void graphWithNoGateways() throws Exception {
        /*
         * Setup. Graph looks like:
         *   start -> one -> double -> toString
         */
        flowParts.put( "one", flowFactory.buildFromConstant( 1 ) );
        flowParts.put( "double", flowFactory.buildFromFunction( (final Integer x) -> 2*x ) );
        flowParts.put( "toString", flowFactory.buildFromFunction( Object::toString ) );
        final NodeImpl<View<StartNoneEvent>> start = addNode( start() );
        final NodeImpl<View<DataStep>> one = addNode( dataStepWithName( "one" ) );
        final NodeImpl<View<DataStep>> doubleNode = addNode( dataStepWithName( "double" ) );
        final NodeImpl<View<DataStep>> toString = addNode( dataStepWithName( "toString" ) );
        sequence( start, one );
        sequence( one, doubleNode );
        sequence( doubleNode, toString );

        // Test
        final AppFlow<Unit, String> flow = (AppFlow<Unit, String>) interpreter.convert( graph );
        assertNotNull( flow );
        final String str = executeSynchronously( flow );
        assertEquals( "2", str );
    }

    @Test
    public void lookupDataStepWithType() throws Exception {
        /*
         * Setup. Graph looks like:
         *   start -> one -> double:Integer -> toString
         */
        flowParts.put( "one", flowFactory.buildFromConstant( 1 ) );
        flowParts.put( "double:Integer", flowFactory.buildFromFunction( (final Integer x) -> 2*x ) );
        flowParts.put( "toString", flowFactory.buildFromFunction( Object::toString ) );
        final NodeImpl<View<StartNoneEvent>> start = addNode( start() );
        final NodeImpl<View<DataStep>> one = addNode( dataStepWithName( "one" ) );
        final NodeImpl<View<DataStep>> doubleNode = addNode( dataStep( "double", "Integer" ) );
        final NodeImpl<View<DataStep>> toString = addNode( dataStepWithName( "toString" ) );
        sequence( start, one );
        sequence( one, doubleNode );
        sequence( doubleNode, toString );

        // Test
        final AppFlow<Unit, String> flow = (AppFlow<Unit, String>) interpreter.convert( graph );
        assertNotNull( flow );
        final String str = executeSynchronously( flow );
        assertEquals( "2", str );
    }

    @Test
    public void graphWithDecisionAndJoin() throws Exception {
        /*
         * Setup. Graph looks like:
         *   start -> decision  -> matcher(CREATE) -> one   \
         *                     \-> matcher(UPDATE) -> two    |-> join
         *                     \-> matcher(DELETE) -> three /
         */
        flowParts.put( "one", flowFactory.buildFromFunction( (final String s) -> "one:" + s ) );
        flowParts.put( "two", flowFactory.buildFromFunction( (final String s) -> "two:" + s ) );
        flowParts.put( "three", flowFactory.buildFromFunction( (final String s) -> "three:" + s ) );
        final NodeImpl<View<StartNoneEvent>> start = addNode( start() );
        final NodeImpl<View<DecisionGateway>> decision = addNode( decision() );
        final NodeImpl<View<MatcherStep>> createMatcher = addNode( matcherWithOperation( CREATE ) );
        final NodeImpl<View<MatcherStep>> updateMatcher = addNode( matcherWithOperation( UPDATE ) );
        final NodeImpl<View<MatcherStep>> deleteMatcher = addNode( matcherWithOperation( DELETE ) );
        final NodeImpl<View<DataStep>> one = addNode( dataStepWithName( "one" ) );
        final NodeImpl<View<DataStep>> two = addNode( dataStepWithName( "two" ) );
        final NodeImpl<View<DataStep>> three = addNode( dataStepWithName( "three" ) );
        final NodeImpl<View<JoinGateway>> join = addNode( join() );
        sequence( start, decision );
        sequence( decision, createMatcher );
        sequence( decision, updateMatcher );
        sequence( decision, deleteMatcher );
        sequence( createMatcher, one );
        sequence( updateMatcher, two );
        sequence( deleteMatcher, three );
        sequence( one, join );
        sequence( two, join );
        sequence( three, join );

        // Test
        final AppFlow<Command<CrudOperation, String>, Integer> flow = (AppFlow<Command<CrudOperation, String>, Integer>) interpreter.convert( graph );
        assertNotNull( flow );
        assertEquals( "one:a", executeSynchronously( new Command<>( CREATE, "a" ), flow ) );
        assertEquals( "two:b", executeSynchronously( new Command<>( UPDATE, "b" ), flow ) );
        assertEquals( "three:c", executeSynchronously( new Command<>( DELETE, "c" ), flow ) );
    }

    @Test
    public void graphWithDecisionOnOtherEnum() throws Exception {
        /*
         * Setup. Graph looks like:
         *   start -> decision  -> matcher(ONE) -> one   \
         *                     \-> matcher(TWO) -> two    |-> join
         */
        flowParts.put( "one", flowFactory.buildFromFunction( (final String s) -> "one:" + s ) );
        flowParts.put( "two", flowFactory.buildFromFunction( (final String s) -> "two:" + s ) );
        final NodeImpl<View<StartNoneEvent>> start = addNode( start() );
        final NodeImpl<View<DecisionGateway>> decision = addNode( decision() );
        final NodeImpl<View<MatcherStep>> oneMatcher = addNode( matcherWithOperation( ONE ) );
        final NodeImpl<View<MatcherStep>> twoMatcher = addNode( matcherWithOperation( TWO ) );
        final NodeImpl<View<DataStep>> one = addNode( dataStepWithName( "one" ) );
        final NodeImpl<View<DataStep>> two = addNode( dataStepWithName( "two" ) );
        final NodeImpl<View<JoinGateway>> join = addNode( join() );
        sequence( start, decision );
        sequence( decision, oneMatcher );
        sequence( decision, twoMatcher );
        sequence( oneMatcher, one );
        sequence( twoMatcher, two );
        sequence( one, join );
        sequence( two, join );

        // Test
        final AppFlow<Command<Choice, String>, Integer> flow = (AppFlow<Command<Choice, String>, Integer>) interpreter.convert( graph );
        assertNotNull( flow );
        assertEquals( "one:a", executeSynchronously( new Command<>( ONE, "a" ), flow ) );
        assertEquals( "two:b", executeSynchronously( new Command<>( TWO, "b" ), flow ) );
    }

    @Test
    public void graphWithDecisionAndNoJoin() throws Exception {
        /*
         * Setup. Graph looks like:
         *   start -> toCommand -> decision  -> matcher(CREATE) -> one
         *                                   \-> matcher(UPDATE) -> two
         *                                   \-> matcher(DELETE) -> three
         */
        flowParts.put( "toCommand", flowFactory.buildFromFunction( (final String s) -> new Command<>( CREATE, s ) ) );
        flowParts.put( "one", flowFactory.buildFromFunction( (final String s) -> "one:" + s ) );
        flowParts.put( "two", flowFactory.buildFromFunction( (final String s) -> "two:" + s ) );
        flowParts.put( "three", flowFactory.buildFromFunction( (final String s) -> "three:" + s ) );
        final NodeImpl<View<StartNoneEvent>> start = addNode( start() );
        final NodeImpl<View<DataStep>> toCommand = addNode( dataStepWithName( "toCommand" ) );
        final NodeImpl<View<DecisionGateway>> decision = addNode( decision() );
        final NodeImpl<View<MatcherStep>> createMatcher = addNode( matcherWithOperation( CREATE ) );
        final NodeImpl<View<MatcherStep>> updateMatcher = addNode( matcherWithOperation( UPDATE ) );
        final NodeImpl<View<MatcherStep>> deleteMatcher = addNode( matcherWithOperation( DELETE ) );
        final NodeImpl<View<DataStep>> one = addNode( dataStepWithName( "one" ) );
        final NodeImpl<View<DataStep>> two = addNode( dataStepWithName( "two" ) );
        final NodeImpl<View<DataStep>> three = addNode( dataStepWithName( "three" ) );
        sequence( start, toCommand );
        sequence( toCommand, decision );
        sequence( decision, createMatcher );
        sequence( decision, updateMatcher );
        sequence( decision, deleteMatcher );
        sequence( createMatcher, one );
        sequence( updateMatcher, two );
        sequence( deleteMatcher, three );

        // Test
        final AppFlow<String, Integer> flow = (AppFlow<String, Integer>) interpreter.convert( graph );
        assertNotNull( flow );
        assertEquals( "one:a", executeSynchronously( "a", flow ) );
        assertEquals( "one:b", executeSynchronously( "b", flow ) );
    }

    @Test
    public void recursiveFlow() throws Exception {
        /*
         * Setup. Graph looks like:
         *   start -> decision  -> matcher(CREATE) -> one -> start
         *                     \-> matcher(UPDATE) -> two -> start
         *                     \-> matcher(DELETE) -> three -> join
         */
        flowParts.put( "one", flowFactory.buildFromFunction( (final String s) -> new Command<>( UPDATE, s ) ) );
        flowParts.put( "two", flowFactory.buildFromFunction( (final String s) -> new Command<>( DELETE, s ) ) );
        flowParts.put( "three", flowFactory.buildFromFunction( Function.identity() ) );
        final NodeImpl<View<StartNoneEvent>> start = addNode( start() );
        final NodeImpl<View<DecisionGateway>> decision = addNode( decision() );
        final NodeImpl<View<MatcherStep>> createMatcher = addNode( matcherWithOperation( CREATE ) );
        final NodeImpl<View<MatcherStep>> updateMatcher = addNode( matcherWithOperation( UPDATE ) );
        final NodeImpl<View<MatcherStep>> deleteMatcher = addNode( matcherWithOperation( DELETE ) );
        final NodeImpl<View<DataStep>> one = addNode( dataStepWithName( "one" ) );
        final NodeImpl<View<DataStep>> two = addNode( dataStepWithName( "two" ) );
        final NodeImpl<View<DataStep>> three = addNode( dataStepWithName( "three" ) );
        final NodeImpl<View<JoinGateway>> join = addNode( join() );
        sequence( start, decision );
        sequence( decision, createMatcher );
        sequence( decision, updateMatcher );
        sequence( decision, deleteMatcher );
        sequence( createMatcher, one );
        sequence( updateMatcher, two );
        sequence( deleteMatcher, three );
        sequence( one, start );
        sequence( two, start );
        sequence( three, join );

        // Test
        final AppFlow<Command<CrudOperation, String>, Integer> flow = (AppFlow<Command<CrudOperation, String>, Integer>) interpreter.convert( graph );
        assertNotNull( flow );
        assertEquals( "a", executeSynchronously( new Command<>( CREATE, "a" ), flow ) );
        assertEquals( "b", executeSynchronously( new Command<>( UPDATE, "b" ), flow ) );
        assertEquals( "c", executeSynchronously( new Command<>( DELETE, "c" ), flow ) );
    }

    @Test
    @SuppressWarnings( "unchecked" )
    public void multiStepFormHappyPath() throws Exception {
        /*
         * Setup. Graph looks like:
         *            ---------------------------------------------
         *            |                New User                   |
         *            | ----------------------------------------- |
         *   start -> | | name -> primaryAddress -> mailAddress | |
         *            | ----------------------------------------- |
         *            ---------------------------------------------
         */
        flowParts.put( "New:User", flowFactory.buildFromSupplier( User::new ) );
        final TestUIComponent<org.kie.appformer.flowset.interpeter.res.Name> nameUI =
                new TestUIComponent<>( n -> {
                    n.setFirst( "John" );
                    n.setLast( "Doe" );
                    return new Command<>( NEXT, n );
                } );
        final TestUIComponent<Address> primaryUI = new TestUIComponent<>( a -> {
            a.setStreet( "Fake Street" );
            a.setNumber( "123" );
            return new Command<>( NEXT, a );
        } );
        final TestUIComponent<Address> mailUI = new TestUIComponent<>( a -> {
            a.setStreet( "Penny Lane" );
            a.setNumber( "321" );
            return new Command<>( SUBMIT, a );
        } );
        formSteps.put( "Name", nameUI );
        // In a real run these would be the same component, but need to distinguish for testing.
        formSteps.put( "PrimaryAddress", primaryUI );
        formSteps.put( "MailingAddress", mailUI );
        final NodeImpl<View<StartNoneEvent>> start = addNode( start() );
        final NodeImpl<View<RootStep>> newUser = addNode( rootStep( "New", "User" ) );
        final NodeImpl<View<MultiStep>> multi = addNode( multiStep() );
        final NodeImpl<View<FormStep>> nameStep = addNode( formPart( "Name", "name" ) );
        final NodeImpl<View<FormStep>> primaryStep = addNode( formPart( "PrimaryAddress", "primary" ) );
        final NodeImpl<View<FormStep>> mailingStep = addNode( formPart( "MailingAddress", "mailing" ) );
        sequence( start, newUser );
        sequence( nameStep, primaryStep );
        sequence( primaryStep, mailingStep );
        containment( newUser, multi );
        containment( multi, nameStep );
        containment( multi, primaryStep );
        containment( multi, mailingStep );

        final AppFlow<Unit, Command<FormOperation, User>> flow = (AppFlow<Unit, Command<FormOperation, User>>) interpreter.convert( graph );
        assertNotNull( flow );
        final User expected = new User();
        expected.setName( new org.kie.appformer.flowset.interpeter.res.Name() );
        expected.setPrimary( new Address() );
        expected.setMailing( new Address() );
        expected.getName().setFirst( "John" );
        expected.getName().setLast( "Doe" );
        expected.getPrimary().setNumber( "123" );
        expected.getPrimary().setStreet( "Fake Street" );
        expected.getMailing().setStreet( "Penny Lane" );
        expected.getMailing().setNumber( "321" );
        final Command<FormOperation, User> observed = executeSynchronously( flow );
        assertEquals( SUBMIT, observed.commandType );
        assertEquals( expected.toString(), observed.value.toString() );
    }

    @Test
    @SuppressWarnings( "unchecked" )
    public void multiStepFormEarlyCancel() throws Exception {
        /*
         * Setup. Graph looks like:
         *            ---------------------------------------------
         *            |                New User                   |
         *            | ----------------------------------------- |
         *   start -> | | name -> primaryAddress -> mailAddress | |
         *            | ----------------------------------------- |
         *            ---------------------------------------------
         */
        flowParts.put( "New:User", flowFactory.buildFromSupplier( User::new ) );
        final TestUIComponent<org.kie.appformer.flowset.interpeter.res.Name> nameUI =
                new TestUIComponent<>( n -> {
                    n.setFirst( "John" );
                    n.setLast( "Doe" );
                    return new Command<>( NEXT, n );
                } );
        final TestUIComponent<Address> primaryUI = new TestUIComponent<>( a -> {
            return new Command<>( CANCEL, a );
        } );
        final TestUIComponent<Address> mailUI = new TestUIComponent<>( a -> {
            a.setStreet( "Penny Lane" );
            a.setNumber( "321" );
            return new Command<>( SUBMIT, a );
        } );
        formSteps.put( "Name", nameUI );
        // In a real run these would be the same component, but need to distinguish for testing.
        formSteps.put( "PrimaryAddress", primaryUI );
        formSteps.put( "MailingAddress", mailUI );
        final NodeImpl<View<StartNoneEvent>> start = addNode( start() );
        final NodeImpl<View<RootStep>> newUser = addNode( rootStep( "New", "User" ) );
        final NodeImpl<View<MultiStep>> multi = addNode( multiStep() );
        final NodeImpl<View<FormStep>> nameStep = addNode( formPart( "Name", "name" ) );
        final NodeImpl<View<FormStep>> primaryStep = addNode( formPart( "PrimaryAddress", "primary" ) );
        final NodeImpl<View<FormStep>> mailingStep = addNode( formPart( "MailingAddress", "mailing" ) );
        sequence( start, newUser );
        sequence( nameStep, primaryStep );
        sequence( primaryStep, mailingStep );
        containment( newUser, multi );
        containment( multi, nameStep );
        containment( multi, primaryStep );
        containment( multi, mailingStep );

        final AppFlow<Unit, Command<FormOperation, User>> flow = (AppFlow<Unit, Command<FormOperation, User>>) interpreter.convert( graph );
        assertNotNull( flow );
        final User expected = new User();
        expected.setName( new org.kie.appformer.flowset.interpeter.res.Name() );
        expected.setPrimary( new Address() );
        expected.setMailing( new Address() );
        expected.getName().setFirst( "John" );
        expected.getName().setLast( "Doe" );
        expected.getPrimary().setNumber( null );
        expected.getPrimary().setStreet( null );
        expected.getMailing().setStreet( null );
        expected.getMailing().setNumber( null );
        final Command<FormOperation, User> observed = executeSynchronously( flow );
        assertEquals( CANCEL, observed.commandType );
        assertEquals( expected.toString(), observed.value.toString() );
    }

    @Test
    @SuppressWarnings( "unchecked" )
    public void multiStepFormBackAndForth() throws Exception {
        /*
         * Setup. Graph looks like:
         *            ---------------------------------------------
         *            |                New User                   |
         *            | ----------------------------------------- |
         *   start -> | | name -> primaryAddress -> mailAddress | |
         *            | ----------------------------------------- |
         *            ---------------------------------------------
         */
        flowParts.put( "New:User", flowFactory.buildFromSupplier( User::new ) );
        final TestUIComponent<org.kie.appformer.flowset.interpeter.res.Name> nameUI =
                new TestUIComponent<>( n -> {
                    n.setFirst( "John" );
                    n.setLast( "Doe" );
                    return new Command<>( NEXT, n );
                } );
        final TestUIComponent<Address> primaryUI = new TestUIComponent<>( a -> {
            a.setStreet( "Fake Avenue" );
            a.setNumber( "456" );
            return new Command<>( NEXT, a );
        },
        a -> {
            a.setStreet( "Fake Street" );
            a.setNumber( "123" );
            return new Command<>( NEXT, a );
        } );
        final TestUIComponent<Address> mailUI = new TestUIComponent<>( a -> {
            return new Command<>( PREVIOUS, a );
        }, a -> {
            a.setStreet( "Penny Lane" );
            a.setNumber( "321" );
            return new Command<>( SUBMIT, a );
        } );
        formSteps.put( "Name", nameUI );
        // In a real run these would be the same component, but need to distinguish for testing.
        formSteps.put( "PrimaryAddress", primaryUI );
        formSteps.put( "MailingAddress", mailUI );
        final NodeImpl<View<StartNoneEvent>> start = addNode( start() );
        final NodeImpl<View<RootStep>> newUser = addNode( rootStep( "New", "User" ) );
        final NodeImpl<View<MultiStep>> multi = addNode( multiStep() );
        final NodeImpl<View<FormStep>> nameStep = addNode( formPart( "Name", "name" ) );
        final NodeImpl<View<FormStep>> primaryStep = addNode( formPart( "PrimaryAddress", "primary" ) );
        final NodeImpl<View<FormStep>> mailingStep = addNode( formPart( "MailingAddress", "mailing" ) );
        sequence( start, newUser );
        sequence( nameStep, primaryStep );
        sequence( primaryStep, mailingStep );
        containment( newUser, multi );
        containment( multi, nameStep );
        containment( multi, primaryStep );
        containment( multi, mailingStep );

        final AppFlow<Unit, Command<FormOperation, User>> flow = (AppFlow<Unit, Command<FormOperation, User>>) interpreter.convert( graph );
        assertNotNull( flow );
        final User expected = new User();
        expected.setName( new org.kie.appformer.flowset.interpeter.res.Name() );
        expected.setPrimary( new Address() );
        expected.setMailing( new Address() );
        expected.getName().setFirst( "John" );
        expected.getName().setLast( "Doe" );
        expected.getPrimary().setNumber( "123" );
        expected.getPrimary().setStreet( "Fake Street" );
        expected.getMailing().setStreet( "Penny Lane" );
        expected.getMailing().setNumber( "321" );
        final Command<FormOperation, User> observed = executeSynchronously( flow );
        assertEquals( SUBMIT, observed.commandType );
        assertEquals( expected.toString(), observed.value.toString() );
    }

    @Test
    @SuppressWarnings( "unchecked" )
    public void multiStepFormWithStepAfter() throws Exception {
        /*
         * Setup. Graph looks like:
         *            ---------------------------------------------
         *            |                New User                   |
         *            | ----------------------------------------- |
         *   start -> | | name -> primaryAddress -> mailAddress | | -> extractUser
         *            | ----------------------------------------- |
         *            ---------------------------------------------
         */
        flowParts.put( "New:User", flowFactory.buildFromSupplier( User::new ) );
        flowParts.put( "extractUser", flowFactory.buildFromFunction( (final Command<?, User> c) -> c.value ) );
        final TestUIComponent<org.kie.appformer.flowset.interpeter.res.Name> nameUI =
                new TestUIComponent<>( n -> {
                    n.setFirst( "John" );
                    n.setLast( "Doe" );
                    return new Command<>( NEXT, n );
                } );
        final TestUIComponent<Address> primaryUI = new TestUIComponent<>( a -> {
            a.setStreet( "Fake Street" );
            a.setNumber( "123" );
            return new Command<>( NEXT, a );
        } );
        final TestUIComponent<Address> mailUI = new TestUIComponent<>( a -> {
            a.setStreet( "Penny Lane" );
            a.setNumber( "321" );
            return new Command<>( SUBMIT, a );
        } );
        formSteps.put( "Name", nameUI );
        // In a real run these would be the same component, but need to distinguish for testing.
        formSteps.put( "PrimaryAddress", primaryUI );
        formSteps.put( "MailingAddress", mailUI );
        final NodeImpl<View<StartNoneEvent>> start = addNode( start() );
        final NodeImpl<View<RootStep>> newUser = addNode( rootStep( "New", "User" ) );
        final NodeImpl<View<MultiStep>> multi = addNode( multiStep() );
        final NodeImpl<View<FormStep>> nameStep = addNode( formPart( "Name", "name" ) );
        final NodeImpl<View<FormStep>> primaryStep = addNode( formPart( "PrimaryAddress", "primary" ) );
        final NodeImpl<View<FormStep>> mailingStep = addNode( formPart( "MailingAddress", "mailing" ) );
        final NodeImpl<View<DataStep>> extractUser = addNode( dataStepWithName( "extractUser" ) );
        sequence( start, newUser );
        sequence( nameStep, primaryStep );
        sequence( primaryStep, mailingStep );
        containment( newUser, multi );
        containment( multi, nameStep );
        containment( multi, primaryStep );
        containment( multi, mailingStep );
        sequence( newUser, extractUser );

        final AppFlow<Unit, User> flow = (AppFlow<Unit, User>) interpreter.convert( graph );
        assertNotNull( flow );
        final User expected = new User();
        expected.setName( new org.kie.appformer.flowset.interpeter.res.Name() );
        expected.setPrimary( new Address() );
        expected.setMailing( new Address() );
        expected.getName().setFirst( "John" );
        expected.getName().setLast( "Doe" );
        expected.getPrimary().setNumber( "123" );
        expected.getPrimary().setStreet( "Fake Street" );
        expected.getMailing().setStreet( "Penny Lane" );
        expected.getMailing().setNumber( "321" );
        final User observed = executeSynchronously( flow );
        assertEquals( expected.toString(), observed.toString() );
    }

    @Test
    @SuppressWarnings( "unchecked" )
    public void rootStepWithSingleFormStep() throws Exception {
        /*
         * Setup. Graph looks like:
         *            ---------------
         *            | New Address |
         *            ---------------
         *   start -> |    name     |
         *            ---------------
         */
        flowParts.put( "New:Address", flowFactory.buildFromSupplier( Address::new ) );
        final TestUIComponent<Address> addressUI =
                new TestUIComponent<>( a -> {
                    a.setStreet( "Fake Street" );
                    a.setNumber( "123" );
                    return new Command<>( SUBMIT, a );
                } );
        formSteps.put( "Address", addressUI );
        final NodeImpl<View<StartNoneEvent>> start = addNode( start() );
        final NodeImpl<View<RootStep>> newAddress = addNode( rootStep( "New", "Address" ) );
        final NodeImpl<View<FormStep>> addressStep = addNode( formPart( "Address", "whatever" ) );
        sequence( start, newAddress );
        containment( newAddress, addressStep );

        final AppFlow<Unit, Command<FormOperation, Address>> flow = (AppFlow<Unit, Command<FormOperation, Address>>) interpreter.convert( graph );
        assertNotNull( flow );
        final Address expected = new Address();
        expected.setStreet( "Fake Street" );
        expected.setNumber( "123" );
        final Command<FormOperation, Address> observed = executeSynchronously( flow );
        assertEquals( SUBMIT, observed.commandType );
        assertEquals( expected.toString(), observed.value.toString() );
    }

    private FormStep formPart( final String entityType, final String property ) {
        final FormStep part = new FormStep.FormPartBuilder().build();
        part.setName( new Name( property ) );
        part.setEntityType( new Type( entityType ) );

        return part;
    }

    private MultiStep multiStep() {
        final MultiStep multiStep = new MultiStep.MultiStepBuilder().build();
        return multiStep;
    }

    private JoinGateway join() {
        return new JoinGateway.JoinGatewayBuilder().build();
    }

    private MatcherStep matcherWithOperation( final Enum<?> op ) {
        final MatcherStep matcher = new MatcherStep.MatcherGatewayBuilder().build();
        matcher.getGeneral().getName().setValue( op.name() );
        matcher.setOperation( new MatchedOperation( op.getClass().getSimpleName() ) );
        return matcher;
    }

    private DecisionGateway decision() {
        return new DecisionGateway.DecisionGatewayBuilder().build();
    }

    private StartNoneEvent start() {
        return new StartNoneEvent.StartNoneEventBuilder().build();
    }

    private DataStep dataStepWithName( final String name ) {
        final DataStep part = new DataStep.FlowPartBuilder().build();
        part.setName( new Name( name ) );
        return part;
    }

    private DataStep dataStep( final String name, final String type ) {
        final DataStep part = new DataStep.FlowPartBuilder().build();
        part.setName( new Name( name ) );
        part.setEntityType( new Type( type ) );
        return part;
    }

    private RootStep rootStep( final String operation, final String type ) {
        final RootStep step = new RootStep.FlowPartBuilder().build();
        step.setName( new Name( operation ) );
        step.setEntityType( new Type( type ) );

        return step;
    }

    private <T> NodeImpl<View<T>> addNode( final T content ) {
        final NodeImpl<View<T>> node = new NodeImpl<>( Long.toString( rand.nextLong() ) );
        final View<T> view = new ViewImpl<>( content, null );
        node.setContent( view );
        graph.addNode( node );
        return node;
    }

    private void sequence( final NodeImpl<?> source, final NodeImpl<?> target ) {
        final EdgeImpl<View<SequenceFlow>> edgeImpl = new EdgeImpl<>( Long.toString( rand.nextLong() ) );
        edgeImpl.setContent( new ViewImpl<>( new SequenceFlow(), null ) );
        addEdge( source, target, edgeImpl );
    }

    private void addEdge( final NodeImpl<?> source,
                            final NodeImpl<?> target,
                            final EdgeImpl<?> edgeImpl ) {
        edgeImpl.setSourceNode( source );
        edgeImpl.setTargetNode( target );
        source.getOutEdges().add( edgeImpl );
        target.getInEdges().add( edgeImpl );
    }

    private void containment( final NodeImpl<?> parent, final NodeImpl<?> child ) {
        final EdgeImpl<Child> edgeImpl = new EdgeImpl<>( Long.toString( rand.nextLong() ) );
        edgeImpl.setContent( new Child() );
        addEdge( parent, child, edgeImpl );
    }

    private <O> O executeSynchronously( final AppFlow<Unit, O> flow ) {
        return executeSynchronously( Unit.INSTANCE, flow );
    }

    private <I, O> O executeSynchronously( final I input, final AppFlow<I, O> flow ) {
        class Ref {
            O t;
        }
        final Ref ref = new Ref();
        executor.execute( input, flow, t -> ref.t = t );
        if (ref.t != null) {
            return ref.t;
        }
        else {
            throw new AssertionError( "Given flow did not execute synchronously." );
        }
    }

    public static enum Choice {
        ONE, TWO
    }

}
