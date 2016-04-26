package org.opencb.opencga.storage.mongodb.variant.load;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.protobuf.InvalidProtocolBufferException;
import com.mongodb.MongoBulkWriteException;
import com.mongodb.bulk.BulkWriteError;
import com.mongodb.bulk.BulkWriteResult;
import org.bson.conversions.Bson;
import org.bson.types.Binary;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.biodata.models.variant.VariantSource;
import org.opencb.biodata.models.variant.avro.VariantAvro;
import org.opencb.biodata.models.variant.avro.VariantType;
import org.opencb.biodata.models.variant.protobuf.VcfSliceProtos;
import org.opencb.biodata.tools.variant.converter.VariantToVcfSliceConverter;
import org.opencb.biodata.tools.variant.converter.VcfSliceToVariantListConverter;
import org.opencb.commons.datastore.core.ComplexTypeConverter;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.mongodb.MongoDBCollection;
import org.opencb.commons.utils.CompressionUtils;
import org.opencb.commons.utils.CryptoUtils;
import org.opencb.opencga.storage.mongodb.variant.MongoDBVariantWriteResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.zip.DataFormatException;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Updates.*;

/**
 * Created on 07/04/16.
 *
 * @author Jacobo Coll &lt;jacobo167@gmail.com&gt;
 */
public class MongoDBVariantStageLoader {

    public static final String NEW_STUDY_FIELD = "new";
    public static final boolean NEW_STUDY_DEFAULT = true;

    private static final QueryOptions QUERY_OPTIONS = new QueryOptions(MongoDBCollection.UPSERT, true);
    private final Pattern writeResultErrorPattern = Pattern.compile("^.*dup key: \\{ : \"([^\"]*)\" \\}$");

    private final MongoDBCollection collection;
    private final int studyId;
    private final int fileId;
    private final int numTotalVariants;
    private final String fieldName;
    private final Logger logger = LoggerFactory.getLogger(MongoDBVariantStageLoader.class);

    private final AtomicInteger variantsCount;
    public static final int DEFAULT_LOGING_BATCH_SIZE = 5000;
    private final int logingBatchSize;
    private final MongoDBVariantWriteResult writeResult = new MongoDBVariantWriteResult();



