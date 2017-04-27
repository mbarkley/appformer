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

package org.kie.appformer.flowset.client.shape.factory;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.kie.appformer.flowset.api.definition.DataStep;
import org.kie.appformer.flowset.api.definition.DecisionGateway;
import org.kie.appformer.flowset.api.definition.FormStep;
import org.kie.appformer.flowset.api.definition.JoinGateway;
import org.kie.appformer.flowset.api.definition.MatcherStep;
import org.kie.appformer.flowset.api.definition.MultiStep;
import org.kie.appformer.flowset.api.definition.RootStep;
import org.kie.appformer.flowset.api.definition.SequenceFlow;
import org.kie.appformer.flowset.api.definition.StartNoneEvent;
import org.kie.appformer.flowset.client.shape.def.DataStepShapeDef;
import org.kie.appformer.flowset.client.shape.def.FormStepShapeDef;
import org.kie.appformer.flowset.client.shape.def.JoinShapeDef;
import org.kie.appformer.flowset.client.shape.def.DecisionShapeDef;
import org.kie.appformer.flowset.client.shape.def.MultiStepShapeDef;
import org.kie.appformer.flowset.client.shape.def.RootStepShapeDef;
import org.kie.appformer.flowset.client.shape.def.SequenceFlowConnectorDef;
import org.kie.appformer.flowset.client.shape.def.StartEventShapeDef;
import org.kie.workbench.common.stunner.core.api.DefinitionManager;
import org.kie.workbench.common.stunner.core.client.canvas.AbstractCanvasHandler;
import org.kie.workbench.common.stunner.core.client.shape.Shape;
import org.kie.workbench.common.stunner.core.client.shape.factory.DelegateShapeFactory;
import org.kie.workbench.common.stunner.core.client.shape.view.ShapeView;
import org.kie.workbench.common.stunner.shapes.factory.BasicShapesFactory;
import org.kie.workbench.common.stunner.svg.client.shape.factory.SVGShapeFactory;

@ApplicationScoped
public class FlowSVGShapeFactory extends DelegateShapeFactory<Object, AbstractCanvasHandler, Shape<ShapeView<?>>> {

    private final DefinitionManager  definitionManager;
    private final SVGShapeFactory    svgShapeFactory;
    private final BasicShapesFactory basicShapesFactory;

    protected FlowSVGShapeFactory() {
        this( null,
              null,
              null );
    }

    @Inject
    public FlowSVGShapeFactory( final DefinitionManager definitionManager,
                                final SVGShapeFactory svgShapeFactory,
                                final BasicShapesFactory basicShapesFactory ) {
        this.definitionManager = definitionManager;
        this.svgShapeFactory = svgShapeFactory;
        this.basicShapesFactory = basicShapesFactory;
    }

    @PostConstruct
    @SuppressWarnings( "unchecked" )
    public void init() {
        // Register the factories to delegate.
        addDelegate( svgShapeFactory );
        addDelegate( basicShapesFactory );
        // Register the shapes and definitions.
        svgShapeFactory.addShapeDef( RootStep.class,
                                     new RootStepShapeDef() );
        svgShapeFactory.addShapeDef( DataStep.class,
                                     new DataStepShapeDef() );
        svgShapeFactory.addShapeDef( StartNoneEvent.class,
                                     new StartEventShapeDef() );
        svgShapeFactory.addShapeDef( FormStep.class,
                                     new FormStepShapeDef() );
        svgShapeFactory.addShapeDef( DecisionGateway.class,
                                     new DecisionShapeDef() );
        svgShapeFactory.addShapeDef( JoinGateway.class,
                                     new JoinShapeDef() );
        svgShapeFactory.addShapeDef( MatcherStep.class,
                                     new DataStepShapeDef() );
        svgShapeFactory.addShapeDef( MultiStep.class,
                                     new MultiStepShapeDef() );
        basicShapesFactory.addShapeDef( SequenceFlow.class,
                                        new SequenceFlowConnectorDef() );
    }

    @Override
    protected DefinitionManager getDefinitionManager() {
        return definitionManager;
    }
}
