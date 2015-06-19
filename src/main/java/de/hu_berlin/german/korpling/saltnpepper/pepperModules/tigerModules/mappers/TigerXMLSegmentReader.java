/**
 * Copyright 2009 Humboldt-Universität zu Berlin, INRIA.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 *
 */
package de.hu_berlin.german.korpling.saltnpepper.pepperModules.tigerModules.mappers;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.eclipse.emf.common.util.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.hu_berlin.german.korpling.tiger2.Annotation;
import de.hu_berlin.german.korpling.tiger2.Corpus;
import de.hu_berlin.german.korpling.tiger2.DEFAULT_TYPE;
import de.hu_berlin.german.korpling.tiger2.DOMAIN;
import de.hu_berlin.german.korpling.tiger2.Edge;
import de.hu_berlin.german.korpling.tiger2.Feature;
import de.hu_berlin.german.korpling.tiger2.FeatureValue;
import de.hu_berlin.german.korpling.tiger2.Graph;
import de.hu_berlin.german.korpling.tiger2.Meta;
import de.hu_berlin.german.korpling.tiger2.NonTerminal;
import de.hu_berlin.german.korpling.tiger2.Segment;
import de.hu_berlin.german.korpling.tiger2.SyntacticNode;
import de.hu_berlin.german.korpling.tiger2.Terminal;
import de.hu_berlin.german.korpling.tiger2.Tiger2Factory;
import de.hu_berlin.german.korpling.tiger2.exceptions.TigerImplausibleContentException;
import de.hu_berlin.german.korpling.tiger2.exceptions.TigerInternalException;
import de.hu_berlin.german.korpling.tiger2.exceptions.TigerInvalidModelException;
import de.hu_berlin.german.korpling.tiger2.resources.tigerXML.TigerXMLDictionary;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 * A StAX parser for reading a &lt;tiger2/&gt; xml file and mapping its content
 * to the lt&tiger2/&gt; model.
 *
 * @author Florian Zipser
 *
 */
public class TigerXMLSegmentReader
{

  private static final Logger LOGGER = LoggerFactory.getLogger(
    TigerXMLSegmentReader.class);
//	private static final Log LOGGER = LogFactory.getLog(Tiger2Reader.class);
  /**
   * The {@link Corpus} object, which shall be filled.
   */
  private Corpus rootCorpus = null;
  
  private final XMLStreamReader parser;

  /**
   * Sets the {@link Corpus} object, which shall be filled.
   *
   * @param corpus
   */
  public void setRootCorpus(Corpus corpus)
  {
    this.rootCorpus = corpus;
    this.currentCorpus = corpus;
  }

  /**
   * Returns the {@link Corpus} object, which shall be filled.
   *
   * @return
   */
  public Corpus getRootCorpus()
  {
    return rootCorpus;
  }

  /**
   * The uri, of the file which is currently read.
   */
  private URI inputURI = null;

  /**
   * Sets the uri, of the file which is currently read.
   *
   * @param inputURI
   */
  public void setInputURI(URI inputURI)
  {
    this.inputURI = inputURI;
  }

  /**
   * Returns the uri, of the file which is currently read.
   *
   * @return
   */
  public URI getInputURI()
  {
    return inputURI;
  }

  public TigerXMLSegmentReader(XMLStreamReader parser)
  {
    this.parser = parser;
    this.init();
  }

  /**
   * Initializes this object. Initializing is used when a
   * {@link TigerXMLSegmentReader} object is created or in case such an object
   * shall be reset for starting a new parsing. All internal sets, lists,
   * variables and so on will be set to their initial value.
   */
  public final void init()
  {
    this.elementStack = new Stack<String>();
    this.id2synNode = Collections.synchronizedMap(
      new HashMap<String, SyntacticNode>());
    this.notTargetedEdges = Collections.synchronizedMap(
      new HashMap<Edge, String>());

    currentSegment = null;
    currentGraph = null;
    currentSyntacticNode = null;
  }

  /**
   * Stack, storing the path of read xml elements, starting from root to current
   * element.
   */
  private Stack<String> elementStack = null;

  /**
   * Stores the current {@link TigerXMLDictionary#ELEMENT_SEGMENT} object
   */
  private Segment currentSegment = null;

  /**
   * Stores the current {@link TigerXMLDictionary#ELEMENT_GRAPH} object
   */
  private Graph currentGraph = null;
  /**
   * Stores the current {@link TigerXMLDictionary#ELEMENT_FEATURE} object
   */
  private Feature currentFeature = null;
  /**
   * Stores the current {@link TigerXMLDictionary#ELEMENT_VALUE} object
   */
  private FeatureValue currentFeatureValue = null;

