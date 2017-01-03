/*
 * Copyright 2017 Humboldt-Universität zu Berlin, INRIA.
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
package org.corpus_tools.peppermodules.tigerModules;

import de.hu_berlin.german.korpling.tiger2.resources.tigerXML.TigerXMLDictionary;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.corpus_tools.pepper.util.XMLStreamWriter;
import org.corpus_tools.salt.common.SCorpus;
import org.corpus_tools.salt.common.SCorpusGraph;
import org.corpus_tools.salt.common.SDocument;
import org.corpus_tools.salt.graph.Identifier;
import org.eclipse.emf.common.util.URI;
import org.jdom2.CDATA;
import org.jdom2.Comment;
import org.jdom2.Content;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.input.StAXStreamBuilder;
import org.jdom2.input.stax.DefaultStAXFilter;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class TigerXMLSegmentSplitter
{

  private final static Logger log = LoggerFactory.getLogger(TigerXMLSegmentSplitter.class);

  private final File file;
  private final SCorpusGraph corpusGraph;
  private final SCorpus parent;
  private final File outputDirectory;

  private final XMLInputFactory xmlInFactory = XMLInputFactory.newFactory();
  private final XMLOutputFactory xmlOutFactory = XMLOutputFactory.newFactory();

  private final LinkedHashMap<Identifier, URI> resourceMap = new LinkedHashMap<>();
  
  private Element corpusTemplate = null;

  public TigerXMLSegmentSplitter(File file, SCorpusGraph corpusGraph, 
    SCorpus parent, File outputDirectory)
  {
    this.file = file;
    this.corpusGraph = corpusGraph;
    this.parent = parent;
    this.outputDirectory = outputDirectory;
  }

  public void split(File outputDirectory)
  {
    

    if (outputDirectory.isFile())
    {
      return;
    }
    else if (!outputDirectory.exists())
    {
      if (!outputDirectory.mkdirs())
      {
        return;
      }
    }
    
    corpusTemplate = new Element(TigerXMLDictionary.ELEMENT_CORPUS);
    Element head = readHead();
    if(head != null)
    {
      corpusTemplate.addContent(head);
    }
    
    try (FileInputStream iStream = new FileInputStream(file))
    {
      XMLStreamReader parser = xmlInFactory.createXMLStreamReader(iStream);
      parse(parser);

    }
    catch (IOException | XMLStreamException ex)
    {
      log.error(null, ex);
    }


  }

  private void parse(XMLStreamReader parser) throws XMLStreamException
  {
    while (parser.hasNext())
    {
      switch (parser.getEventType())
      {
        case XMLStreamConstants.START_ELEMENT:
          startElement(parser);
          break;
      }
      parser.next();
    }
  }

  public LinkedHashMap<Identifier, URI> getResourceMap()
  {
    return resourceMap;
  }
  
  
  private Element readHead()
  {
    List<Content> fragments = new LinkedList<>();
    try (FileInputStream iStream = new FileInputStream(file))
    {
      XMLStreamReader parser = xmlInFactory.createXMLStreamReader(iStream);
      StAXStreamBuilder builder = new StAXStreamBuilder();
      fragments = builder.buildFragments(parser, new DefaultStAXFilter()
      {
        @Override
        public boolean includeElement(int depth, String name, Namespace ns)
        {
          if (depth > 1)
          {
            return true;
          }
          else if (TigerXMLDictionary.ELEMENT_HEAD.equals(name))
          {
            return true;
          }
          else
          {
            return false;
          }
        }
      });
    }
    catch (IOException | XMLStreamException | JDOMException ex)
    {
      log.error(null, ex);
    }
    
    
    if(fragments.size() == 1 && fragments.get(0) instanceof Element)
    {
      return (Element) fragments.get(0);
    }
    else
    {
      return null;
    }

  }
  
  private static void readSegment(Element segmentElem, XMLStreamReader parser) throws XMLStreamException
  {
    Element parent = segmentElem;
    while(parser.hasNext())
    {
      switch (parser.getEventType())
      {
        case XMLStreamConstants.START_ELEMENT:
          if(parent != null)
          {
            // TODO: support namespaces?
            Element elem = new Element(parser.getLocalName());
            for(int i=0; i < parser.getAttributeCount(); i++)
            {
              elem.setAttribute(parser.getAttributeLocalName(i), parser.getAttributeLocalName(i));
            }
            parent.addContent(elem);
            parent = elem;
          }
          break;
        case XMLStreamConstants.CDATA:
          if(parent != null)
          {
            parent.addContent(new CDATA(parser.getText()));
          }
          break;
        case XMLStreamConstants.COMMENT:
         if(parent != null)
         {
           parent.addContent(new Comment(parser.getText()));
         }
         break;
        case XMLStreamConstants.END_ELEMENT:
          if(TigerXMLDictionary.ELEMENT_SEGMENT.equals(parser.getLocalName()))
          {
            return;
          }
          else
          {
            if(parent != null)
            {
              parent = parent.getParentElement();
            }
          }
          break;
      }
      parser.next();
    }
  }

  public void startElement(XMLStreamReader parser) throws XMLStreamException
  {
    if (TigerXMLDictionary.ELEMENT_SEGMENT.equals(parser.getLocalName()))
    {
      String id = parser.getAttributeValue(null, TigerXMLDictionary.ATTRIBUTE_ID);
      if (id == null)
      {
        log.warn("Found a segment that has no ID. This segment will be ignored.");
      }
      else if(corpusTemplate != null)
      {
        SDocument doc = corpusGraph.createDocument(parent, id);
        
        // create a temporary file for this document
        try
        {
          File tmpFileOut = File.createTempFile(id, ".xml", outputDirectory);
          tmpFileOut.deleteOnExit();
          URI tmpFileOutURI = URI.createFileURI(tmpFileOut.getAbsolutePath());

          Element corpus = corpusTemplate.clone();
          Element bodyElem = corpus.addContent(TigerXMLDictionary.ELEMENT_BODY);
          Element segment = bodyElem.addContent(TigerXMLDictionary.ELEMENT_SEGMENT);
          segment.setAttribute(TigerXMLDictionary.ATTRIBUTE_ID, id);
          readSegment(segment, parser);
          
          try (FileOutputStream oStream = new FileOutputStream(tmpFileOut))
          {
            XMLOutputter xml = new XMLOutputter();
            // we want to format the xml. This is used only for demonstration. pretty formatting adds extra spaces and is generally not required.
            xml.setFormat(Format.getPrettyFormat());
            xml.output(corpus, oStream);
            resourceMap.put(doc.getIdentifier(), tmpFileOutURI);
          }
        }
        catch (IOException ex)
        {
          log.error(null, ex);
        }
        
      }
    }
  }

}
