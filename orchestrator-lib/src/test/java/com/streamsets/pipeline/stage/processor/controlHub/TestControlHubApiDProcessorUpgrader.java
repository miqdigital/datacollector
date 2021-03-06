/*
 * Copyright 2020 StreamSets Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.streamsets.pipeline.stage.processor.controlHub;

import com.streamsets.pipeline.api.Config;
import com.streamsets.pipeline.api.StageUpgrader;
import com.streamsets.pipeline.config.upgrade.UpgraderTestUtils;
import com.streamsets.pipeline.upgrader.SelectorStageUpgrader;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class TestControlHubApiDProcessorUpgrader {

  private StageUpgrader upgrader;
  private List<Config> configs;
  private StageUpgrader.Context context;

  @Before
  public void setUp() {
    URL yamlResource = ClassLoader.getSystemClassLoader().getResource("upgrader/ControlHubApiDProcessor.yaml");
    upgrader = new SelectorStageUpgrader("stage", null, yamlResource);
    configs = new ArrayList<>();
    context = Mockito.mock(StageUpgrader.Context.class);
  }

  @Test
  public void testV1ToV2Upgrade() {
    Mockito.doReturn(1).when(context).getFromVersion();
    Mockito.doReturn(2).when(context).getToVersion();

    String configPrefix = "conf.client.tlsConfig.";
    configs = upgrader.upgrade(configs, context);

    UpgraderTestUtils.assertExists(configs, configPrefix + "useRemoteKeyStore", false);
    UpgraderTestUtils.assertExists(configs, configPrefix + "privateKey", "");
    UpgraderTestUtils.assertExists(configs, configPrefix + "certificateChain", new ArrayList<>());
    UpgraderTestUtils.assertExists(configs, configPrefix + "trustedCertificates", new ArrayList<>());
  }

  @Test
  public void testV2ToV3Upgrade() {
    configs.add(new Config("conf.baseUrl", "http://sch"));
    configs.add(new Config("conf.client.basicAuth.username", "user"));
    configs.add(new Config("conf.client.basicAuth.password", "pass"));

    Mockito.doReturn(2).when(context).getFromVersion();
    Mockito.doReturn(3).when(context).getToVersion();

    configs = upgrader.upgrade(configs, context);

    UpgraderTestUtils.assertExists(configs, "conf.controlHubConfig.baseUrl", "http://sch");
    UpgraderTestUtils.assertExists(configs, "conf.controlHubConfig.username", "user");
    UpgraderTestUtils.assertExists(configs, "conf.controlHubConfig.password", "pass");
  }

  @Test
  public void testV3ToV4Upgrade() {
    configs.add(new Config("conf.client.connectTimeoutMillis", 1));
    configs.add(new Config("conf.client.readTimeoutMillis", 2));
    configs.add(new Config("conf.client.numThreads", 3));
    configs.add(new Config("conf.client.useProxy", true));

    configs.add(new Config("conf.client.proxy.foo", "FOO"));
    configs.add(new Config("conf.client.tlsConfig.bar", "BAR"));
    configs.add(new Config("conf.client.requestLoggingConfig.zoo", "ZOO"));

    Mockito.doReturn(3).when(context).getFromVersion();
    Mockito.doReturn(4).when(context).getToVersion();

    configs = upgrader.upgrade(configs, context);

    UpgraderTestUtils.assertExists(configs, "conf.controlHubConfig.client.connectTimeoutMillis", 1);
    UpgraderTestUtils.assertExists(configs, "conf.controlHubConfig.client.readTimeoutMillis", 2);
    UpgraderTestUtils.assertExists(configs, "conf.controlHubConfig.client.numThreads", 3);
    UpgraderTestUtils.assertExists(configs, "conf.controlHubConfig.client.useProxy", true);

    UpgraderTestUtils.assertExists(configs, "conf.controlHubConfig.client.proxy.foo", "FOO");
    UpgraderTestUtils.assertExists(configs, "conf.controlHubConfig.client.tlsConfig.bar", "BAR");
    UpgraderTestUtils.assertExists(configs, "conf.controlHubConfig.client.requestLoggingConfig.zoo", "ZOO");

  }

  @Test
  public void testV4ToV5Upgrade() {
    Mockito.doReturn(4).when(context).getFromVersion();
    Mockito.doReturn(5).when(context).getToVersion();

    configs = upgrader.upgrade(configs, context);

    UpgraderTestUtils.assertExists(configs, "conf.controlHubConfig.authenticationType", "USER_PASSWORD");
    UpgraderTestUtils.assertExists(configs, "conf.controlHubConfig.componentId", "");
    UpgraderTestUtils.assertExists(configs, "conf.controlHubConfig.authToken", "");
  }

}
