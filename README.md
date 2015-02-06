# cluster-simulator-for-geoevent

This Simulator functions as a source of data feeding GeoEvents into a cluster of ArcGIS Servers running GeoEvent Extension.
This application is a JavaFX application.

![App](cluster-simulator-for-geoevent.png?raw=true)

## Instructions

### Before Building:

1. Open a command prompt in the directory containing the pom.xml file.
2. Make sure JavaFX is on the classpath with this command "mvn com.zenjava:javafx-maven-plugin:2.0:fix-classpath"   (for details on this, see http://zenjava.com/javafx/maven/fix-classpath.html)

### Building a JAR file
  1. "mvn clean jfx:jar" which creates a jar file in the ./target/jfx/app folder.
  2. Now you can run the executable jar file by going to the folder where the jar file is (./target/jfx/app), and running "java -jar <jar-file-name>"

### Building a native executable
  1. "mvn clean jfx:native", which will place the executable program in "target/jfx/native/bundles"
  2. This executable can be run directly, if copying it to another location, also copy the subdirectories with the libraries it depends on.

## Requirements

* ArcGIS 10.3.x GeoEvent Extension for Server.
* Java JDK 1.7 or greater.
* JavaFX.
* Maven.

## Resources

* [ArcGIS GeoEvent Extension for Server Resource Center](http://pro.arcgis.com/share/geoevent-processor/)
* [ArcGIS Blog](http://blogs.esri.com/esri/arcgis/)
* [twitter@esri](http://twitter.com/esri)

## Issues

Find a bug or want to request a new feature?  Please let us know by submitting an issue.

## Contributing

Esri welcomes contributions from anyone and everyone. Please see our [guidelines for contributing](https://github.com/esri/contributing).

## Licensing
Copyright 2013 Esri

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

A copy of the license is available in the repository's [license.txt](license.txt?raw=true) file.

[](ArcGIS, GeoEvent, Processor)
[](Esri Tags: ArcGIS GeoEvent Processor for Server)
[](Esri Language: Java)