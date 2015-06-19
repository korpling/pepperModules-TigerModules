/*
 * Copyright 2015 Humboldt-Universität zu Berlin, INRIA.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.hu_berlin.german.korpling.saltnpepper.pepperModules.tigerModules;

import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SCorpus;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SCorpusGraph;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SDocument;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SElementId;
import de.hu_berlin.german.korpling.tiger2.resources.tigerXML.TigerXMLDictionary;
import java.io.File;

import java.util.LinkedHashMap;
import java.util.Map;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.eclipse.emf.common.util.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class TigerXMLSegmentFinder
{
  
  private final static Logger log = LoggerFactory.getLogger(TigerXMLSegmentFinder.class);

  private final File file;
  private final XMLStreamReader xmlReader;
  private final SCorpusGraph corpusGraph;
  private final SCorpus parent;
  
  private final Map<SElementId, URI> resourceMap = new LinkedHashMap<>();
  
  public TigerXMLSegmentFinder(XMLStreamReader xmlReader, File file, SCorpusGraph corpusGraph, SCorpus parent)
  {
    this.xmlReader = xmlReader;
    this.file = file;
    this.corpusGraph = corpusGraph;
    
    this.parent = parent;    
  }
  
  public void parse(XMLStreamReader parser) throws XMLStreamException
  {
    while(parser.hasNext())
    {
      switch(parser.next()) {
        case XMLStreamConstants.START_ELEMENT:
          startElement(parser);
          break;
      }
    }
  }

  public Map<SElementId, URI> getResourceMap()
  {
    return resourceMap;
  }
  
  
  public void startElement(XMLStreamReader parser)
  {
    if (TigerXMLDictionary.ELEMENT_SEGMENT.equals(parser.getLocalName()))
    {
      String id = parser.getAttributeValue(null, TigerXMLDictionary.ATTRIBUTE_ID);
      if(id == null)
      {
        log.warn("Found a segment that has no ID. This segment will be ignored.");
      }
      else
      {
        SDocument doc = corpusGraph.createSDocument(parent, id);
        resourceMap.put(doc.getSElementId(), URI.createFileURI(file.getAbsolutePath()));
      }
    }
  }

}
