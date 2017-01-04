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
import java.util.ArrayList;

import java.util.LinkedHashMap;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
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
import org.jdom2.input.StAXStreamBuilder;
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

  private final SplitHeuristic heuristic;
  private final File file;
  private final SCorpusGraph corpusGraph;
  private final SCorpus parent;
  private final File outputDirectory;

  private final XMLInputFactory xmlInFactory = XMLInputFactory.newFactory();

  private final LinkedHashMap<Identifier, URI> resourceMap = new LinkedHashMap<>();
  
  private Element corpusTemplate = null;

  public TigerXMLSegmentSplitter(SplitHeuristic heuristic, 
    File file, SCorpusGraph corpusGraph, 
    SCorpus parent, File outputDirectory)
  {
    this.heuristic = heuristic;
    this.file = file;
    this.corpusGraph = corpusGraph;
    this.parent = parent;
    this.outputDirectory = outputDirectory;
  }

  public void split()
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
      readAllDocuments(parser);

    }
    catch (IOException | XMLStreamException ex)
    {
      log.error(null, ex);
    }


  }

  private void readAllDocuments(XMLStreamReader parser) throws XMLStreamException
  {
    Element remainingSegment = null;
    while (parser.hasNext())
    {
      parser.next();
      
      switch (parser.getEventType())
      {
        case XMLStreamConstants.START_ELEMENT:
          Element newRemainingSegment = readDocument(parser, remainingSegment);
          remainingSegment = newRemainingSegment;
          break;
      }
    }
  }

  public LinkedHashMap<Identifier, URI> getResourceMap()
  {
    return resourceMap;
  }
  
  
  private Element readHead()
  {
    Content fragment = null;
    try (FileInputStream iStream = new FileInputStream(file))
    {
      XMLStreamReader parser = xmlInFactory.createXMLStreamReader(iStream);
      StAXStreamBuilder builder = new StAXStreamBuilder();
      
      // skip forward until head element
      while(parser.hasNext())
      {
        if(parser.getEventType() == XMLStreamConstants.START_ELEMENT
          && TigerXMLDictionary.ELEMENT_HEAD.equals(parser.getLocalName()))
        {
          break;
        }
        parser.next();
      }
      fragment = builder.fragment(parser);
    }
    catch (IOException | XMLStreamException | JDOMException ex)
    {
      log.error(null, ex);
    }
    
    
    if(fragment instanceof Element)
    {
      return (Element) fragment;
    }
    else
    {
      return null;
    }

  }
  
  private boolean shouldSplitAtSegment(ArrayList<Element> segments)
  {
    if(heuristic == SplitHeuristic.segment)
    {
      // always split at each segment
      return true;
    }
    else if(heuristic == SplitHeuristic.vroot)
    {
      // check if the last segment does not contain a vroot but the one before does
      if(segments.size() >= 2)
      {
        Element last = segments.get(segments.size()-1);
        Element secondLast = segments.get(segments.size()-2);
        
        Element lastGraph = last.getChild(TigerXMLDictionary.ELEMENT_GRAPH);
        Element secondLastGraph = secondLast.getChild(TigerXMLDictionary.ELEMENT_GRAPH);
        
        
        if(lastGraph != null && secondLastGraph != null
          &&!lastGraph.getAttributeValue(TigerXMLDictionary.ATTRIBUTE_ROOT, "").endsWith("_VROOT")
          && secondLastGraph.getAttributeValue(TigerXMLDictionary.ATTRIBUTE_ROOT, "").endsWith("_VROOT"))
        {
          return true;
        }
      }
    }
    // default to not split
    return false;
  }
  
  private Element readSegments(Element parent, XMLStreamReader parser) throws XMLStreamException
  {
    Element current = null;
    ArrayList<Element> segments = new ArrayList<>();
    
    while(parser.hasNext())
    {
      switch (parser.getEventType())
      {
        case XMLStreamConstants.START_ELEMENT:
          
          // TODO: support namespaces?
          Element elem = new Element(parser.getLocalName());
          for(int i=0; i < parser.getAttributeCount(); i++)
          {
            elem.setAttribute(parser.getAttributeLocalName(i), parser.getAttributeValue(i));
          }

          if(TigerXMLDictionary.ELEMENT_SEGMENT.equals(elem.getName()))
          {
            segments.add(elem);
            current = elem;
          }
          else if(current != null)
          {
            // directly append sub-elements
            current.addContent(elem);
            current = elem;
          }
          break;
        case XMLStreamConstants.CDATA:
          if(current != null)
          {
            current.addContent(new CDATA(parser.getText()));
          }
          break;
        case XMLStreamConstants.COMMENT:
         if(current != null)
         {
           current.addContent(new Comment(parser.getText()));
         }
         break;
        case XMLStreamConstants.END_ELEMENT:
          if(TigerXMLDictionary.ELEMENT_SEGMENT.equals(parser.getLocalName()))
          {
            boolean doSplit = shouldSplitAtSegment(segments);
            if(doSplit)
            {
              // split the remaining element from the ones belonging to this document
              Element remainingElement = null;
              if(segments.size() > 1)
              {
                remainingElement = segments.remove(segments.size()-1);
              }
              parent.addContent(segments);
              return remainingElement;
            }
            current = null;
          }
          else
          {
            if(current != null)
            {
              current = current.getParentElement();
            }
          }
          break;
      }
      parser.next();
    }
    return null;
  }

  public Element readDocument(XMLStreamReader parser, Element lastRemainingSegment) throws XMLStreamException
  {
    Element remainingSegment = null;
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
          File tmpFileOut = File.createTempFile("segment_" + id + "_", ".xml", outputDirectory);
          tmpFileOut.deleteOnExit();
          URI tmpFileOutURI = URI.createFileURI(tmpFileOut.getAbsolutePath());

          Element corpus = corpusTemplate.clone();
          Element bodyElem = new Element(TigerXMLDictionary.ELEMENT_BODY);
          corpus.addContent(bodyElem);
          
          if(lastRemainingSegment != null)
          {
            bodyElem.addContent(lastRemainingSegment);
          }
          
          remainingSegment = readSegments(bodyElem, parser);
          
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
    return remainingSegment;
  }

}