  /**
   * Stores the current {@link TigerXMLDictionary#ELEMENT_TERMINAL} or
   * {@link TigerXMLDictionary#ELEMENT_NONTERMINAL} object
   */
  private SyntacticNode currentSyntacticNode = null;

  /**
   * Stores the current {@link TigerXMLDictionary#ELEMENT_CORPUS} or
   * {@link TigerXMLDictionary#ELEMENT_SUB_CORPUS} object
   */
  private Corpus currentCorpus = null;
  /**
   * A map storing tiger-ids and the created corresponding {@link SyntacticNode}
   * object
   */
  private Map<String, SyntacticNode> id2synNode = null;

  /**
   * A map storing the edges, which do not have a target yet. When an edge was
   * read, it is possible, that its target node haven't been read yet. In such
   * cases all the edges have to processed at the end see {@link #endDocument()}
   */
  private Map<Edge, String> notTargetedEdges = null;

  public void parseHead() throws XMLStreamException
  {
    while (parser.hasNext())
    {
      switch (parser.getEventType())
      {
        case XMLStreamConstants.START_DOCUMENT:
          startElement(parser);
          break;
        case XMLStreamConstants.END_ELEMENT:
          endElement(parser);
          break;
        case XMLStreamReader.CHARACTERS:
          characters(parser);
          break;
        case XMLStreamReader.END_DOCUMENT:
          endDocument();
          break;
      }
      parser.next();
    }
  }
  
  /**
   * Execute parse for at most one sentence.
   * 
   * @return parser The ID of the segment sentence or null if none found.
   * @throws javax.xml.stream.XMLStreamException 
   */
  public String parseSegment() throws XMLStreamException
  {
    while (parser.hasNext())
    {
      switch (parser.getEventType())
      {
        case XMLStreamConstants.START_ELEMENT:
          startElement(parser);
          break;
        case XMLStreamConstants.END_ELEMENT:
          endElement(parser);
          
          if(TigerXMLDictionary.ELEMENT_SEGMENT.equals(parser.getLocalName()))
          {
            parser.next();
            return currentSegment == null ? null : currentSegment.getId();
          }
          
          break;
        case XMLStreamReader.CHARACTERS:
          characters(parser);
          break;
        case XMLStreamReader.END_DOCUMENT:
          endDocument();
          break;
      }
      parser.next();
    }
    return null;
  }

  /**
   * Reads the sub-elements of {@link TigerXMLDictionary#ELEMENT_META}
   *
   * @param parser
   */
  private void characters(XMLStreamReader parser)
  {
    if ((this.elementStack != null) && (this.elementStack.size() > 0))
    {//only if this reader has seen an opening element
      String text = parser.getText();

      if(text != null)
      {
        if (TigerXMLDictionary.ELEMENT_NAME.equals(this.elementStack.peek()))
        {
          currentCorpus.getMeta().setName(text);
        }
        else if (TigerXMLDictionary.ELEMENT_AUTHOR.
          equals(this.elementStack.peek()))
        {
          currentCorpus.getMeta().setAuthor(text);
        }
        else if (TigerXMLDictionary.ELEMENT_DATE.equals(this.elementStack.peek()))
        {
          currentCorpus.getMeta().setDate(text);
        }
        else if (TigerXMLDictionary.ELEMENT_DESCRIPTION.equals(this.elementStack.
          peek()))
        {
          currentCorpus.getMeta().setDescription(text);
        }
        else if (TigerXMLDictionary.ELEMENT_FORMAT.
          equals(this.elementStack.peek()))
        {
          currentCorpus.getMeta().setFormat(text);
        }
        else if (TigerXMLDictionary.ELEMENT_HISTORY.equals(this.elementStack.
          peek()))
        {
          currentCorpus.getMeta().setHistory(text);
        } 
        else if (TigerXMLDictionary.ELEMENT_VALUE.equals(this.elementStack.peek()))
        {//set description of feature
          this.currentFeatureValue.setDescription(text);
        }//set description of feature
      }
    }//only if this reader has seen an opening element
  }

  /**
   * This counter is only necessary to count the current number of {@link Edge}
   * objects, to give them an artificial id. This is necessary, because of edges
   * are sometimes added to the graph first, when all nodes are read.
   */
  private int edgeCounter = 0;

