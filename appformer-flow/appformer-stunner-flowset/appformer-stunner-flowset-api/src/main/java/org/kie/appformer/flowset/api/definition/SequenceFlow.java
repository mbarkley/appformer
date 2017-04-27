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

import java.util.Set;

import org.jboss.errai.common.client.api.annotations.MapsTo;
import org.jboss.errai.common.client.api.annotations.NonPortable;
import org.jboss.errai.common.client.api.annotations.Portable;
import org.jboss.errai.databinding.client.api.Bindable;
import org.kie.appformer.flowset.api.definition.property.background.BackgroundSet;
import org.kie.appformer.flowset.api.definition.property.font.FontSet;
import org.kie.appformer.flowset.api.definition.property.general.FlowGeneralSet;
import org.kie.workbench.common.forms.adf.definitions.annotations.FormDefinition;
import org.kie.workbench.common.stunner.core.definition.annotation.Definition;
import org.kie.workbench.common.stunner.core.definition.annotation.definition.Title;
import org.kie.workbench.common.stunner.core.factory.graph.EdgeFactory;
import org.kie.workbench.common.stunner.core.rule.annotation.CanConnect;
import org.kie.workbench.common.stunner.core.rule.annotation.EdgeOccurrences;

@Portable
@Bindable
@Definition(graphFactory = EdgeFactory.class, builder = SequenceFlow.SequenceFlowBuilder.class)
// *** Connection rules for sequence flows ****
@CanConnect(startRole = "sequenceable", endRole = "sequenceable")
// **** Cardinality rules for connectors ****
@EdgeOccurrences(role = "fan_out", type = EdgeOccurrences.EdgeType.OUTGOING, max = -1)
@EdgeOccurrences(role = "fan_in", type = EdgeOccurrences.EdgeType.INCOMING, max = -1)
@EdgeOccurrences(role = "linear_out", type = EdgeOccurrences.EdgeType.OUTGOING, max = 1)
@EdgeOccurrences(role = "linear_in", type = EdgeOccurrences.EdgeType.INCOMING, max = 1)
@EdgeOccurrences(role = "start", type = EdgeOccurrences.EdgeType.INCOMING, max = 0)
@FormDefinition(
        startElement = "general"
)
public class SequenceFlow extends BaseConnector {

    @Title
    public static final transient String title = "Sequence Flow";

    @NonPortable
    public static class SequenceFlowBuilder extends BaseConnectorBuilder<SequenceFlow> {

        @Override
        public SequenceFlow build() {
            return new SequenceFlow(new FlowGeneralSet("Sequence"),
                                    new BackgroundSet("#CCC",
                                                      "#CCC",
                                                      BORDER_SIZE),
                                    new FontSet());
        }
    }

    public SequenceFlow() {
    }

    public SequenceFlow(final @MapsTo("general") FlowGeneralSet general,
                        final @MapsTo("backgroundSet") BackgroundSet backgroundSet,
                        final @MapsTo("fontSet") FontSet fontSet) {
        super(general,
              backgroundSet,
              fontSet);
    }

    @Override
    public String getCategory() {
        return category;
    }

    public String getTitle() {
        return title;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public Set<String> getLabels() {
        return labels;
    }

    @Override
    public FlowGeneralSet getGeneral() {
        return general;
    }

    @Override
    public BackgroundSet getBackgroundSet() {
        return backgroundSet;
    }

    @Override
    public void setGeneral(final FlowGeneralSet general) {
        this.general = general;
    }

    @Override
    public void setBackgroundSet(final BackgroundSet backgroundSet) {
        this.backgroundSet = backgroundSet;
    }
}
