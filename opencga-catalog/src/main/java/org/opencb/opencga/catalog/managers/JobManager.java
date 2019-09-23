/*
 * Copyright 2015-2017 OpenCB
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

package org.opencb.opencga.catalog.managers;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import org.opencb.commons.datastore.core.ObjectMap;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.core.QueryResult;
import org.opencb.commons.datastore.core.result.Error;
import org.opencb.commons.datastore.core.result.WriteResult;
import org.opencb.commons.utils.ListUtils;
import org.opencb.opencga.catalog.audit.AuditManager;
import org.opencb.opencga.catalog.audit.AuditRecord;
import org.opencb.opencga.catalog.auth.authorization.AuthorizationManager;
import org.opencb.opencga.catalog.db.DBAdaptorFactory;
import org.opencb.opencga.catalog.db.api.DBIterator;
import org.opencb.opencga.catalog.db.api.JobDBAdaptor;
import org.opencb.opencga.catalog.db.api.SampleDBAdaptor;
import org.opencb.opencga.catalog.db.api.StudyDBAdaptor;
import org.opencb.opencga.catalog.exceptions.CatalogAuthorizationException;
import org.opencb.opencga.catalog.exceptions.CatalogDBException;
import org.opencb.opencga.catalog.exceptions.CatalogException;
import org.opencb.opencga.catalog.io.CatalogIOManagerFactory;
import org.opencb.opencga.catalog.models.InternalGetQueryResult;
import org.opencb.opencga.catalog.utils.ParamUtils;
import org.opencb.opencga.catalog.utils.UUIDUtils;
import org.opencb.opencga.core.common.Entity;
import org.opencb.opencga.core.common.TimeUtils;
import org.opencb.opencga.core.config.Configuration;
import org.opencb.opencga.core.models.File;
import org.opencb.opencga.core.models.Job;
import org.opencb.opencga.core.models.Study;
import org.opencb.opencga.core.models.acls.AclParams;
import org.opencb.opencga.core.models.acls.permissions.FileAclEntry;
import org.opencb.opencga.core.models.acls.permissions.JobAclEntry;
import org.opencb.opencga.core.models.acls.permissions.StudyAclEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.net.URI;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.opencb.opencga.catalog.auth.authorization.CatalogAuthorizationManager.checkPermissions;

/**
 * @author Jacobo Coll &lt;jacobo167@gmail.com&gt;
 */
public class JobManager extends ResourceManager<Job> {

    protected static Logger logger = LoggerFactory.getLogger(JobManager.class);
    private UserManager userManager;
    private StudyManager studyManager;

    public static final QueryOptions INCLUDE_JOB_IDS = new QueryOptions(QueryOptions.INCLUDE,
            Arrays.asList(JobDBAdaptor.QueryParams.ID.key(), JobDBAdaptor.QueryParams.UID.key(), JobDBAdaptor.QueryParams.UUID.key()));

    JobManager(AuthorizationManager authorizationManager, AuditManager auditManager, CatalogManager catalogManager,
               DBAdaptorFactory catalogDBAdaptorFactory, CatalogIOManagerFactory ioManagerFactory,
               Configuration configuration) {
        super(authorizationManager, auditManager, catalogManager, catalogDBAdaptorFactory, ioManagerFactory, configuration);

        this.userManager = catalogManager.getUserManager();
        this.studyManager = catalogManager.getStudyManager();
    }

    @Override
    AuditRecord.Resource getEntity() {
        return AuditRecord.Resource.JOB;
    }

    @Override
    QueryResult<Job> internalGet(long studyUid, String entry, @Nullable Query query, QueryOptions options, String user)
            throws CatalogException {
        ParamUtils.checkIsSingleID(entry);
        Query queryCopy = query == null ? new Query() : new Query(query);
        queryCopy.put(JobDBAdaptor.QueryParams.STUDY_UID.key(), studyUid);

        if (UUIDUtils.isOpenCGAUUID(entry)) {
            queryCopy.put(JobDBAdaptor.QueryParams.UUID.key(), entry);
        } else {
            queryCopy.put(JobDBAdaptor.QueryParams.ID.key(), entry);
        }
//        QueryOptions options = new QueryOptions(QueryOptions.INCLUDE, Arrays.asList(
//                JobDBAdaptor.QueryParams.UUID.key(), JobDBAdaptor.QueryParams.UID.key(), JobDBAdaptor.QueryParams.STUDY_UID.key(),
//                JobDBAdaptor.QueryParams.ID.key(), JobDBAdaptor.QueryParams.STATUS.key()));
        QueryResult<Job> jobQueryResult = jobDBAdaptor.get(queryCopy, options, user);
        if (jobQueryResult.getNumResults() == 0) {
            jobQueryResult = jobDBAdaptor.get(queryCopy, options);
            if (jobQueryResult.getNumResults() == 0) {
                throw new CatalogException("Job " + entry + " not found");
            } else {
                throw new CatalogAuthorizationException("Permission denied. " + user + " is not allowed to see the job " + entry);
            }
        } else if (jobQueryResult.getNumResults() > 1) {
            throw new CatalogException("More than one job found based on " + entry);
        } else {
            return jobQueryResult;
        }
    }

