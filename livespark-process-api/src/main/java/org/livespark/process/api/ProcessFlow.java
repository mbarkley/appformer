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
import java.util.function.Supplier;

/**
 * @author Max Barkley <mbarkley@redhat.com>
 */
public interface ProcessFlow<INPUT, OUTPUT> {

    <T> ProcessFlow<INPUT, T> andThen(Step<OUTPUT, T> nextStep);

    <T> ProcessFlow<INPUT, T> andThen(Function<OUTPUT, T> transformation);

    <T> ProcessFlow<T, OUTPUT> butFirst(Function<T, INPUT> transformation);

    <T> ProcessFlow<T, OUTPUT> butFirst(Step<T, INPUT> prevStep);

    <T> ProcessFlow<INPUT, T> transition(Function<OUTPUT, ProcessFlow<INPUT, T>> chooser);

    default <T> ProcessFlow<INPUT, T> andThen(Supplier<ProcessFlow<OUTPUT, T>> supplier) {
        return transition( (OUTPUT output) -> supplier.get().butFirst( ignore -> output ) );
    }

    default <T> ProcessFlow<INPUT, T> andThen(ProcessFlow<OUTPUT, T> other) {
        return andThen( () -> other );
    }

}
