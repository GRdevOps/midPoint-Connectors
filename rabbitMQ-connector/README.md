# rabbitMQ Connector

This connector will grab all the messages from a rabbitMQ queue and process them in the IDM system.

To setup the schema you must point it to a text file that contains your schema information using the following format:

Name,Creatable,Updateable,Readable,Required,Multivalued
fullName,TRUE,true,TRUE,false,false
givenName,TRUE,true,TRUE,false,TRUE
familyName,TRUE,true,TRUE,false,TRUE
fakeValue,TRUE,true,TRUE,false,TRUE
newValue,TRUE,True,True,false,false