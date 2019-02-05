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

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.opencb.biodata.models.pedigree.IndividualProperty;
import org.opencb.commons.datastore.core.ObjectMap;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.core.QueryResult;
import org.opencb.commons.datastore.core.result.Error;
import org.opencb.commons.datastore.core.result.FacetQueryResult;
import org.opencb.commons.datastore.core.result.WriteResult;
import org.opencb.commons.utils.CollectionUtils;
import org.opencb.opencga.catalog.audit.AuditManager;
import org.opencb.opencga.catalog.audit.AuditRecord;
import org.opencb.opencga.catalog.auth.authorization.AuthorizationManager;
import org.opencb.opencga.catalog.db.DBAdaptorFactory;
import org.opencb.opencga.catalog.db.api.*;
import org.opencb.opencga.catalog.exceptions.CatalogAuthorizationException;
import org.opencb.opencga.catalog.exceptions.CatalogDBException;
import org.opencb.opencga.catalog.exceptions.CatalogException;
import org.opencb.opencga.catalog.exceptions.CatalogParameterException;
import org.opencb.opencga.catalog.io.CatalogIOManagerFactory;
import org.opencb.opencga.catalog.stats.solr.CatalogSolrManager;
import org.opencb.opencga.catalog.utils.AnnotationUtils;
import org.opencb.opencga.catalog.utils.Constants;
import org.opencb.opencga.catalog.utils.ParamUtils;
import org.opencb.opencga.catalog.utils.UUIDUtils;
import org.opencb.opencga.core.common.Entity;
import org.opencb.opencga.core.common.TimeUtils;
import org.opencb.opencga.core.config.Configuration;
import org.opencb.opencga.core.models.*;
import org.opencb.opencga.core.models.acls.AclParams;
import org.opencb.opencga.core.models.acls.permissions.IndividualAclEntry;
import org.opencb.opencga.core.models.acls.permissions.StudyAclEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.opencb.opencga.catalog.auth.authorization.CatalogAuthorizationManager.checkPermissions;

/**
 * Created by hpccoll1 on 19/06/15.
 */
public class IndividualManager extends AnnotationSetManager<Individual> {

    protected static Logger logger = LoggerFactory.getLogger(IndividualManager.class);
    private UserManager userManager;
    private StudyManager studyManager;

    private final String defaultFacet = "creationYear>>creationMonth;status;multiplesType;ethnicity;population;lifeStatus;"
            + "affectationStatus;phenotypes;sex;numSamples[0..10]:1";

    private static final Map<IndividualProperty.KaryotypicSex, IndividualProperty.Sex> KARYOTYPIC_SEX_SEX_MAP;

    static {
        KARYOTYPIC_SEX_SEX_MAP = new HashMap<>();
        KARYOTYPIC_SEX_SEX_MAP.put(IndividualProperty.KaryotypicSex.UNKNOWN, IndividualProperty.Sex.UNKNOWN);
        KARYOTYPIC_SEX_SEX_MAP.put(IndividualProperty.KaryotypicSex.XX, IndividualProperty.Sex.FEMALE);
        KARYOTYPIC_SEX_SEX_MAP.put(IndividualProperty.KaryotypicSex.XO, IndividualProperty.Sex.FEMALE);
        KARYOTYPIC_SEX_SEX_MAP.put(IndividualProperty.KaryotypicSex.XXX, IndividualProperty.Sex.FEMALE);
        KARYOTYPIC_SEX_SEX_MAP.put(IndividualProperty.KaryotypicSex.XXXX, IndividualProperty.Sex.FEMALE);
        KARYOTYPIC_SEX_SEX_MAP.put(IndividualProperty.KaryotypicSex.XY, IndividualProperty.Sex.MALE);
        KARYOTYPIC_SEX_SEX_MAP.put(IndividualProperty.KaryotypicSex.XXY, IndividualProperty.Sex.MALE);
        KARYOTYPIC_SEX_SEX_MAP.put(IndividualProperty.KaryotypicSex.XXYY, IndividualProperty.Sex.MALE);
        KARYOTYPIC_SEX_SEX_MAP.put(IndividualProperty.KaryotypicSex.XXXY, IndividualProperty.Sex.MALE);
        KARYOTYPIC_SEX_SEX_MAP.put(IndividualProperty.KaryotypicSex.XYY, IndividualProperty.Sex.MALE);
        KARYOTYPIC_SEX_SEX_MAP.put(IndividualProperty.KaryotypicSex.OTHER, IndividualProperty.Sex.UNDETERMINED);
    }

    IndividualManager(AuthorizationManager authorizationManager, AuditManager auditManager, CatalogManager catalogManager,
                      DBAdaptorFactory catalogDBAdaptorFactory, CatalogIOManagerFactory ioManagerFactory,
                      Configuration configuration) {
        super(authorizationManager, auditManager, catalogManager, catalogDBAdaptorFactory, ioManagerFactory, configuration);

        this.userManager = catalogManager.getUserManager();
        this.studyManager = catalogManager.getStudyManager();
    }

    @Override
    Individual smartResolutor(long studyUid, String entry, String user) throws CatalogException {
        Query query = new Query()
                .append(IndividualDBAdaptor.QueryParams.STUDY_UID.key(), studyUid);

        if (UUIDUtils.isOpenCGAUUID(entry)) {
            query.put(IndividualDBAdaptor.QueryParams.UUID.key(), entry);
        } else {
            query.put(IndividualDBAdaptor.QueryParams.ID.key(), entry);
        }
        QueryOptions options = new QueryOptions(QueryOptions.INCLUDE, Arrays.asList(
                IndividualDBAdaptor.QueryParams.UUID.key(), IndividualDBAdaptor.QueryParams.UID.key(),
                IndividualDBAdaptor.QueryParams.STUDY_UID.key(), IndividualDBAdaptor.QueryParams.ID.key(),
                IndividualDBAdaptor.QueryParams.RELEASE.key(), IndividualDBAdaptor.QueryParams.VERSION.key(),
                IndividualDBAdaptor.QueryParams.STATUS.key(), IndividualDBAdaptor.QueryParams.FATHER.key(),
                IndividualDBAdaptor.QueryParams.MOTHER.key(), IndividualDBAdaptor.QueryParams.MULTIPLES.key(),
                IndividualDBAdaptor.QueryParams.SEX.key()));
        QueryResult<Individual> individualQueryResult = individualDBAdaptor.get(query, options, user);
        if (individualQueryResult.getNumResults() == 0) {
            individualQueryResult = individualDBAdaptor.get(query, options);
            if (individualQueryResult.getNumResults() == 0) {
                throw new CatalogException("Individual " + entry + " not found");
            } else {
                throw new CatalogAuthorizationException("Permission denied. " + user + " is not allowed to see the individual " + entry);
            }
        } else if (individualQueryResult.getNumResults() > 1) {
            throw new CatalogException("More than one individual found based on " + entry);
        } else {
            return individualQueryResult.first();
        }
    }