    @Override
    InternalGetQueryResult<Job> internalGet(long studyUid, List<String> entryList, @Nullable Query query, QueryOptions options, String user,
                                            boolean silent) throws CatalogException {
        if (ListUtils.isEmpty(entryList)) {
            throw new CatalogException("Missing job entries.");
        }
        List<String> uniqueList = ListUtils.unique(entryList);

        QueryOptions queryOptions = new QueryOptions(ParamUtils.defaultObject(options, QueryOptions::new));
        Query queryCopy = query == null ? new Query() : new Query(query);
        queryCopy.put(JobDBAdaptor.QueryParams.STUDY_UID.key(), studyUid);

        Function<Job, String> jobStringFunction = Job::getId;
        JobDBAdaptor.QueryParams idQueryParam = null;
        for (String entry : uniqueList) {
            JobDBAdaptor.QueryParams param = JobDBAdaptor.QueryParams.ID;
            if (UUIDUtils.isOpenCGAUUID(entry)) {
                param = JobDBAdaptor.QueryParams.UUID;
                jobStringFunction = Job::getUuid;
            }
            if (idQueryParam == null) {
                idQueryParam = param;
            }
            if (idQueryParam != param) {
                throw new CatalogException("Found uuids and ids in the same query. Please, choose one or do two different queries.");
            }
        }
        queryCopy.put(idQueryParam.key(), uniqueList);

        // Ensure the field by which we are querying for will be kept in the results
        queryOptions = keepFieldInQueryOptions(queryOptions, idQueryParam.key());

        QueryResult<Job> jobQueryResult = jobDBAdaptor.get(queryCopy, options, user);
        if (silent || jobQueryResult.getNumResults() == uniqueList.size()) {
            return keepOriginalOrder(uniqueList, jobStringFunction, jobQueryResult, silent, false);
        }
        // Query without adding the user check
        QueryResult<Job> resultsNoCheck = jobDBAdaptor.get(queryCopy, queryOptions);

        if (resultsNoCheck.getNumResults() == jobQueryResult.getNumResults()) {
            throw CatalogException.notFound("jobs", getMissingFields(uniqueList, jobQueryResult.getResult(), jobStringFunction));
        } else {
            throw new CatalogAuthorizationException("Permission denied. " + user + " is not allowed to see some or none of the jobs.");
        }
    }

    private QueryResult<Job> getJob(long studyUid, String jobUuid, QueryOptions options) throws CatalogDBException {
        Query query = new Query()
                .append(JobDBAdaptor.QueryParams.STUDY_UID.key(), studyUid)
                .append(JobDBAdaptor.QueryParams.UUID.key(), jobUuid);
        return jobDBAdaptor.get(query, options);
    }

    public Long getStudyId(long jobId) throws CatalogException {
        return jobDBAdaptor.getStudyId(jobId);
    }

    public Study getStudy(Job job, String sessionId) throws CatalogException {
        ParamUtils.checkObj(job, "job");
        ParamUtils.checkObj(sessionId, "session id");

        if (job.getStudyUid() <= 0) {
            throw new CatalogException("Missing study uid field in file");
        }

        String user = catalogManager.getUserManager().getUserId(sessionId);

        Query query = new Query(StudyDBAdaptor.QueryParams.UID.key(), job.getStudyUid());
        QueryResult<Study> studyQueryResult = studyDBAdaptor.get(query, QueryOptions.empty(), user);
        if (studyQueryResult.getNumResults() == 1) {
            return studyQueryResult.first();
        } else {
            authorizationManager.checkCanViewStudy(job.getStudyUid(), user);
            throw new CatalogException("Incorrect study uid");
        }
    }


    public QueryResult<Job> visit(String studyId, String jobId, String token) throws CatalogException {
        String userId = userManager.getUserId(token);
        Study study = studyManager.resolveId(studyId, userId);

        ObjectMap auditParams = new ObjectMap()
                .append("studyId", studyId)
                .append("jobId", jobId)
                .append("token", token);
        try {
            Job job = internalGet(study.getUid(), jobId, INCLUDE_JOB_IDS, userId).first();
            authorizationManager.checkJobPermission(study.getUid(), job.getUid(), userId, JobAclEntry.JobPermissions.VIEW);
            ObjectMap params = new ObjectMap(JobDBAdaptor.QueryParams.VISITED.key(), true);
            WriteResult result = jobDBAdaptor.update(job.getUid(), params, QueryOptions.empty());
            QueryResult<Job> queryResult = jobDBAdaptor.get(job.getUid(), QueryOptions.empty());
            queryResult.setDbTime(queryResult.getDbTime() + result.getDbTime());

            auditManager.audit(userId, AuditRecord.Action.VISIT, AuditRecord.Resource.JOB, job.getId(), job.getUuid(), study.getId(),
                    study.getUuid(), auditParams, new AuditRecord.Status(AuditRecord.Status.Result.SUCCESS));

            return queryResult;
        } catch (CatalogException e) {
            auditManager.audit(userId, AuditRecord.Action.VISIT, AuditRecord.Resource.JOB, jobId, "", study.getId(), study.getUuid(),
                    auditParams, new AuditRecord.Status(AuditRecord.Status.Result.ERROR, e.getError()));
            throw e;
        }
    }

