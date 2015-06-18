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
package de.hu_berlin.german.korpling.saltnpepper.pepperModules.tigerModules.mappers;

import de.hu_berlin.german.korpling.saltnpepper.pepper.common.DOCUMENT_STATUS;
import de.hu_berlin.german.korpling.saltnpepper.pepper.modules.impl.PepperMapperImpl;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SCorpus;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SDocument;
import de.hu_berlin.german.korpling.tiger2.Corpus;
import de.hu_berlin.german.korpling.tiger2.Tiger2Factory;
import de.hu_berlin.german.korpling.tiger2.resources.tigerXML.TigerXMLDictionary;
import de.hu_berlin.german.korpling.tiger2.resources.tigerXML.TigerXMLReader;
import de.hu_berlin.german.korpling.tiger2.resources.util.EndOfProcessingException;
import de.hu_berlin.german.korpling.tiger2.resources.util.XMLHelper;
import java.io.File;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.ext.DefaultHandler2;

/**
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class TigerSegmentMapper extends PepperMapperImpl
{

  private final Corpus rootCorpus;
  private String documentName;
 
  public TigerSegmentMapper()
  {
    this.rootCorpus = Tiger2Factory.eINSTANCE.createCorpus();
  }

  @Override
  public DOCUMENT_STATUS mapSDocument()
  {
    SDocument doc = getSDocument();
    this.documentName = doc.getSName();
    File f = new File(getResourceURI().toFileString());

    Handler handler = new Handler();
    handler.setRootCorpus(rootCorpus);

    XMLHelper.readXml(f, handler);
    
    Tiger22SaltMapper docMapper = new Tiger22SaltMapper();
    docMapper.setCorpus(rootCorpus);
    docMapper.setSDocument(getSDocument());
    docMapper.setProperties(getProperties());
    
    DOCUMENT_STATUS result = docMapper.mapSDocument();
    
    if(result == DOCUMENT_STATUS.COMPLETED)
    {
      // reset the document name to the original one, since the docMapper might overwrite it
      getSDocument().setSName(documentName);
      String corpusName = docMapper.getCorpus().getMeta().getName();
      if(corpusName != null && !corpusName.isEmpty())
      {
        // instead assign the corpus name to the parent corpus
        SCorpus parentCorpus = doc.getSCorpusGraph().getSCorpus(doc);
        if(parentCorpus != null)
        {
          parentCorpus.setSName(corpusName);
        }
      }
    }
    
    return result;
  }

  public class Handler extends TigerXMLReader
  {
    private boolean headFinished = false;
    private boolean inCorrectSegment = false;

    @Override
    public void startElement(String uri, String localName, String qName,
      Attributes attributes) throws SAXException
    {
      
      if(!headFinished)
      {
        // map everything until head element was closed
        super.startElement(uri, localName, qName, attributes);
      }
      else if(inCorrectSegment)
      {
        super.startElement(uri, localName, qName, attributes);
      }
      else if(TigerXMLDictionary.ELEMENT_SEGMENT.equals(qName)
            && documentName.equals(attributes.getValue(
                TigerXMLDictionary.ATTRIBUTE_ID)))
      {
        inCorrectSegment = true;
        super.startElement(uri, localName, qName, attributes);
      }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException
    {
      if(!headFinished || inCorrectSegment)
      {
        super.characters(ch, start, length);
      }
    }
    
    @Override
    public void endElement(String uri, String localName, String qName) throws
      SAXException
    {
      if (!headFinished)
      {
        // map everything until head element was closed
        super.endElement(qName, localName, qName);
        if (TigerXMLDictionary.ELEMENT_HEAD.equals(qName))
        {
          headFinished = true;
        }
      }
      else if (inCorrectSegment)
      {
        super.endElement(qName, localName, qName);
        if (TigerXMLDictionary.ELEMENT_SEGMENT.equals(qName))
        {
          // we are finished
          throw new EndOfProcessingException();
        }
      }
    }
  }

  }
