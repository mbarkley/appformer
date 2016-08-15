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

package org.livespark.formmodeler.codegen.model.impl;

import static org.livespark.formmodeler.codegen.util.SourceGenerationUtil.ERRAI_BINDABLE;
import static org.livespark.formmodeler.codegen.util.SourceGenerationUtil.ERRAI_PORTABLE;
import static org.livespark.formmodeler.codegen.util.SourceGenerationUtil.FORM_MODEL_CLASS;
import static org.livespark.formmodeler.codegen.util.SourceGenerationUtil.INJECT_NAMED;
import static org.livespark.formmodeler.codegen.util.SourceGenerationUtil.VALIDATION_NOT_NULL;
import static org.livespark.formmodeler.codegen.util.SourceGenerationUtil.VALIDATION_VALID;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.lang3.ArrayUtils;
import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.FieldSource;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.MethodSource;
import org.kie.workbench.common.forms.model.DataHolder;
import org.livespark.formmodeler.codegen.JavaSourceGenerator;
import org.livespark.formmodeler.codegen.SourceGenerationContext;
import org.livespark.formmodeler.codegen.model.FormModel;

@FormModel
@ApplicationScoped
public class RoasterFormModelSourceGenerator implements JavaSourceGenerator {

    public static final String[] BASIC_TYPES = new String[]{
            BigDecimal.class.getName(),
            BigInteger.class.getName(),
            Byte.class.getName(),
            byte.class.getName(),
            Boolean.class.getName(),
            boolean.class.getName(),
            Character.class.getName(),
            char.class.getName(),
            Date.class.getName(),
            Double.class.getName(),
            double.class.getName(),
            Float.class.getName(),
            float.class.getName(),
            Integer.class.getName(),
            int.class.getName(),
            Long.class.getName(),
            long.class.getName(),
            Short.class.getName(),
            short.class.getName(),
            String.class.getName()
    };

    @Inject
    public RoasterFormModelSourceGenerator( final ConstructorGenerator constructorGenerator ) {
        this.constructorGenerator = constructorGenerator;
    }

    private final ConstructorGenerator constructorGenerator;

    @Override
    public String generateJavaSource( final SourceGenerationContext context ) {

        final JavaClassSource modelClass = Roaster.create( JavaClassSource.class );

        addTypeSignature( context, modelClass );
        addImports( context, modelClass );
        addTypeAnnotations( context, modelClass );
        addProperties( context, modelClass );
        addConstructors( context, modelClass );
        addMethodImpls( context, modelClass );

        return modelClass.toString();
    }

    private void addImports( final SourceGenerationContext context,
                             final JavaClassSource modelClass ) {
        modelClass.addImport( List.class );
        modelClass.addImport( Arrays.class );
    }

    private void addMethodImpls( final SourceGenerationContext context,
                                 final JavaClassSource modelClass ) {
        addGetDataModelsImpl( context, modelClass );
    }

    private void addGetDataModelsImpl( final SourceGenerationContext context,
                                       final JavaClassSource modelClass ) {
        final MethodSource<JavaClassSource> getDataModels = modelClass.addMethod();
        getDataModels.setPublic()
                .setName( "getDataModels" )
                .setReturnType( "List<Object>" )
                .setBody( generateGetDataModelsBody( context ) )
                .addAnnotation( Override.class );
    }

    private String generateGetDataModelsBody( final SourceGenerationContext context ) {
        final StringBuilder body = new StringBuilder();

        body.append( "return Arrays.<Object>asList(" );

        final Iterator<DataHolder> iter = context.getFormDefinition().getDataHolders().iterator();
        while ( iter.hasNext() ) {
            final DataHolder holder = iter.next();
            body.append( holder.getName() );
            if ( iter.hasNext() ) {
                body.append( ", " );
            }
        }
        body.append( ");" );

        return body.toString();
    }

    private void addConstructors( final SourceGenerationContext context,
                                  final JavaClassSource modelClass ) {
        constructorGenerator.addNoArgConstructor( modelClass );
        constructorGenerator.addFormModelConstructor( context, modelClass );
    }

    private void addTypeAnnotations( final SourceGenerationContext context,
                                     final JavaClassSource modelClass ) {
        modelClass.addAnnotation( ERRAI_PORTABLE );
        modelClass.addAnnotation( ERRAI_BINDABLE );
        modelClass.addAnnotation( INJECT_NAMED ).setStringValue( context.getFormModelName() );
    }

    private void addTypeSignature( final SourceGenerationContext context,
                                   final JavaClassSource modelClass ) {
        modelClass.setPackage( context.getSharedPackage().getPackageName() )
                .setPublic()
                .setName( context.getFormModelName() );

        modelClass.setSuperType( FORM_MODEL_CLASS );
    }

    private void addProperties( final SourceGenerationContext context, final JavaClassSource modelClass ) {
        for ( final DataHolder dataHolder : context.getFormDefinition().getDataHolders() ) {
            final FieldSource<JavaClassSource> modelField = modelClass.addProperty( dataHolder.getType(),
                                                                              dataHolder.getName() ).getField();

            if ( ArrayUtils.contains( BASIC_TYPES,
                                      dataHolder.getType() ) ) {
                modelField.addAnnotation( VALIDATION_NOT_NULL );
            } else {
                modelField.addAnnotation( VALIDATION_VALID );
            }

        }
    }
}