    private static final ComplexTypeConverter<Variant, Binary> VARIANT_CONVERTER_JSON = new ComplexTypeConverter<Variant, Binary>() {

        private ObjectMapper mapper = new ObjectMapper();

        @Override
        public Variant convertToDataModelType(Binary object) {
            try {
                byte[] data = object.getData();
                try {
                    data = CompressionUtils.decompress(data);
                } catch (DataFormatException e) {
                    throw new RuntimeException(e);
                }
                return new Variant(mapper.readValue(data, VariantAvro.class));
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        @Override
        public Binary convertToStorageType(Variant variant) {
            byte [] data = variant.toJson().getBytes();
            try {
                data = CompressionUtils.compress(data);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
            return new Binary(data);
        }
    };

    private static final ComplexTypeConverter<Variant, Binary> VARIANT_CONVERTER_PROTO = new ComplexTypeConverter<Variant, Binary>() {
//        private final VariantToProtoVcfRecord converter = new VariantToProtoVcfRecord();
        private final VariantToVcfSliceConverter converter = new VariantToVcfSliceConverter();
        private final VcfSliceToVariantListConverter converterBack
                = new VcfSliceToVariantListConverter(new VariantSource("", "4", "4", ""));



        @Override
        public Variant convertToDataModelType(Binary object) {
            try {
                return converterBack.convert(VcfSliceProtos.VcfSlice.parseFrom(object.getData())).get(0);
            } catch (InvalidProtocolBufferException e) {
                throw new UncheckedIOException(e);
            }
        }

        @Override
        public Binary convertToStorageType(Variant object) {
            return new Binary(converter.convert(object).toByteArray());
        }
    };

    public static final ComplexTypeConverter<Variant, Binary> VARIANT_CONVERTER_DEFAULT = VARIANT_CONVERTER_JSON;

    public static final ComplexTypeConverter<Variant, Binary> BINARY_ID_CONVERTER = new ComplexTypeConverter<Variant, Binary>() {
        // Just so we don't have to copy the buffer
        final class ExposedByteArrayOutputStream extends ByteArrayOutputStream {
            public ExposedByteArrayOutputStream(int size) {
                super(size);
            }

            public synchronized byte[] getInternalByteArray() {
                return buf;
            }
        }
        @Override
        public Variant convertToDataModelType(Binary object) {
            return null;
        }

        @Override
        public Binary convertToStorageType(Variant variant) {


            int size = Math.max(variant.getChromosome().length(), 2) * Character.BYTES
                    + 1
                    + Integer.BYTES
                    + Integer.BYTES
                    + Integer.BYTES;


            ExposedByteArrayOutputStream out = new ExposedByteArrayOutputStream(size);
            DataOutputStream dataOutputStream = new DataOutputStream(out);

            try {
                if (variant.getChromosome().length() == 0) {
                    //Throw exception?
                    dataOutputStream.writeChars("  ");
                } else if (variant.getChromosome().length() == 1) {
                    dataOutputStream.writeChars(" ");
                }
                dataOutputStream.writeChars(variant.getChromosome());
                dataOutputStream.write(0);
                dataOutputStream.writeInt(variant.getStart());
                dataOutputStream.writeInt(variant.getReference().hashCode());
                dataOutputStream.writeInt(variant.getAlternate().hashCode());
//                dataOutputStream.writeChars(variant.getReference());
//                dataOutputStream.write(0);
//                dataOutputStream.writeChars(variant.getAlternate());
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }

            return new Binary(out.getInternalByteArray());
        }
    };

    public static final ComplexTypeConverter<Variant, String> STRING_ID_CONVERTER = new ComplexTypeConverter<Variant, String>() {

        @Override
        public Variant convertToDataModelType(String object) {
            String[] split = object.split(":", -1);
            return new Variant(split[0].trim(), Integer.parseInt(split[1].trim()), split[2], split[3]);
        }

        @Override public String convertToStorageType(Variant variant) {
            String reference = variant.getReference();
            String alternate = variant.getAlternate();
            if (reference.length() > Variant.SV_THRESHOLD) {
                reference = new String(CryptoUtils.encryptSha1(reference));
            }
            if (alternate.length() > Variant.SV_THRESHOLD) {
                alternate = new String(CryptoUtils.encryptSha1(alternate));
            }
            if (variant.getChromosome().length() == 1) {
                return String.format(" %s:%10d:%s:%s", variant.getChromosome(), variant.getStart(), reference, alternate);
            } else {
                return String.format("%s:%10d:%s:%s", variant.getChromosome(), variant.getStart(), reference, alternate);
            }
        }
    };

    public MongoDBVariantStageLoader(MongoDBCollection collection, int studyId, int fileId, int numTotalVariants) {
        this.collection = collection;
        this.studyId = studyId;
        this.fileId = fileId;
        this.numTotalVariants = numTotalVariants;
        fieldName = studyId + "." + fileId;
        variantsCount = new AtomicInteger(0);
        logingBatchSize = Math.max(numTotalVariants / 200, DEFAULT_LOGING_BATCH_SIZE);
    }

    public MongoDBVariantWriteResult insert(List<Variant> variants) {
        return insert(variants.stream());
    }

    public MongoDBVariantWriteResult insert(Stream<Variant> stream) {
        final long start = System.nanoTime();
        final int[] variantsLocalCount = {0};
        final int[] skippedVariants = {0};

        ListMultimap<String, Binary> ids = LinkedListMultimap.create();

        stream.forEach(variant -> {
            variantsLocalCount[0]++;
            if (variant.getType().equals(VariantType.NO_VARIATION) || variant.getType().equals(VariantType.SYMBOLIC)) {
                skippedVariants[0]++;
                return;
            }
            Binary binary = VARIANT_CONVERTER_DEFAULT.convertToStorageType(variant);
            String id = STRING_ID_CONVERTER.convertToStorageType(variant);

            ids.put(id, binary);
        });

        MongoDBVariantWriteResult result = new MongoDBVariantWriteResult();
        Set<String> retryKeys = updateMongo(ids, result, null);
        if (!retryKeys.isEmpty()) {
            updateMongo(ids, result, retryKeys);
        }

        int previousCount = variantsCount.getAndAdd(variantsLocalCount[0]);
        if ((previousCount + variantsLocalCount[0]) / logingBatchSize != previousCount / logingBatchSize) {
            logger.info("Write variants in STAGE collection " + (previousCount + variantsLocalCount[0]) + "/" + numTotalVariants + " "
                    + String.format("%.2f%%", ((float) (previousCount + variantsLocalCount[0])) / numTotalVariants * 100.0));
        }

        result.setNewVariantsNanoTime(System.nanoTime() - start)
                .setSkippedVariants(skippedVariants[0]);

        synchronized (writeResult) {
            writeResult.merge(result);
        }

        return result;
    }

    /**
     * Given a map of id -> binary[], inserts the binary objects in the stage collection.
     *
     * {
     *     <studyId> : {
     *         <fileId> : [ BinData(), BinData() ]
     *     }
     * }
     *
     * The field <fileId> is an array to detect duplicated variants within the same file.
     *
     * It may happen that an update with upsert:true fail if two different threads try to
     * update the same non existing document.
     * See https://jira.mongodb.org/browse/SERVER-14322
     *
     * In that case, the non inserted values will be returned.
     *
     * @param values        Map with all the values to insert
     * @param result        MongoDBVariantWriteResult to fill
     * @param retryIds      List of IDs to retry. If not null, only will update those documents within this set
     * @return              List of non updated documents.
     * @throws MongoBulkWriteException if the exception was not a DuplicatedKeyException (e:11000)
     */
    private Set<String> updateMongo(ListMultimap<String, Binary> values, MongoDBVariantWriteResult result, Set<String> retryIds) {

        Set<String> nonInsertedIds = Collections.emptySet();
        if (values.isEmpty()) {
            return nonInsertedIds;
        }

        List<Bson> queries = new LinkedList<>();
        List<Bson> updates = new LinkedList<>();
        for (String id : values.keySet()) {
            if (retryIds == null || retryIds.contains(id)) {
                List<Binary> binaryList = values.get(id);
                queries.add(eq("_id", id));
                if (binaryList.size() == 1) {
                    updates.add(push(fieldName, binaryList.get(0)));
                } else {
                    updates.add(pushEach(fieldName, binaryList));
                }
            }
        }

        try {
            final BulkWriteResult mongoResult = collection.update(queries, updates, QUERY_OPTIONS).first();
            result.setNewVariants(mongoResult.getInsertedCount())
                    .setUpdatedVariants(mongoResult.getModifiedCount());
        } catch (MongoBulkWriteException e) {
            result.setNewVariants(e.getWriteResult().getInsertedCount())
                    .setUpdatedVariants(e.getWriteResult().getModifiedCount());


            if (retryIds != null) {
                // If retryIds != null, means that this this was the second attempt to update. In this case, do fail.
                logger.error("BulkWriteErrors when retrying the updates");
                throw e;
            }

            nonInsertedIds = new HashSet<>();
            for (BulkWriteError writeError : e.getWriteErrors()) {
                if (writeError.getCode() == 11000) { //Dup Key error code
                    Matcher matcher = writeResultErrorPattern.matcher(writeError.getMessage());
                    if (matcher.find()) {
                        String id = matcher.group(1);
                        nonInsertedIds.add(id);
                        logger.warn("Catch error : {}",  writeError.toString());
                        logger.warn("DupKey exception inserting '{}'. Retry!", id);
                    } else {
                        logger.error("WriteError with code {} does not match with the pattern {}",
                                writeError.getCode(), writeResultErrorPattern.pattern());
                        throw e;
                    }
                } else {
                    throw e;
                }
            }
        }
        return nonInsertedIds;
    }

    public static long cleanStageCollection(MongoDBCollection stageCollection, int studyId, int fileId) {
        //Delete those studies that have duplicated variants. Those are not inserted, so they are not new variants.
        long modifiedCount = stageCollection.update(
                and(exists(studyId + "." + fileId + ".1"), exists(studyId + "." + NEW_STUDY_FIELD, false)),
                unset(Integer.toString(studyId)),
                new QueryOptions(MongoDBCollection.MULTI, true)).first().getModifiedCount();
        modifiedCount += stageCollection.update(
                exists(studyId + "." + fileId),
                combine(
//                        unset(studyId + "." + fileId),
                        set(studyId + "." + fileId, null),
                        set(studyId + "." + NEW_STUDY_FIELD, false)
                ), new QueryOptions(MongoDBCollection.MULTI, true)).first().getModifiedCount();
        return modifiedCount;
    }

    public static long cleanStageCollection(MongoDBCollection stageCollection, int studyId, List<Integer> fileIds) {
        // Delete those new studies that have duplicated variants. Those are not inserted, so they are not new variants.
        // i.e: For each file, or the file has not been loaded (empty), or the file has more than one element.
        //     { $or : [ { <study>.<file>.0 : {$exists:false} }, { <study>.<file>.1 : {$exists:true} } ] }
        List<Bson> filters = new LinkedList<>();
        filters.add(exists(studyId + "." + NEW_STUDY_FIELD, false));
        for (Integer fileId : fileIds) {
            filters.add(or(exists(studyId + "." + fileId + ".0", false), exists(studyId + "." + fileId + ".1")));
        }
        long modifiedCount = stageCollection.update(
                and(filters), unset(Integer.toString(studyId)),
                new QueryOptions(MongoDBCollection.MULTI, true)).first().getModifiedCount();


        filters.clear();
        List<Bson> updates = new LinkedList<>();
        for (Integer fileId : fileIds) {
            filters.add(exists(studyId + "." + fileId));
//            updates.add(unset(studyId + "." + fileId));
            updates.add(set(studyId + "." + fileId, null));
        }
        updates.add(set(studyId + "." + NEW_STUDY_FIELD, false));
        modifiedCount += stageCollection.update(or(filters), combine(updates), new QueryOptions(MongoDBCollection.MULTI, true))
                .first().getModifiedCount();

        return modifiedCount;
    }


    public MongoDBVariantWriteResult getWriteResult() {
        return writeResult;
    }
}
