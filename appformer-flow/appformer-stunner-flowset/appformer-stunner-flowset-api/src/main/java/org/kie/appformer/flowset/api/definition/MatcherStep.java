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

import org.jboss.errai.common.client.api.annotations.MapsTo;
import org.jboss.errai.common.client.api.annotations.NonPortable;
import org.jboss.errai.common.client.api.annotations.Portable;
import org.jboss.errai.databinding.client.api.Bindable;
import org.kie.appformer.flowset.api.definition.property.background.BackgroundSet;
import org.kie.appformer.flowset.api.definition.property.dimensions.RectangleDimensionsSet;
import org.kie.appformer.flowset.api.definition.property.font.FontBorderSize;
import org.kie.appformer.flowset.api.definition.property.font.FontColor;
import org.kie.appformer.flowset.api.definition.property.font.FontFamily;
import org.kie.appformer.flowset.api.definition.property.font.FontSet;
import org.kie.appformer.flowset.api.definition.property.gateway.MatchedOperation;
import org.kie.appformer.flowset.api.definition.property.general.FlowGeneralSet;
import org.kie.workbench.common.forms.adf.definitions.annotations.FormDefinition;
import org.kie.workbench.common.forms.adf.definitions.annotations.FormField;
import org.kie.workbench.common.forms.adf.definitions.settings.FieldPolicy;
import org.kie.workbench.common.forms.fields.shared.fieldTypes.basic.textBox.type.TextBoxFieldType;
import org.kie.workbench.common.stunner.core.definition.annotation.Definition;
import org.kie.workbench.common.stunner.core.definition.annotation.Description;
import org.kie.workbench.common.stunner.core.definition.annotation.Property;
import org.kie.workbench.common.stunner.core.definition.annotation.PropertySet;
import org.kie.workbench.common.stunner.core.definition.annotation.definition.Category;
import org.kie.workbench.common.stunner.core.definition.annotation.definition.Title;
import org.kie.workbench.common.stunner.core.factory.graph.NodeFactory;

@Portable
@Bindable
@Definition(graphFactory = NodeFactory.class, builder = MatcherStep.MatcherGatewayBuilder.class)
@FormDefinition(
        startElement = "general",
        policy = FieldPolicy.ONLY_MARKED
)
public class MatcherStep extends BaseStep {

    @Title
    public static final transient String title = "Decision Case";

    @Category
    public static final transient String category = Categories.FLOW;

    @Description
    public static final transient String description = "Matches a form output for a decision";

    @FormField(type = TextBoxFieldType.class)
    @Property
    private MatchedOperation operation;

    @FormField
    @PropertySet
    private FlowGeneralSet general;

    @NonPortable
    public static class MatcherGatewayBuilder extends BasePartBuilder<MatcherStep> {

        @Override
        public MatcherStep build() {
            return new MatcherStep(new FlowGeneralSet("Matcher"),
                                   new BackgroundSet("#CCC",
                                                     "#CCC",
                                                     BORDER_SIZE),
                                   new FontSet(FontFamily.defaultValue,
                                               FontColor.defaultValue,
                                               12d,
                                               FontBorderSize.defaultValue),
                                   new RectangleDimensionsSet(WIDTH, HEIGHT),
                                   new MatchedOperation("FormOperation"));
        }

    }

    public MatcherStep() {
    }

    public MatcherStep( final @MapsTo( "general" ) FlowGeneralSet general,
                        final @MapsTo( "backgroundSet" ) BackgroundSet backgroundSet,
                        final @MapsTo( "fontSet" ) FontSet fontSet,
                        final @MapsTo( "dimensionsSet" ) RectangleDimensionsSet dimensionsSet,
                        final @MapsTo( "operation" ) MatchedOperation operation ) {
        super(backgroundSet,
              fontSet,
              dimensionsSet);
        this.general = general;
        this.operation = operation;
    }

    public String getTitle() {
        return title;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public MatchedOperation getOperation() {
        return operation;
    }

    public void setOperation( final MatchedOperation operation ) {
        this.operation = operation;
    }

    public FlowGeneralSet getGeneral() {
        return general;
    }

    public void setGeneral( final FlowGeneralSet general ) {
        this.general = general;
    }

    @Override
    public String getCategory() {
        return category;
    }
}
