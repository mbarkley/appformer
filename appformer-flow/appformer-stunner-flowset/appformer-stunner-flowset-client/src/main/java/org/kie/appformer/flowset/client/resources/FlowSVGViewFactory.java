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

package org.kie.appformer.flowset.client.resources;

import org.kie.workbench.common.stunner.svg.annotation.SVGSource;
import org.kie.workbench.common.stunner.svg.annotation.SVGViewFactory;
import org.kie.workbench.common.stunner.svg.client.shape.view.SVGShapeView;

@SVGViewFactory
public interface FlowSVGViewFactory {

    String START = "images/shapes/start.svg";
    String ROOT_STEP = "images/shapes/root-step.svg";
    String DATA_STEP = "images/shapes/data-step.svg";
    String FORM_STEP = "images/shapes/form-step.svg";
    String MULTI_STEP = "images/shapes/multi-step.svg";
    String DECISION = "images/shapes/decision.svg";
    String JOIN = "images/shapes/join.svg";

    @SVGSource(START)
    SVGShapeView start(final double width,
                       final double height,
                       final boolean resizable);

    @SVGSource(ROOT_STEP)
    SVGShapeView rootStep(final double width,
                          final double height,
                          final boolean resizable);

    @SVGSource(DATA_STEP)
    SVGShapeView dataStep(final double width,
                          final double height,
                          final boolean resizable);

    @SVGSource(FORM_STEP)
    SVGShapeView formStep(final double width,
                          final double height,
                          final boolean resizable);

    @SVGSource(MULTI_STEP)
    SVGShapeView multiStep(final double width,
                           final double height,
                           final boolean resizable);

    @SVGSource(DECISION)
    SVGShapeView decision(final double width,
                          final double height,
                          final boolean resizable);

    @SVGSource(JOIN)
    SVGShapeView join(final double width,
                      final double height,
                      final boolean resizable);
}
