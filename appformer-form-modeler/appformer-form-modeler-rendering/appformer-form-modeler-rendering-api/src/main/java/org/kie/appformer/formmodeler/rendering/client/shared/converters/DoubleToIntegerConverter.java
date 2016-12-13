/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
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

package org.kie.appformer.formmodeler.rendering.client.shared.converters;

import org.jboss.errai.databinding.client.api.Converter;
import org.jboss.errai.databinding.client.api.DefaultConverter;

/**
 * @author Pere Fernandez <pefernan@redhat.com>
 */
@DefaultConverter
public class DoubleToIntegerConverter implements Converter<Double, Integer> {
    @Override
    public Class<Double> getModelType() {
        return Double.class;
    }

    @Override
    public Class<Integer> getComponentType() {
        return Integer.class;
    }

    @Override
    public Double toModelValue( Integer aInteger ) {
        if ( aInteger == null ) {
            return null;
        }
        return aInteger.doubleValue();
    }

    @Override
    public Integer toWidgetValue( Double aDouble ) {
        if ( aDouble == null ) {
            return null;
        }
        return aDouble.intValue();
    }
}
