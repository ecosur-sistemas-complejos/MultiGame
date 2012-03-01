This project creates an AIR installation file, that can be installed onto a computer with an existing Adobe AIR runtime,
or compiled for a mobile device using Adobe's packaging tools.

NOTE: This application is signed by a self-generated 2048-RSA keyfile "src/main/resources/sign.p12" created with
the "adt" binary in the Adobe AIR SDK. I used the following command to generate this "sample" keyfile:

adt -certificate -cn SelfSign -ou SC -o "Sistemas Complejo, ECOSUR" -c MX 2048-RSA sign.p12 secret

To test with the mobileprofile and the iPad screensize, move the descriptor.xml file from the target/classes
directory to the target directory and execute the following command form that location:

${Path-to-Adobe-AIR-SDK}/bin/adl -profile mobileDevice -screensize iPad descriptor.xml
