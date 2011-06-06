GETTING STARTED

SUBMODULES

Multi-Game uses git submodules to reference the dependent projects that make it work.  
We have split off the project into four separate aspects: Deploy, Engine, Games and
Research.  Each submodule contains it's entire history from the onset of the project
and will allow us to publish our finished games and research more quickly.

In order to get the master project working, please read the following instructions:
 
    Pulling down the submodules is a two-step process. First run git submodule
    init to add the submodule repository URLs to .git/config:

    $ git submodule init
    Now use git-submodule update to clone the repositories and check out the commits
    specified in the superproject:

    $ git submodule update
    $ cd a
    $ ls -a
    .  ..  .git  a.txt

    One major difference between git-submodule update and git-submodule add is that
    git-submodule update checks out a specific commit, rather than the tip of a
    branch. It's like checking out a tag: the head is detached, so you're not working
    on a branch.

    $ git branch
    * (no branch)
    master

    If you want to make a change within a submodule and you have a detached head, then
    you should create or checkout a branch, make your changes, publish the change within
    the submodule, and then update the superproject to reference the new commit:

    $ git checkout master
    or

    $ git checkout -b fix-up
    [http://book.git-scm.com/5_submodules.html]

NOTE: this process will only work if you have access to the downstream submodules.  
Access is currently reserved only for project committers (this should change quickly,
as I am in the process of modifying our source code to seperate out unpublished games
and techniques).


JBOSS AS6 CONFIGURATION

1. Persistence.xml.

   Hibernate is the default provider for JBoss, but Derby is not the default
   database. Users can install derby on the target system, or simply use the
   configured hibernate dialect to work with the default JBoss database (hsql):

   <property name="hibernate.dialect" value="org.hibernate.dialect.HSQLDialect"/>

   The datasource name may need to be changed.  Be sure to examine that the
   local persistence.xml file points to the correct datasource, "java:DefaultDS"
   for JBoss.

2. Local users and roles.  The file "jbossws-users.properties" controls user access to
   the server as part of the local file domain.  Please edit this file to contain the
   users and passwords that you wish to authenticate.  The file "jbossws-roles.properties"
   must be modified to contain the username + "=" and the group "admin" for all users
   created in the jbossws-users.properties file that need access to multi-game.

GLASSFISH 3.1 CONFIGURATION

The following configuration changes must be made on the Glassfish server to which
the EAR file is being deployed:


1.  Persistence.xml.

    Hibernate must be configured as the provider in the persistence.xml file.
    In addition, the correct datasource name must be specified. Please make the following
    modifications.  From:

        <!--provider>org.hibernate.ejb.HibernatePersistence</provider-->
        <!--jta-data-source>jdbc/__default</jta-data-source-->
        <jta-data-source>java:DefaultDS</jta-data-source>

    To:

        <provider>org.hibernate.ejb.HibernatePersistence</provider>
        <jta-data-source>jdbc/__default</jta-data-source>
        <!--jta-data-source>java:DefaultDS</jta-data-source-->

2.  A JMS connection factory must be created with the following details:
    a) "Name" set to "XAConnectionFactory"
    b) "Resource Type" set to "java.jmx.TopicConnectionFactory"
    b) "Transaction Support" set to "XATransaction"

3.  A new JMS "destination resource" must be created with the following details:
    a) "JNDI Name" set to "MultiGame"
    b) "Physical Destination Name" set to "MultiGame"
    c) "Resource Type" set to "javax.jms.Topic"
    d) All other fields may remain in default state.

4.  Security

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

5.  Hibernate

    We use non-jpa specific Hibernate annotations in the internal ordering of several
    entity collections, and therefore, must use hibernate as a persistence provider.
    To deploy on Glassfish, you will need to install and enable it via the "Update Tool"
    provided on the bottom-left hand side of the Admin screen.  Select and check "hibernate"
    from the available frameworks and click install.

    You will need to restart the server to make hibernate available for the application.

6.  Database.

    **** IMPORTANT ****
    Before deployment, the local Glassfish Derby database must be up and running:

    %> asadmin start-database


FLEXMOJOS INTERNATIONALIZATION CONFIGURATION

 This project is internationalized in English and Spanish, so it requires access to
 the Spanish flex framework locale files.  

 Unfortunately flex mojos requires localized versions of the flashplayer core libraries. 
 These must be imported from the us_US bundle.

 mvn install:install-file -DgroupId=com.adobe.flex.framework -DartifactId=flash-integration
     -Dversion=4.1.0.16076 -Dclassifier=es_ES -Dpackaging=rb.swc
     -Dfile=/path/to/flex-sdk-4.1.0.16076/frameworks/locale/en_US/flash-integration_rb.swc

 mvn install:install-file -DgroupId=com.adobe.flex.framework -DartifactId=playerglobal
    -Dversion=4.1.0.16076 -Dclassifier=es_ES -Dpackaging=rb.swc
    -Dfile=/path/to/flex-sdk-4.1.0.16076/frameworks/locale/en_US/playerglobal_rb.swc

 For more information:
    http://groups.google.com/group/flex-mojos/browse_thread/thread/5b5ff62290d1cb56/d7013abdae604828


FLEX CONFIGURATION

  AS3ISOLIB

  as3isolib will need to be installed locally.  Please download a copy of the swc
  file from google code:

  http://as3isolib.googlecode.com/svn/trunk/fp10/download/as3isolib.zip

  Use the maven install plugin to install the dependency locally:

  %> mvn install:install-file -DgroupId=as3isolib -DartifactId=as3isolib -Dversion=1.0 -Dpackaging=swc
    -Dfile=as3isolib.v1.core.swc


FRAMEWORK RUNTIME SHARED LIBRARIES

  This project uses signed framework libraries distributed by adobe and not present
  in public repositories for legal reasons.

  These must be imported into your local repository

  mvn install:install-file -DgroupId=com.adobe.flex.framework -DartifactId=textLayout
    -Dversion=4.1.0.16076 -Dpackaging=swz
    -Dfile=/path/to/flex-sdk-4.1.0.16076/frameworks/rsls/textLayout_1.1.0.604.swz

  mvn install:install-file -DgroupId=com.adobe.flex.framework -DartifactId=framework
    -Dversion=4.1.0.16076 -Dpackaging=swz
    -Dfile=/path/to/flex-sdk-4.1.0.16076/frameworks/rsls/framework_1.1.0.604.swz

  mvn install:install-file -DgroupId=com.adobe.flex.framework -DartifactId=spark
    -Dversion=4.1.0.16076 -Dpackaging=swz
    -Dfile=/path/to/flex-sdk-4.1.0.16076/frameworks/rsls/spark_1.1.0.604.swz

  mvn install:install-file -DgroupId=com.adobe.flex.framework -DartifactId=sparkskins
    -Dversion=4.1.0.16076 -Dpackaging=swz
    -Dfile=/path/to/flex-sdk-4.1.0.16076/frameworks/rsls/sparkskins_1.1.0.604.swz

  mvn install:install-file -DgroupId=com.adobe.flex.framework -DartifactId=rpc
    -Dversion=4.1.0.16076 -Dpackaging=swz
    -Dfile=/path/to/flex-sdk-4.1.0.16076/frameworks/rsls/rpc_1.1.0.604.swz

  All other dependencies are managed by means of Maven.

