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

package org.opencb.opencga.core.models.variant;

import org.opencb.opencga.core.tools.ToolParams;

public class MendelianErrorAnalysisParams extends ToolParams {
    public static final String DESCRIPTION = "Mendelian error analysis params";
    private String family;
    private String individual;
    private String sample;
    private String outdir;

    public MendelianErrorAnalysisParams() {
    }

    public MendelianErrorAnalysisParams(String family, String individual, String sample, String outdir) {
        this.family = family;
        this.individual = individual;
        this.sample = sample;
        this.outdir = outdir;
    }

    public String getFamily() {
        return family;
    }

    public MendelianErrorAnalysisParams setFamily(String family) {
        this.family = family;
        return this;
    }

    public String getIndividual() {
        return individual;
    }

    public MendelianErrorAnalysisParams setIndividual(String individual) {
        this.individual = individual;
        return this;
    }

    public String getSample() {
        return sample;
    }

    public MendelianErrorAnalysisParams setSample(String sample) {
        this.sample = sample;
        return this;
    }

    public String getOutdir() {
        return outdir;
    }

    public MendelianErrorAnalysisParams setOutdir(String outdir) {
        this.outdir = outdir;
        return this;
    }
}
