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

package org.kie.appformer.formmodeler.rendering.client.view.util;

import java.util.List;

import org.gwtbootstrap3.client.ui.ValueListBox;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.enterprise.client.jaxrs.api.RestClient;
import org.kie.appformer.formmodeler.rendering.client.shared.AppFormerRestService;

/**
 * @author Pere Fernandez <pefernan@redhat.com>
 */
public abstract class RemoteListBoxValuesProvider<T> implements ListBoxValuesProvider<T> {

    @Override
    public void loadValues( final ValueListBox<T> valueListBox ) {
        RestClient.create( getRemoteServiceClass(), new RemoteCallback<List<T>>() {
            @Override
            public void callback( List<T> values ) {
                if ( values != null && values.contains( valueListBox.getValue() ) ) {
                    values.remove( valueListBox.getValue() );
                }
                valueListBox.setAcceptableValues( values );
            }
        } ).load();
    }

    protected abstract Class<? extends AppFormerRestService<T>> getRemoteServiceClass();
}