  /**
   * true, if an edge with no id was found while reading *
   */
  boolean edgesWithNoIds = false;
  /**
   * true, if a terminal with no id was found while reading *
   */
  boolean terminalsWithNoIds = false;
  /**
   * true, if a non-terminal with no id was found while reading *
   */
  boolean nonTerminalsWithNoIds = false;

  private void startElement(XMLStreamReader parser)
  {
    if (TigerXMLDictionary.ELEMENT_CORPUS.equals(parser.getLocalName()))
    {
      String idVal = parser.getAttributeValue(null,
        TigerXMLDictionary.ATTRIBUTE_ID);
      this.currentCorpus.setId(idVal);
    }
    else if (TigerXMLDictionary.ELEMENT_SUB_CORPUS.equals(parser.getLocalName()))
    {
      Corpus subCorpus = Tiger2Factory.eINSTANCE.createCorpus();
      this.currentCorpus.getSubCorpora().add(subCorpus);
      this.currentCorpus = subCorpus;
    }
    else if (TigerXMLDictionary.ELEMENT_META.equals(parser.getLocalName()))
    {//corpus/meta
      Meta meta = Tiger2Factory.eINSTANCE.createMeta();
      this.currentCorpus.setMeta(meta);
      elementStack.push(parser.getLocalName());
    }//corpus/meta
    else if (TigerXMLDictionary.ELEMENT_FEATURE.equals(parser.getLocalName()))
    {//corpus/annotation/feature
      if (TigerXMLDictionary.ELEMENT_ANNOTATION.equals(elementStack.peek()))
      {
        this.currentFeature = Tiger2Factory.eINSTANCE.createFeature();
        for (int i = 0; i < parser.getAttributeCount(); i++)
        {
          String attName = parser.getAttributeLocalName(i);
          String attValue = parser.getAttributeValue(i);
          if (TigerXMLDictionary.ATTRIBUTE_ID.equals(attName))
          {
            this.currentFeature.setId(attValue);
          }
          else if (TigerXMLDictionary.ATTRIBUTE_NAME.equals(attName))
          {
            this.currentFeature.setName(attValue);
          }
          else if (TigerXMLDictionary.ATTRIBUTE_DOMAIN.equals(attName))
          {
            DOMAIN domain = DOMAIN.get(attValue.toLowerCase());
            if (domain == null)
            {
              throw new TigerImplausibleContentException(
                "The domain value for a Feature can only be one of the following: '"
                + DOMAIN.T.toString() + "', '" + DOMAIN.NT.toString() + "' or '"
                + DOMAIN.EDGE.toString() + "'.");
            }
            this.currentFeature.setDomain(domain);
          }
        }
        currentCorpus.getFeatures().add(currentFeature);
      }
      else
      {
        throw new TigerImplausibleContentException("An element of type '"
          + TigerXMLDictionary.ELEMENT_FEATURE
          + "' was found, which is not nedted in a '"
          + TigerXMLDictionary.ELEMENT_ANNOTATION + "' element");
      }
    }//corpus/annotation/feature
    else if ((TigerXMLDictionary.ELEMENT_EDGELABEL.equals(parser.getLocalName()))
      || (TigerXMLDictionary.ELEMENT_SECEDGELABEL.equals(parser.getLocalName())))
    {//corpus/annotation/edgelabel or corpus/annotation/secedgelabel
      if (TigerXMLDictionary.ELEMENT_ANNOTATION.equals(elementStack.peek()))
      {
        String idValue = parser.getAttributeValue(null,
          TigerXMLDictionary.ATTRIBUTE_ID);
        this.currentFeature.setId(idValue);
        this.currentFeature = Tiger2Factory.eINSTANCE.createFeature();
        this.currentFeature.setName(DEFAULT_TYPE.LABEL.toString());
        this.currentFeature.setDomain(DOMAIN.EDGE);

        if ((TigerXMLDictionary.ELEMENT_EDGELABEL.equals(parser.getLocalName())))
        {
          this.currentFeature.setType(DEFAULT_TYPE.EDGE.toString());
        }
        else if ((TigerXMLDictionary.ELEMENT_SECEDGELABEL.equals(parser.
          getLocalName())))
        {
          this.currentFeature.setType(DEFAULT_TYPE.SECEDGE.toString());
        }

        currentCorpus.getFeatures().add(currentFeature);
      }
    }//corpus/annotation/edgelabel or corpus/annotation/secedgelabel
    else if (TigerXMLDictionary.ELEMENT_VALUE.equals(parser.getLocalName()))
    {//corpus/annotation/feature/value
      if ((TigerXMLDictionary.ELEMENT_FEATURE.equals(this.elementStack.peek()))
        || (TigerXMLDictionary.ELEMENT_EDGELABEL.
        equals(this.elementStack.peek()))
        || (TigerXMLDictionary.ELEMENT_SECEDGELABEL.equals(this.elementStack.
          peek())))
      {
        this.currentFeatureValue = Tiger2Factory.eINSTANCE.createFeatureValue();
        for (int i = 0; i < parser.getAttributeCount(); i++)
        {
          String attName = parser.getAttributeLocalName(i);
          String attValue = parser.getAttributeValue(i);
          if (TigerXMLDictionary.ATTRIBUTE_ID.equals(attName))
          {
            this.currentFeatureValue.setId(attValue);
          }
          else if (TigerXMLDictionary.ATTRIBUTE_NAME.equals(attName))
          {
            this.currentFeatureValue.setValue(attValue);
          }
        }
        this.currentFeature.getFeatureValues().add(this.currentFeatureValue);
      }
      else
      {
        throw new TigerImplausibleContentException("An element of type '"
          + TigerXMLDictionary.ELEMENT_VALUE
          + "' was found, which is not contained in a '"
          + TigerXMLDictionary.ELEMENT_FEATURE + "' element.");
      }
    }//corpus/annotation/feature/value
    else if (TigerXMLDictionary.ELEMENT_SEGMENT.equals(parser.getLocalName()))
    {
      Segment segment = Tiger2Factory.eINSTANCE.createSegment();
      this.currentSegment = segment;
      //start: @xml:id
      String id = parser.
        getAttributeValue(null, TigerXMLDictionary.ATTRIBUTE_ID);
      if (id != null)
      {
        segment.setId(id);
      }
      //end: @xml:id
      this.currentCorpus.getSegments().add(segment);
    }
    else if (TigerXMLDictionary.ELEMENT_GRAPH.equals(parser.getLocalName()))
    {
      Graph graph = Tiger2Factory.eINSTANCE.createGraph();
      this.currentGraph = graph;
      //start: @xml:id
      String id = parser.
        getAttributeValue(null, TigerXMLDictionary.ATTRIBUTE_ID);
      if (id != null)
      {
        graph.setId(id);
      }
      //end: @xml:id
      if (this.currentSegment == null)
      {
        throw new TigerInternalException(
          "The variable 'currentSegment' of Tiger2Reader was empty when reding a graph element.");
      }
      this.currentSegment.getGraphs().add(graph);
    }
    else if (TigerXMLDictionary.ELEMENT_TERMINAL.equals(parser.getLocalName()))
    {
      this.importTerminal(parser);
    }
    else if (TigerXMLDictionary.ELEMENT_NONTERMINAL.
      equals(parser.getLocalName()))
    {
      importNonTerminal(parser);
    }
    else if ((TigerXMLDictionary.ELEMENT_EDGE.equals(parser.getLocalName()))
      || (TigerXMLDictionary.ELEMENT_SECEDGE.equals(parser.getLocalName())))
    {//ELEMENT_EDGE
      importEdges(parser);
    }//ELEMENT_EDGE
    elementStack.push(parser.getLocalName());
  }

