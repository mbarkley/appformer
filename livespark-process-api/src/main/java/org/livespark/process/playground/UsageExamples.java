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


package org.livespark.process.playground;

import java.util.List;

import org.livespark.process.api.Command;
import org.livespark.process.api.CrudOperation;
import org.livespark.process.api.InputSource;
import org.livespark.process.api.ProcessFactory;
import org.livespark.process.api.Step;

/**
 * @author Max Barkley <mbarkley@redhat.com>
 */
public class UsageExamples {

    <MODEL> void example(final ProcessFactory factory) {
        final InputSource<List<MODEL>> allModels = factory.getInputSource( "AllEntities" );
        final Step<List<MODEL>, Command<CrudOperation, MODEL>> listView = factory.getStep( "ListView" );
        final Step<MODEL, MODEL> formView = factory.getStep( "FormView" );
        final Step<MODEL, Boolean> save = factory.getStep( "SaveEntity" );
        final Step<MODEL, Boolean> update = factory.getStep( "UpdateEntity" );

        listView.startProcess( allModels )
                .transition( output -> {
                    switch (output.commandType) {
                        case CREATE :
                            return formView.startProcess( factory.createInputSource( output.value ) )
                                           .map( save )
                                           .transition( success -> factory.getProcessFlow( "Main" ) );
                        case UPDATE :
                            return formView.startProcess( factory.createInputSource( output.value ) )
                                           .map( update )
                                           .transition( success -> factory.<Boolean>getProcessFlow( "Main" ) );
                        default :
                            return factory.getProcessFlow( "Main" );
                    }
                } );

    }

}
