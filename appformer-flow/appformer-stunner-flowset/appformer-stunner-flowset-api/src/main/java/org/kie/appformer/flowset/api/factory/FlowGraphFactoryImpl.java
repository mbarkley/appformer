/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.appformer.flowset.api.factory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.kie.appformer.flowset.api.definition.StartNoneEvent;
import org.kie.workbench.common.stunner.core.api.DefinitionManager;
import org.kie.workbench.common.stunner.core.api.FactoryManager;
import org.kie.workbench.common.stunner.core.command.impl.CompositeCommandImpl;
import org.kie.workbench.common.stunner.core.factory.graph.ElementFactory;
import org.kie.workbench.common.stunner.core.factory.impl.AbstractGraphFactory;
import org.kie.workbench.common.stunner.core.graph.Edge;
import org.kie.workbench.common.stunner.core.graph.Graph;
import org.kie.workbench.common.stunner.core.graph.Node;
import org.kie.workbench.common.stunner.core.graph.command.EmptyRulesCommandExecutionContext;
import org.kie.workbench.common.stunner.core.graph.command.GraphCommandExecutionContext;
import org.kie.workbench.common.stunner.core.graph.command.GraphCommandManager;
import org.kie.workbench.common.stunner.core.graph.command.impl.GraphCommandFactory;
import org.kie.workbench.common.stunner.core.graph.content.definition.Definition;
import org.kie.workbench.common.stunner.core.graph.content.definition.DefinitionSet;
import org.kie.workbench.common.stunner.core.graph.processing.index.GraphIndexBuilder;
import org.kie.workbench.common.stunner.core.graph.processing.index.Index;
import org.kie.workbench.common.stunner.core.rule.RuleManager;
import org.kie.workbench.common.stunner.core.util.UUID;

/**
 * The custom factory for Flow graphs.
 * It initializes the Flow graph with a new Diagram node instance, which represents the main process.
 * This class uses the Commands API in order to avoid adding nodes/edges and setting bean values manually on the graph structure,
 * so this will avoid further errors, but in fact there should be not need to check runtime rules when executing
 * these commands.
 */
@ApplicationScoped
public class FlowGraphFactoryImpl
        extends AbstractGraphFactory
        implements FlowGraphFactory {

    private static final double INITIAL_X = 50d;
    private static final double INITIAL_Y = 50d;

    private final DefinitionManager definitionManager;
    private final GraphCommandManager graphCommandManager;
    private final GraphCommandFactory graphCommandFactory;
    private final FactoryManager factoryManager;
    private final GraphIndexBuilder<?> indexBuilder;
    private final RuleManager ruleManager;

    protected FlowGraphFactoryImpl() {
        this(null,
             null,
             null,
             null,
             null,
             null);
    }

    @Override
    protected double getWidth() {
        return FlowGraphFactory.GRAPH_DEFAULT_WIDTH;
    }

    @Override
    protected double getHeight() {
        return FlowGraphFactory.GRAPH_DEFAULT_HEIGHT;
    }

    @Inject
    public FlowGraphFactoryImpl(final DefinitionManager definitionManager,
                                final FactoryManager factoryManager,
                                final GraphCommandManager graphCommandManager,
                                final GraphCommandFactory graphCommandFactory,
                                final GraphIndexBuilder<?> indexBuilder,
                                final RuleManager ruleManager) {
        this.definitionManager = definitionManager;
        this.factoryManager = factoryManager;
        this.graphCommandManager = graphCommandManager;
        this.graphCommandFactory = graphCommandFactory;
        this.indexBuilder = indexBuilder;
        this.ruleManager = ruleManager;
    }

    @Override
    public Class<? extends ElementFactory> getFactoryType() {
        return FlowGraphFactory.class;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Graph<DefinitionSet, Node> build(final String uuid,
                                            final String definitionSetId) {
        final Graph<DefinitionSet, Node> graph = super.build(uuid,
                                                             definitionSetId);
        final Node<Definition<StartNoneEvent>, Edge> startEventNode =
                (Node<Definition<StartNoneEvent>, Edge>) factoryManager.newElement(UUID.uuid(),
                                                                                   StartNoneEvent.class);
        // Append an initial node and update its shape view's  location close to the canvas top left.
        graphCommandManager.execute(createGraphContext(graph),
                                    new CompositeCommandImpl.CompositeCommandBuilder()
                                            .addCommand(graphCommandFactory.addNode(startEventNode))
                                            .addCommand(graphCommandFactory.updatePosition(startEventNode,
                                                                                           INITIAL_X,
                                                                                           INITIAL_Y))
                                            .build()
        );
        return graph;
    }

    @SuppressWarnings("unchecked")
    private GraphCommandExecutionContext createGraphContext(final Graph<DefinitionSet, Node> graph) {
        final Index<?, ?> index = (Index) indexBuilder.build(graph);
        return new EmptyRulesCommandExecutionContext(
                definitionManager,
                factoryManager,
                ruleManager,
                index);
    }

    @Override
    public boolean accepts(final String source) {
        return true;
    }

    @Override
    protected DefinitionManager getDefinitionManager() {
        return definitionManager;
    }
}