  /**
   * Reads content of xml element {@link TigerXMLDictionary#ELEMENT_TERMINAL}.
   *
   * @param parser
   */
  private void importTerminal(XMLStreamReader parser)
  {
    Terminal terminal = Tiger2Factory.eINSTANCE.createTerminal();

    //start: @xml:id
    String id = parser.getAttributeValue(null, TigerXMLDictionary.ATTRIBUTE_ID);
    if (id == null)
    {
      terminalsWithNoIds = true;
      id = "synNode_" + this.currentGraph.getSyntacticNodes().size() + 1;
    }
    else
    {
      if (this.currentGraph.findNode(id) != null)
      {
        id = id + "_" + this.currentGraph.getSyntacticNodes().size() + 1;
      }
    }
    terminal.setId(id);
    this.id2synNode.put(id, terminal);
		//end: @xml:id

    for (int i = 0; i < parser.getAttributeCount(); i++)
    {
      String attName = parser.getAttributeLocalName(i);
      String attValue = parser.getAttributeValue(i);

      if (TigerXMLDictionary.ATTRIBUTE_ID.
        equals(parser.getAttributeLocalName(i)))
				;
      else if ((TigerXMLDictionary.ATTRIBUTE_WORD.equals(attName))
        || ((TigerXMLDictionary.ATTRIBUTE_WORD.equals(attName)) && (parser.
        getAttributeNamespace(i).equals(TigerXMLDictionary.NAMESPACE_TIGER))))
      {
        terminal.setWord(attValue);
      }
      else
      {
        Annotation annotation = null;
        if (currentCorpus.findFeature(attName, DOMAIN.T) == null)
        {
          Feature newFeature = Tiger2Factory.eINSTANCE.createFeature();
          newFeature.setDomain(DOMAIN.T);
          newFeature.setName(attName);
          currentCorpus.getFeatures().add(newFeature);
        }
        try
        {
          annotation = this.currentCorpus.createAnnotation(attName, DOMAIN.T,
            attValue);
        }
        catch (TigerInvalidModelException e)
        {
          throw new TigerImplausibleContentException("Error in corpus in file '"
            + getInputURI() + "'.", e);
        }

        if (annotation != null)
        {
          terminal.getAnnotations().add(annotation);
        }
      }
    }
    this.currentGraph.getSyntacticNodes().add(terminal);
    this.currentSyntacticNode = terminal;
  }

