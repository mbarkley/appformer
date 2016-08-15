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

package org.livespark.formmodeler.codegen.rest.impl;

import static org.livespark.formmodeler.codegen.util.SourceGenerationUtil.BASE_REST_SERVICE;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.Path;

import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.JavaInterfaceSource;
import org.livespark.formmodeler.codegen.SourceGenerationContext;
import org.livespark.formmodeler.codegen.rest.RestApi;


@ApplicationScoped
@RestApi
public class RoasterRestApiSourceGenerator extends RoasterRestServiceSourceGenerator<JavaInterfaceSource> {

    @Override
    public String generateJavaSource( final SourceGenerationContext context ) {
        final JavaInterfaceSource restIface = Roaster.create( JavaInterfaceSource.class );
        final String packageName = getPackageName( context );

        addTypeSignature( context, restIface, packageName );
        addTypeLevelPath( restIface, context );
        addImports( context, restIface );

        return restIface.toString();
    }

    private void addTypeSignature( final SourceGenerationContext context,
            final JavaInterfaceSource restIface,
            final String packageName ) {
        restIface.setPackage( packageName )
                .setPublic()
                .setName( context.getRestServiceName() )
                .addInterface( BASE_REST_SERVICE + "<" + context.getEntityName() + ">" );
    }

    private void addTypeLevelPath( final JavaInterfaceSource restIface , final SourceGenerationContext context  ) {
        restIface.addAnnotation( Path.class ).setStringValue( context.getFormDefinition().getName().toLowerCase() );
    }

    @Override
    protected String getPackageName( final SourceGenerationContext context ) {
        return context.getSharedPackage().getPackageName();
    }

    @Override
    protected void addImports( final SourceGenerationContext context, final JavaInterfaceSource restIface ) {
        super.addImports( context, restIface );
        restIface.addImport( BASE_REST_SERVICE );
    }
}
