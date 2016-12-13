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

package org.kie.appformer.formmodeler.codegen.view.impl.java;

import static org.kie.appformer.formmodeler.codegen.util.SourceGenerationUtil.BEFORE_DISPLAY_METHOD;
import static org.kie.appformer.formmodeler.codegen.util.SourceGenerationUtil.DO_EXTRA_VALIDATIONS_METHOD;
import static org.kie.appformer.formmodeler.codegen.util.SourceGenerationUtil.ERRAI_TEMPLATED;
import static org.kie.appformer.formmodeler.codegen.util.SourceGenerationUtil.FORM_VIEW_CLASS;
import static org.kie.appformer.formmodeler.codegen.util.SourceGenerationUtil.INIT_FORM_METHOD;
import static org.kie.appformer.formmodeler.codegen.util.SourceGenerationUtil.INJECT_INJECT;
import static org.kie.appformer.formmodeler.codegen.util.SourceGenerationUtil.INJECT_NAMED;
import static org.kie.appformer.formmodeler.codegen.util.SourceGenerationUtil.JAVA_LANG_OVERRIDE;
import static org.kie.appformer.formmodeler.codegen.util.SourceGenerationUtil.JAVA_UTIL_ARRAYLIST_CLASSNAME;
import static org.kie.appformer.formmodeler.codegen.util.SourceGenerationUtil.JAVA_UTIL_LIST_CLASSNAME;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.jboss.forge.roaster.model.source.FieldSource;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.MethodSource;
import org.kie.appformer.formmodeler.codegen.SourceGenerationContext;
import org.kie.appformer.formmodeler.codegen.view.FormView;
import org.kie.appformer.formmodeler.codegen.view.impl.java.inputs.InputCreatorHelper;
import org.kie.workbench.common.forms.model.FieldDefinition;
import org.kie.workbench.common.forms.model.JavaModel;

@FormView
@ApplicationScoped
public class RoasterFormViewSourceGenerator extends RoasterViewSourceGenerator {

    @Inject
    public RoasterFormViewSourceGenerator( final Instance<InputCreatorHelper<? extends FieldDefinition>> creatorInstances ) {
        super( creatorInstances );
    }

    @Override
    protected void addAdditional( final SourceGenerationContext context,
            final JavaClassSource viewClass ) {

        viewClass.addMethod()
                .setName( INIT_FORM_METHOD )
                .setBody( "" )
                .setReturnTypeVoid()
                .setProtected()
                .setBody( "" )
                .addAnnotation( JAVA_LANG_OVERRIDE );

        viewClass.addMethod()
                .setName( BEFORE_DISPLAY_METHOD )
                .setReturnTypeVoid()
                .setPublic()
                .setBody( "" )
                .addAnnotation( JAVA_LANG_OVERRIDE );

        viewClass.addMethod()
                .setName( DO_EXTRA_VALIDATIONS_METHOD )
                .setBody("boolean valid = true; return valid;")
                .setReturnType(boolean.class)
                .setPublic()
                .addAnnotation( JAVA_LANG_OVERRIDE );
    }

    @Override
    protected void addTypeSignature( final SourceGenerationContext context,
            final JavaClassSource viewClass,
            final String packageName ) {
        viewClass.setPackage( packageName )
                .setPublic()
                .setName( context.getFormViewName() )
                .setSuperType( FORM_VIEW_CLASS + "<" + context.getEntityName() + ", " + context.getFormModelName() + ">" );
    }

    @Override
    protected void addImports( final SourceGenerationContext context,
            final JavaClassSource viewClass ) {
        viewClass.addImport( JAVA_UTIL_LIST_CLASSNAME );
        viewClass.addImport( JAVA_UTIL_ARRAYLIST_CLASSNAME );
        viewClass.addImport( context.getSharedPackage().getPackageName() + "." + context.getFormModelName() );

        final JavaModel model = (JavaModel) context.getFormDefinition().getModel();

        viewClass.addImport( model.getType() );

    }

    @Override
    protected void addAnnotations( final SourceGenerationContext context,
            final JavaClassSource viewClass ) {
        viewClass.addAnnotation( ERRAI_TEMPLATED );
        viewClass.addAnnotation( INJECT_NAMED ).setStringValue( context.getFormViewName() );
    }

    @Override
    protected String getWidgetFromHelper( final InputCreatorHelper helper, final FieldDefinition fieldDefinition ) {
        return helper.getInputWidget( fieldDefinition );
    }

    @Override
    protected boolean isEditable() {
        return true;
    }

    @Override
    protected void addExtraFields( final InputCreatorHelper helper, final SourceGenerationContext context, final JavaClassSource viewClass, final FieldDefinition fieldDefinition ) {
        if ( helper instanceof RequiresExtraFields ) {
            ((RequiresExtraFields)helper).addExtraFields( viewClass, fieldDefinition, context );
        }
    }

    @Override
    protected void initializeProperty( final InputCreatorHelper helper,
                                       final SourceGenerationContext context,
                                       final JavaClassSource viewClass,
                                       final FieldDefinition fieldDefinition,
                                       final FieldSource<JavaClassSource> field ) {
        if (helper.isInputInjectable()) field.addAnnotation( INJECT_INJECT );
        else field.setLiteralInitializer( helper.getInputInitLiteral( context, fieldDefinition) );

        final MethodSource<JavaClassSource> initMethod = viewClass.getMethod( INIT_FORM_METHOD );

        final StringBuffer body = new StringBuffer( initMethod.getBody() );

        body.append( "validator.registerInput( \"" )
            .append( fieldDefinition.getName() )
            .append( "\"," )
            .append( fieldDefinition.getName() )
            .append( " );" );

        initMethod.setBody( body.toString() );
    }

    @Override
    protected boolean isBanned( final FieldDefinition definition ) {
        return false;
    }

    @Override
    protected boolean extraFieldsEnabled() {
        return true;
    }
}