  /**
   * Reads content of xml element {@link TigerXMLDictionary#ELEMENT_TERMINAL}.
   *
   * @param attributes
   */
  private void importNonTerminal(XMLStreamReader parser)
  {
    NonTerminal nonTerminal = Tiger2Factory.eINSTANCE.createNonTerminal();

    //start: @xml:id
    String id = parser.getAttributeValue(null, TigerXMLDictionary.ATTRIBUTE_ID);
    if (id == null)
    {
      nonTerminalsWithNoIds = true;
      id = "synNode_" + this.currentGraph.getSyntacticNodes().size() + 1;
    }
    else
    {
      if (this.currentGraph.findNode(id) != null)
      {
        id = id + "_" + this.currentGraph.getSyntacticNodes().size() + 1;
      }
    }
    nonTerminal.setId(id);
    this.id2synNode.put(id, nonTerminal);
		//end: @xml:id

    for (int i = 0; i < parser.getAttributeCount(); i++)
    {
      String attName = parser.getAttributeLocalName(i);
      String attValue = parser.getAttributeValue(i);
      if (TigerXMLDictionary.ATTRIBUTE_ID.
        equals(parser.getAttributeLocalName(i)))
				;//ignore it
      else
      {

        Annotation annotation = null;
        if (currentCorpus.findFeature(attName, DOMAIN.NT) == null)
        {
          Feature newFeature = Tiger2Factory.eINSTANCE.createFeature();
          newFeature.setDomain(DOMAIN.NT);
          newFeature.setName(attName);
          currentCorpus.getFeatures().add(newFeature);
        }
        try
        {
          annotation = currentCorpus.createAnnotation(attName, DOMAIN.NT,
            attValue);
        }
        catch (TigerInvalidModelException e)
        {
          throw new TigerImplausibleContentException("Error in corpus in file '"
            + getInputURI() + "'.", e);
        }
        if (annotation != null)
        {
          nonTerminal.getAnnotations().add(annotation);
        }
      }
    }
    this.currentGraph.getSyntacticNodes().add(nonTerminal);
    this.currentSyntacticNode = nonTerminal;
  }

