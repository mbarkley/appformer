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


package org.kie.appformer.flowset.api.definition;

import org.kie.appformer.flowset.api.definition.property.background.BackgroundSet;
import org.kie.appformer.flowset.api.definition.property.dimensions.RectangleDimensionsSet;
import org.kie.appformer.flowset.api.definition.property.font.FontSet;
import org.kie.appformer.flowset.api.definition.property.general.Name;
import org.kie.appformer.flowset.api.definition.property.general.Type;
import org.kie.workbench.common.forms.adf.definitions.annotations.FormField;
import org.kie.workbench.common.forms.adf.definitions.annotations.field.selector.SelectorDataProvider;
import org.kie.workbench.common.forms.adf.definitions.annotations.field.selector.SelectorDataProvider.ProviderType;
import org.kie.workbench.common.forms.fields.shared.fieldTypes.basic.selectors.listBox.type.ListBoxFieldType;
import org.kie.workbench.common.stunner.core.definition.annotation.Property;

public abstract class EntityStep extends BaseStep {

    @Property
    @FormField(type = ListBoxFieldType.class, afterElement = "name")
    @SelectorDataProvider(
                          className = "org.kie.appformer.flowset.backend.EntityProvider",
                          type = ProviderType.REMOTE
                         )

    protected Type entityType;
    protected EntityStep() {
    }

    public EntityStep( final Type entityType,
                       final BackgroundSet backgroundSet,
                       final FontSet fontSet,
                       final RectangleDimensionsSet dimensionsSet ) {
        super( backgroundSet,
               fontSet,
               dimensionsSet );
        this.entityType = entityType;
    }

    public abstract void setName( final Name name );

    public abstract Name getName();

    public Type getEntityType() {
        return entityType;
    }

    public void setEntityType( final Type entityType ) {
        this.entityType = entityType;
    }
}
