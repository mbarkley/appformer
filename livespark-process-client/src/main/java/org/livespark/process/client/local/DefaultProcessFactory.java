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


package org.livespark.process.client.local;

import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;

import org.livespark.process.api.DataSource;
import org.livespark.process.api.ProcessFactory;
import org.livespark.process.api.ProcessFlow;
import org.livespark.process.api.Step;

@ApplicationScoped
public class DefaultProcessFactory implements ProcessFactory {

    private final Map<String, Step<?, ?>> steps = new HashMap<>();
    private final Map<String, DataSource<?>> dataSources = new HashMap<>();
    private final Map<String, ProcessFlow<?, ?>> processes = new HashMap<>();

    @SuppressWarnings( "unchecked" )
    @Override
    public <INPUT, OUTPUT> Step<INPUT, OUTPUT> getStep( final String stepId ) {
        return (Step<INPUT, OUTPUT>) steps.get( stepId );
    }

    @SuppressWarnings( "unchecked" )
    @Override
    public <INPUT> DataSource<INPUT> getDataSource( final String sourceId ) {
        return (DataSource<INPUT>) dataSources.get( sourceId );
    }

    @SuppressWarnings( "unchecked" )
    @Override
    public <INPUT, OUTPUT> ProcessFlow<INPUT, OUTPUT> getProcessFlow( final String processFlowId ) {
        return (ProcessFlow<INPUT, OUTPUT>) processes.get( processFlowId );
    }

    @Override
    public <INPUT, OUTPUT> ProcessFlow<INPUT, OUTPUT> buildProcessFrom( final Step<INPUT, OUTPUT> step ) {
        return new DefaultProcessFlow<>( step );
    }

    @Override
    public void registerProcess( final String id, final ProcessFlow<?, ?> main ) {
        processes.put( id, main );
    }

    @Override
    public void registerDataSource( final String id, final DataSource<?> source ) {
        dataSources.put( id, source );
    }

    @Override
    public void registerStep( final String id, final Step<? , ?> step ) {
        steps.put( id, step );
    }

}
