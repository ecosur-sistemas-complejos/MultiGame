GETTING STARTED

This README requires you to have installed the following products:

  AS3ISOLIB

  as3isolib will need to be installed locally.  Please download a copy of the swc
  file from google code:

  http://as3isolib.googlecode.com/svn/trunk/fp10/download/as3isolib.zip

  Use the maven install plugin to install the dependency locally:

  %> mvn install:install-file -DgroupId=as3isolib -DartifactId=as3isolib -Dversion=1.0 -Dpackaging=swc -Dfile=as3isolib.v1.core.swc

All other dependencies are managed by means of Maven.

GLASSFISH CONFIGURATION

The following configuration changes must be made on the Glassfish server to which
the EAR file is being deployed:

1.  A JMS connection factory must be created with the following details:
    a) "Name" set to "ConnectionFactory"
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
        3.  Add a user with the username you want and the password.

    Note:  security can be modified within the build to work with any other JAAS
    components available on the Server.  The "j2ee" user is required for the
    message bean to be able to receive messages and message other secured EJB
    components.

JBOSS CONFIGURATION

1. Hibernate is the default provider for JBoss, but Derby is not the default
   database. Users can install derby on the target system, or simply change the
   hibernate dialect to work with the default JBoss database.

2. Users must modify the persistence.xml file to use the "java:" + jndi naming
   convention that the JBoss server uses for finding the local datasource.

3.  HornetQ. A "j2ee" user in the "admin" group must be defined to allow


MAVEN CONFIGURATION
 
 This prototype requires the following environmental changes be made, before being
 built and deployed.

 The environmental property "GLASSFISH_HOME" must be set by the builder to
 point to the top-level directory of glassfish-v2.1.

 For example, in BASH, you would do the following:

 export GLASSFISH_HOME=/home/myuser/glassfish


NON-MAVEN LIBRARIES

  Some libraries in this build a not available on Maven repositories and will need
  to be deployed locally.  One such library is the Spanish Flex Internationalization
  files, please follow the internationalization instructions for installation below.


 **** IMPORTANT ****
 
 Before deployment, the local Glassfish Derby database must be up and running:
 
 %> asadmin start-database

 FLEXMOJOS INTERNATIONALIZATION CONFIGURATION

 This project is internationalized in English and Spanish, so it requires access to
 the Spanish flex framework locale files.  These are distributed by default in the
 3.2.0 FlexMojos maven archives, but are note available for 3.5.  You can make the
 library available locally by using the "copylocale" utility in the Flex SDK.
 Christopher Herremean does a great job detailing this process here:

 http://www.herrodius.com/blog/123

 You will then need to stage the 2 files from the copied locale(s) into your local
 maven repository.

 DEPLOYMENT

 In order to deploy the private build to a running Glassfish server, you need to
 execute with the deploy profile from the toplevel:

 %> mvn -P deploy clean install
 
 
