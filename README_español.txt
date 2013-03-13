PRIMEROS PASOS

GIT

Multi-Game usa Git para el control de versiones. Si usted no está familiarizado con Git, puede
aprender a cómo usarlo (después de la descarga de una distribución) con el siguiente
tutorial disponible en GitHub:

http://try.github.com/levels/1/challenges/1

El proyecto de nivel superior (MultiGame) y todos los submódulos se almacenan de manera publica en GitHub:

https://github.com/ecosur-sistemas-complejos/MultiGame

Todas las notas son marcadas y existen dos ramas principales: Master and Development. El Master siempre tendra el último código "liberado", mientras que el deployment contendra el trabajo derivado.


Submódulos

Los submódulos de Multi-Game en git se utilizan para hacer referencia a los proyectos dependientes que lo hacen funcionar.
Hemos dividido  el funcionamiento del proyecto en cuatro aspectos distintos: Deploy, Engine, Games y Research. Cada submodulo contiene un historial completo, desde el inicio del proyecto, con todo el historial antes de la división en submódulos del nivel superior.

Para comenzar, basta con hacer lo siguiente desde la parte superior de este proyecto (MultiGame) con esto obtendra su clon de forma exitosa:

    $ Git submodule init
    $ Git submodule update

A continuación la rama de desarrollo en el módulo que desea agregar y con esto ya está listo para contribuir!

    $ Cd "SUB_MODULE"
    $ Git checkout-B development


Si estás usando git-flow, entonces podrias empezar a trabajar en "FeatureName" con lo siguiente:

$git flow init
$git flow feature start "featureName"


NOTA: Asegúrese de utilizar "git update submodule" cuando se mueve por el nivel superior.


CONFIGURACIÓN DE JBOSS AS6

1. MySQL-ds.xml y persistence.xml.

Hibernate es el proveedor de base de datos predeterminada para JBoss sin embargo multi-game está configurado para ejecutarse con MySQL.

Para su comodidad, hemos colocado una muestra de mysql-ds.xml en los recursos del modulo EcosurJPA, el cual se puede utilizar con un servidor MySQL configurado correctamente

NOTA: También es posible utilizar el lenguaje de Hibernate por defecto para trabajar con la base de datos JBoss (HSQL) modificando el marcado y comentado áreas del archivo persistence.xml y comentando las ubicaciones específicas de MySQL.

2. Para configurar usuarios y sus contraseñas, debe agregar la siguente al archivo "login-config.xml" que está en el domino que esta usando por "deploy." (En nuestro caso, esto es "default/conf"):

     <!-- Custom JDBC Security Module for MultiGame -->
     <application-policy name="multigame">
         <authentication>
             <login-module code="org.jboss.security.auth.spi.DatabaseServerLoginModule" flag="required">
                 <module-option name="dsJndiName">java:/MySQLDS</module-option>
                 <module-option name="principalsQuery">select password from user where username=?</module-option>
                 <module-option name="rolesQuery">select name, 'Roles' from role where username=?</module-option>
                 <module-option name="hashAlgorithm">MD5</module-option>
                 <module-option name="hashEncoding">HEX</module-option>
             </login-module>
         </authentication>
     </application-policy>

   Nuevos usuarios pueden estar localizado con nuestra pagina de registracíon, o con la siguente SQL ejemplo:

  >insert into user (id, name, password) values ( 1, 'test', md5('test'));

3. Temas y Configuracion de conexion. El archivo "hornetq-jms.xml" contiene las definiciones de los dos temas que se encuentran suscritas en el juego. Este archivo se incluye en el archivo WAR generado por este proyecto y tambien en el EAR desplegable.

4. BlazeDS
Existen dos archivos jar necesarios de "blazeds-3.3 turnkey distribution" los cuales deben ser colocados en un lugar accesible del directorio "Lib" en el servidor JBoss (esto no se ha probado en Glassfish). Estos archivos son "Flex-tomcat-common.jar" y "flex-tomcat-server.jar". Son utilizados por BlazeDS para realizar la autenticación a distancia con el cliente movil AdobeAir.

Coloque ambos archivos en el directorio << JBOSS_HOME>>/server/default/lib.

