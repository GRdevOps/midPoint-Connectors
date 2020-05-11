package edu.davenport.edu;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.annotation.processing.FilerException;

import org.identityconnectors.common.logging.Log;
import org.identityconnectors.common.security.GuardedString;
import org.identityconnectors.framework.api.operations.APIOperation;
import org.identityconnectors.framework.api.operations.ResolveUsernameApiOp;
import org.identityconnectors.framework.common.exceptions.ConnectorException;
import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.AttributeInfo;
import org.identityconnectors.framework.common.objects.AttributeInfoBuilder;
import org.identityconnectors.framework.common.objects.ConnectorObjectBuilder;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.ObjectClassInfo;
import org.identityconnectors.framework.common.objects.OperationOptionInfo;
import org.identityconnectors.framework.common.objects.OperationOptions;
import org.identityconnectors.framework.common.objects.ResultsHandler;
import org.identityconnectors.framework.common.objects.Schema;
import org.identityconnectors.framework.common.objects.SchemaBuilder;
import org.identityconnectors.framework.common.objects.SyncResultsHandler;
import org.identityconnectors.framework.common.objects.SyncToken;
import org.identityconnectors.framework.common.objects.Uid;
import org.identityconnectors.framework.common.objects.filter.AbstractFilterTranslator;
import org.identityconnectors.framework.common.objects.filter.FilterTranslator;
import org.identityconnectors.framework.spi.Configuration;
import org.identityconnectors.framework.spi.Connector;
import org.identityconnectors.framework.spi.ConnectorClass;
import org.identityconnectors.framework.spi.operations.AuthenticateOp;
import org.identityconnectors.framework.spi.operations.CreateOp;
import org.identityconnectors.framework.spi.operations.DeleteOp;
import org.identityconnectors.framework.spi.operations.SchemaOp;
import org.identityconnectors.framework.spi.operations.SearchOp;
import org.identityconnectors.framework.spi.operations.SyncOp;
import org.identityconnectors.framework.spi.operations.TestOp;
import org.identityconnectors.framework.spi.operations.UpdateAttributeValuesOp;
import org.identityconnectors.framework.spi.operations.UpdateOp;


import com.rabbitmq.client.*;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 * This RabbitMQ connector provides (empty) implementations for all ConnId operations, but this is not mandatory: any
 * connector can choose which operations are actually to be implemented.
 */