    @Deprecated
    public QueryResult<Job> create(long studyId, String name, String toolName, String description, String executor,
                                   Map<String, String> params, String commandLine, URI tmpOutDirUri, long outDirId,
                                   List<File> inputFiles, List<File> outputFiles, Map<String, Object> attributes,
                                   Map<String, Object> resourceManagerAttributes, Job.JobStatus status, long startTime,
                                   long endTime, QueryOptions options, String sessionId) throws CatalogException {
        Job job = new Job(-1, null, name, "", toolName, null, "", description, startTime, endTime, executor, "", commandLine, false, status,
                -1, new File().setUid(outDirId), inputFiles, outputFiles, Collections.emptyList(), params, -1, attributes,
                resourceManagerAttributes);
        return create(String.valueOf(studyId), job, options, sessionId);
    }

    public QueryResult<Job> create(String studyStr, String jobName, String description, String toolId, String execution, String outDir,
                                   Map<String, String> params, String sessionId) throws CatalogException {
        ParamUtils.checkObj(toolId, "toolId");
        if (StringUtils.isEmpty(jobName)) {
            jobName = toolId + "_" + TimeUtils.getTime();
        }
        ObjectMap attributes = new ObjectMap();
        attributes.putIfNotNull(Job.OPENCGA_OUTPUT_DIR, outDir);
        attributes.putIfNotNull(Job.OPENCGA_STUDY, studyStr);
        Job job = new Job(jobName, toolId, execution, Job.Type.ANALYSIS, description, params, attributes);

        return create(studyStr, job, QueryOptions.empty(), sessionId);
    }

    @Override
    public QueryResult<Job> create(String studyStr, Job job, QueryOptions options, String token) throws CatalogException {
        String userId = userManager.getUserId(token);
        Study study = catalogManager.getStudyManager().resolveId(studyStr, userId);

        ObjectMap auditParams = new ObjectMap()
                .append("study", studyStr)
                .append("job", job)
                .append("options", options)
                .append("token", token);
        try {
            authorizationManager.checkStudyPermission(study.getUid(), userId, StudyAclEntry.StudyPermissions.WRITE_JOBS);

            ParamUtils.checkObj(job, "Job");
            ParamUtils.checkParameter(job.getToolId(), "toolId");
            job.setId(ParamUtils.defaultString(job.getId(), job.getToolId() + "_" + TimeUtils.getTimeMillis()));
//        ParamUtils.checkParameter(job.getCommandLine(), "commandLine");
//        ParamUtils.checkObj(job.getOutDir(), "outDir");
            job.setDescription(ParamUtils.defaultString(job.getDescription(), ""));
            job.setCreationDate(ParamUtils.defaultString(job.getCreationDate(), TimeUtils.getTime()));
            job.setStatus(ParamUtils.defaultObject(job.getStatus(), new Job.JobStatus(Job.JobStatus.PREPARED)));
            job.setInput(ParamUtils.defaultObject(job.getInput(), Collections.emptyList()));
            job.setOutput(ParamUtils.defaultObject(job.getOutput(), Collections.emptyList()));
            job.setExecution(ParamUtils.defaultObject(job.getExecution(), ""));
            job.setParams(ParamUtils.defaultObject(job.getParams(), HashMap::new));
            job.setResourceManagerAttributes(ParamUtils.defaultObject(job.getResourceManagerAttributes(), HashMap::new));
            job.setAttributes(ParamUtils.defaultObject(job.getAttributes(), HashMap::new));
            job.setUserId(userId);
            job.setRelease(catalogManager.getStudyManager().getCurrentRelease(study));
            job.setOutDir(job.getOutDir() != null && StringUtils.isNotEmpty(job.getOutDir().getPath()) ? job.getOutDir() : null);

            // FIXME check inputFiles? is a null conceptually valid?
//        URI tmpOutDirUri = createJobOutdir(studyId, randomString, sessionId);

            if (ListUtils.isNotEmpty(job.getInput())) {
                List<String> inputFileStringList = new ArrayList<>(job.getInput().size());
                for (File inputFile : job.getInput()) {
                    inputFileStringList.add(StringUtils.isEmpty(inputFile.getPath()) ? inputFile.getName() : inputFile.getPath());
                }
                List<File> inputFileList = catalogManager.getFileManager().internalGet(study.getUid(), inputFileStringList,
                        QueryOptions.empty(), userId, false).getResult();
                job.setInput(inputFileList);
            }

            if (ListUtils.isNotEmpty(job.getOutput())) {
                List<String> outputFileStringList = new ArrayList<>(job.getOutput().size());
                for (File outputFile : job.getOutput()) {
                    outputFileStringList.add(StringUtils.isEmpty(outputFile.getPath()) ? outputFile.getName() : outputFile.getPath());
                }
                List<File> outputFileList = catalogManager.getFileManager().internalGet(study.getUid(), outputFileStringList,
                        QueryOptions.empty(), userId, false).getResult();
                job.setOutput(outputFileList);
            }

            if (job.getOutDir() != null) {
                String fileName = StringUtils.isNotEmpty(job.getOutDir().getPath()) ? job.getOutDir().getPath() : job.getOutDir().getName();
                File file = catalogManager.getFileManager().internalGet(study.getUid(), fileName, QueryOptions.empty(), userId).first();

                authorizationManager.checkFilePermission(study.getUid(), file.getUid(), userId, FileAclEntry.FilePermissions.WRITE);

                if (!file.getType().equals(File.Type.DIRECTORY)) {
                    throw new CatalogException("Bad outDir type. Required type : " + File.Type.DIRECTORY);
                }

                job.setOutDir(file);
            }

            job.setUuid(UUIDUtils.generateOpenCGAUUID(UUIDUtils.Entity.JOB));
            jobDBAdaptor.insert(study.getUid(), job, options);
            QueryResult<Job> queryResult = getJob(study.getUid(), job.getUuid(), options);
            auditManager.auditCreate(userId, AuditRecord.Resource.JOB, job.getId(), job.getUuid(), study.getId(), study.getUuid(),
                    auditParams, new AuditRecord.Status(AuditRecord.Status.Result.SUCCESS));

            return queryResult;
        } catch (CatalogException e) {
            auditManager.auditCreate(userId, AuditRecord.Resource.JOB, job.getId(), "", study.getId(), study.getUuid(), auditParams,
                    new AuditRecord.Status(AuditRecord.Status.Result.ERROR, e.getError()));
            throw e;
        }
    }