    @Deprecated
    public QueryResult<Individual> create(long studyId, String name, String family, long fatherId, long motherId,
                                          IndividualProperty.Sex sex, String ethnicity, String populationName,
                                          String populationSubpopulation, String populationDescription, String dateOfBirth,
                                          IndividualProperty.KaryotypicSex karyotypicSex, IndividualProperty.LifeStatus lifeStatus,
                                          IndividualProperty.AffectationStatus affectationStatus, QueryOptions options, String sessionId)
            throws CatalogException {
        Individual individual = new Individual(name, name, null, null, null, null, sex, karyotypicSex,
                ethnicity, new Individual.Population(populationName, populationSubpopulation, populationDescription), dateOfBirth, -1, 1,
                null, null, lifeStatus, affectationStatus, null, null, null, false, null, null);
        return create(String.valueOf(studyId), individual, options, sessionId);
    }

    @Override
    public QueryResult<Individual> create(String studyStr, Individual individual, QueryOptions options, String sessionId)
            throws CatalogException {
        options = ParamUtils.defaultObject(options, QueryOptions::new);

        ParamUtils.checkAlias(individual.getId(), "id");
        individual.setName(StringUtils.isEmpty(individual.getName()) ? individual.getId() : individual.getName());
        individual.setLocation(ParamUtils.defaultObject(individual.getLocation(), Location::new));
        individual.setEthnicity(ParamUtils.defaultObject(individual.getEthnicity(), ""));
        individual.setPopulation(ParamUtils.defaultObject(individual.getPopulation(), Individual.Population::new));
        individual.setLifeStatus(ParamUtils.defaultObject(individual.getLifeStatus(), IndividualProperty.LifeStatus.UNKNOWN));
        individual.setKaryotypicSex(ParamUtils.defaultObject(individual.getKaryotypicSex(), IndividualProperty.KaryotypicSex.UNKNOWN));
        individual.setSex(ParamUtils.defaultObject(individual.getSex(), IndividualProperty.Sex.UNKNOWN));
        individual.setAffectationStatus(ParamUtils.defaultObject(individual.getAffectationStatus(),
                IndividualProperty.AffectationStatus.UNKNOWN));
        individual.setPhenotypes(ParamUtils.defaultObject(individual.getPhenotypes(), Collections.emptyList()));
        individual.setAnnotationSets(ParamUtils.defaultObject(individual.getAnnotationSets(), Collections.emptyList()));
        individual.setAttributes(ParamUtils.defaultObject(individual.getAttributes(), Collections.emptyMap()));
        individual.setSamples(ParamUtils.defaultObject(individual.getSamples(), Collections.emptyList()));
        individual.setStatus(new Status());
        individual.setCreationDate(TimeUtils.getTime());

        String userId = userManager.getUserId(sessionId);
        Study study = studyManager.resolveId(studyStr, userId);
        long studyUid = study.getUid();

        authorizationManager.checkStudyPermission(studyUid, userId, StudyAclEntry.StudyPermissions.WRITE_INDIVIDUALS);

        List<VariableSet> variableSetList = validateNewAnnotationSetsAndExtractVariableSets(studyUid, individual.getAnnotationSets());

        individual.setRelease(studyManager.getCurrentRelease(study, userId));

        // Check samples exist and can be used or can be created by the user
        Set<Long> existingSampleIds = new HashSet<>();
        List<Sample> nonExistingSamples = new ArrayList<>();
        if (individual.getSamples().size() > 0) {
            for (Sample sample : individual.getSamples()) {
                try {
                    MyResource<Sample> resource = catalogManager.getSampleManager().getUid(sample.getId(), studyStr, sessionId);
                    existingSampleIds.add(resource.getResource().getUid());
                } catch (CatalogException e) {
                    // Sample does not exist so we need to check if the user has permissions to create the samples
                    nonExistingSamples.add(sample);
                }
            }
            if (!existingSampleIds.isEmpty()) {
                checkSamplesNotInUseInOtherIndividual(existingSampleIds, studyUid, null);
            }
            if (!nonExistingSamples.isEmpty()) {
                // Check the user can create new samples
                authorizationManager.checkStudyPermission(studyUid, userId, StudyAclEntry.StudyPermissions.WRITE_SAMPLES);
            }
        }

        // Fetch the sample id and version necessary to point the individual to the proper samples
        List<Sample> sampleList = new ArrayList<>(existingSampleIds.size() + nonExistingSamples.size());
        if (!existingSampleIds.isEmpty()) {
            // We need to obtain the latest version of the samples
            Query sampleQuery = new Query().append(SampleDBAdaptor.QueryParams.UID.key(), existingSampleIds);
            QueryOptions sampleOptions = new QueryOptions(QueryOptions.INCLUDE, Arrays.asList(
                    SampleDBAdaptor.QueryParams.UID.key(), SampleDBAdaptor.QueryParams.VERSION.key()));

            QueryResult<Sample> sampleQueryResult = sampleDBAdaptor.get(sampleQuery, sampleOptions);
            if (sampleQueryResult.getNumResults() < existingSampleIds.size()) {
                throw new CatalogException("Internal error. Could not obtain the current version of all the existing samples.");
            }
            sampleList.addAll(sampleQueryResult.getResult());
        }
        if (!nonExistingSamples.isEmpty()) {
            for (Sample sample : nonExistingSamples) {
                QueryResult<Sample> sampleQueryResult = catalogManager.getSampleManager().create(studyStr, sample, QueryOptions.empty(),
                        sessionId);
                if (sampleQueryResult.getNumResults() == 0) {
                    throw new CatalogException("Internal error. Could not obtain created sample");
                }
                sampleList.add(sampleQueryResult.first());
            }
        }
        individual.setSamples(sampleList);

        if (individual.getFather() != null && StringUtils.isNotEmpty(individual.getFather().getId())) {
            MyResource<Individual> fatherResource = getUid(individual.getFather().getId(), study.getFqn(), sessionId);
            individual.setFather(fatherResource.getResource());
        }

        if (individual.getMother() != null && StringUtils.isNotEmpty(individual.getMother().getId())) {
            MyResource<Individual> motherResource = getUid(individual.getMother().getId(), study.getFqn(), sessionId);
            individual.setMother(motherResource.getResource());
        }

        // Create the individual
        individual.setUuid(UUIDUtils.generateOpenCGAUUID(UUIDUtils.Entity.INDIVIDUAL));
        QueryResult<Individual> queryResult = individualDBAdaptor.insert(studyUid, individual, variableSetList, options);
        auditManager.recordCreation(AuditRecord.Resource.individual, queryResult.first().getUid(), userId, queryResult.first(), null, null);

        return queryResult;
    }

