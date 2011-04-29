GETTING STARTED

GLASSFISH CONFIGURATION

The following configuration changes must be made on the Glassfish server to which
the EAR file is being deployed:

1.  A JMS connection factory must be created with the following details:
    a) "Name" set to "ConnectionFactory"
    b) "Resource Type" set to "java.jmx.TopicConnectionFactory"
    b) "Transaction Support" set to "XATransaction"

2.  A new JMS "destination resource" must be created with the following details:
    a) "Name" set to "MultiGame"
    b) "Resource Type" set to "javax.jms.Topic"
    c) All other fields may remain in default state.

3.  Security

    A.  The "Default Principal to role mapping" option must be checked on the security
        configuration screen.
    B.  A JMS user must be created in the file-realm for secure messaging.
        1.  Click on the "file" security realm.
        2.  Click on manage existing users.
        3.  Add the user "j2ee" with any password (authentication is not checked
            by password for a MessageBean.
    C.  At least one user must be created in the "file-realm" of JAAS for logging in
        and playing the games.
        1.  Click on the "file" security realm.
        2.  Click on manage existing users.
        3.  Add a user with the group admin, the username you want and the password.

    Note:  security can be modified within the build to work with any other JAAS
    components available on the Server.  The "j2ee" user is required for the
    message bean to be able to receive messages and message other secured EJB
    components.

JBOSS CONFIGURATION

1. Hibernate is the default provider for JBoss, but Derby is not the default
   database. Users can install derby on the target system, or simply change the
   hibernate dialect to work with the default JBoss database (hsql):  

   <property name="hibernate.dialect" value="org.hibernate.dialect.HSQLDialect"/>

2. Users must modify the persistence.xml file to use the "java:" + jndi naming
   convention that the JBoss server uses for finding the local datasource.

3. HornetQ. A "j2ee" user in the "admin" group must be created for MDB authentication.
   The connection factory, "ConnectionFactory" must be configured for publishing to the
   Topic "MultiGame" as described above.  The ConnectionFactory must be configured to
   participate in XATransactions within the application server.

 FLEXMOJOS INTERNATIONALIZATION CONFIGURATION

 This project is internationalized in English and Spanish, so it requires access to
 the Spanish flex framework locale files.  

 Unfortunately flex mojos requires localized versions of the flashplayer core libraries. These must be imported from the us_US bundle.

 mvn install:install-file -DgroupId=com.adobe.flex.framework -DartifactId=flash-integration -Dversion=4.1.0.16076 -Dclassifier=es_ES -Dpackaging=rb.swc -Dfile=/path/to/flex-sdk-4.1.0.16076/frameworks/locale/en_US/flash-integration_rb.swc

 mvn install:install-file -DgroupId=com.adobe.flex.framework -DartifactId=playerglobal -Dversion=4.1.0.16076 -Dclassifier=es_ES -Dpackaging=rb.swc -Dfile=/path/to/flex-sdk-4.1.0.16076/frameworks/locale/en_US/playerglobal_rb.swc

 For more information:
    http://groups.google.com/group/flex-mojos/browse_thread/thread/5b5ff62290d1cb56/d7013abdae604828


  FLEX CONFIGURATION

  AS3ISOLIB

  as3isolib will need to be installed locally.  Please download a copy of the swc
  file from google code:

  http://as3isolib.googlecode.com/svn/trunk/fp10/download/as3isolib.zip

  Use the maven install plugin to install the dependency locally:

  %> mvn install:install-file -DgroupId=as3isolib -DartifactId=as3isolib -Dversion=1.0 -Dpackaging=swc -Dfile=as3isolib.v1.core.swc


  FRAMEWORK RUNTIME SHARED LIBRARIES

  This project uses signed framework libraries distributed by adobe and not present in public repositories for legal reasons.

  These must be imported into your local repository

  mvn install:install-file -DgroupId=com.adobe.flex.framework -DartifactId=textLayout -Dversion=4.1.0.16076 -Dpackaging=swz -Dfile=/path/to/flex-sdk-4.1.0.16076/frameworks/rsls/textLayout_1.1.0.604.swz  

  mvn install:install-file -DgroupId=com.adobe.flex.framework -DartifactId=framework -Dversion=4.1.0.16076 -Dpackaging=swz -Dfile=/path/to/flex-sdk-4.1.0.16076/frameworks/rsls/framework_1.1.0.604.swz

  mvn install:install-file -DgroupId=com.adobe.flex.framework -DartifactId=spark -Dversion=4.1.0.16076 -Dpackaging=swz -Dfile=/path/to/flex-sdk-4.1.0.16076/frameworks/rsls/spark_1.1.0.604.swz

  mvn install:install-file -DgroupId=com.adobe.flex.framework -DartifactId=sparkskins -Dversion=4.1.0.16076 -Dpackaging=swz -Dfile=/path/to/flex-sdk-4.1.0.16076/frameworks/rsls/sparkskins_1.1.0.604.swz

  mvn install:install-file -DgroupId=com.adobe.flex.framework -DartifactId=rpc -Dversion=4.1.0.16076 -Dpackaging=swz -Dfile=/path/to/flex-sdk-4.1.0.16076/frameworks/rsls/rpc_1.1.0.604.swz


  FLEXMOJOS FRAMEWORK LOCALIZATION

  Unfortunately flex mojos requires localized versions of the flashplayer 
  core libraries. These must be imported from the us_US bundle.

  mvn install:install-file -DgroupId=com.adobe.flex.framework -DartifactId=flash-integration -Dversion=4.1.0.16076 -Dclassifier=es_ES -Dpackaging=rb.swc -Dfile=/path/to/flex-sdk-4.1.0.16076/frameworks/locale/en_US/flash-integration_rb.swc

  mvn install:install-file -DgroupId=com.adobe.flex.framework -DartifactId=playerglobal -Dversion=4.1.0.16076 -Dclassifier=es_ES -Dpackaging=rb.swc -Dfile=/path/to/flex-sdk-4.1.0.16076/frameworks/locale/en_US/playerglobal_rb.swc

  For more information:
    http://groups.google.com/group/flex-mojos/browse_thread/thread/5b5ff62290d1cb56/d7013abdae604828


  All other dependencies are managed by means of Maven.



  MAVEN CONFIGURATION
 
  This prototype requires the following environmental changes be made, before being
  built and deployed.

  The environmental property "GLASSFISH_HOME" must be set by the builder to
  point to the top-level directory of glassfish-v2.1.

  For example, in BASH, you would do the following:

  export GLASSFISH_HOME=/home/myuser/glassfish


 **** IMPORTANT ****
 
 Before deployment, the local Glassfish Derby database must be up and running:
 
 %> asadmin start-database