    public QueryResult<Job> get(long jobId, QueryOptions options, String sessionId) throws CatalogException {
        return get(null, String.valueOf(jobId), options, sessionId);
    }

    public List<QueryResult<Job>> get(List<String> jobIds, QueryOptions options, boolean silent, String sessionId) throws CatalogException {
        return get(null, jobIds, options, silent, sessionId);
    }

    @Override
    public QueryResult<Job> get(String studyId, Query query, QueryOptions options, String token) throws CatalogException {
        query = ParamUtils.defaultObject(query, Query::new);
        options = ParamUtils.defaultObject(options, QueryOptions::new);

        String userId = userManager.getUserId(token);
        Study study = catalogManager.getStudyManager().resolveId(studyId, userId);

        ObjectMap auditParams = new ObjectMap()
                .append("studyId", studyId)
                .append("query", new Query(query))
                .append("options", options)
                .append("token", token);
        try {
            query.put(JobDBAdaptor.QueryParams.STUDY_UID.key(), study.getUid());

            fixQueryObject(study, query, userId);

            QueryResult<Job> jobQueryResult = jobDBAdaptor.get(query, options, userId);
            auditManager.auditSearch(userId, AuditRecord.Resource.JOB, study.getId(), study.getUuid(), auditParams,
                    new AuditRecord.Status(AuditRecord.Status.Result.SUCCESS));

            return jobQueryResult;
        } catch (CatalogException e) {
            auditManager.auditSearch(userId, AuditRecord.Resource.JOB, study.getId(), study.getUuid(), auditParams,
                    new AuditRecord.Status(AuditRecord.Status.Result.ERROR, e.getError()));
            throw e;
        }
    }

    private void fixQueryObject(Study study, Query query, String userId) throws CatalogException {
        if (query.containsKey("inputFiles")) {
            List<File> inputFiles = catalogManager.getFileManager().internalGet(study.getUid(), query.getAsStringList("inputFiles"),
                    FileManager.INCLUDE_FILE_IDS, userId, true).getResult();
            if (ListUtils.isNotEmpty(inputFiles)) {
                query.put(JobDBAdaptor.QueryParams.INPUT_UID.key(), inputFiles.stream().map(File::getUid).collect(Collectors.toList()));
            } else {
                // We add 0 so the query returns no results
                query.put(JobDBAdaptor.QueryParams.INPUT_UID.key(), 0);
            }
            query.remove("inputFiles");
        }
        if (query.containsKey("outputFiles")) {
            List<File> inputFiles = catalogManager.getFileManager().internalGet(study.getUid(), query.getAsStringList("outputFiles"),
                    FileManager.INCLUDE_FILE_IDS, userId, true).getResult();
            if (ListUtils.isNotEmpty(inputFiles)) {
                query.put(JobDBAdaptor.QueryParams.OUTPUT_UID.key(), inputFiles.stream().map(File::getUid).collect(Collectors.toList()));
            } else {
                // We add 0 so the query returns no results
                query.put(JobDBAdaptor.QueryParams.OUTPUT_UID.key(), 0);
            }
            query.remove("outputFiles");
        }
    }

