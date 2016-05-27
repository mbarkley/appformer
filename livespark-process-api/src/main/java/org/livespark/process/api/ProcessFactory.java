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


package org.livespark.process.api;

import java.util.function.Function;

/**
 * @author Max Barkley <mbarkley@redhat.com>
 */
public interface ProcessFactory {

    <INPUT, OUTPUT> Step<INPUT, OUTPUT> getStep(String stepId);
    <INPUT> DataSource<INPUT> getInputSource(String inputSourceId);
    <INPUT, OUTPUT> ProcessFlow<INPUT, OUTPUT> getProcessFlow(String processFlowId);
    <INPUT> DataSource<INPUT> createInputSource(INPUT value);
    <INPUT, OUTPUT> ProcessFlow<INPUT, OUTPUT> startProcess(Step<INPUT, OUTPUT> step);
    <INPUT, OUTPUT> ProcessFlow<INPUT, OUTPUT> process(Function<INPUT, OUTPUT> func);
    void registerProcess( String string, ProcessFlow<?, ?> main );

}
