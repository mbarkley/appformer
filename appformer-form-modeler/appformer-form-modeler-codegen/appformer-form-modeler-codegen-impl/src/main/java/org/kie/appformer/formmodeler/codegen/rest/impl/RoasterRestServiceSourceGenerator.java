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

package org.kie.appformer.formmodeler.codegen.rest.impl;

import java.util.List;

import org.jboss.forge.roaster.model.source.JavaSource;
import org.jboss.forge.roaster.model.source.MethodSource;
import org.kie.appformer.formmodeler.codegen.JavaSourceGenerator;
import org.kie.appformer.formmodeler.codegen.SourceGenerationContext;
import org.kie.appformer.formmodeler.rendering.client.shared.query.QueryCriteria;

public abstract class RoasterRestServiceSourceGenerator<O extends JavaSource<O>> implements JavaSourceGenerator {

    protected void setCreateMethodSignature( final SourceGenerationContext context,
                                             final MethodSource<O> create ) {
        create.setName( "create" )
              .setPublic()
              .setReturnType( context.getEntityName() )
              .addParameter( context.getEntityName(), "model" );
    }

    protected void setLookupMethodSignature( final SourceGenerationContext context,
                                             final MethodSource<O> lookup ) {
        lookup.setName( "lookup" )
              .setPublic()
              .setReturnType( context.getEntityName() )
              .addParameter( long.class, "id" );
    }

    protected void setLoadMethodSignature( final SourceGenerationContext context,
                                           final MethodSource<O> load ) {
        load.setName( "load" )
            .setPublic()
            .setReturnType( "List<" + context.getEntityName() + ">" );
    }

    protected void setUpdateMethodSignature( final SourceGenerationContext context,
                                             final MethodSource<O> update ) {
        update.setName( "update" )
              .setPublic()
              .setReturnType( Boolean.class )
              .addParameter( context.getEntityName(), "model" );
    }

    protected void setDeleteMethodSignature( final SourceGenerationContext context,
                                             final MethodSource<O> delete ) {
        delete.setName( "delete" )
              .setPublic()
              .setReturnType( Boolean.class );
        // TODO The parameter should be a unique identifier, not the entire model.
        delete.addParameter( context.getEntityName(), "model" );
    }

    protected void setListMethodSignature( final SourceGenerationContext context,
                                             final MethodSource<O> list ) {
        list.setName( "list" )
                .setPublic()
                .setReturnType( "List<" + context.getEntityName() + ">" )
                .addParameter( QueryCriteria.class, "criteria" );
    }

    protected abstract String getPackageName( SourceGenerationContext context );

    protected void addImports( final SourceGenerationContext context,
                               final O restIface ) {
        restIface.addImport( context.getSharedPackage().getPackageName() + "." + context.getEntityName() );
        restIface.addImport( List.class );
    }

}