    @Override
    public QueryResult<Job> search(String studyId, Query query, QueryOptions options, String token) throws CatalogException {
        throw new NotImplementedException("To be implemented");
    }

    @Override
    public DBIterator<Job> iterator(String studyId, Query query, QueryOptions options, String token) throws CatalogException {
        query = ParamUtils.defaultObject(query, Query::new);
        options = ParamUtils.defaultObject(options, QueryOptions::new);

        String userId = userManager.getUserId(token);
        Study study = catalogManager.getStudyManager().resolveId(studyId, userId);

        query.put(JobDBAdaptor.QueryParams.STUDY_UID.key(), study.getUid());

        fixQueryObject(study, query, userId);

        return jobDBAdaptor.iterator(query, options, userId);
    }

    @Override
    public QueryResult<Job> count(String studyId, Query query, String token) throws CatalogException {
        query = ParamUtils.defaultObject(query, Query::new);

        String userId = userManager.getUserId(token);
        Study study = catalogManager.getStudyManager().resolveId(studyId, userId);

        ObjectMap auditParams = new ObjectMap()
                .append("studyId", studyId)
                .append("query", new Query(query))
                .append("token", token);
        try {
            fixQueryObject(study, query, userId);

            query.append(JobDBAdaptor.QueryParams.STUDY_UID.key(), study.getUid());
            QueryResult<Long> queryResultAux = jobDBAdaptor.count(query, userId, StudyAclEntry.StudyPermissions.VIEW_JOBS);

            auditManager.auditCount(userId, AuditRecord.Resource.JOB, study.getId(), study.getUuid(), auditParams,
                    new AuditRecord.Status(AuditRecord.Status.Result.SUCCESS));

            return new QueryResult<>("count", queryResultAux.getDbTime(), 0, queryResultAux.first(), queryResultAux.getWarningMsg(),
                    queryResultAux.getErrorMsg(), Collections.emptyList());
        } catch (CatalogException e) {
            auditManager.auditCount(userId, AuditRecord.Resource.JOB, study.getId(), study.getUuid(), auditParams,
                    new AuditRecord.Status(AuditRecord.Status.Result.ERROR, e.getError()));
            throw e;
        }
    }

    @Override
    public WriteResult delete(String studyId, Query query, ObjectMap params, String token) throws CatalogException {
        Query finalQuery = new Query(ParamUtils.defaultObject(query, Query::new));
        WriteResult writeResult = new WriteResult();

        String userId = catalogManager.getUserManager().getUserId(token);
        Study study = catalogManager.getStudyManager().resolveId(studyId, userId);

        String operationUuid = UUIDUtils.generateOpenCGAUUID(UUIDUtils.Entity.AUDIT);

        Query auditQuery = new Query(query);
        ObjectMap auditParams = new ObjectMap()
                .append("study", studyId)
                .append("query", new Query(query))
                .append("params", params)
                .append("token", token);

        // If the user is the owner or the admin, we won't check if he has permissions for every single entry
        boolean checkPermissions;

        // We try to get an iterator containing all the jobs to be deleted
        DBIterator<Job> iterator;
        try {
            fixQueryObject(study, query, userId);
            finalQuery.append(JobDBAdaptor.QueryParams.STUDY_UID.key(), study.getUid());

            iterator = jobDBAdaptor.iterator(finalQuery, QueryOptions.empty(), userId);

            // If the user is the owner or the admin, we won't check if he has permissions for every single entry
            checkPermissions = !authorizationManager.checkIsOwnerOrAdmin(study.getUid(), userId);
        } catch (CatalogException e) {
            auditManager.auditDelete(operationUuid, userId, AuditRecord.Resource.JOB, "", "", study.getId(), study.getUuid(), auditParams,
                    new AuditRecord.Status(AuditRecord.Status.Result.ERROR, e.getError()));
            throw e;
        }

        while (iterator.hasNext()) {
            Job job = iterator.next();

            try {
                if (checkPermissions) {
                    authorizationManager.checkJobPermission(study.getUid(), job.getUid(), userId, JobAclEntry.JobPermissions.DELETE);
                }

                // Check if the job can be deleted
                checkJobCanBeDeleted(job);

                writeResult.append(jobDBAdaptor.delete(job.getUid()));

                auditManager.auditDelete(operationUuid, userId, AuditRecord.Resource.JOB, job.getId(), job.getUuid(), study.getId(),
                        study.getUuid(), auditParams, new AuditRecord.Status(AuditRecord.Status.Result.SUCCESS));
            } catch (CatalogException e) {
                writeResult.getFailed().add(new WriteResult.Fail(job.getId(), e.getMessage()));
                logger.debug("Cannot delete job {}: {}", job.getId(), e.getMessage(), e);

                auditManager.auditDelete(operationUuid, userId, AuditRecord.Resource.JOB, job.getId(), job.getUuid(), study.getId(),
                        study.getUuid(), auditParams, new AuditRecord.Status(AuditRecord.Status.Result.ERROR, e.getError()));
            }
        }

        if (!writeResult.getFailed().isEmpty()) {
            writeResult.setWarnings(Collections.singletonList("Some jobs could not be deleted"));
        }

        return writeResult;
    }

