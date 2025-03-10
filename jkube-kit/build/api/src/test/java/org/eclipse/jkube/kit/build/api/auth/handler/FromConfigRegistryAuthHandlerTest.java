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
package org.eclipse.jkube.kit.build.api.auth.handler;

import java.util.Base64;

import org.eclipse.jkube.kit.build.api.auth.AuthConfig;
import org.eclipse.jkube.kit.build.api.auth.RegistryAuth;
import org.eclipse.jkube.kit.build.api.auth.RegistryAuthConfig;
import org.eclipse.jkube.kit.common.KitLogger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

/**
 * @author roland
 * @since 23.10.18
 */
class FromConfigRegistryAuthHandlerTest {

  private KitLogger log;

  @BeforeEach
  void setUp() {
    log = new KitLogger.SilentLogger();
  }

  @Test
  void fromPluginConfiguration() {
    FromConfigRegistryAuthHandler handler = new FromConfigRegistryAuthHandler(setupAuthConfigFactoryWithConfigData(), log);

    AuthConfig config = handler.create(RegistryAuthConfig.Kind.PUSH, null, null, s -> s);
    verifyAuthConfig(config, "roland", "secret", "roland@jolokia.org");
  }

  protected RegistryAuthConfig setupAuthConfigFactoryWithConfigData() {
    return RegistryAuthConfig.builder()
        .skipExtendedAuthentication(false)
        .putDefaultConfig(RegistryAuth.USERNAME, "roland")
        .putDefaultConfig(RegistryAuth.PASSWORD, "secret")
        .putDefaultConfig(RegistryAuth.EMAIL, "roland@jolokia.org")
        .build();
  }

  private RegistryAuthConfig setupAuthConfigFactoryWithConfigDataForKind(RegistryAuthConfig.Kind kind) {
    return RegistryAuthConfig.builder()
        .skipExtendedAuthentication(false)
        .addKindConfig(kind, RegistryAuth.USERNAME, "roland")
        .addKindConfig(kind, RegistryAuth.PASSWORD, "secret")
        .addKindConfig(kind, RegistryAuth.EMAIL, "roland@jolokia.org")
        .build();
  }

  @Test
  void fromPluginConfigurationPull() {
    FromConfigRegistryAuthHandler handler = new FromConfigRegistryAuthHandler(
        setupAuthConfigFactoryWithConfigDataForKind(RegistryAuthConfig.Kind.PULL), log);

    AuthConfig config = handler.create(RegistryAuthConfig.Kind.PULL, null, null, s -> s);
    verifyAuthConfig(config, "roland", "secret", "roland@jolokia.org");
  }

  @Test
  void fromPluginConfigurationFailed() {
    FromConfigRegistryAuthHandler handler = new FromConfigRegistryAuthHandler(
        RegistryAuthConfig.builder().putDefaultConfig(RegistryAuth.USERNAME, "admin").build(), log);

    assertThatIllegalArgumentException()
        .isThrownBy(() -> handler.create(RegistryAuthConfig.Kind.PUSH, null, null, s -> s))
        .withMessageContaining("password");
  }

  private void verifyAuthConfig(AuthConfig config, String username, String password, String email) {
    JsonObject params = new Gson().fromJson(new String(Base64.getDecoder().decode(config.toHeaderValue(log).getBytes())),
        JsonObject.class);
    assertThat(params)
        .returns(username, j -> j.get("username").getAsString())
        .returns(password, j -> j.get("password").getAsString());
    if (email != null) {
      assertThat(params.get("email").getAsString()).isEqualTo(email);
    }
  }

}