Por favor vea el siguiente enlace para ver el contexto:

http://livedocs.adobe.com/blazeds/1/blazeds_devguide/help.html?content=services_security_2.html

Y el siguiente enlace para descargar el "turnkey" install:

http://opensource.adobe.com/wiki/display/blazeds/download+blazeds+3

5. MYSQL JDBC.
Agregar el archivo jar mysql-connector en el directorio lib por defecto. Por ejemplo, en JBoss:
               mv mysql-connector-java-1.5.17-bin.jar $ JBOSS_HOME / server / default / lib

CONFIGURACION DE LA BASE DE DATOS MYSQL.

Para utilizar MySQL con MultiGame tendrá que hacer lo siguiente de forma local en donde el servidor de aplicaciones será instalado.

   1. Instalar MySQL.
   2. Conectarse al servidor local de MySQL como root, y crear la base de datos "multigame".
   3. Crear y conceder todos los privilegios al usuario "mg"@'localhost' en multigame:
                 grant all on multigame.* to 'mg'@'localhost' identified by 'secret'
   4. Compruebe que el archivo jar mysql-connector se encuentra en el directorio lib por defecto.
                 $ ls-la JBOSS_HOME/server/default/lib/mysql-connector-java-5.1.17-bin.jar

Inicie el servidor y realize un deploy con el ear generado por el proyecto EcosurEAR. Nota: la tablas serán creados por el deploy JPA, borrando todos los datos anteriores, que puedan haber estado en la base de datos.

Nota: Si cambia el nombre de usuario o contraseña que se describen en el paso 3, por favor, actualice el
desploy mysql-ds.xml y reiniciar el servidor JBoss.


FLEXMOJOS INTERNACIONALIZACIÓN DE CONFIGURACIÓN

Este proyecto se encuentra internacionalizado en Inglés y Español, por lo que requiere el acceso a
los Flex Framework en español los cuales contienen los archivos de configuración regional.

Desafortunadamente flexmojos requiere de versiones localizadas de las bibliotecas básicas de Flash Player. Estas deben ser importados desde el paquete us_US.. (Por favor reemplace "$ {version}-flex" a continuación con la versión actual tal como se define en el POM games-module.)

mvn install:install-file -DgroupId=com.adobe.flex.framework -DartifactId=flash-integration
     -Dversion=${flex-version} -Dclassifier=es_ES -Dpackaging=rb.swc
     -Dfile=/path/to/flex-sdk/frameworks/locale/en_US/flash-integration_rb.swc

mvn install:install-file -DgroupId=com.adobe.flex.framework -DartifactId=playerglobal
     -Dversion=${flex-version} -Dclassifier=es_ES -Dpackaging=rb.swc
     -Dfile=/path/to/flex-sdk/frameworks/locale/en_US/playerglobal_rb.swc


Para más información visite el sitio:
    http://groups.google.com/group/flexmojos/browse_thread/thread/5b5ff62290d1cb56/d7013abdae604828


FLEX Mobile Theme

El "mobile" es el tema disponible en el SDK de Flex, dentro de los marcos / path móvil. Por favor, instale la versión correcta con el plugin de instalación. El siguiente trabaja bien (por favor, sustituya {version}-flex por la version actual):

   mvn install:install-file -Dfile=mobile.swc -DgroupId=com.adobe.flex.framework -DartifactId=mobile
        -Dpackaging=swc -Dversion={flex-version}  -DgeneratePom=true



FLEX UNIT 4

Actualmente estamos utilizando FlexUnit 4 para la parte Flex basado en nuestras de pruebas unitarias. En orden para ejecutar estas pruebas (y obtener la estructura completa con éxito), se necesita el Flash Player configurado en una variable de entorno dentro del PATH de su sistema, referenciado como "flashplayer". Hay una excelente reportaje de lo que debe hacer si tiene algún problema, especialmente si usted está utilizando OSX, en el siguiente blog post:

http://seanp33.wordpress.com/2010/09/16/flexmojos-mac-osx-and-the-stand-alone-flash-player-debugger/


** Todas las demás dependencias se manejan por medio de Maven y depósitos incluidos en
el pom.xml del nivel superior. **