    private void checkJobCanBeDeleted(Job job) throws CatalogException {
        switch (job.getStatus().getName()) {
            case Job.JobStatus.DELETED:
                throw new CatalogException("Job already deleted.");
            case Job.JobStatus.PREPARED:
            case Job.JobStatus.RUNNING:
            case Job.JobStatus.QUEUED:
                throw new CatalogException("The status of the job is " + job.getStatus().getName()
                        + ". Please, stop the job before deleting it.");
            case Job.JobStatus.DONE:
            case Job.JobStatus.ERROR:
            case Job.JobStatus.READY:
            default:
                break;
        }
    }

    public QueryResult<Job> update(String studyId, String jobId, ObjectMap parameters, QueryOptions options, String token)
            throws CatalogException {
        String userId = userManager.getUserId(token);
        Study study = studyManager.resolveId(studyId, userId);

        ObjectMap auditParams = new ObjectMap()
                .append("study", studyId)
                .append("jobId", jobId)
                .append("parameters", parameters)
                .append("options", options)
                .append("token", token);

        Job job;
        try {
            job = internalGet(study.getUid(), jobId, INCLUDE_JOB_IDS, userId).first();
        } catch (CatalogException e) {
            auditManager.auditUpdate(userId, AuditRecord.Resource.JOB, jobId, "", study.getId(), study.getUuid(), auditParams,
                    new AuditRecord.Status(AuditRecord.Status.Result.ERROR, e.getError()));
            throw e;
        }

        try {
            ParamUtils.checkObj(parameters, "parameters");
            options = ParamUtils.defaultObject(options, QueryOptions::new);

            authorizationManager.checkJobPermission(study.getUid(), job.getUid(), userId, JobAclEntry.JobPermissions.UPDATE);

            WriteResult result = jobDBAdaptor.update(job.getUid(), parameters, options);
            auditManager.auditUpdate(userId, AuditRecord.Resource.JOB, job.getId(), job.getUuid(), study.getId(), study.getUuid(),
                    auditParams, new AuditRecord.Status(AuditRecord.Status.Result.SUCCESS));

            QueryResult<Job> queryResult = jobDBAdaptor.get(job.getUid(), new QueryOptions(QueryOptions.INCLUDE, parameters.keySet()));
            queryResult.setDbTime(queryResult.getDbTime() + result.getDbTime());

            return queryResult;
        } catch (CatalogException e) {
            auditManager.auditUpdate(userId, AuditRecord.Resource.JOB, job.getId(), job.getUuid(), study.getId(), study.getUuid(),
                    auditParams, new AuditRecord.Status(AuditRecord.Status.Result.ERROR, e.getError()));
            throw e;
        }
    }

    public QueryResult<Job> update(Long jobId, ObjectMap parameters, QueryOptions options, String token) throws CatalogException {
        return update(null, String.valueOf(jobId), parameters, options, token);
    }

    public void setStatus(String studyId, String jobId, String status, String message, String token) throws CatalogException {
        String userId = userManager.getUserId(token);
        Study study = studyManager.resolveId(studyId, userId);

        ObjectMap auditParams = new ObjectMap()
                .append("study", studyId)
                .append("jobId", jobId)
                .append("status", status)
                .append("message", message)
                .append("token", token);
        Job job;
        try {
            job = internalGet(study.getUid(), jobId, INCLUDE_JOB_IDS, userId).first();
        } catch (CatalogException e) {
            auditManager.auditUpdate(userId, AuditRecord.Resource.JOB, jobId, "", study.getId(), study.getUuid(), auditParams,
                    new AuditRecord.Status(AuditRecord.Status.Result.ERROR, e.getError()));
            throw e;
        }

        try {
            authorizationManager.checkJobPermission(study.getUid(), job.getUid(), userId, JobAclEntry.JobPermissions.UPDATE);

            if (status != null && !Job.JobStatus.isValid(status)) {
                throw new CatalogException("The status " + status + " is not valid job status.");
            }

            ObjectMap parameters = new ObjectMap();
            parameters.putIfNotNull(JobDBAdaptor.QueryParams.STATUS_NAME.key(), status);
            parameters.putIfNotNull(JobDBAdaptor.QueryParams.STATUS_MSG.key(), message);

            jobDBAdaptor.update(job.getUid(), parameters, QueryOptions.empty());
            auditManager.auditUpdate(userId, AuditRecord.Resource.JOB, job.getId(), job.getUuid(), study.getId(), study.getUuid(),
                    auditParams, new AuditRecord.Status(AuditRecord.Status.Result.SUCCESS));
        } catch (CatalogException e) {
            auditManager.auditUpdate(userId, AuditRecord.Resource.JOB, job.getId(), job.getUuid(), study.getId(), study.getUuid(),
                    auditParams, new AuditRecord.Status(AuditRecord.Status.Result.ERROR, e.getError()));
            throw e;
        }
    }

