/*
 * Copyright (c) 2019 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at:
 *
 *     https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.jkube.kit.build.api;

import java.io.IOException;

import org.eclipse.jkube.kit.build.api.auth.AuthConfig;
import org.eclipse.jkube.kit.build.api.auth.RegistryAuthConfig;
import org.eclipse.jkube.kit.config.image.build.ImagePullPolicy;

/**
 * @author roland
 * @since 17.10.18
 */
public interface RegistryContext {

    ImagePullPolicy getDefaultImagePullPolicy();

    String getRegistry(RegistryAuthConfig.Kind kind);

    AuthConfig getAuthConfig(RegistryAuthConfig.Kind kind, String user, String registry) throws IOException;

}
