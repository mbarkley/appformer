/*
 * Copyright 2015 JBoss Inc
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

package org.livespark.formmodeler.rendering.client.shared.converters;

import java.util.Date;

import org.jboss.errai.databinding.client.api.Converter;
import org.jboss.errai.databinding.client.api.DefaultConverter;

import com.google.gwt.i18n.client.DateTimeFormat;


@DefaultConverter
public class DateConverter implements Converter<Date, String> {

    private static final String FORMAT = "yyyy/MM/dd";

    @Override
    public Date toModelValue( String widgetValue ) {
        return DateTimeFormat.getFormat( FORMAT ).parse( widgetValue );
    }

    @Override
    public String toWidgetValue( Date modelValue ) {
        return DateTimeFormat.getFormat( FORMAT ).format( modelValue );
    }

    @Override
    public Class<Date> getModelType() {
        return Date.class;
    }

    @Override
    public Class<String> getWidgetType() {
        return String.class;
    }

}