    @Override
    public QueryResult rank(String studyId, Query query, String field, int numResults, boolean asc, String token)
            throws CatalogException {
        query = ParamUtils.defaultObject(query, Query::new);
        ParamUtils.checkObj(field, "field");
        ParamUtils.checkObj(token, "sessionId");

        String userId = userManager.getUserId(token);
        Study study = catalogManager.getStudyManager().resolveId(studyId, userId);
        authorizationManager.checkStudyPermission(study.getUid(), userId, StudyAclEntry.StudyPermissions.VIEW_JOBS);

        // TODO: In next release, we will have to check the count parameter from the queryOptions object.
        boolean count = true;
        //query.append(CatalogJobDBAdaptor.QueryParams.STUDY_UID.key(), studyId);
        QueryResult queryResult = null;
        if (count) {
            // We do not need to check for permissions when we show the count of files
            queryResult = jobDBAdaptor.rank(query, field, numResults, asc);
        }

        return ParamUtils.defaultObject(queryResult, QueryResult::new);
    }

    @Override
    public QueryResult groupBy(@Nullable String studyId, Query query, List<String> fields, QueryOptions options, String token)
            throws CatalogException {
        query = ParamUtils.defaultObject(query, Query::new);
        options = ParamUtils.defaultObject(options, QueryOptions::new);
        ParamUtils.checkObj(fields, "fields");
        if (fields == null || fields.size() == 0) {
            throw new CatalogException("Empty fields parameter.");
        }

        String userId = userManager.getUserId(token);
        Study study = catalogManager.getStudyManager().resolveId(studyId, userId);

        // Add study id to the query
        query.put(SampleDBAdaptor.QueryParams.STUDY_UID.key(), study.getUid());

        QueryResult queryResult = jobDBAdaptor.groupBy(query, fields, options, userId);

        return ParamUtils.defaultObject(queryResult, QueryResult::new);
    }

    public QueryResult<Job> queue(String studyId, String jobName, String toolId, String description, String execution, Job.Type type,
                                  Map<String, String> params, List<File> input, List<File> output, File outDir,
                                  Map<String, Object> attributes, String token)
            throws CatalogException {
        Job job = new Job(jobName, toolId, execution, type, description, params, attributes)
                .setInput(input)
                .setOutput(output)
                .setOutDir(outDir);

        return create(studyId, job, QueryOptions.empty(), token);
    }

    // **************************   ACLs  ******************************** //
    public List<QueryResult<JobAclEntry>> getAcls(String studyId, List<String> jobList, String member, boolean silent, String token)
            throws CatalogException {
        String user = userManager.getUserId(token);
        Study study = studyManager.resolveId(studyId, user);

        String operationId = UUIDUtils.generateOpenCGAUUID(UUIDUtils.Entity.AUDIT);
        ObjectMap auditParams = new ObjectMap()
                .append("studyId", studyId)
                .append("jobList", jobList)
                .append("member", member)
                .append("silent", silent)
                .append("token", token);
        try {
            List<QueryResult<JobAclEntry>> jobAclList = new ArrayList<>(jobList.size());
            InternalGetQueryResult<Job> queryResult = internalGet(study.getUid(), jobList, INCLUDE_JOB_IDS, user, silent);

            Map<String, InternalGetQueryResult.Missing> missingMap = new HashMap<>();
            if (queryResult.getMissing() != null) {
                missingMap = queryResult.getMissing().stream()
                        .collect(Collectors.toMap(InternalGetQueryResult.Missing::getId, Function.identity()));
            }
            int counter = 0;
            for (String jobId : jobList) {
                if (!missingMap.containsKey(jobId)) {
                    Job job = queryResult.getResult().get(counter);
                    try {
                        QueryResult<JobAclEntry> allJobAcls;
                        if (StringUtils.isNotEmpty(member)) {
                            allJobAcls = authorizationManager.getJobAcl(study.getUid(), job.getUid(), user, member);
                        } else {
                            allJobAcls = authorizationManager.getAllJobAcls(study.getUid(), job.getUid(), user);
                        }
                        allJobAcls.setId(jobId);
                        jobAclList.add(allJobAcls);
                        auditManager.audit(operationId, user, AuditRecord.Action.FETCH_ACLS, AuditRecord.Resource.JOB, job.getId(),
                                job.getUuid(), study.getId(), study.getUuid(), auditParams,
                                new AuditRecord.Status(AuditRecord.Status.Result.SUCCESS), new ObjectMap());
                    } catch (CatalogException e) {
                        auditManager.audit(operationId, user, AuditRecord.Action.FETCH_ACLS, AuditRecord.Resource.JOB, job.getId(),
                                job.getUuid(), study.getId(), study.getUuid(), auditParams,
                                new AuditRecord.Status(AuditRecord.Status.Result.ERROR, e.getError()), new ObjectMap());
                        if (!silent) {
                            throw e;
                        } else {
                            jobAclList.add(new QueryResult<>(jobId, queryResult.getDbTime(), 0, 0, "", missingMap.get(jobId).getErrorMsg(),
                                    Collections.emptyList()));
                        }
                    }
                    counter += 1;
                } else {
                    jobAclList.add(new QueryResult<>(jobId, queryResult.getDbTime(), 0, 0, "", missingMap.get(jobId).getErrorMsg(),
                            Collections.emptyList()));

                    auditManager.audit(operationId, user, AuditRecord.Action.FETCH_ACLS, AuditRecord.Resource.JOB, jobId, "", study.getId(),
                            study.getUuid(), auditParams, new AuditRecord.Status(AuditRecord.Status.Result.ERROR,
                                    new Error(0, "", missingMap.get(jobId).getErrorMsg())), new ObjectMap());
                }
            }
            return jobAclList;
        } catch (CatalogException e) {
            for (String jobId : jobList) {
                auditManager.audit(operationId, user, AuditRecord.Action.FETCH_ACLS, AuditRecord.Resource.JOB, jobId, "", study.getId(),
                        study.getUuid(), auditParams, new AuditRecord.Status(AuditRecord.Status.Result.ERROR, e.getError()),
                        new ObjectMap());
            }
            throw e;
        }
    }

