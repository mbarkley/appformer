/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
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
package org.kie.appformer.flowset.api.definition;

import java.util.HashSet;
import java.util.Set;

import javax.validation.Valid;

import org.jboss.errai.common.client.api.annotations.NonPortable;
import org.kie.appformer.flowset.api.definition.property.background.BackgroundSet;
import org.kie.appformer.flowset.api.definition.property.dimensions.RectangleDimensionsSet;
import org.kie.appformer.flowset.api.definition.property.font.FontSet;
import org.kie.workbench.common.forms.adf.definitions.annotations.FormField;
import org.kie.workbench.common.stunner.core.definition.annotation.Description;
import org.kie.workbench.common.stunner.core.definition.annotation.PropertySet;
import org.kie.workbench.common.stunner.core.definition.annotation.definition.Labels;
import org.kie.workbench.common.stunner.core.definition.builder.Builder;

public abstract class BaseStep implements FlowDefinition {

    @Description
    public static final transient String description = "A task is a unit of work - the job to be performed";

    @PropertySet
    @FormField(
            afterElement = "general"
    )
    @Valid
    protected BackgroundSet backgroundSet;

    @FormField(
               afterElement = "backgroundSet"
    )
    @PropertySet
    protected FontSet fontSet;

    @PropertySet
    protected RectangleDimensionsSet dimensionsSet;

    @Labels
    protected final Set<String> labels = new HashSet<String>() {{
        add("all");
        add("sequenceable");
        add("linear_in");
        add("linear_out");
    }};

    @NonPortable
    static abstract class BasePartBuilder<T extends BaseStep> implements Builder<T> {

        public static final String COLOR = "#f9fad2";
        public static final Double WIDTH = 136d;
        public static final Double HEIGHT = 48d;
        public static final Double BORDER_SIZE = 0d;
        public static final String BORDER_COLOR = "#000000";
    }

    protected BaseStep() {
    }

    public BaseStep(final BackgroundSet backgroundSet,
                    final FontSet fontSet,
                    final RectangleDimensionsSet dimensionsSet) {
        this.backgroundSet = backgroundSet;
        this.fontSet = fontSet;
        this.dimensionsSet = dimensionsSet;
    }

    public abstract String getCategory();

    public String getDescription() {
        return description;
    }

    public Set<String> getLabels() {
        return labels;
    }

    public BackgroundSet getBackgroundSet() {
        return backgroundSet;
    }

    public FontSet getFontSet() {
        return fontSet;
    }

    public void setBackgroundSet(final BackgroundSet backgroundSet) {
        this.backgroundSet = backgroundSet;
    }

    public void setFontSet(final FontSet fontSet) {
        this.fontSet = fontSet;
    }

    public RectangleDimensionsSet getDimensionsSet() {
        return dimensionsSet;
    }

    public void setDimensionsSet(final RectangleDimensionsSet dimensionsSet) {
        this.dimensionsSet = dimensionsSet;
    }
}
