package hudson.plugins.mstest;

import hudson.Plugin;

import hudson.tasks.BuildStep;

/**
 * 
 * Entry point of a plugin.
 * 
 * @author Antonio Marques
 * 
 */

public class PluginImpl extends Plugin {

    @Override
    public void start() throws Exception {

        BuildStep.PUBLISHERS.addRecorder(MSTestPublisher.DESCRIPTOR);

    }

}
