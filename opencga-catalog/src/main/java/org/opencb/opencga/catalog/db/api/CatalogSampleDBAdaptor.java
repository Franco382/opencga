/*
 * Copyright 2015 OpenCB
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

package org.opencb.opencga.catalog.db.api;

import org.apache.commons.collections.map.LinkedMap;
import org.opencb.commons.datastore.core.*;
import org.opencb.opencga.catalog.db.AbstractCatalogDBAdaptor;
import org.opencb.opencga.catalog.exceptions.CatalogDBException;
import org.opencb.opencga.catalog.models.*;

import java.util.List;
import java.util.Map;

import static org.opencb.commons.datastore.core.QueryParam.Type.*;

/**
 * @author Jacobo Coll &lt;jacobo167@gmail.com&gt;
 */
public interface CatalogSampleDBAdaptor extends CatalogDBAdaptor<Sample> {

    /*
     * Samples methods
     */

    default boolean sampleExists(int sampleId) throws CatalogDBException {
        return count(new Query(QueryParams.ID.key(), sampleId)).first() > 0;
    }

    default void checkSampleId(int sampleId) throws CatalogDBException {
        if (sampleId < 0) {
            throw CatalogDBException.newInstance("Sample id '{}' is not valid: ", sampleId);
        }

        if (!sampleExists(sampleId)) {
            throw CatalogDBException.newInstance("Sample id '{}' does not exist", sampleId);
        }
    }

    QueryResult<Sample> createSample(int studyId, Sample sample, QueryOptions options) throws CatalogDBException;

    QueryResult<Sample> getSample(int sampleId, QueryOptions options) throws CatalogDBException;

    @Deprecated
    QueryResult<Sample> getAllSamples(QueryOptions options) throws CatalogDBException;

    @Deprecated
    QueryResult<Sample> getAllSamples(Map<String, Variable> variableMap, QueryOptions options) throws CatalogDBException;

    QueryResult<Sample> getAllSamplesInStudy(int studyId, QueryOptions options) throws CatalogDBException;

    QueryResult<Sample> modifySample(int sampleId, QueryOptions parameters) throws CatalogDBException;

    QueryResult<AclEntry> getSampleAcl(int sampleId, String userId) throws CatalogDBException;

    QueryResult<Map<String, AclEntry>> getSampleAcl(int sampleId, List<String> userIds) throws CatalogDBException;

    QueryResult<AclEntry> setSampleAcl(int sampleId, AclEntry acl) throws CatalogDBException;

    QueryResult<AclEntry> unsetSampleAcl(int sampleId, String userId) throws CatalogDBException;

    @Deprecated
    default QueryResult<Sample> deleteSample(int sampleId) throws CatalogDBException {
        return delete(sampleId);
        /*
        // TODO check that the sample is not in use!

        Query query = new Query(CatalogStudyDBAdaptor.QueryParams.ID.key(), sampleId);
        QueryResult<Sample> sampleQueryResult = get(query, new QueryOptions());
        if (sampleQueryResult.getResult().size() == 1) {
            QueryResult<Long> delete = delete(query);
            if (delete.getResult().size() == 0) {
                throw CatalogDBException.newInstance("Sample id '{}' has not been deleted", sampleId);
            }
        } else {
            throw CatalogDBException.newInstance("Sample id '{}' does not exist", sampleId);
        }
        return sampleQueryResult;*/
    }

    int getStudyIdBySampleId(int sampleId) throws CatalogDBException;

    QueryResult<AnnotationSet> annotateSample(int sampleId, AnnotationSet annotationSet, boolean overwrite) throws CatalogDBException;

    QueryResult<AnnotationSet> deleteAnnotation(int sampleId, String annotationId) throws CatalogDBException;


    /*
     * Cohort methods
     */

    QueryResult<Cohort> createCohort(int studyId, Cohort cohort, QueryOptions options) throws CatalogDBException;

    QueryResult<Cohort> getCohort(int cohortId, QueryOptions options) throws CatalogDBException;

    QueryResult<Cohort> getAllCohorts(int studyId, QueryOptions options) throws CatalogDBException;

    QueryResult<Cohort> modifyCohort(int cohortId, ObjectMap parameters, QueryOptions options) throws CatalogDBException;

    QueryResult<Cohort> deleteCohort(int cohortId, QueryOptions queryOptions) throws CatalogDBException;

    int getStudyIdByCohortId(int cohortId) throws CatalogDBException;

    enum QueryParams implements QueryParam {
        ID("id", INTEGER_ARRAY, ""),
        NAME("name", TEXT_ARRAY, ""),
        SOURCE("source", TEXT_ARRAY, ""),
        INDIVIDUAL_ID("individualId", INTEGER_ARRAY, ""),
        DESCRIPTION("description", TEXT, ""),
        ATTRIBUTES("attributes", TEXT, ""), // "Format: <key><operation><stringValue> where <operation> is [<|<=|>|>=|==|!=|~|!~]"
        NATTRIBUTES("nattributes", DECIMAL, ""), // "Format: <key><operation><numericalValue> where <operation> is [<|<=|>|>=|==|!=|~|!~]"
        BATTRIBUTES("battributes", BOOLEAN, ""), // "Format: <key><operation><true|false> where <operation> is [==|!=]"

