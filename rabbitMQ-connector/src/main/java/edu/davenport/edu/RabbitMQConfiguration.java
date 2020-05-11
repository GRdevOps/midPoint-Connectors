package edu.davenport.edu;

import org.identityconnectors.common.StringUtil;
import org.identityconnectors.framework.common.exceptions.ConfigurationException;
import org.identityconnectors.framework.common.exceptions.ConnectorException;
import org.identityconnectors.framework.spi.AbstractConfiguration;
import org.identityconnectors.framework.spi.ConfigurationProperty;

public class RabbitMQConfiguration extends AbstractConfiguration {

    // Fields that show up in the midPoint GUI
    private String UserName;
    private String Password;
    private String QueueName;
    private String ServerName;
    private String SchemaConfigFile;
    private String UIDAttribute;
    private String NameAttribute;

    // Display name and help tip of fields
    private final String userName_display = "User name";
    private final String userName_help = "RabbitMQ Service Account Username";
    private final String password_display = "Password";
    private final String password_help = "RabbitMQ Service Account Password";
    private final String queueName_display = "Queue name";
    private final String queueName_help = "RabbitMQ queue name which holds messages for midPoint";
    private final String serverName_display = "Server name";
    private final String serverName_help = "IP address or name that resolves to an IP address of the RabbitMQ Server";
    private final String schemaConfigFile_display = "Schema config";
    private final String schemaConfigFile_help = "Path where the schema config file is located";
    private final String uidAttribute_display = "UID Attribute";
    private final String uidAttribute_help = "Attribute assigned as the UID in midPoint";
    private final String nameAttribute_display = "Name Attribute";
    private final String nameAttribute_help = "Attribute assigned as the Name in midPoint";

    @ConfigurationProperty(displayMessageKey = userName_display,
            helpMessageKey = userName_help)
    public String getUserName() {
        return UserName;
    }

    @ConfigurationProperty(displayMessageKey = password_display,
            helpMessageKey = password_help)
    public String getPassword() {
        return Password;
    }

    @ConfigurationProperty(displayMessageKey = queueName_display,
            helpMessageKey = queueName_help)
    public String getQueueName() {
        return QueueName;
    }

    @ConfigurationProperty(displayMessageKey = serverName_display,
            helpMessageKey = serverName_help)
    public String getServerName() {
        return ServerName;
    }

    @ConfigurationProperty(displayMessageKey = schemaConfigFile_display,
            helpMessageKey = schemaConfigFile_help)
    public String getSchemaConfigFile() {
        return SchemaConfigFile;
    }

    @ConfigurationProperty(displayMessageKey = uidAttribute_display,
            helpMessageKey = uidAttribute_help)
    public String getUIDAttribute() {
        return UIDAttribute;
    }

    @ConfigurationProperty(displayMessageKey = nameAttribute_display,
            helpMessageKey = nameAttribute_help)
    
    public String getNameAttribute() {
        return NameAttribute;
    }

    public void setUserName(String UserName) {
        this.UserName = UserName;
    }

    public void setPassword(String Password) {
        this.Password = Password;
    }

    public void setQueueName(String QueueName) {
        this.QueueName = QueueName;
    }

    public void setServerName(String ServerName) {
        this.ServerName = ServerName;
    }

    public void setSchemaConfigFile(String SchemaConfigFile) {
        this.SchemaConfigFile = SchemaConfigFile;
    }

    public void setUIDAttribute(String UIDAttribute) {
        this.UIDAttribute = UIDAttribute;
    }

    public void setNameAttribute(String NameAttribute) {
        this.NameAttribute = NameAttribute;
    }

    @Override
    public void validate() {
        if (StringUtil.isBlank(UserName)) {
            throw new ConfigurationException("UserName must not be blank!");
        }

        if (StringUtil.isBlank(Password)) {
            throw new ConfigurationException("Password must not be blank!");
        }

        if (StringUtil.isBlank(QueueName)) {
            throw new ConfigurationException("QueueName must not be blank!");
        }

        if (StringUtil.isBlank(ServerName)) {
            throw new ConfigurationException("ServerName must not be blank!");
        }

        if (StringUtil.isBlank(SchemaConfigFile)) {
            throw new ConfigurationException("SchemaConfigFile must not be blank!");
        }

        if (StringUtil.isBlank(UIDAttribute)) {
            throw new ConfigurationException("UIDAttribute must not be blank!");
        }

        if (StringUtil.isBlank(NameAttribute)) {
            throw new ConfigurationException("NameAttribute must not be blank!");
        }
    }

}
