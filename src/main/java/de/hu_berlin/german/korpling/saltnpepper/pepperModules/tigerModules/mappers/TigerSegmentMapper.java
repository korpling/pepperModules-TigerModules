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

import com.google.common.base.Preconditions;
import de.hu_berlin.german.korpling.saltnpepper.pepper.common.DOCUMENT_STATUS;
import de.hu_berlin.german.korpling.saltnpepper.pepper.modules.exceptions.PepperModuleXMLResourceException;
import de.hu_berlin.german.korpling.saltnpepper.pepper.modules.impl.PepperMapperImpl;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SCorpus;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SDocument;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SMetaAnnotation;
import de.hu_berlin.german.korpling.tiger2.Corpus;
import de.hu_berlin.german.korpling.tiger2.Tiger2Factory;
import java.io.File;
import java.util.LinkedList;
import java.util.List;
import javax.xml.stream.XMLStreamException;

/**
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class TigerSegmentMapper extends PepperMapperImpl
{

  private final Corpus docCorpus;
  private String documentName;
  private final TigerXMLSegmentReader tigerReader;

  public TigerSegmentMapper(TigerXMLSegmentReader tigerReader)
  {
    this.docCorpus = Tiger2Factory.eINSTANCE.createCorpus();
    this.tigerReader = tigerReader;
  }

  @Override
  public DOCUMENT_STATUS mapSDocument()
  {
    SDocument doc = getSDocument();
    this.documentName = doc.getSName();
    File f = new File(getResourceURI().toFileString());
    
    if (tigerReader != null)
    {
      tigerReader.setRootCorpus(docCorpus);
      try
      {
        String segmentID = tigerReader.parseSegment();
        Preconditions.checkNotNull(segmentID);
        Preconditions.checkState(segmentID.equals(documentName),
          "Wrong segment order, expected %s but got %s", documentName, segmentID);

     
        Tiger22SaltMapper docMapper = new Tiger22SaltMapper();
        docMapper.setCorpus(docCorpus);
        docMapper.setSDocument(getSDocument());
        docMapper.setProperties(getProperties());

        DOCUMENT_STATUS result = docMapper.mapSDocument();
        
        if (result == DOCUMENT_STATUS.COMPLETED)
        {
          // reset the document name to the original one, since the docMapper might overwrite it
          getSDocument().setSName(documentName);
          SCorpus parentCorpus = doc.getSCorpusGraph().getSCorpus(doc);
          if(parentCorpus != null)
          {
            if (docMapper.getCorpus() != null && docMapper.getCorpus().getMeta()
              != null)
            {
              String corpusName = docMapper.getCorpus().getMeta().getName();
              if (corpusName != null && !corpusName.isEmpty())
              {
                // instead assign the corpus name to the parent corpus
                parentCorpus.setSName(corpusName);
              }
            }
            // segments can't have meta-data but if any meta-data was set this actually belonged to the parent corpus
            if(getSDocument().getSMetaAnnotations() != null)
            {
              List<SMetaAnnotation> metaData = new LinkedList<>(getSDocument().getSMetaAnnotations());
              for(SMetaAnnotation singleMeta : metaData) {
                getSDocument().removeLabel(singleMeta.getQName());
                parentCorpus.createSMetaAnnotation(singleMeta.getSNS(), 
                  singleMeta.getSName(), singleMeta.getSValue(), 
                  singleMeta.getSValueType());
              }
            }
          }

        }
        return result;

      }
      catch (XMLStreamException ex)
      {
        throw new PepperModuleXMLResourceException("Could not read XML file "
          + f.getAbsolutePath(), ex);
      }
    }

    return DOCUMENT_STATUS.FAILED;
  }

}
