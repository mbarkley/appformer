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


package org.livespark.flow.api;

import java.util.function.Function;
import java.util.function.Supplier;

public interface AppFlowFactory {

    void registerFlow( String id, AppFlow<?, ?> flow );

    <INPUT, OUTPUT> AppFlow<INPUT, OUTPUT> getFlow( String id );

    <INPUT, OUTPUT> AppFlow<INPUT, OUTPUT> buildFromStep( Step<INPUT, OUTPUT> step );
    <INPUT, OUTPUT> AppFlow<INPUT, OUTPUT> buildFromFunction( Function<INPUT, OUTPUT> transformation );
    <INPUT, OUTPUT> AppFlow<INPUT, OUTPUT> buildFromTransition( Function<INPUT, AppFlow<Unit, OUTPUT>> transition );
    default <OUTPUT> AppFlow<Unit, OUTPUT> buildFromConstant( final OUTPUT constant ) {
        return buildFromFunction( u -> constant );
    }
    default <OUTPUT> AppFlow<Unit, OUTPUT> buildFromSupplier( final Supplier<OUTPUT> supplier ) {
        return buildFromFunction( u -> supplier.get() );
    }
    default AppFlow<Unit, Unit> unitFlow() {
        return buildFromConstant( Unit.INSTANCE );
    }
}
