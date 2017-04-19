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

import org.jboss.errai.common.client.api.annotations.MapsTo;
import org.jboss.errai.common.client.api.annotations.NonPortable;
import org.kie.appformer.flowset.api.definition.property.background.BackgroundSet;
import org.kie.appformer.flowset.api.definition.property.font.FontSet;
import org.kie.appformer.flowset.api.definition.property.general.FlowGeneralSet;
import org.kie.workbench.common.forms.adf.definitions.annotations.FormField;
import org.kie.workbench.common.stunner.core.definition.annotation.Description;
import org.kie.workbench.common.stunner.core.definition.annotation.PropertySet;
import org.kie.workbench.common.stunner.core.definition.annotation.definition.Category;
import org.kie.workbench.common.stunner.core.definition.annotation.definition.Labels;
import org.kie.workbench.common.stunner.core.definition.builder.Builder;

public abstract class BaseConnector implements FlowDefinition {

    @Category
    public static final transient String category = Categories.CONNECTING_OBJECTS;

    @Description
    public static final transient String description = "A Connecting Object";

    @PropertySet
    @FormField
    @Valid
    protected FlowGeneralSet general;

    @PropertySet
    @Valid
    protected BackgroundSet backgroundSet;

    @PropertySet
    protected FontSet fontSet;

    @NonPortable
    static abstract class BaseConnectorBuilder<T extends BaseConnector> implements Builder<BaseConnector> {

        public static final transient String COLOR = "#000000";
        public static final transient String BORDER_COLOR = "#000000";
        public static final Double BORDER_SIZE = 1d;
    }

    @Labels
    protected final Set<String> labels = new HashSet<String>() {{
        add("all");
        add("connector");
    }};

    protected BaseConnector() {
    }

    public BaseConnector(final @MapsTo("general") FlowGeneralSet general,
                         final @MapsTo("backgroundSet") BackgroundSet backgroundSet,
                         final @MapsTo("fontSet") FontSet fontSet
    ) {
        this.general = general;
        this.backgroundSet = backgroundSet;
        this.fontSet = fontSet;
    }

    public String getCategory() {
        return category;
    }

    public String getDescription() {
        return description;
    }

    public Set<String> getLabels() {
        return labels;
    }

    public FlowGeneralSet getGeneral() {
        return general;
    }

    public void setGeneral(final FlowGeneralSet general) {
        this.general = general;
    }

    public BackgroundSet getBackgroundSet() {
        return backgroundSet;
    }

    public void setBackgroundSet(final BackgroundSet backgroundSet) {
        this.backgroundSet = backgroundSet;
    }

    public FontSet getFontSet() {
        return fontSet;
    }

    public void setFontSet(final FontSet fontSet) {
        this.fontSet = fontSet;
    }
}
