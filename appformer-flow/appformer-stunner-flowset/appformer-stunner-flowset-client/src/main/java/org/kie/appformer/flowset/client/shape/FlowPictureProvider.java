/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *     http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.appformer.flowset.client.shape;

import static org.uberfire.commons.validation.PortablePreconditions.checkNotNull;

import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;

import org.kie.appformer.flowset.api.shape.def.FlowPictures;
import org.kie.appformer.flowset.client.resources.FlowImageResources;
import org.kie.workbench.common.stunner.shapes.def.picture.PictureProvider;

import com.google.gwt.safehtml.shared.SafeUri;

@ApplicationScoped
public class FlowPictureProvider implements PictureProvider<FlowPictures> {

    private static final Map<FlowPictures, SafeUri> PICTURE_URIS =
            new HashMap<FlowPictures, SafeUri>() {{
                put(FlowPictures.DATA_STEP,
                    FlowImageResources.INSTANCE.dataStep().getSafeUri());
                put(FlowPictures.FORM_STEP,
                    FlowImageResources.INSTANCE.formStep().getSafeUri());
                put(FlowPictures.ROOT_STEP,
                    FlowImageResources.INSTANCE.rootStep().getSafeUri());
                put(FlowPictures.START,
                    FlowImageResources.INSTANCE.start().getSafeUri());
                put(FlowPictures.MULTI_STEP,
                    FlowImageResources.INSTANCE.multiStep().getSafeUri());
                put(FlowPictures.DECISION,
                    FlowImageResources.INSTANCE.decision().getSafeUri());
                put(FlowPictures.DECISION_ICON,
                    FlowImageResources.INSTANCE.decisionIcon().getSafeUri());
                put(FlowPictures.MATCHER,
                    FlowImageResources.INSTANCE.dataStep().getSafeUri());
                put(FlowPictures.JOIN,
                    FlowImageResources.INSTANCE.join().getSafeUri());
                put(FlowPictures.JOIN_ICON,
                    FlowImageResources.INSTANCE.joinIcon().getSafeUri());
            }};

    @Override
    public Class<FlowPictures> getSourceType() {
        return FlowPictures.class;
    }

    @Override
    public boolean thumbFor(final FlowPictures source) {
        return get(source) != null;
    }

    @Override
    public SafeUri getThumbnailUri(final FlowPictures source) {
        return get(source);
    }

    private SafeUri get(final FlowPictures source) {
        checkNotNull("source",
                     source);
        return PICTURE_URIS.get(source);
    }
}