  /**
   * Reads content of xml element {@link TigerXMLDictionary#ELEMENT_EDGE} and
   * {@link TigerXMLDictionary#ELEMENT_SECEDGE}.
   *
   * @param attributes
   */
  private void importEdges(XMLStreamReader parser)
  {
    Edge edge = Tiger2Factory.eINSTANCE.createEdge();
    //start: @xml:id
    String id = parser.getAttributeValue(null, TigerXMLDictionary.ATTRIBUTE_ID);
    if (id == null)
    {
      edgesWithNoIds = true;
      id = "edge_" + edgeCounter;
      this.edgeCounter++;
    }
    else
    {
      if (this.currentGraph.findEdge(id) != null)
      {
        id = id + "_" + edgeCounter;
        this.edgeCounter++;
      }
    }
    edge.setId(id);
		//end: @xml:id

    //start: @type
    String type = null;
    if (TigerXMLDictionary.ELEMENT_EDGE.equals(parser.getLocalName()))
    {
      type = DEFAULT_TYPE.EDGE.toString();
    }
    else
    {
      type = DEFAULT_TYPE.SECEDGE.toString();
    }
    edge.setType(type);
		//end: @type

    for (int i = 0; i < parser.getAttributeCount(); i++)
    {
      String attName = parser.getAttributeLocalName(i);
      String attValue = parser.getAttributeValue(i);

      if (TigerXMLDictionary.ATTRIBUTE_ID.
        equals(parser.getAttributeLocalName(i)))
				;//ignore that
      else if ((TigerXMLDictionary.ATTRIBUTE_IDREF.equals(attName)))
      {
        if ((attName == null) || (attName.isEmpty()))
        {
          throw new TigerImplausibleContentException(
            "An edge was found with an empty '"
            + TigerXMLDictionary.ATTRIBUTE_IDREF + "' value.");
        }
        if (attValue.startsWith("#"))
        {
          attValue = attValue.replace("#", "");
        }
        SyntacticNode synNode = id2synNode.get(attValue);
        if (synNode != null)
        {
          edge.setTarget(synNode);
        }
        else
        {
          this.notTargetedEdges.put(edge, attValue);
        }
      }
      else
      {
        Annotation annotation = null;
        if (currentCorpus.findFeature(attName, DOMAIN.EDGE, type) == null)
        {
          Feature newFeature = Tiger2Factory.eINSTANCE.createFeature();
          newFeature.setDomain(DOMAIN.EDGE);
          newFeature.setName(attName);
          newFeature.setType(type);
          currentCorpus.getFeatures().add(newFeature);
        }
        try
        {
          annotation = currentCorpus.createAnnotation(attName, DOMAIN.EDGE,
            edge.getType(), attValue);
        }
        catch (TigerInvalidModelException e)
        {
          throw new TigerImplausibleContentException("Error in corpus in file '"
            + getInputURI() + "'.", e);
        }

        if (annotation != null)
        {
          edge.getAnnotations().add(annotation);
        }
      }
    }
    edge.setSource(this.currentSyntacticNode);
    if (edge.getTarget() != null)
    {
      currentGraph.getEdges().add(edge);
    }
  }

  /**
   * If the element {@link TigerXMLDictionary#ELEMENT_CORPUS} or
   * {@link TigerXMLDictionary#ELEMENT_CORPUS} ends, all not yet targeted edges
   * will be targeted and stored in the graph by calling
   * {@link #processNotTargetedEdges()}.
   */
  private void endElement(XMLStreamReader parser)
  {
    //determines if an xml element is to pop from stack
    if (TigerXMLDictionary.ELEMENT_GRAPH.equals(parser.getLocalName()))
    {
      this.processNotTargetedEdges();
      this.currentGraph = null;
    }
    elementStack.pop();
  }

  private void endDocument()
  {
    if (edgesWithNoIds)
    {
      LOGGER.warn("One or more edge elements have no id in file '"
        + this.inputURI + "'. ");
    }
    if (nonTerminalsWithNoIds)
    {
      LOGGER.warn(
        "One or more syntactic node (non-terminal) elements have no id in file '"
        + this.inputURI + "'. ");
    }
    if (terminalsWithNoIds)
    {
      LOGGER.warn(
        "One or more syntactic node (terminal) elements have no id in file '"
        + this.inputURI + "'. ");
    }
  }

  /**
   * Setting the target of all not targeted edges (see
   * {@link #notTargetedEdges}) and adds them to the current graph (see
   * {@link TigerXMLReader#currentGraph}).
   */
  private void processNotTargetedEdges()
  {
    if ((notTargetedEdges != null) && (notTargetedEdges.size() > 0))
    {
      Set<Edge> edges = this.notTargetedEdges.keySet();
      for (Edge edge : edges)
      {
        String targetId = notTargetedEdges.get(edge);
        if (targetId == null)
        {
          throw new TigerInternalException(
            "An edge was found in 'NotTargetedEdges', that has no corresponding target node id.");
        }
        SyntacticNode target = this.id2synNode.get(targetId);
        if (target == null)
        {
          throw new TigerImplausibleContentException(
            "The referred target of an edge ( id of target is '" + targetId
            + "') in the document '" + this.getInputURI()
            + "' cannot be found. The reason might be, that the node is not contained in document.");
        }
        edge.setTarget(target);
        this.currentGraph.getEdges().add(edge);
      }
      notTargetedEdges = Collections.
        synchronizedMap(new HashMap<Edge, String>());
    }
  }
}