    private Map<Long, Integer> checkSamplesNotInUseInOtherIndividual(Set<Long> sampleIds, long studyId, Long individualId)
            throws CatalogException {
        Map<Long, Integer> currentSamples = new HashMap<>();

        // Check if any of the existing samples already belong to an individual
        Query query = new Query()
                .append(IndividualDBAdaptor.QueryParams.SAMPLE_UIDS.key(), sampleIds)
                .append(IndividualDBAdaptor.QueryParams.STUDY_UID.key(), studyId);
        QueryOptions options = new QueryOptions(QueryOptions.INCLUDE, Arrays.asList(
                IndividualDBAdaptor.QueryParams.SAMPLES.key(), IndividualDBAdaptor.QueryParams.UID.key()));
        QueryResult<Individual> queryResult = individualDBAdaptor.get(query, options);
        if (queryResult.getNumResults() > 0) {
            // Check which of the samples are already associated to an individual
            List<String> usedSamples = new ArrayList<>();
            for (Individual individual1 : queryResult.getResult()) {
                if (individualId != null && individualId == individual1.getUid()) {
                    // It already belongs to the proper individual.
                    for (Sample sample : individual1.getSamples()) {
                        currentSamples.put(sample.getUid(), sample.getVersion());
                    }
                    continue;
                }
                if (individual1.getSamples() != null) {
                    for (Sample sample : individual1.getSamples()) {
                        if (sampleIds.contains(sample.getUid())) {
                            usedSamples.add(sample.getId());
                        }
                    }
                }
            }

            if (usedSamples.size() > 0) {
                throw new CatalogException("Cannot associate some of the samples to the individual. Samples belonging to other "
                        + "individuals: " + StringUtils.join(usedSamples, ", "));
            }
        }

        return currentSamples;
    }

    @Override
    public QueryResult<Individual> get(String studyStr, Query query, QueryOptions options, String sessionId) throws CatalogException {
        query = ParamUtils.defaultObject(query, Query::new);
        options = ParamUtils.defaultObject(options, QueryOptions::new);

        String userId = userManager.getUserId(sessionId);
        Study study = studyManager.resolveId(studyStr, userId, new QueryOptions(QueryOptions.INCLUDE,
                StudyDBAdaptor.QueryParams.VARIABLE_SET.key()));

        // Fix query if it contains any annotation
        AnnotationUtils.fixQueryAnnotationSearch(study, query);
        AnnotationUtils.fixQueryOptionAnnotation(options);
        fixQuery(study, query, sessionId);

        query.append(IndividualDBAdaptor.QueryParams.STUDY_UID.key(), study.getUid());

        QueryResult<Individual> individualQueryResult = individualDBAdaptor.get(query, options, userId);

        if (individualQueryResult.getNumResults() == 0 && query.containsKey(IndividualDBAdaptor.QueryParams.UID.key())) {
            List<Long> idList = query.getAsLongList(IndividualDBAdaptor.QueryParams.UID.key());
            for (Long myId : idList) {
                authorizationManager.checkIndividualPermission(study.getUid(), myId, userId, IndividualAclEntry.IndividualPermissions.VIEW);
            }
        }

        return individualQueryResult;
    }

    public QueryResult<Individual> get(long studyId, Query query, QueryOptions options, String sessionId) throws CatalogException {
        return get(String.valueOf(studyId), query, options, sessionId);
    }

    @Override
    public DBIterator<Individual> iterator(String studyStr, Query query, QueryOptions options, String sessionId) throws CatalogException {
        ParamUtils.checkObj(sessionId, "sessionId");
        query = ParamUtils.defaultObject(query, Query::new);
        options = ParamUtils.defaultObject(options, QueryOptions::new);

        String userId = userManager.getUserId(sessionId);
        Study study = catalogManager.getStudyManager().resolveId(studyStr, userId);
        query.append(IndividualDBAdaptor.QueryParams.STUDY_UID.key(), study.getUid());

        return individualDBAdaptor.iterator(query, options, userId);
    }

    @Override
    public QueryResult<Individual> search(String studyStr, Query query, QueryOptions options, String sessionId) throws CatalogException {
        query = ParamUtils.defaultObject(query, Query::new);
        options = ParamUtils.defaultObject(options, QueryOptions::new);

        String userId = userManager.getUserId(sessionId);
        Study study = catalogManager.getStudyManager().resolveId(studyStr, userId, new QueryOptions(QueryOptions.INCLUDE,
                StudyDBAdaptor.QueryParams.VARIABLE_SET.key()));

        Query finalQuery = new Query(query);
        try {
            fixQuery(study, finalQuery, sessionId);
        } catch (CatalogException e) {
            // Any of mother, father or sample ids or names do not exist or were not found
            return new QueryResult<>("Get");
        }

        // Fix query if it contains any annotation
        AnnotationUtils.fixQueryAnnotationSearch(study, finalQuery);
        AnnotationUtils.fixQueryOptionAnnotation(options);

        finalQuery.append(IndividualDBAdaptor.QueryParams.STUDY_UID.key(), study.getUid());

        QueryResult<Individual> queryResult = individualDBAdaptor.get(finalQuery, options, userId);
//        authorizationManager.filterIndividuals(userId, studyId, queryResultAux.getResult());

        return queryResult;
    }

    @Override
    public QueryResult<Individual> count(String studyStr, Query query, String sessionId) throws CatalogException {
        query = ParamUtils.defaultObject(query, Query::new);

        String userId = userManager.getUserId(sessionId);
        Study study = catalogManager.getStudyManager().resolveId(studyStr, userId, new QueryOptions(QueryOptions.INCLUDE,
                StudyDBAdaptor.QueryParams.VARIABLE_SET.key()));

        Query finalQuery = new Query(query);
        try {
            fixQuery(study, finalQuery, sessionId);
        } catch (CatalogException e) {
            // Any of mother, father or sample ids or names do not exist or were not found
            return new QueryResult<>(null);
        }

        // Fix query if it contains any annotation
        AnnotationUtils.fixQueryAnnotationSearch(study, finalQuery);


        finalQuery.append(IndividualDBAdaptor.QueryParams.STUDY_UID.key(), study.getUid());
        QueryResult<Long> queryResultAux = individualDBAdaptor.count(finalQuery, userId, StudyAclEntry.StudyPermissions.VIEW_INDIVIDUALS);
        return new QueryResult<>("count", queryResultAux.getDbTime(), 0, queryResultAux.first(), queryResultAux.getWarningMsg(),
                queryResultAux.getErrorMsg(), Collections.emptyList());
    }

