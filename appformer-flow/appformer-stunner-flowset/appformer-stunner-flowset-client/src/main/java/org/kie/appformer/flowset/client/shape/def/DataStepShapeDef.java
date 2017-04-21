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

package org.kie.appformer.flowset.client.shape.def;

import java.util.HashMap;
import java.util.Map;

import org.kie.appformer.flowset.api.definition.BaseStep;
import org.kie.appformer.flowset.api.definition.DataStep;
import org.kie.appformer.flowset.api.definition.MatcherStep;
import org.kie.appformer.flowset.api.shape.def.FlowPictures;
import org.kie.appformer.flowset.client.resources.FlowSVGViewFactory;
import org.kie.workbench.common.stunner.core.client.shape.view.HasTitle;
import org.kie.workbench.common.stunner.core.definition.shape.AbstractShapeDef;
import org.kie.workbench.common.stunner.core.definition.shape.GlyphDef;
import org.kie.workbench.common.stunner.shapes.def.picture.PictureGlyphDef;
import org.kie.workbench.common.stunner.svg.client.shape.def.SVGMutableShapeDef;
import org.kie.workbench.common.stunner.svg.client.shape.view.SVGShapeView;

public class DataStepShapeDef
        extends AbstractShapeDef<BaseStep>
        implements SVGMutableShapeDef<BaseStep, FlowSVGViewFactory> {

    @Override
    public double getAlpha(final BaseStep element) {
        return 1d;
    }

    @Override
    public String getBackgroundColor(final BaseStep element) {
        return element.getBackgroundSet().getBgColor().getValue();
    }

    @Override
    public double getBackgroundAlpha(final BaseStep element) {
        return 1;
    }

    @Override
    public String getBorderColor(final BaseStep element) {
        return element.getBackgroundSet().getBorderColor().getValue();
    }

    @Override
    public double getBorderSize(final BaseStep element) {
        return element.getBackgroundSet().getBorderSize().getValue();
    }

    @Override
    public double getBorderAlpha(final BaseStep element) {
        return 1;
    }

    @Override
    public String getFontFamily(final BaseStep element) {
        return element.getFontSet().getFontFamily().getValue();
    }

    @Override
    public String getFontColor(final BaseStep element) {
        return element.getFontSet().getFontColor().getValue();
    }

    @Override
    public double getFontSize(final BaseStep element) {
        return element.getFontSet().getFontSize().getValue();
    }

    @Override
    public double getFontBorderSize(final BaseStep element) {
        return element.getFontSet().getFontBorderSize().getValue();
    }

    @Override
    public HasTitle.Position getFontPosition(final BaseStep element) {
        return HasTitle.Position.CENTER;
    }

    @Override
    public double getFontRotation(final BaseStep element) {
        return 0;
    }

    private static final PictureGlyphDef<BaseStep, FlowPictures> TASK_GLYPH_DEF = new PictureGlyphDef<BaseStep, FlowPictures>() {

        private final Map<Class<?>, FlowPictures> PICTURES = new HashMap<Class<?>, FlowPictures>() {{
            put(DataStep.class,
                FlowPictures.DATA_STEP);
            put(MatcherStep.class,
                FlowPictures.MATCHER);
        }};

        @Override
        public String getGlyphDescription(final BaseStep element) {
            return element.getDescription();
        }

        @Override
        public FlowPictures getSource(final Class<?> type) {
            return PICTURES.get(type);
        }
    };

    @Override
    public GlyphDef<BaseStep> getGlyphDef() {
        return TASK_GLYPH_DEF;
    }

    @Override
    public double getWidth(final BaseStep element) {
        return element.getDimensionsSet().getWidth().getValue();
    }

    @Override
    public double getHeight(final BaseStep element) {
        return element.getDimensionsSet().getHeight().getValue();
    }

    @Override
    public boolean isSVGViewVisible(final String viewName,
                                    final BaseStep element) {
        return true;
    }

    @Override
    public SVGShapeView<?> newViewInstance(final FlowSVGViewFactory factory,
                                           final BaseStep task) {
        return factory.dataStep(getWidth(task),
                                getHeight(task),
                                true);
    }

    @Override
    public Class<FlowSVGViewFactory> getViewFactoryType() {
        return FlowSVGViewFactory.class;
    }
}