        STUDY_ID("studyId", INTEGER_ARRAY, ""),

        ACL_USER_ID("acl.userId", TEXT_ARRAY, ""),
        ACL_READ("acl.read", BOOLEAN, ""),
        ACL_WRITE("acl.write", BOOLEAN, ""),
        ACL_EXECUTE("acl.execute", BOOLEAN, ""),
        ACL_DELETE("acl.delete", BOOLEAN, ""),

        VARIABLE_SET_ID("variableSetId", INTEGER, ""),
        ANNOTATION_SET_ID("annotationSetId", TEXT_ARRAY, ""),
        ANNOTATION("annotation", TEXT_ARRAY, "");

        private static Map<String, QueryParams> map;
        static {
            map = new LinkedMap();
            for (QueryParams params : QueryParams.values()) {
                map.put(params.key(), params);
            }
        }

        private final String key;
        private Type type;
        private String description;

        QueryParams(String key, Type type, String description) {
            this.key = key;
            this.type = type;
            this.description = description;
        }

        @Override
        public String key() {
            return key;
        }

        @Override
        public Type type() {
            return type;
        }

        @Override
        public String description() {
            return description;
        }

        public static Map<String, QueryParams> getMap() {
            return map;
        }

        public static QueryParams getParam(String key) {
            return map.get(key);
        }
    }

    enum VariableSetParams implements QueryParam {
        ID("id", DECIMAL, ""),
        NAME("name", TEXT, ""),
        DESCRIPTION("description", TEXT, ""),
        ATTRIBUTES("attributes", TEXT, "Format: <key><operation><stringValue> where <operation> is [<|<=|>|>=|==|!=|~|!~]"),
        NATTRIBUTES("nattributes", DECIMAL, "Format: <key><operation><numericalValue> where <operation> is [<|<=|>|>=|==|!=|~|!~]"),
        BATTRIBUTES("battributes", BOOLEAN, "Format: <key><operation><true|false> where <operation> is [==|!=]"),
        STUDY_ID("studyId", DECIMAL, "");

        private static Map<String, VariableSetParams> map;
        static {
            map = new LinkedMap();
            for (VariableSetParams params : VariableSetParams.values()) {
                map.put(params.key(), params);
            }
        }

        private final String key;
        private Type type;
        private String description;

        VariableSetParams(String key, Type type, String description) {
            this.key = key;
            this.type = type;
            this.description = description;
        }

        @Override
        public String key() {
            return key;
        }

        @Override
        public Type type() {
            return type;
        }

        @Override
        public String description() {
            return description;
        }

        public static Map<String, VariableSetParams> getMap() {
            return map;
        }

        public static VariableSetParams getParam(String key) {
            return map.get(key);
        }
    }

    enum CohortParams implements QueryParam {
        ID("id", DECIMAL, ""),
        NAME("name", TEXT, ""),
        TYPE("type", TEXT, ""),
        STATUS("status", TEXT, ""),
        CREATION_DATE("creationDate", TEXT, ""),
        DESCRIPTION("description", TEXT, ""),

        SAMPLES("samples", DECIMAL, ""),

        ATTRIBUTES("attributes", TEXT, "Format: <key><operation><stringValue> where <operation> is [<|<=|>|>=|==|!=|~|!~]"),
        NATTRIBUTES("nattributes", DECIMAL, "Format: <key><operation><numericalValue> where <operation> is [<|<=|>|>=|==|!=|~|!~]"),
        BATTRIBUTES("battributes", BOOLEAN, "Format: <key><operation><true|false> where <operation> is [==|!=]"),

        STATS("stats", TEXT, "Format: <key><operation><stringValue> where <operation> is [<|<=|>|>=|==|!=|~|!~]"),
        NSTATS("nstats", DECIMAL, "Format: <key><operation><numericalValue> where <operation> is [<|<=|>|>=|==|!=|~|!~]"),
        BSTATS("bstats", BOOLEAN, "Format: <key><operation><true|false> where <operation> is [==|!=]"),

        STUDY_ID("studyId", DECIMAL, "");

        private static Map<String, CohortParams> map;
        static {
            map = new LinkedMap();
            for (CohortParams params : CohortParams.values()) {
                map.put(params.key(), params);
            }
        }

        private final String key;
        private Type type;
        private String description;

        CohortParams(String key, Type type, String description) {
            this.key = key;
            this.type = type;
            this.description = description;
        }

        @Override
        public String key() {
            return key;
        }

        @Override
        public Type type() {
            return type;
        }

