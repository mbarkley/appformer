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


package org.kie.appformer.flow.impl.descriptor;

import java.util.Collections;
import java.util.List;

import org.jboss.errai.common.client.api.annotations.MapsTo;
import org.jboss.errai.common.client.api.annotations.Portable;
import org.kie.appformer.flow.api.descriptor.type.Type;

@Portable
public class GenericTypeImpl implements Type.GenericType {

    private final SimpleType rawType;
    private final List<TypeVariable> typeParameters;

    public GenericTypeImpl( final @MapsTo("rawType") SimpleType rawType, final @MapsTo("typeParameters") List<TypeVariable> typeParameters ) {
        this.rawType = rawType;
        this.typeParameters = Collections.unmodifiableList( typeParameters );
    }

    @Override
    public String getName() {
        return rawType.getName();
    }

    @Override
    public String getSimpleName() {
        return rawType.getSimpleName();
    }

    @Override
    public SimpleType getErasure() {
        return rawType;
    }

    @Override
    public List<TypeVariable> getTypeParameters() {
        return typeParameters;
    }

    @Override
    public int hashCode() {
        return rawType.hashCode() ^ typeParameters.hashCode();
    }

    @Override
    public boolean equals( final Object obj ) {
        return obj instanceof GenericType && equals( (GenericType) obj );
    }

}