    @Override
    public WriteResult delete(String studyStr, Query query, ObjectMap params, String sessionId) {
        Query finalQuery = new Query(ParamUtils.defaultObject(query, Query::new));
        WriteResult writeResult = new WriteResult("delete", -1, -1, -1, null, null, null);

        String userId;
        Study study;

        StopWatch watch = StopWatch.createStarted();

        // If the user is the owner or the admin, we won't check if he has permissions for every single entry
        boolean checkPermissions;

        // We try to get an iterator containing all the individuals to be deleted
        DBIterator<Individual> iterator;
        try {
            userId = catalogManager.getUserManager().getUserId(sessionId);
            study = catalogManager.getStudyManager().resolveId(studyStr, userId, new QueryOptions(QueryOptions.INCLUDE,
                    StudyDBAdaptor.QueryParams.VARIABLE_SET.key()));

            // Fix query if it contains any annotation
            fixQuery(study, finalQuery, sessionId);
            // Fix query if it contains any annotation
            AnnotationUtils.fixQueryAnnotationSearch(study, finalQuery);

            finalQuery.append(FamilyDBAdaptor.QueryParams.STUDY_UID.key(), study.getUid());

            iterator = individualDBAdaptor.iterator(finalQuery, QueryOptions.empty(), userId);

            // If the user is the owner or the admin, we won't check if he has permissions for every single entry
            checkPermissions = !authorizationManager.checkIsOwnerOrAdmin(study.getUid(), userId);
        } catch (CatalogException e) {
            logger.error("Delete individual: {}", e.getMessage(), e);
            writeResult.setError(new Error(-1, null, e.getMessage()));
            writeResult.setDbTime((int) watch.getTime(TimeUnit.MILLISECONDS));
            return writeResult;
        }

        long numMatches = 0;
        long numModified = 0;
        List<WriteResult.Fail> failedList = new ArrayList<>();

        String suffixName = INTERNAL_DELIMITER + "DELETED_" + TimeUtils.getTime();

        while (iterator.hasNext()) {
            Individual individual = iterator.next();
            numMatches += 1;

            try {
                if (checkPermissions) {
                    authorizationManager.checkIndividualPermission(study.getUid(), individual.getUid(), userId,
                            IndividualAclEntry.IndividualPermissions.DELETE);
                }

                // Get the families the individual is a member of
                Query tmpQuery = new Query()
                        .append(FamilyDBAdaptor.QueryParams.MEMBER_UID.key(), individual.getUid())
                        .append(FamilyDBAdaptor.QueryParams.STUDY_UID.key(), study.getUid());
                QueryResult<Family> familyQueryResult = familyDBAdaptor.get(tmpQuery, new QueryOptions(QueryOptions.INCLUDE,
                        Arrays.asList(FamilyDBAdaptor.QueryParams.UID.key(), FamilyDBAdaptor.QueryParams.ID.key(),
                                FamilyDBAdaptor.QueryParams.MEMBERS.key())));

                // Check if the individual can be deleted
                if (!params.getBoolean(Constants.FORCE, false)) {
                    if (familyQueryResult.getNumResults() > 0) {
                        throw new CatalogException("Individual found in the families: " + familyQueryResult.getResult()
                                .stream()
                                .map(Family::getId)
                                .collect(Collectors.joining(", ")));
                    }
                } else {
                    logger.info("Forcing deletion of individuals belonging to families");
                }

                // Remove references of the individual in those families
                for (Family family : familyQueryResult.getResult()) {
                    List<Individual> members = new ArrayList<>();
                    for (Individual member : family.getMembers()) {
                        if (member.getUid() != individual.getUid()) {
                            members.add(member);
                        }
                    }

                    // In theory, the array of member should contain 1 element less than the original one
                    if (members.size() + 1 == family.getMembers().size()) {
                        // Remove member from the array of members in the family entry
                        Query familyQuery = new Query()
                                .append(FamilyDBAdaptor.QueryParams.UID.key(), family.getUid())
                                .append(FamilyDBAdaptor.QueryParams.STUDY_UID.key(), study.getUid())
                                .append(Constants.ALL_VERSIONS, true);
                        ObjectMap familyUpdate = new ObjectMap()
                                .append(FamilyDBAdaptor.UpdateParams.MEMBERS.key(), members);

                        QueryResult<Long> update = familyDBAdaptor.update(familyQuery, familyUpdate, QueryOptions.empty());
                        if (update.first() == 0) {
                            throw new CatalogException("Individual could not be extracted from family " + family.getId() + ". "
                                    + "Individual not deleted");
                        }
                    } else {
                        logger.error("Could not delete individual {}. The family {} that in theory contains that individual has the "
                                        + "following members: {}", individual.getId(), family.getId(),
                                family.getMembers().stream().map(Individual::getId).collect(Collectors.toList()));
                        throw new CatalogException("Internal error: Could not delete individual");
                    }
                }

                // Delete the individual
                Query updateQuery = new Query()
                        .append(IndividualDBAdaptor.QueryParams.UID.key(), individual.getUid())
                        .append(IndividualDBAdaptor.QueryParams.STUDY_UID.key(), study.getUid())
                        .append(Constants.ALL_VERSIONS, true);
                ObjectMap updateParams = new ObjectMap()
                        .append(IndividualDBAdaptor.QueryParams.STATUS_NAME.key(), Status.DELETED)
                        .append(IndividualDBAdaptor.QueryParams.ID.key(), individual.getName() + suffixName);
                QueryResult<Long> update = individualDBAdaptor.update(updateQuery, updateParams, QueryOptions.empty());
                if (update.first() > 0) {
                    numModified += 1;
                    auditManager.recordDeletion(AuditRecord.Resource.individual, individual.getUid(), userId, null, updateParams, null,
                            null);
                } else {
                    failedList.add(new WriteResult.Fail(individual.getId(), "Unknown reason"));
                }
            } catch (Exception e) {
                failedList.add(new WriteResult.Fail(individual.getId(), e.getMessage()));
                logger.debug("Cannot delete individual {}: {}", individual.getId(), e.getMessage(), e);
            }
        }

        writeResult.setDbTime((int) watch.getTime(TimeUnit.MILLISECONDS));
        writeResult.setNumMatches(numMatches);
        writeResult.setNumModified(numModified);
        writeResult.setFailed(failedList);

        if (!failedList.isEmpty()) {
            writeResult.setWarning(Collections.singletonList(new Error(-1, null, "There are individuals that could not be deleted")));
        }

        return writeResult;
    }

    public QueryResult<Individual> updateAnnotationSet(String studyStr, String individualStr, List<AnnotationSet> annotationSetList,
                                                       ParamUtils.UpdateAction action, QueryOptions options, String token)
            throws CatalogException {
        ObjectMap params = new ObjectMap(AnnotationSetManager.ANNOTATION_SETS, annotationSetList);
        options = ParamUtils.defaultObject(options, QueryOptions::new);
        options.put(Constants.ACTIONS, new ObjectMap(AnnotationSetManager.ANNOTATION_SETS, action));

        return update(studyStr, individualStr, params, options, token);
    }

    public QueryResult<Individual> addAnnotationSet(String studyStr, String individualStr, AnnotationSet annotationSet,
                                                    QueryOptions options, String token) throws CatalogException {
        return addAnnotationSets(studyStr, individualStr, Collections.singletonList(annotationSet), options, token);
    }