    public List<QueryResult<JobAclEntry>> updateAcl(String studyId, List<String> jobStrList, String memberList, AclParams aclParams,
                                                    String token) throws CatalogException {
        String userId = userManager.getUserId(token);
        Study study = studyManager.resolveId(studyId, userId);

        ObjectMap auditParams = new ObjectMap()
                .append("studyId", studyId)
                .append("jobStrList", jobStrList)
                .append("memberList", memberList)
                .append("aclParams", aclParams)
                .append("token", token);
        String operationId = UUIDUtils.generateOpenCGAUUID(UUIDUtils.Entity.AUDIT);

        try {
            if (jobStrList == null || jobStrList.isEmpty()) {
                throw new CatalogException("Missing job parameter");
            }

            if (aclParams.getAction() == null) {
                throw new CatalogException("Invalid action found. Please choose a valid action to be performed.");
            }

            List<String> permissions = Collections.emptyList();
            if (StringUtils.isNotEmpty(aclParams.getPermissions())) {
                permissions = Arrays.asList(aclParams.getPermissions().trim().replaceAll("\\s", "").split(","));
                checkPermissions(permissions, JobAclEntry.JobPermissions::valueOf);
            }

            List<Job> jobList = internalGet(study.getUid(), jobStrList, INCLUDE_JOB_IDS, userId, false).getResult();

            authorizationManager.checkCanAssignOrSeePermissions(study.getUid(), userId);

            // Validate that the members are actually valid members
            List<String> members;
            if (memberList != null && !memberList.isEmpty()) {
                members = Arrays.asList(memberList.split(","));
            } else {
                members = Collections.emptyList();
            }
            authorizationManager.checkNotAssigningPermissionsToAdminsGroup(members);
            checkMembers(study.getUid(), members);

            List<QueryResult<JobAclEntry>> queryResultList;
            switch (aclParams.getAction()) {
                case SET:
                    queryResultList = authorizationManager.setAcls(study.getUid(), jobList.stream().map(Job::getUid)
                            .collect(Collectors.toList()), members, permissions, Entity.JOB);
                    break;
                case ADD:
                    queryResultList = authorizationManager.addAcls(study.getUid(), jobList.stream().map(Job::getUid)
                            .collect(Collectors.toList()), members, permissions, Entity.JOB);
                    break;
                case REMOVE:
                    queryResultList = authorizationManager.removeAcls(jobList.stream().map(Job::getUid).collect(Collectors.toList()),
                            members, permissions, Entity.JOB);
                    break;
                case RESET:
                    queryResultList = authorizationManager.removeAcls(jobList.stream().map(Job::getUid).collect(Collectors.toList()),
                            members, null, Entity.JOB);
                    break;
                default:
                    throw new CatalogException("Unexpected error occurred. No valid action found.");
            }

            for (Job job : jobList) {
                auditManager.audit(operationId, userId, AuditRecord.Action.UPDATE_ACLS, AuditRecord.Resource.JOB, job.getId(),
                        job.getUuid(), study.getId(), study.getUuid(), auditParams,
                        new AuditRecord.Status(AuditRecord.Status.Result.SUCCESS), new ObjectMap());
            }
            return queryResultList;
        } catch (CatalogException e) {
            if (jobStrList != null) {
                for (String jobId : jobStrList) {
                    auditManager.audit(operationId, userId, AuditRecord.Action.UPDATE_ACLS, AuditRecord.Resource.JOB, jobId, "",
                            study.getId(), study.getUuid(), auditParams, new AuditRecord.Status(AuditRecord.Status.Result.ERROR,
                                    e.getError()), new ObjectMap());
                }
            }
            throw e;
        }
    }

}
