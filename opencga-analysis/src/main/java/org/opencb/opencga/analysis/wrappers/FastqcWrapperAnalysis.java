/*
 * Copyright 2015-2020 OpenCB
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

package org.opencb.opencga.analysis.wrappers;


import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.opencb.commons.exec.Command;
import org.opencb.opencga.analysis.wrappers.executors.FastqcWrapperAnalysisExecutor;
import org.opencb.opencga.core.tools.annotations.Tool;
import org.opencb.opencga.core.exceptions.ToolException;
import org.opencb.opencga.core.models.common.Enums;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Tool(id = FastqcWrapperAnalysis.ID, resource = Enums.Resource.ALIGNMENT, description = FastqcWrapperAnalysis.DESCRIPTION)
public class FastqcWrapperAnalysis extends OpenCgaWrapperAnalysis {

    public final static String ID = "fastqc";
    public final static String DESCRIPTION = "A quality control tool for high throughput sequence data.";

    public final static String FASTQC_DOCKER_IMAGE = "dceoy/fastqc";

    private String file;

    protected void check() throws Exception {
        super.check();

        if (StringUtils.isEmpty(file)) {
            throw new ToolException("Missing input file when executing 'fastqc'.");
        }
    }

    @Override
    protected void run() throws Exception {
        step(() -> {

            try {
                FastqcWrapperAnalysisExecutor executor = new FastqcWrapperAnalysisExecutor(getStudy(), params, getOutDir(),
                        getScratchDir(), catalogManager, token);

                executor.setFile(file);
                executor.run();

                // Check fastqc errors
                boolean success = false;
                List<String> filenames = Files.walk(getOutDir()).map(f -> f.getFileName().toString()).collect(Collectors.toList());
                for (String filename : filenames) {
                    if (filename.endsWith("html")) {
                        success = true;
                        break;
                    }
                }
                if (!success) {
                    File file = getScratchDir().resolve(STDERR_FILENAME).toFile();
                    String msg = "Something wrong happened when executing FastQC";
                    if (file.exists()) {
                        msg = StringUtils.join(FileUtils.readLines(file, Charset.defaultCharset()), "\n");
                    }
                    throw new ToolException(msg);
                }
            } catch (Exception e) {
                throw new ToolException(e);
            }
        });
    }

    public String getFile() {
        return file;
    }

    public FastqcWrapperAnalysis setFile(String file) {
        this.file = file;
        return this;
    }

    @Override
    public String getDockerImageName() {
        return null;
    }
}