    public QueryResult<Individual> addAnnotationSets(String studyStr, String individualStr, List<AnnotationSet> annotationSetList,
                                                     QueryOptions options, String token) throws CatalogException {
        return updateAnnotationSet(studyStr, individualStr, annotationSetList, ParamUtils.UpdateAction.ADD, options, token);
    }

    public QueryResult<Individual> setAnnotationSet(String studyStr, String individualStr, AnnotationSet annotationSet,
                                                    QueryOptions options, String token) throws CatalogException {
        return setAnnotationSets(studyStr, individualStr, Collections.singletonList(annotationSet), options, token);
    }

    public QueryResult<Individual> setAnnotationSets(String studyStr, String individualStr, List<AnnotationSet> annotationSetList,
                                                     QueryOptions options, String token) throws CatalogException {
        return updateAnnotationSet(studyStr, individualStr, annotationSetList, ParamUtils.UpdateAction.SET, options, token);
    }

    public QueryResult<Individual> removeAnnotationSet(String studyStr, String individualStr, String annotationSetId, QueryOptions options,
                                                       String token) throws CatalogException {
        return removeAnnotationSets(studyStr, individualStr, Collections.singletonList(annotationSetId), options, token);
    }

    public QueryResult<Individual> removeAnnotationSets(String studyStr, String individualStr, List<String> annotationSetIdList,
                                                        QueryOptions options, String token) throws CatalogException {
        List<AnnotationSet> annotationSetList = annotationSetIdList
                .stream()
                .map(id -> new AnnotationSet().setId(id))
                .collect(Collectors.toList());
        return updateAnnotationSet(studyStr, individualStr, annotationSetList, ParamUtils.UpdateAction.REMOVE, options, token);
    }

    public QueryResult<Individual> updateAnnotations(String studyStr, String individualStr, String annotationSetId,
                                                     Map<String, Object> annotations, ParamUtils.CompleteUpdateAction action,
                                                     QueryOptions options, String token) throws CatalogException {
        if (annotations == null || annotations.isEmpty()) {
            return new QueryResult<>(individualStr, -1, -1, -1, "Nothing to do: The map of annotations is empty", "",
                    Collections.emptyList());
        }
        ObjectMap params = new ObjectMap(AnnotationSetManager.ANNOTATIONS, new AnnotationSet(annotationSetId, "", annotations));
        options = ParamUtils.defaultObject(options, QueryOptions::new);
        options.put(Constants.ACTIONS, new ObjectMap(AnnotationSetManager.ANNOTATIONS, action));

        return update(studyStr, individualStr, params, options, token);
    }

    public QueryResult<Individual> removeAnnotations(String studyStr, String individualStr, String annotationSetId,
                                                     List<String> annotations, QueryOptions options, String token) throws CatalogException {
        return updateAnnotations(studyStr, individualStr, annotationSetId, new ObjectMap("remove", StringUtils.join(annotations, ",")),
                ParamUtils.CompleteUpdateAction.REMOVE, options, token);
    }

    public QueryResult<Individual> resetAnnotations(String studyStr, String individualStr, String annotationSetId, List<String> annotations,
                                                    QueryOptions options, String token) throws CatalogException {
        return updateAnnotations(studyStr, individualStr, annotationSetId, new ObjectMap("reset", StringUtils.join(annotations, ",")),
                ParamUtils.CompleteUpdateAction.RESET, options, token);
    }

