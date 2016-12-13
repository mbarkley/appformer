/*
 * Copyright (C) 2016 Red Hat, Inc. and/or its affiliates.
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


package org.kie.appformer.formmodeler.rendering.client.view;

import java.util.function.Consumer;

import org.kie.appformer.flow.api.UIComponent;

public class UIComponentCleanUpWrapper<INPUT, OUTPUT, COMPONENT, WRAPPED extends UIComponent<INPUT, OUTPUT, COMPONENT>>
    implements
    UIComponent<INPUT, OUTPUT, COMPONENT> {

    private final WRAPPED wrapped;
    private final Consumer<WRAPPED> destructionCallback;

    public UIComponentCleanUpWrapper( final WRAPPED wrapped, final Consumer<WRAPPED> destructionCallback ) {
        this.wrapped = wrapped;
        this.destructionCallback = destructionCallback;
    }

    @Override
    public void start( final INPUT input,
                       final Consumer<OUTPUT> callback ) {
        wrapped.start( input, callback );
    }

    @Override
    public void onHide() {
        wrapped.onHide();
        destructionCallback.accept( wrapped );
    }

    @Override
    public COMPONENT asComponent() {
        return wrapped.asComponent();
    }

    @Override
    public String getName() {
        return wrapped.getName();
    }

}
