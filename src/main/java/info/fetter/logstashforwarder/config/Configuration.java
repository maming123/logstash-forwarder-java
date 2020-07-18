package info.fetter.logstashforwarder.config;

/*
 * Copyright 2015 Didier Fetter
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;

public class Configuration {
    private OutputSection output;
    private List<FilesSection> files;
    private SettingsSection settings;

    public OutputSection getOutput() {
        return output;
    }

    public void setOutput(OutputSection output) {
        this.output = output;
    }

    public List<FilesSection> getFiles() {
        return files;
    }

    public void setFiles(List<FilesSection> files) {
        this.files = files;
    }

    public SettingsSection getSettings() {
        return settings;
    }

    public void setSettings(SettingsSection settings) {
        this.settings = settings;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).
                append("output", output).
                append("files", files).
                append("settings", settings).
                toString();
    }

}