    @Override
    public QueryResult<Individual> update(String studyStr, String entryStr, ObjectMap parameters, QueryOptions options, String sessionId)
            throws CatalogException {
        ParamUtils.checkObj(parameters, "parameters");
        parameters = new ObjectMap(parameters);
        options = ParamUtils.defaultObject(options, QueryOptions::new);

        MyResource<Individual> resource = getUid(entryStr, studyStr, sessionId);
        String userId = resource.getUser();
        long studyId = resource.getStudy().getUid();
        long individualId = resource.getResource().getUid();

        // Check permissions...
        // Only check write annotation permissions if the user wants to update the annotation sets
        if (parameters.containsKey(IndividualDBAdaptor.QueryParams.ANNOTATION_SETS.key())) {
            authorizationManager.checkIndividualPermission(studyId, individualId, userId,
                    IndividualAclEntry.IndividualPermissions.WRITE_ANNOTATIONS);
        }
        // Only check update permissions if the user wants to update anything apart from the annotation sets
        if ((parameters.size() == 1 && !parameters.containsKey(IndividualDBAdaptor.QueryParams.ANNOTATION_SETS.key()))
                || parameters.size() > 1) {
            authorizationManager.checkIndividualPermission(studyId, individualId, userId, IndividualAclEntry.IndividualPermissions.UPDATE);
        }

        if (parameters.containsKey(IndividualDBAdaptor.UpdateParams.NAME.key())) {
            ParamUtils.checkAlias(parameters.getString(IndividualDBAdaptor.UpdateParams.NAME.key()), "name");

            String myName = parameters.getString(IndividualDBAdaptor.QueryParams.ID.key());
            Query query = new Query()
                    .append(IndividualDBAdaptor.QueryParams.STUDY_UID.key(), studyId)
                    .append(IndividualDBAdaptor.QueryParams.ID.key(), myName);
            if (individualDBAdaptor.count(query).first() > 0) {
                throw new CatalogException("Individual name " + myName + " already in use");
            }
        }
        if (parameters.containsKey(IndividualDBAdaptor.UpdateParams.DATE_OF_BIRTH.key())) {
            if (StringUtils.isEmpty(parameters.getString(IndividualDBAdaptor.UpdateParams.DATE_OF_BIRTH.key()))) {
                parameters.put(IndividualDBAdaptor.UpdateParams.DATE_OF_BIRTH.key(), "");
            } else {
                if (!TimeUtils.isValidFormat("yyyyMMdd", parameters.getString(IndividualDBAdaptor.UpdateParams.DATE_OF_BIRTH.key()))) {
                    throw new CatalogException("Invalid date of birth format. Valid format yyyyMMdd");
                }
            }
        }
        if (parameters.containsKey(IndividualDBAdaptor.UpdateParams.KARYOTYPIC_SEX.key())) {
            try {
                IndividualProperty.KaryotypicSex.valueOf(parameters.getString(IndividualDBAdaptor.UpdateParams.KARYOTYPIC_SEX.key()));
            } catch (IllegalArgumentException e) {
                logger.error("Invalid karyotypic sex found: {}", e.getMessage(), e);
                throw new CatalogException("Invalid karyotypic sex detected");
            }
        }
        if (parameters.containsKey(IndividualDBAdaptor.UpdateParams.SEX.key())) {
            try {
                IndividualProperty.Sex.valueOf(parameters.getString(IndividualDBAdaptor.UpdateParams.SEX.key()));
            } catch (IllegalArgumentException e) {
                logger.error("Invalid sex found: {}", e.getMessage(), e);
                throw new CatalogException("Invalid sex detected");
            }
        }
        if (parameters.containsKey(IndividualDBAdaptor.UpdateParams.MULTIPLES.key())) {
            // Check individual names exist
            Map<String, Object> multiples = parameters.getMap(IndividualDBAdaptor.UpdateParams.MULTIPLES.key());
            List<String> siblingList = (List<String>) multiples.get("siblings");
            Query query = new Query()
                    .append(IndividualDBAdaptor.QueryParams.STUDY_UID.key(), studyId)
                    .append(IndividualDBAdaptor.QueryParams.ID.key(), StringUtils.join(siblingList, ","));
            QueryOptions queryOptions = new QueryOptions(QueryOptions.INCLUDE, IndividualDBAdaptor.QueryParams.UID.key());
            QueryResult<Individual> individualQueryResult = individualDBAdaptor.get(query, queryOptions);
            if (individualQueryResult.getNumResults() < siblingList.size()) {
                int missing = siblingList.size() - individualQueryResult.getNumResults();
                throw new CatalogDBException("Missing " + missing + " siblings in the database.");
            }
        }
        if (parameters.containsKey(IndividualDBAdaptor.UpdateParams.SAMPLES.key())) {
            // Check those samples can be used
            List<String> samples = parameters.getAsStringList(IndividualDBAdaptor.UpdateParams.SAMPLES.key());
            MyResources<Sample> sampleResource = catalogManager.getSampleManager().getUids(samples, studyStr, sessionId);
            Map<Long, Integer> existingSamplesInIndividual = checkSamplesNotInUseInOtherIndividual(
                    sampleResource.getResourceList().stream().map(Sample::getUid).collect(Collectors.toSet()), studyId, individualId);

            List<Sample> updatedSamples = new ArrayList<>();
            Map<String, Object> actionMap = options.getMap(Constants.ACTIONS, new HashMap<>());
            String action = (String) actionMap.getOrDefault(IndividualDBAdaptor.UpdateParams.SAMPLES.key(),
                    ParamUtils.UpdateAction.ADD.name());
            if (ParamUtils.UpdateAction.ADD.name().equals(action)) {
                // We will convert the ADD action into a SET to remove existing samples with older versions and replace them for the newest
                // ones
                Iterator<Sample> iterator = sampleResource.getResourceList().iterator();
                while (iterator.hasNext()) {
                    Sample sample = iterator.next();
                    // We check if the sample is already present in the individual. If so, and the current version is higher than the one
                    // stored, we will change the version to the current one.
                    if (existingSamplesInIndividual.containsKey(sample.getUid())
                            && existingSamplesInIndividual.get(sample.getUid()) < sample.getVersion()) {
                        existingSamplesInIndividual.put(sample.getUid(), sample.getVersion());

                        // We remove the sample from the list to avoid duplicities
                        iterator.remove();
                    }
                }
                for (Map.Entry<Long, Integer> entry : existingSamplesInIndividual.entrySet()) {
                    updatedSamples.add(new Sample().setUid(entry.getKey()).setVersion(entry.getValue()));
                }

                updatedSamples.addAll(sampleResource.getResourceList());

                // Replace action
                actionMap.put(IndividualDBAdaptor.UpdateParams.SAMPLES.key(),  ParamUtils.UpdateAction.SET.name());
            }
            // We add the rest of the samples the user want to add
            updatedSamples.addAll(sampleResource.getResourceList());

            // Update the parameters with the proper list of samples
            parameters.put(IndividualDBAdaptor.QueryParams.SAMPLES.key(), updatedSamples);
        }

        if (StringUtils.isNotEmpty(parameters.getString(IndividualDBAdaptor.QueryParams.FATHER.key()))) {
            Map<String, Object> map = parameters.getMap(IndividualDBAdaptor.QueryParams.FATHER.key());
            if (map != null && StringUtils.isNotEmpty((String) map.get(IndividualDBAdaptor.QueryParams.ID.key()))) {
                MyResource tmpResource = getUid((String) map.get(IndividualDBAdaptor.QueryParams.ID.key()), studyStr, sessionId);
                parameters.remove(IndividualDBAdaptor.QueryParams.FATHER.key());
                parameters.put(IndividualDBAdaptor.QueryParams.FATHER_UID.key(), tmpResource.getResource().getUid());
            } else {
                throw new CatalogException("Cannot update father parameter. Father name or id not passed");
            }
        }
        if (StringUtils.isNotEmpty(parameters.getString(IndividualDBAdaptor.QueryParams.MOTHER.key()))) {
            Map<String, Object> map = parameters.getMap(IndividualDBAdaptor.QueryParams.MOTHER.key());
            if (map != null && StringUtils.isNotEmpty((String) map.get(IndividualDBAdaptor.QueryParams.ID.key()))) {
                MyResource tmpResource = getUid((String) map.get(IndividualDBAdaptor.QueryParams.ID.key()), studyStr, sessionId);
                parameters.remove(IndividualDBAdaptor.QueryParams.MOTHER.key());
                parameters.put(IndividualDBAdaptor.QueryParams.MOTHER_UID.key(), tmpResource.getResource().getUid());
            } else {
                throw new CatalogException("Cannot update mother parameter. Mother name or id not passed");
            }
        }

        try {
            ParamUtils.checkAllParametersExist(parameters.keySet().iterator(), (a) -> IndividualDBAdaptor.UpdateParams.getParam(a) != null);
        } catch (CatalogParameterException e) {
            throw new CatalogException("Could not update: " + e.getMessage(), e);
        }

        return unsafeUpdate(resource.getStudy(), resource.getResource(), parameters, options, userId);
    }

    QueryResult<Individual> unsafeUpdate(Study study, Individual individual, ObjectMap parameters, QueryOptions options, String userId)
            throws CatalogException {
        try {
            ParamUtils.checkAllParametersExist(parameters.keySet().iterator(), (a) -> IndividualDBAdaptor.UpdateParams.getParam(a) != null);
        } catch (CatalogParameterException e) {
            throw new CatalogException("Could not update: " + e.getMessage(), e);
        }

        MyResource<Individual> resource = new MyResource<>(userId, study, individual);
        List<VariableSet> variableSetList = checkUpdateAnnotationsAndExtractVariableSets(resource, parameters, options,
                individualDBAdaptor);

        if (options.getBoolean(Constants.INCREMENT_VERSION)) {
            // We do need to get the current release to properly create a new version
            options.put(Constants.CURRENT_RELEASE, studyManager.getCurrentRelease(study, userId));
        }

        QueryResult<Individual> queryResult = individualDBAdaptor.update(individual.getUid(), parameters, variableSetList, options);
        auditManager.recordUpdate(AuditRecord.Resource.individual, individual.getUid(), userId, parameters, null, null);

        if (!options.getBoolean(Constants.INCREMENT_VERSION)) {
            // Check if user is updating the list of disorders and/or phenotypes
            if (parameters.containsKey(FamilyDBAdaptor.UpdateParams.DISORDERS.key())
                    || parameters.containsKey(FamilyDBAdaptor.UpdateParams.PHENOTYPES.key())) {
                // Update the final list of disorders and phenotypes from family
                catalogManager.getFamilyManager().updatePhenotypesAndDisorders(study, individual);
            }
        }

        return queryResult;
    }

