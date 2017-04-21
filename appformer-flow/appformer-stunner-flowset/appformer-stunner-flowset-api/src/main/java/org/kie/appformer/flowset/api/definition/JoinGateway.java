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
import org.kie.appformer.flowset.api.definition.property.dimensions.CircleDimensionSet;
import org.kie.appformer.flowset.api.definition.property.dimensions.Radius;
import org.kie.appformer.flowset.api.definition.property.font.FontBorderSize;
import org.kie.appformer.flowset.api.definition.property.font.FontColor;
import org.kie.appformer.flowset.api.definition.property.font.FontFamily;
import org.kie.appformer.flowset.api.definition.property.font.FontSet;
import org.kie.appformer.flowset.api.definition.property.font.FontSize;
import org.kie.appformer.flowset.api.definition.property.general.Name;
import org.kie.workbench.common.forms.adf.definitions.annotations.FormDefinition;
import org.kie.workbench.common.forms.adf.definitions.settings.FieldPolicy;
import org.kie.workbench.common.stunner.core.definition.annotation.Definition;
import org.kie.workbench.common.stunner.core.definition.annotation.Description;
import org.kie.workbench.common.stunner.core.definition.annotation.definition.Title;
import org.kie.workbench.common.stunner.core.factory.graph.NodeFactory;

@Portable
@Bindable
@Definition(graphFactory = NodeFactory.class, builder = JoinGateway.JoinGatewayBuilder.class)
@FormDefinition(
        policy = FieldPolicy.ONLY_MARKED
)
public class JoinGateway extends BaseGateway {

    @Title
    public static final transient String title = "Join Gateway";

    @Description
    public static final transient String description = "Join Gateway";

    @NonPortable
    public static class JoinGatewayBuilder extends BaseGatewayBuilder<JoinGateway> {

        @Override
        protected String[] getAdditionalLabels() {
            return new String[]{ "fan_in", "linear_out" };
        }

        @Override
        protected JoinGateway doBuild() {
            return  new JoinGateway(new Name(""),
                                    new BackgroundSet("#CCC",
                                                      "#CCC",
                                                      BORDER_SIZE),
                                    new FontSet(FontFamily.defaultValue,
                                                FontColor.defaultValue,
                                                FontSize.defaultValue,
                                                FontBorderSize.defaultValue),
                                    new CircleDimensionSet(new Radius(15d)));
        }

    }

    public JoinGateway() {
    }

    public JoinGateway(final @MapsTo("name") Name name,
                           final @MapsTo("backgroundSet") BackgroundSet backgroundSet,
                           final @MapsTo("fontSet") FontSet fontSet,
                           final @MapsTo("dimensionsSet") CircleDimensionSet dimensionsSet) {
        super(name,
              backgroundSet,
              fontSet,
              dimensionsSet);
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }
}
