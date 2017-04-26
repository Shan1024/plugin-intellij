/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.ballerinalang.plugins.idea.run.configuration;

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.RuntimeConfigurationError;
import com.intellij.execution.configurations.RuntimeConfigurationException;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.JDOMExternalizerUtil;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import org.ballerinalang.plugins.idea.psi.BallerinaFile;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

public abstract class BallerinaRunConfigurationWithMain<T extends BallerinaRunningState> extends
        BallerinaRunConfigurationBase<T> {

    private static final String FILE_PATH_ATTRIBUTE_NAME = "filePath";
    private static final String KIND_ATTRIBUTE_NAME = "myRunKind";

    @NotNull
    private String myFilePath = "";
    @NotNull
    protected RunConfigurationKind myRunKind = RunConfigurationKind.MAIN;

    public BallerinaRunConfigurationWithMain(String name, BallerinaModuleBasedConfiguration configurationModule,
                                             ConfigurationFactory factory) {
        super(name, configurationModule, factory);
        myFilePath = getWorkingDirectory();
    }

    @Override
    public void readExternal(@NotNull Element element) throws InvalidDataException {
        super.readExternal(element);
        myFilePath = StringUtil.notNullize(JDOMExternalizerUtil.getFirstChildValueAttribute(element,
                FILE_PATH_ATTRIBUTE_NAME));
        myRunKind = RunConfigurationKind.valueOf(StringUtil.notNullize(
                JDOMExternalizerUtil.getFirstChildValueAttribute(element, KIND_ATTRIBUTE_NAME)));
    }

    @Override
    public void writeExternal(Element element) throws WriteExternalException {
        super.writeExternal(element);
        addNonEmptyElement(element, FILE_PATH_ATTRIBUTE_NAME, myFilePath);
        addNonEmptyElement(element, KIND_ATTRIBUTE_NAME, myRunKind.toString());
    }

    protected void checkFileConfiguration() throws RuntimeConfigurationError {
        VirtualFile file = findFile(getFilePath());
        if (file == null) {
            throw new RuntimeConfigurationError("Main file is not specified");
        }
        PsiFile psiFile = PsiManager.getInstance(getProject()).findFile(file);
        if (!(psiFile instanceof BallerinaFile)) {
            throw new RuntimeConfigurationError("Main file is invalid");
        }

        if (myRunKind == RunConfigurationKind.MAIN &&
                !BallerinaRunUtil.hasMainFunction(psiFile)) {
            throw new RuntimeConfigurationError("Main file does not contain a main function.");
        }
        if (myRunKind == RunConfigurationKind.SERVICE && !BallerinaRunUtil.hasServices(psiFile)) {
            throw new RuntimeConfigurationError("Main file does not contain any service.");
        }
    }

    protected void checkBaseConfiguration() throws RuntimeConfigurationException {
        super.checkConfiguration();
    }

    @NotNull
    public String getFilePath() {
        return myFilePath;
    }

    public void setFilePath(@NotNull String filePath) {
        myFilePath = filePath;
    }

    public RunConfigurationKind getRunKind() {
        return myRunKind;
    }

    public void setRunKind(RunConfigurationKind runKind) {
        this.myRunKind = runKind;
    }
}