    @Override
    public QueryResult rank(String studyStr, Query query, String field, int numResults, boolean asc, String sessionId)
            throws CatalogException {
        query = ParamUtils.defaultObject(query, Query::new);
        ParamUtils.checkObj(field, "field");
        ParamUtils.checkObj(sessionId, "sessionId");

        String userId = userManager.getUserId(sessionId);
        Study study = catalogManager.getStudyManager().resolveId(studyStr, userId);

        authorizationManager.checkStudyPermission(study.getUid(), userId, StudyAclEntry.StudyPermissions.VIEW_INDIVIDUALS);

        // Fix query if it contains any annotation
        AnnotationUtils.fixQueryAnnotationSearch(study, userId, query, authorizationManager);

        // TODO: In next release, we will have to check the count parameter from the queryOptions object.
        boolean count = true;
//        query.append(CatalogIndividualDBAdaptor.QueryParams.STUDY_UID.key(), studyId);
        QueryResult queryResult = null;
        if (count) {
            // We do not need to check for permissions when we show the count of files
            queryResult = individualDBAdaptor.rank(query, field, numResults, asc);
        }

        return ParamUtils.defaultObject(queryResult, QueryResult::new);
    }

    @Override
    public QueryResult groupBy(@Nullable String studyStr, Query query, List<String> fields, QueryOptions options, String sessionId)
            throws CatalogException {
        query = ParamUtils.defaultObject(query, Query::new);
        options = ParamUtils.defaultObject(options, QueryOptions::new);
        ParamUtils.checkObj(fields, "fields");

        String userId = userManager.getUserId(sessionId);
        Study study = catalogManager.getStudyManager().resolveId(studyStr, userId);

        Query finalQuery = new Query(query);

        // Fix query if it contains any annotation
        AnnotationUtils.fixQueryAnnotationSearch(study, userId, finalQuery, authorizationManager);
        AnnotationUtils.fixQueryOptionAnnotation(options);

        try {
            fixQuery(study, finalQuery, sessionId);
        } catch (CatalogException e) {
            // Any of mother, father or sample ids or names do not exist or were not found
            return new QueryResult<>(null);
        }

        // Add study id to the query
        finalQuery.put(IndividualDBAdaptor.QueryParams.STUDY_UID.key(), study.getUid());

        QueryResult queryResult = individualDBAdaptor.groupBy(finalQuery, fields, options, userId);

        return ParamUtils.defaultObject(queryResult, QueryResult::new);
    }


    // **************************   ACLs  ******************************** //
    public List<QueryResult<IndividualAclEntry>> getAcls(String studyStr, List<String> individualList, String member,
                                                         boolean silent, String sessionId) throws CatalogException {
        List<QueryResult<IndividualAclEntry>> individualAclList = new ArrayList<>(individualList.size());

        for (String individual : individualList) {
            try {
                MyResource<Individual> resource = getUid(individual, studyStr, sessionId);

                QueryResult<IndividualAclEntry> allIndividualAcls;
                if (StringUtils.isNotEmpty(member)) {
                    allIndividualAcls = authorizationManager.getIndividualAcl(resource.getStudy().getUid(), resource.getResource().getUid(),
                            resource.getUser(), member);
                } else {
                    allIndividualAcls = authorizationManager.getAllIndividualAcls(resource.getStudy().getUid(),
                            resource.getResource().getUid(), resource.getUser());
                }
                allIndividualAcls.setId(individual);
                individualAclList.add(allIndividualAcls);
            } catch (CatalogException e) {
                if (silent) {
                    individualAclList.add(new QueryResult<>(individual, 0, 0, 0, "", e.toString(), new ArrayList<>(0)));
                } else {
                    throw e;
                }
            }
        }
        return individualAclList;
    }

    public List<QueryResult<IndividualAclEntry>> updateAcl(String studyStr, List<String> individualList, String memberIds,
                                                           Individual.IndividualAclParams aclParams, String sessionId)
            throws CatalogException {
        int count = 0;
        count += individualList != null && !individualList.isEmpty() ? 1 : 0;
        count += StringUtils.isNotEmpty(aclParams.getSample()) ? 1 : 0;

        if (count > 1) {
            throw new CatalogException("Update ACL: Only one of these parameters are allowed: individual or sample per query.");
        } else if (count == 0) {
            throw new CatalogException("Update ACL: At least one of these parameters should be provided: individual or sample");
        }

        if (aclParams.getAction() == null) {
            throw new CatalogException("Invalid action found. Please choose a valid action to be performed.");
        }

        List<String> permissions = Collections.emptyList();
        if (StringUtils.isNotEmpty(aclParams.getPermissions())) {
            permissions = Arrays.asList(aclParams.getPermissions().trim().replaceAll("\\s", "").split(","));
            checkPermissions(permissions, IndividualAclEntry.IndividualPermissions::valueOf);
        }

        if (StringUtils.isNotEmpty(aclParams.getSample())) {
            Query query = new Query(IndividualDBAdaptor.QueryParams.SAMPLES.key(), aclParams.getSample());
            QueryOptions options = new QueryOptions(QueryOptions.INCLUDE, IndividualDBAdaptor.QueryParams.ID.key());
            QueryResult<Individual> indQueryResult = catalogManager.getIndividualManager().get(studyStr, query, options, sessionId);

            individualList = indQueryResult.getResult().stream().map(Individual::getId).collect(Collectors.toList());
        }

        // Obtain the resource ids
        MyResources<Individual> resource = getUids(individualList, studyStr, sessionId);

        authorizationManager.checkCanAssignOrSeePermissions(resource.getStudy().getUid(), resource.getUser());

        // Validate that the members are actually valid members
        List<String> members;
        if (memberIds != null && !memberIds.isEmpty()) {
            members = Arrays.asList(memberIds.split(","));
        } else {
            members = Collections.emptyList();
        }
        authorizationManager.checkNotAssigningPermissionsToAdminsGroup(members);
        checkMembers(resource.getStudy().getUid(), members);
//        studyManager.membersHavePermissionsInStudy(resourceIds.getStudyId(), members);

        List<QueryResult<IndividualAclEntry>> queryResults;
        switch (aclParams.getAction()) {
            case SET:
                // Todo: Remove this in 1.4
                List<String> allIndividualPermissions = EnumSet.allOf(IndividualAclEntry.IndividualPermissions.class)
                        .stream()
                        .map(String::valueOf)
                        .collect(Collectors.toList());
                queryResults = authorizationManager.setAcls(resource.getStudy().getUid(), resource.getResourceList().stream()
                                .map(Individual::getUid).collect(Collectors.toList()), members, permissions,
                        allIndividualPermissions, Entity.INDIVIDUAL);
                if (aclParams.isPropagate()) {
                    List<String> sampleIds = getSamplesFromIndividuals(resource);
                    if (sampleIds.size() > 0) {
                        Sample.SampleAclParams sampleAclParams = new Sample.SampleAclParams(aclParams.getPermissions(),
                                AclParams.Action.SET, null, null, null);
                        catalogManager.getSampleManager().updateAcl(studyStr, sampleIds, memberIds, sampleAclParams, sessionId);
                    }
                }
                break;
            case ADD:
                queryResults = authorizationManager.addAcls(resource.getStudy().getUid(), resource.getResourceList().stream()
                        .map(Individual::getUid).collect(Collectors.toList()), members, permissions, Entity.INDIVIDUAL);
                if (aclParams.isPropagate()) {
                    List<String> sampleIds = getSamplesFromIndividuals(resource);
                    if (sampleIds.size() > 0) {
                        Sample.SampleAclParams sampleAclParams = new Sample.SampleAclParams(aclParams.getPermissions(),
                                AclParams.Action.ADD, null, null, null);
                        catalogManager.getSampleManager().updateAcl(studyStr, sampleIds, memberIds, sampleAclParams, sessionId);
                    }
                }
                break;
            case REMOVE:
                queryResults = authorizationManager.removeAcls(resource.getResourceList().stream().map(Individual::getUid)
                        .collect(Collectors.toList()), members, permissions, Entity.INDIVIDUAL);
                if (aclParams.isPropagate()) {
                    List<String> sampleIds = getSamplesFromIndividuals(resource);
                    if (CollectionUtils.isNotEmpty(sampleIds)) {
                        Sample.SampleAclParams sampleAclParams = new Sample.SampleAclParams(aclParams.getPermissions(),
                                AclParams.Action.REMOVE, null, null, null);
                        catalogManager.getSampleManager().updateAcl(studyStr, sampleIds, memberIds, sampleAclParams, sessionId);
                    }
                }
                break;
            case RESET:
                queryResults = authorizationManager.removeAcls(resource.getResourceList().stream().map(Individual::getUid)
                        .collect(Collectors.toList()), members, null, Entity.INDIVIDUAL);
                if (aclParams.isPropagate()) {
                    List<String> sampleIds = getSamplesFromIndividuals(resource);
                    if (CollectionUtils.isNotEmpty(sampleIds)) {
                        Sample.SampleAclParams sampleAclParams = new Sample.SampleAclParams(aclParams.getPermissions(),
                                AclParams.Action.RESET, null, null, null);
                        catalogManager.getSampleManager().updateAcl(studyStr, sampleIds, memberIds, sampleAclParams, sessionId);
                    }
                }
                break;
            default:
                throw new CatalogException("Unexpected error occurred. No valid action found.");
        }

        return queryResults;
    }

