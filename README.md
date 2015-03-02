![SaltNPepper project](./gh-site/img/SaltNPepper_logo2010.png)
# pepperModules-TigerModules
This project provides an importer to support the TigerXML format and the ISOTiger format for the linguistic converter framework Pepper (see https://u.hu-berlin.de/saltnpepper). A detailed description of that importer can be found in section [Tiger2Importer](#details).

Pepper is a pluggable framework to convert a variety of linguistic formats (like [TigerXML](http://www.ims.uni-stuttgart.de/forschung/ressourcen/werkzeuge/TIGERSearch/doc/html/TigerXML.html), the [EXMARaLDA format](http://www.exmaralda.org/), [PAULA](http://www.sfb632.uni-potsdam.de/paula.html) etc.) into each other. Furthermore Pepper uses Salt (see https://github.com/korpling/salt), the graph-based meta model for linguistic data, which acts as an intermediate model to reduce the number of mappings to be implemented. That means converting data from a format _A_ to format _B_ consists of two steps. First the data is mapped from format _A_ to Salt and second from Salt to format _B_. This detour reduces the number of Pepper modules from _n<sup>2</sup>-n_ (in the case of a direct mapping) to _2n_ to handle a number of n formats.

![n:n mappings via SaltNPepper](./gh-site/img/puzzle.png)

In Pepper there are three different types of modules:
* importers (to map a format _A_ to a Salt model)
* manipulators (to map a Salt model to a Salt model, e.g. to add additional annotations, to rename things to merge data etc.)
* exporters (to map a Salt model to a format _B_).

For a simple Pepper workflow you need at least one importer and one exporter.

## Requirements
Since the here provided module is a plugin for Pepper, you need an instance of the Pepper framework. If you do not already have a running Pepper instance, click on the link below and download the latest stable version (not a SNAPSHOT):

> Note:
> Pepper is a Java based program, therefore you need to have at least Java 7 (JRE or JDK) on your system. You can download Java from https://www.oracle.com/java/index.html or http://openjdk.java.net/ .


## Install module
If this Pepper module is not yet contained in your Pepper distribution, you can easily install it. Just open a command line and enter one of the following program calls:

**Windows**
```
pepperStart.bat 
```

**Linux/Unix**
```
bash pepperStart.sh 
```

Then type in command *is* and the path from where to install the module:
```
pepper> update de.hu_berlin.german.korpling.saltnpepper::pepperModules-pepperModules-TigerModules::https://korpling.german.hu-berlin.de/maven2/
```

## Usage
To use this module in your Pepper workflow, put the following lines into the workflow description file. Note the fixed order of xml elements in the workflow description file: &lt;importer/>, &lt;manipulator/>, &lt;exporter/>. The Tiger2Importer is an importer module, which can be addressed by one of the following alternatives.
A detailed description of the Pepper workflow can be found on the [Pepper project site](https://u.hu-berlin.de/saltnpepper). 

### a) Identify the module by name

```xml
<importer name="Tiger2Importer" path="PATH_TO_CORPUS"/>
```

### b) Identify the module by formats
```xml
<importer formatName="tigerXML" formatVersion="1.0" path="PATH_TO_CORPUS"/>
```
or
```xml
<importer formatName="tiger2" formatVersion="2.0.5" path="PATH_TO_CORPUS"/>
```

### c) Use properties
```xml
<importer name="Tiger2Importer" path="PATH_TO_CORPUS">
  <property key="PROPERTY_NAME">PROPERTY_VALUE</key>
</importer>
```

## Contribute
Since this Pepper module is under a free license, please feel free to fork it from github and improve the module. If you even think that others can benefit from your improvements, don't hesitate to make a pull request, so that your changes can be merged.
If you have found any bugs, or have some feature request, please open an issue on github. If you need any help, please write an e-mail to saltnpepper@lists.hu-berlin.de .

## Funders
This project has been funded by the [department of corpus linguistics and morphology](https://www.linguistik.hu-berlin.de/institut/professuren/korpuslinguistik/) of the Humboldt-Universität zu Berlin, the Institut national de recherche en informatique et en automatique ([INRIA](www.inria.fr/en/)) and the [Sonderforschungsbereich 632](https://www.sfb632.uni-potsdam.de/en/). 

## License
  Copyright 2009 Humboldt-Universität zu Berlin, INRIA.

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at
 
  http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.


# <a name="details"/>Tiger2Importer
The TigerImporter is able to import data comming from the TigerXML format and from the ISOTiger-format as well. Therefore, the here described mapping only covers the mapping between the ISOTiger-api and Salt.

## document-structure
The mapping of the document-structure of a Document in ISOTiger to a SDocument in Salt is very straight forward.

### metadata
Metadata in the ISOTiger-model are all fields of the object Meta. These are for instance name, author and date. Each of these fields is mapped to an own metadata objects in Salt called SMetaAnnotation. The name of the metadate in the ISOTiger-model is mapped to the field SMetadata.sName and its value values is mapped to the SMetadata.sValue. All SMetaAnnotation objects are added to the SDocument object representing the Corpus object in the ISOTiger-model

### text, token and terminal
A terminal node (Terminal) is mapped to a SToken node. The overlaped text is mapped to a STextualDS object. During the mapping, only one STextualDS object is created for the entire document. Neither in the TigerXML format nor in the ISOTiger format the primary text can not be recreated, since only tokens are kept, but no information about separators like whitespaces. Therefore the importer provides a property () to customize a separator between tokens. The default separator is the blank character.
Imagine two terminals covering the text "a" and "sample", the default mapping will produce the sText value "a sample".
non-terminal
A non-terminal node (NonTerminal) is mapped to a SStructure node.

### edges
The descision to which class of an SRelation an edge is mapped is rule based, depending on the class of source or the target node of the edge. 
when source of Edge object is a SToken object, than the Edge is mapped to a SPointingRelation object
when Edge.source is a SSpan object and Edge.target is a SToken object , than the Edge is mapped to a SSpanningRelation object
when Edge.source is a SStructure object, than the Edge is mapped to a SDominanceRelation object
SPointingRelation otherwise
 
### annotations
Annotations in general (represented by a Annotation object in the ISOTiger-api) are mapped to a SAnnotation object, where the SAnnotation.sName is mapped to the Annotation.name and the Annotation.value is mapped to the SAnnotation.sValue field. An Annotation object can belong to either a Terminal, a NonTerminal or an Edge object and therefore is referred to the corresponding SNode or SRelation object in Salt. To adopt the mapping with renaming name of an annotation, you can use the property .

### segments
In the default case, Segment objects are ignored and not mapped to Salt. To adopt this behavior you can use the property .

## Properties
 The table  contains an overview of all usable properties to customize the behavior of this pepper module. The following section contains a brief description to each single property and describes the resulting differences in the mapping to the Salt model.
properties to customize importer behavior
|Name of property	|Type of property 	|optional/ mandatory |	default value |
|-------------------|-------------------|--------------------|----------------|
|createSSpan4Segment|	Boolean			|	optional		 |false				|
|map				|	String			|	optional		 |whitespace		|
|separator			|	String			|	optional		 |--				|
|edge.type			|	String			|	optional		 |--				|
|annotation.name	|	String			|	optional		 |--				|
	
### createSSpan4Segment
This flag determines if a SSpan object shall be created for each segment. Must be mappable to a Boolean value.

### map
Property to determine, which Egde type shall be mapped to which kind of SRelation.This is just a prefix of the real property, which has a suffix specifying the Edge type. For instance map.dep or map.prim.

### separator
Determines the separator between terminal nodes. The default separator is ' '.

### edge.type
Gives a renaming table for the sType of a SRelation. The syntax of defining such a table is 'OLDNAME=NEWNAME (,OLDNAME=NEWNAME)*', for instance the property value prim=edge, sec=secedge, will rename all sType values from 'prim' to edge and 'sec' to secedge.

### annotation.name
Gives a renaming table for the name of an annotation, or more specific, which value the sName of the SAnnotation object shall get. The syntax of defining such a table is 'OLDNAME=NEWNAME (,OLDNAME=NEWNAME)*', for instance the property value label=func, will rename all sName values from 'label' to 'func'."