        @Override
        public String description() {
            return description;
        }

        public static Map<String, CohortParams> getMap() {
            return map;
        }

        public static CohortParams getParam(String key) {
            return map.get(key);
        }
    }

    // TODO: Implement enum for cohorts

    @Deprecated
    enum SampleFilterOption implements AbstractCatalogDBAdaptor.FilterOption {
        studyId(Type.NUMERICAL, ""),
        annotationSetId(Type.TEXT, ""),
        variableSetId(Type.NUMERICAL, ""),

        annotation(Type.TEXT, "Format: [<VariableId>:[<operator><value>,]+;]+  -> ID:3,4,5;AGE:>30;NAME:Luke,Leia,Vader"),

        id(Type.NUMERICAL, ""),
        name(Type.TEXT, ""),
        source(Type.TEXT, ""),
        individualId(Type.NUMERICAL, ""),

        acl(Type.TEXT, ""),
        bacl("acl", Type.BOOLEAN, ""),

        attributes(Type.TEXT, "Format: <key><operation><stringValue> where <operation> is [<|<=|>|>=|==|!=|~|!~]"),
        nattributes("attributes", Type.NUMERICAL, "Format: <key><operation><numericalValue> where <operation> is [<|<=|>|>=|==|!=|~|!~]"),
        battributes("attributes", Type.BOOLEAN, "Format: <key><operation><true|false> where <operation> is [==|!=]");

        private final String _key;
        private final String _description;
        private final Type _type;

        SampleFilterOption(Type type, String description) {
            this._key = name();
            this._description = description;
            this._type = type;
        }

        SampleFilterOption(String key, Type type, String description) {
            this._key = key;
            this._description = description;
            this._type = type;
        }

        @Override
        public String getDescription() {
            return _description;
        }

        @Override
        public Type getType() {
            return _type;
        }

        @Override
        public String getKey() {
            return _key;
        }
    }

    @Deprecated
    enum CohortFilterOption implements AbstractCatalogDBAdaptor.FilterOption {
        studyId(Type.NUMERICAL, ""),

        id(Type.NUMERICAL, ""),
        name(Type.TEXT, ""),
        type(Type.TEXT, ""),
        status(Type.TEXT, ""),
        creationDate(Type.TEXT, ""),
        description(Type.TEXT, ""),

        samples(Type.NUMERICAL, ""),

        attributes(Type.TEXT, "Format: <key><operation><stringValue> where <operation> is [<|<=|>|>=|==|!=|~|!~]"),
        nattributes("attributes", Type.NUMERICAL, "Format: <key><operation><numericalValue> where <operation> is [<|<=|>|>=|==|!=|~|!~]"),
        battributes("attributes", Type.BOOLEAN, "Format: <key><operation><true|false> where <operation> is [==|!=]"),

        stats(Type.TEXT, "Format: <key><operation><stringValue> where <operation> is [<|<=|>|>=|==|!=|~|!~]"),
        nstats("stats", Type.NUMERICAL, "Format: <key><operation><numericalValue> where <operation> is [<|<=|>|>=|==|!=|~|!~]"),
        bstats("stats", Type.BOOLEAN, "Format: <key><operation><true|false> where <operation> is [==|!=]");


        private final String _key;
        private final String _description;
        private final Type _type;

        CohortFilterOption(Type type, String description) {
            this._key = name();
            this._description = description;
            this._type = type;
        }

        CohortFilterOption(String key, Type type, String description) {
            this._key = key;
            this._description = description;
            this._type = type;
        }

        @Override
        public String getDescription() {
            return _description;
        }

        @Override
        public Type getType() {
            return _type;
        }

        @Override
        public String getKey() {
            return _key;
        }
    }

    @Deprecated
    enum VariableSetFilterOption implements AbstractCatalogDBAdaptor.FilterOption {
        studyId(Type.NUMERICAL, ""),

        id(Type.NUMERICAL, ""),
        name(Type.TEXT, ""),
        description(Type.TEXT, ""),
        attributes(Type.TEXT, "Format: <key><operation><stringValue> where <operation> is [<|<=|>|>=|==|!=|~|!~]"),
        nattributes("attributes", Type.NUMERICAL, "Format: <key><operation><numericalValue> where <operation> is [<|<=|>|>=|==|!=|~|!~]"),
        battributes("attributes", Type.BOOLEAN, "Format: <key><operation><true|false> where <operation> is [==|!=]");

        private final String _key;
        private final String _description;
        private final Type _type;

        VariableSetFilterOption(Type type, String description) {
            this._key = name();
            this._description = description;
            this._type = type;
        }

        VariableSetFilterOption(String key, Type type, String description) {
            this._key = key;
            this._description = description;
            this._type = type;
        }

        @Override
        public String getDescription() {
            return _description;
        }

        @Override
        public Type getType() {
            return _type;
        }

        @Override
        public String getKey() {
            return _key;
        }
    }


}