    public FacetQueryResult facet(String studyStr, Query query, QueryOptions queryOptions, boolean defaultStats, String sessionId)
            throws CatalogException, IOException {
        ParamUtils.defaultObject(query, Query::new);
        ParamUtils.defaultObject(queryOptions, QueryOptions::new);

        if (defaultStats || StringUtils.isEmpty(queryOptions.getString(QueryOptions.FACET))) {
            String facet = queryOptions.getString(QueryOptions.FACET);
            queryOptions.put(QueryOptions.FACET, StringUtils.isNotEmpty(facet) ? defaultFacet + ";" + facet : defaultFacet);
        }

        CatalogSolrManager catalogSolrManager = new CatalogSolrManager(catalogManager);

        String userId = userManager.getUserId(sessionId);
        // We need to add variableSets and groups to avoid additional queries as it will be used in the catalogSolrManager
        Study study = catalogManager.getStudyManager().resolveId(studyStr, userId, new QueryOptions(QueryOptions.INCLUDE,
                Arrays.asList(StudyDBAdaptor.QueryParams.VARIABLE_SET.key(), StudyDBAdaptor.QueryParams.GROUPS.key())));

        AnnotationUtils.fixQueryAnnotationSearch(study, userId, query, authorizationManager);

        return catalogSolrManager.facetedQuery(study, CatalogSolrManager.INDIVIDUAL_SOLR_COLLECTION, query, queryOptions, userId);
    }

    // **************************   Private methods  ******************************** //

    private List<String> getSamplesFromIndividuals(MyResources<Individual> resource) throws CatalogDBException {
        // Look for all the samples belonging to the individual
        Query query = new Query()
                .append(IndividualDBAdaptor.QueryParams.STUDY_UID.key(), resource.getStudy().getUid())
                .append(IndividualDBAdaptor.QueryParams.UID.key(), resource.getResourceList().stream().map(Individual::getUid)
                        .collect(Collectors.toList()));

        QueryResult<Individual> individualQueryResult = individualDBAdaptor.get(query,
                new QueryOptions(QueryOptions.INCLUDE, IndividualDBAdaptor.QueryParams.SAMPLES.key()));

        List<String> sampleIds = new ArrayList<>();
        for (Individual individual : individualQueryResult.getResult()) {
            sampleIds.addAll(individual.getSamples().stream().map(Sample::getId).collect(Collectors.toList()));
        }

        return sampleIds;
    }

    // Checks if father or mother are in query and transforms them into father.id and mother.id respectively

    private void fixQuery(Study study, Query query, String sessionId) throws CatalogException {
        if (StringUtils.isNotEmpty(query.getString(IndividualDBAdaptor.QueryParams.FATHER.key()))) {
            MyResource resource = getUid(query.getString(IndividualDBAdaptor.QueryParams.FATHER.key()), study.getFqn(), sessionId);
            query.remove(IndividualDBAdaptor.QueryParams.FATHER.key());
            query.append(IndividualDBAdaptor.QueryParams.FATHER_UID.key(), resource.getResource().getUid());
        }
        if (StringUtils.isNotEmpty(query.getString(IndividualDBAdaptor.QueryParams.MOTHER.key()))) {
            MyResource resource = getUid(query.getString(IndividualDBAdaptor.QueryParams.MOTHER.key()), study.getFqn(), sessionId);
            query.remove(IndividualDBAdaptor.QueryParams.MOTHER.key());
            query.append(IndividualDBAdaptor.QueryParams.MOTHER_UID.key(), resource.getResource().getUid());
        }
        if (StringUtils.isNotEmpty(query.getString(IndividualDBAdaptor.QueryParams.SAMPLES.key()))) {
            MyResources<Sample> resource = catalogManager.getSampleManager().getUids(
                    query.getString(IndividualDBAdaptor.QueryParams.SAMPLES.key()), study.getFqn(), sessionId);
            query.remove(IndividualDBAdaptor.QueryParams.SAMPLES.key());
            query.append(IndividualDBAdaptor.QueryParams.SAMPLE_UIDS.key(), resource.getResourceList().stream().map(Sample::getUid)
                    .collect(Collectors.toList()));
        }
    }

}
