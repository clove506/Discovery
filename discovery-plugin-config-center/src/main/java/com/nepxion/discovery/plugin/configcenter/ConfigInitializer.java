package com.nepxion.discovery.plugin.configcenter;

/**
 * <p>Title: Nepxion Discovery</p>
 * <p>Description: Nepxion Discovery</p>
 * <p>Copyright: Copyright (c) 2017-2050</p>
 * <p>Company: Nepxion</p>
 * @author Haojun Ren
 * @version 1.0
 */

import java.io.InputStream;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.nepxion.discovery.plugin.configcenter.loader.LocalConfigLoader;
import com.nepxion.discovery.plugin.configcenter.loader.RemoteConfigLoader;
import com.nepxion.discovery.plugin.framework.adapter.PluginAdapter;
import com.nepxion.discovery.plugin.framework.context.PluginContextAware;
import com.nepxion.discovery.plugin.framework.entity.RuleEntity;

public class ConfigInitializer {
    private static final Logger LOG = LoggerFactory.getLogger(ConfigInitializer.class);

    @Autowired
    private PluginContextAware pluginContextAware;

    @Autowired
    private PluginAdapter pluginAdapter;

    @Autowired
    private LocalConfigLoader localConfigLoader;

    @Autowired(required = false)
    private RemoteConfigLoader remoteConfigLoader;

    @Autowired
    private ConfigParser configParser;

    @PostConstruct
    public void initialize() {
        Boolean registerControlEnabled = pluginContextAware.isRegisterControlEnabled();
        Boolean discoveryControlEnabled = pluginContextAware.isDiscoveryControlEnabled();

        if (!registerControlEnabled && !discoveryControlEnabled) {
            LOG.info("Register and Discovery controls are all disabled, ignore to initialize");

            return;
        }

        LOG.info("Rule starts to initialize...");

        InputStream inputStream = getInputStream();
        try {
            RuleEntity ruleEntity = configParser.parse(inputStream);
            pluginAdapter.setLocalRule(ruleEntity);
        } catch (Exception e) {
            LOG.error("Parse rule xml failed", e);
        }
    }

    public InputStream getInputStream() {
        InputStream inputStream = null;

        if (remoteConfigLoader != null) {
            try {
                inputStream = remoteConfigLoader.getInputStream();
            } catch (Exception e) {
                LOG.warn("Get remote input stream failed", e);
            }

            if (inputStream != null) {
                LOG.info("Remote input stream is retrieved");

                return inputStream;
            } else {
                LOG.info("Remote input stream isn't retrieved, use local config loader");
            }
        } else {
            LOG.info("Remote config loader isn't provided, use local config loader");
        }

        try {
            inputStream = localConfigLoader.getInputStream();
        } catch (Exception e) {
            LOG.warn("Get local input stream failed", e);
        }

        if (inputStream != null) {
            LOG.info("Local input stream is retrieved");

            return inputStream;
        } else {
            LOG.info("Local input stream isn't retrieved");
        }
        
        LOG.info("No input stream is retrieved, use no config settings");

        return null;
    }
}