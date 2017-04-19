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
package org.kie.appformer.flowset.api.definition.property.dimensions;

import javax.validation.Valid;

import org.jboss.errai.common.client.api.annotations.MapsTo;
import org.jboss.errai.common.client.api.annotations.Portable;
import org.jboss.errai.databinding.client.api.Bindable;
import org.kie.appformer.flowset.api.definition.FlowPropertySet;
import org.kie.workbench.common.forms.adf.definitions.annotations.FieldParam;
import org.kie.workbench.common.forms.adf.definitions.annotations.FormDefinition;
import org.kie.workbench.common.forms.adf.definitions.annotations.FormField;
import org.kie.workbench.common.forms.adf.definitions.annotations.metaModel.FieldLabel;
import org.kie.workbench.common.forms.fields.shared.fieldTypes.basic.slider.type.SliderFieldType;
import org.kie.workbench.common.stunner.core.definition.annotation.Name;
import org.kie.workbench.common.stunner.core.definition.annotation.Property;
import org.kie.workbench.common.stunner.core.definition.annotation.PropertySet;

@Portable
@Bindable
@PropertySet
@FormDefinition(
        startElement = "width"
)
public class RectangleDimensionsSet implements FlowPropertySet {

    @Name
    @FieldLabel
    public static final transient String propertySetName = "Shape Dimensions";

    @Property
    @FormField(
            type = SliderFieldType.class,
            settings = {
                    @FieldParam(name = "min", value = "100.0"),
                    @FieldParam(name = "max", value = "300.0"),
                    @FieldParam(name = "step", value = "10.0"),
                    @FieldParam(name = "precision", value = "0.0")
            }
    )
    @Valid
    protected Width width;

    @Property
    @FormField(
            type = SliderFieldType.class,
            afterElement = "width",
            settings = {
                    @FieldParam(name = "min", value = "40.0"),
                    @FieldParam(name = "max", value = "100.0"),
                    @FieldParam(name = "step", value = "5.0"),
                    @FieldParam(name = "precision", value = "0.0")
            }
    )
    @Valid
    protected Height height;

    public RectangleDimensionsSet() {
        this(new Width(Width.defaultValue),
             new Height(Height.defaultValue));
    }

    public RectangleDimensionsSet(final Double width,
                                  final Double height) {
        this(new Width(width),
             new Height(height));
    }

    public RectangleDimensionsSet(final @MapsTo("width") Width width,
                                  final @MapsTo("height") Height height) {
        this.width = width;
        this.height = height;
    }

    public Width getWidth() {
        return width;
    }

    public void setWidth(final Width width) {
        this.width = width;
    }

    public Height getHeight() {
        return height;
    }

    public void setHeight(final Height height) {
        this.height = height;
    }
}
