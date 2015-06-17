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

import com.google.common.io.Files;
import de.hu_berlin.german.korpling.saltnpepper.salt.SaltFactory;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SCorpus;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SCorpusGraph;
import de.hu_berlin.german.korpling.tiger2.resources.tigerXML.TigerXMLDictionary;
import java.io.File;

import de.hu_berlin.german.korpling.tiger2.resources.util.XMLHelper;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.ext.DefaultHandler2;

/**
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class TigerXMLSegmentFinder extends DefaultHandler2
{
  
  private final static Logger log = LoggerFactory.getLogger(TigerXMLSegmentFinder.class);

  private final File file;
  private final SCorpusGraph corpusGraph;
  private final SCorpus rootCorpus;
  
  public TigerXMLSegmentFinder(File file, SCorpusGraph corpusGraph)
  {
    this.file = file;
    this.corpusGraph = corpusGraph;
    
    // create a root corpus with the file name as name
    rootCorpus = SaltFactory.eINSTANCE.createSCorpus();
    rootCorpus.setSName(Files.getNameWithoutExtension(file.getName()));
  }

  public void parse()
  {
    corpusGraph.addSNode(rootCorpus);
    XMLHelper.readXml(file, this);
  }

  @Override
  public void startElement(String uri, String localName, String qName,
    Attributes attributes) throws SAXException
  {
    if (TigerXMLDictionary.ELEMENT_SEGMENT.equals(qName))
    {
      String id = attributes.getValue(TigerXMLDictionary.ATTRIBUTE_ID);
      if(id == null)
      {
        log.warn("Found a segment that has no ID. This segment will be ignored.");
      }
      else
      {
        corpusGraph.createSDocument(rootCorpus, id);
      }
    }
  }

}
