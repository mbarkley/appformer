/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.appformer.flowset.client.resources;

import static org.kie.appformer.flowset.client.resources.FlowSVGViewFactory.DATA_STEP;
import static org.kie.appformer.flowset.client.resources.FlowSVGViewFactory.DECISION;
import static org.kie.appformer.flowset.client.resources.FlowSVGViewFactory.FORM_STEP;
import static org.kie.appformer.flowset.client.resources.FlowSVGViewFactory.JOIN;
import static org.kie.appformer.flowset.client.resources.FlowSVGViewFactory.MULTI_STEP;
import static org.kie.appformer.flowset.client.resources.FlowSVGViewFactory.ROOT_STEP;
import static org.kie.appformer.flowset.client.resources.FlowSVGViewFactory.START;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ClientBundleWithLookup;
import com.google.gwt.resources.client.DataResource;

public interface FlowImageResources extends ClientBundleWithLookup {

    public static final FlowImageResources INSTANCE = GWT.create(FlowImageResources.class);
    public static final String SVG_MIME_TYPE = "image/svg+xml";

    @ClientBundle.Source("images/icons/arrows-h.svg")
    @DataResource.MimeType(SVG_MIME_TYPE)
    DataResource horizontalArrows();

    @ClientBundle.Source("images/icons/columns.svg")
    @DataResource.MimeType(SVG_MIME_TYPE)
    DataResource columns();

    @ClientBundle.Source(START)
    @DataResource.MimeType(SVG_MIME_TYPE)
    DataResource start();

    @ClientBundle.Source(ROOT_STEP)
    @DataResource.MimeType(SVG_MIME_TYPE)
    DataResource rootStep();

    @ClientBundle.Source(DATA_STEP)
    @DataResource.MimeType(SVG_MIME_TYPE)
    DataResource dataStep();

    @ClientBundle.Source(FORM_STEP)
    @DataResource.MimeType(SVG_MIME_TYPE)
    DataResource formStep();

    @ClientBundle.Source(MULTI_STEP)
    @DataResource.MimeType(SVG_MIME_TYPE)
    DataResource multiStep();

    @ClientBundle.Source(DECISION)
    @DataResource.MimeType(SVG_MIME_TYPE)
    DataResource decision();

    @ClientBundle.Source(JOIN)
    @DataResource.MimeType(SVG_MIME_TYPE)
    DataResource join();

    @ClientBundle.Source("images/icons/arrows.svg")
    @DataResource.MimeType(SVG_MIME_TYPE)
    DataResource decisionIcon();

    @ClientBundle.Source("images/icons/plus.svg")
    @DataResource.MimeType(SVG_MIME_TYPE)
    DataResource joinIcon();
}