@ConnectorClass(configurationClass = RabbitMQConfiguration.class, displayNameKey = "RabbitM.connector.display")
public class RabbitMQConnector implements Connector,
        CreateOp, UpdateOp, UpdateAttributeValuesOp, DeleteOp,
        AuthenticateOp, ResolveUsernameApiOp, SchemaOp, SyncOp, TestOp, SearchOp<RabbitMQFilter> {

    private static final Log LOG = Log.getLog(RabbitMQConnector.class);

    private RabbitMQConfiguration configuration;

    @Override
    public RabbitMQConfiguration getConfiguration() {
        return configuration;
    }

    @Override
    public void init(final Configuration configuration) {
        this.configuration = (RabbitMQConfiguration) configuration;
        LOG.ok("Connector {0} successfully inited", getClass().getName());
    }

    @Override
    public void dispose() {
        //TODO
    }

    @Override
    public Schema schema() {

        LOG.info("Schema started");

        SchemaBuilder builder = new SchemaBuilder(RabbitMQConnector.class);
        Set<AttributeInfo> attributes= new HashSet<AttributeInfo>();

        //Init variables
        String line = "";
        String cvsSplitBy = ",";
        BufferedReader br;

        try {
            String csvFile = configuration.getSchemaConfigFile();
            br = new BufferedReader(new FileReader(csvFile));

            // skip header line
            br.readLine();
    
            while ((line = br.readLine()) != null) {
    
                // use comma as separator
                String[] attribute = line.split(cvsSplitBy);
                
                AttributeInfoBuilder attrInfo = new AttributeInfoBuilder(attribute[0], String.class);
                attrInfo.setCreateable(attribute[1].toLowerCase().equals("true"));
                attrInfo.setUpdateable(attribute[2].toLowerCase().equals("true"));
                attrInfo.setReadable(attribute[3].toLowerCase().equals("true"));
                attrInfo.setRequired(attribute[4].toLowerCase().equals("true"));
                attrInfo.setMultiValued(attribute[5].toLowerCase().equals("true"));
                attributes.add(attrInfo.build());
            }

        } catch (Exception e){
            LOG.error("Schema update stopped");
            LOG.error(e.getMessage());
            LOG.error("" + e.getCause());
            LOG.info("" + e.getStackTrace());
        }
    
        builder.defineObjectClass(ObjectClass.ACCOUNT.getDisplayNameKey(),attributes);        
        LOG.info(">>> schema finished");

        return builder.build();
    }

    @Override
    public void test() {
        //TODO
    }

    @Override
    public FilterTranslator<RabbitMQFilter> createFilterTranslator(
            final ObjectClass objectClass,
            final OperationOptions options) {

        return new AbstractFilterTranslator<RabbitMQFilter>() {
        };
    }

    @Override
    public void executeQuery(
        
        final ObjectClass objectClass,
        final RabbitMQFilter query,
        final ResultsHandler handler,
        final OperationOptions options) {

        LOG.info(">>> executeQuery ");

        // Init variables
        Connection connection = null;
        BufferedReader br = null;
        List<String> fields = new ArrayList<String>();
        String line = "";
        String cvsSplitBy = ",";

        try {
            // Setup Buffered Reader for CSV File
            String csvFile = configuration.getSchemaConfigFile();
            br = new BufferedReader(new FileReader(csvFile));

            // Skip header
            br.readLine();
    
            while ((line = br.readLine()) != null) {

                // use comma as separator
                String[] attribute = line.split(cvsSplitBy);
                fields.add(attribute[0]);
            }

            br.close();;

            // Setup connection with RabbitMQ
            LOG.info("Setting up new RabbitMQ Connection");
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(configuration.getServerName());
            factory.setUsername(configuration.getUserName());
            factory.setPassword(configuration.getPassword());
            connection = factory.newConnection();
            Channel channel = connection.createChannel();

            boolean autoAck = false;
            boolean loop = true;

            while(loop){
                GetResponse response = channel.basicGet(configuration.getQueueName(), autoAck);

                if (response == null){
                    loop = false;
                    LOG.info("Queue is empty");
                } else {
                    String message = new String(response.getBody(), "UTF-8");
                    long deliveryTag = response.getEnvelope().getDeliveryTag();
                    channel.basicAck(deliveryTag, false);
                    ConnectorObjectBuilder builder = new ConnectorObjectBuilder();
                    JSONParser jsonParser = new JSONParser();
                    JSONObject jsonObject = (JSONObject) jsonParser.parse(message);

                    if (jsonObject.containsKey(configuration.getUIDAttribute()) && jsonObject.get(configuration.getUIDAttribute()) != null && !jsonObject.get(configuration.getUIDAttribute()).equals("")){
                        builder.setUid((String)jsonObject.get(configuration.getUIDAttribute()));
                    }

                    if (jsonObject.containsKey(configuration.getNameAttribute()) && jsonObject.get(configuration.getNameAttribute()) != null && !jsonObject.get(configuration.getNameAttribute()).equals("")){
                        builder.setName((String)jsonObject.get(configuration.getNameAttribute()));
                    }

                    for (String field : fields) {
                        if (jsonObject.containsKey(field) && jsonObject.get(field) != null && !jsonObject.get(field).equals("")){
                            builder.addAttribute(field, jsonObject.get(field));
                        }
                    }
                }
            }

            connection.close();

        } catch (Exception e){
            LOG.error("Failed to initialize connection with RabbitMQ");
            LOG.error(e.getMessage());
            LOG.error("" + e.getCause());
            LOG.error("" + e.getStackTrace());
            throw new ConnectorException("Something went wrong");
        } finally{
            try{
                br.close();
            } catch(Exception e){}
            try{
                connection.close();
            } catch(Exception e){}
        }
    }

    //////////////////////////////////////////////////////////////////
    //                  Currently Not Implemented                   //
    //////////////////////////////////////////////////////////////////

    @Override
    public void sync(
            final ObjectClass objectClass,
            final SyncToken token,
            final SyncResultsHandler handler,
            final OperationOptions options) {
    }

    @Override
    public SyncToken getLatestSyncToken(final ObjectClass objectClass) {
        return new SyncToken(null);
    }

    @Override
    public Uid create(
            final ObjectClass objectClass,
            final Set<Attribute> createAttributes,
            final OperationOptions options) {

        return new Uid(UUID.randomUUID().toString());
    }

    @Override
    public Uid update(
            final ObjectClass objectClass,
            final Uid uid,
            final Set<Attribute> replaceAttributes,
            final OperationOptions options) {

        return uid;
    }

    @Override
    public Uid addAttributeValues(
            final ObjectClass objclass,
            final Uid uid,
            final Set<Attribute> valuesToAdd,
            final OperationOptions options) {

        return uid;
    }

    @Override
    public Uid removeAttributeValues(
            final ObjectClass objclass,
            final Uid uid,
            final Set<Attribute> valuesToRemove,
            final OperationOptions options) {

        return uid;
    }

    @Override
    public void delete(
            final ObjectClass objectClass,
            final Uid uid,
            final OperationOptions options) {
    }

    @Override
    public Uid authenticate(
            final ObjectClass objectClass,
            final String username,
            final GuardedString password,
            final OperationOptions options) {

        return new Uid(username);
    }

    @Override
    public Uid resolveUsername(
            final ObjectClass objectClass,
            final String username,
            final OperationOptions options) {

        return new Uid(username);
    }

    ////////////////////////////////////////////////////////////
    //                    Helper Functions                    //
    ////////////////////////////////////////////////////////////
    
}