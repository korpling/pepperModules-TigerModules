/**
 * Copyright 2009 Humboldt-Universität zu Berlin, INRIA.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 */
package org.corpus_tools.peppermodules.tigerModules;

import com.google.common.base.Splitter;
import java.util.Hashtable;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.corpus_tools.pepper.modules.PepperModuleProperties;
import org.corpus_tools.pepper.modules.PepperModuleProperty;
import org.corpus_tools.pepper.modules.exceptions.PepperModulePropertyException;
import org.corpus_tools.salt.SALT_TYPE;
import org.corpus_tools.salt.common.SSpan;
import org.corpus_tools.salt.core.SAnnotation;
import org.corpus_tools.salt.core.SRelation;
import org.corpus_tools.salt.graph.Relation;

import de.hu_berlin.german.korpling.tiger2.Segment;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * This class provides simple methods to access properties to customize a
 * mapping of a <tiger2/> model to Salt or a Salt model to a <tiger2/> model.
 * This class also contains the names of all available properties.
 * 
 * @author Florian Zipser
 *
 */
@SuppressWarnings("serial")
public class Tiger2ImporterProperties extends PepperModuleProperties {
	/**
	 * This flag determines if a SSpan object shall be created for each segment.
	 * Must be mappable to a {@link Boolean} value.
	 */
	public static final String PROP_CREATE_SSPAN = "createSSpan4Segment";

	/**
	 * Property to determine, which {@link Relation} type shall be mapped to
	 * which kind of {@link SRelation}.
	 */
	public static final String PROP_EDGE_2_SRELATION = "map";

	/**
	 * The default separator to separate to tokens, when no default separator is
	 * given.
	 */
	public static final String DEFAULT_SEPARATOR = " ";
	/**
	 * Determines the separator between terminal nodes. The default separator is
	 * {@value #DEFAULT_SEPARATOR}.
	 */
	public static final String PROP_TERMINAL_SEPARATOR = "separator";
	/**
	 * Name of the property to give a renaming table for the sType of a
	 * SRelation. The syntax of defining such a table is 'OLDNAME=NEWNAME
	 * (,OLDNAME=NEWNAME)*', for instance the property value prim=edge,
	 * sec=secedge, will rename all sType values from 'prim' to edge and 'sec'
	 * to secedge.
	 */
	public static final String PROP_RENAME_EDGE_TYPE = "edge.type";

	/**
	 * Name of the property to give a renaming table for the sType of a
	 * SRelation. The syntax of defining such a table is 'OLDNAME=NEWNAME
	 * (,OLDNAME=NEWNAME)*', for instance the property value prim=edge,
	 * sec=secedge, will rename all sType values from 'prim' to edge and 'sec'
	 * to secedge.
	 */
	public static final String PROP_RENAME_ANNOTATION_NAME = "annotation.name";

	/**
	 * Property to determine, which edge types should be reversed.
	 */
	public static final String PROP_EDGE_REVERSE = "edge.reverse";
  
   /**
   * Name of the property that sets whether segments should be treated
   * as documents.
   */
  public static final String PROP_SPLIT_HEURISITC = "splitHeuristic";
  
  public static final String PROP_MANUAL_SPLITS = "manualSplits";

  
	public Tiger2ImporterProperties() {
		this.addProperty(new PepperModuleProperty<>(PROP_CREATE_SSPAN, Boolean.class, "This flag determines if a SSpan object shall be created for each segment. Must be mappable to a Boolean value.", false, false));
		this.addProperty(new PepperModuleProperty<>(PROP_EDGE_2_SRELATION, String.class, "Property to determine, which Egde type shall be mapped to which kind of SRelation. A mapping has the syntax type=SALT_TYPE(, type=SALT_TYPE)*. For instance 'dep=" + SALT_TYPE.SPOINTING_RELATION + ", prim=" + SALT_TYPE.SDOMINANCE_RELATION + "'.", "secedge:" + SALT_TYPE.SDOMINANCE_RELATION, false));
		this.addProperty(new PepperModuleProperty<>(PROP_TERMINAL_SEPARATOR, String.class, "Determines the separator between terminal nodes. The default separator is '" + DEFAULT_SEPARATOR + "'.", DEFAULT_SEPARATOR, false));
		this.addProperty(new PepperModuleProperty<>(PROP_RENAME_EDGE_TYPE, String.class, "Gives a renaming table for the sType of a SRelation. The syntax of defining such a table is 'OLDNAME=NEWNAME (,OLDNAME=NEWNAME)*', for instance the property value prim=edge, sec=secedge, will rename all sType values from 'prim' to edge and 'sec' to secedge.", false));
		this.addProperty(new PepperModuleProperty<>(PROP_RENAME_ANNOTATION_NAME, String.class, "Gives a renaming table for the name of an annotation, or more specific, which value the sName of the SAnnotation object shall get. The syntax of defining such a table is 'OLDNAME=NEWNAME (,OLDNAME=NEWNAME)*', for instance the property value prim=edge, sec=secedge, will rename all sType values from 'prim' to edge and 'sec' to secedge.", false));
		this.addProperty(new PepperModuleProperty<>(PROP_EDGE_REVERSE, String.class, "If true this will reverse the direction of edges having the given types.\n" + "Thus the source node becomes the target node and the target node\n" + "becomes the source node. This is useful when secondary edges are mapped to dominance\n" + "edges and the annotation scheme would introduce cycles. \n" + "By inverting the edges, cycles are avoided.\n" + "This must be a list of type names, seperated by comma.", "secedge,sec", false));
    this.addProperty(new PepperModuleProperty<>(PROP_SPLIT_HEURISITC, String.class, "Select a heuristic to split original treetagger files into smaller documents. Available are: \"segment\" -> each segment is its own document, \"virtualroot\" -> use non-existance of a VROOT annotation as split criteria, this works on the orginal Tiger2 corpus.", "none", Boolean.FALSE));
    this.addProperty(new PepperModuleProperty<>(PROP_MANUAL_SPLITS, String.class, "TODO", "", Boolean.FALSE));
	}

	public void reset() {
		renamingRelationType = null;
		renamingAnnotationName = null;
	}

	/**
	 * Returns if all {@link Segment} objects shall be mapped to {@link SSpan}
	 * objects. If the value {@value #PROP_CREATE_SSPAN} is not set, the default
	 * is <code>true</code>.
	 * 
	 * @return
	 */
	public boolean propCreateSSpan4Segment() {
		return ((Boolean) this.getProperty(PROP_CREATE_SSPAN).getValue());
	}

	@Override
	public void setPropertyValues(Properties properties) {
		super.setPropertyValues(properties);
	}

	/**
	 * Stores the mapping table for property {@link #PROP_EDGE_2_SRELATION},
	 * storing the result is useful, because the extraction will take some time.
	 */
	private Map<String, SALT_TYPE> edge2Relation = null;

	/**
	 * Returns a list containing all mappings from {@link Relation} types to
	 * derivates of {@link SRelation}.
	 * 
	 * @return
	 */
	public synchronized Map<String, SALT_TYPE> getPropRelation2SRelation() {
		if (edge2Relation == null) {
			edge2Relation = new Hashtable<String, SALT_TYPE>();
			if (getProperty(PROP_EDGE_2_SRELATION).getValue() != null) {
				String edgeTypes = getProperty(PROP_EDGE_2_SRELATION).getValue().toString();
				if ((edgeTypes != null) && (!edgeTypes.isEmpty())) {
					String[] mappings = edgeTypes.split(",");
					for (String mapping : mappings) {
						String[] parts = mapping.split(":");
						if ((parts[0] != null) && (!parts[0].isEmpty()) && (parts[1] != null) && (!parts[1].isEmpty())) {
							SALT_TYPE saltType = SALT_TYPE.valueOf(parts[1].trim());
							if (saltType != null) {
								edge2Relation.put(parts[0].trim(), saltType);
							}
						}
					}
				}
			}
		}
		return (edge2Relation);
	}

	/**
	 * Returns the used separator to separate to tokens.
	 */
	public String getSeparator() {
		return ((String) getProperty(PROP_TERMINAL_SEPARATOR).getValue());
	}

	/** Map containing type renaming, just to save processing time **/
	private Map<String, String> renamingRelationType = null;

	/**
	 * Returns a map containing all renamings for SType of {@link SRelation},
	 * with key= old value and value= new value.
	 */
	public Map<String, String> getRenamingMap_RelationType() {
		if (renamingRelationType == null) {
			synchronized (this) {
				if (renamingRelationType == null) {// double check if
													// STyperenaming
													// isn't set.
					Map<String, String> renamingTable = new Hashtable<String, String>();
					String renamingString = (String) getProperty(PROP_RENAME_EDGE_TYPE).getValue();
					if (renamingString != null) {
						renamingString = renamingString.replace(" ", "");
						if (!renamingString.isEmpty()) {
							String[] mappings = renamingString.split(",");
							if (mappings.length > 0) {
								for (String mapping : mappings) {
									String[] parts = mapping.split("=");
									if (parts.length != 2)
										throw new PepperModulePropertyException("Cannot parse the given property value '" + (String) getProperty(PROP_RENAME_EDGE_TYPE).getValue() + "' for property '" + PROP_TERMINAL_SEPARATOR + "', because it does not follow the form OLDNAME=NEWNAME (,OLDNAME=NEWNAME)*. Note, that neither an empty String nor the whitespace is allowed as sType.");
									renamingTable.put(parts[0], parts[1]);
								}
							}
						}
					}
					renamingRelationType = renamingTable;
				}
			}
		}
		return (renamingRelationType);
	}

	/**
	 * Map containing {@link SAnnotation#getName()} renaming, just to save
	 * processing time
	 **/
	private Map<String, String> renamingAnnotationName = null;

	/**
	 * Returns a map containing all renamings for SAnno , with key= old value
	 * and value= new value.
	 */
	public Map<String, String> getRenamingMap_AnnotationName() {
		if (renamingAnnotationName == null) {
			synchronized (this) {
				if (renamingAnnotationName == null) {
					Map<String, String> renamingTable = new Hashtable<String, String>();
					String renamingString = (String) getProperty(PROP_RENAME_ANNOTATION_NAME).getValue();
					if (renamingString != null) {
						renamingString = renamingString.replace(" ", "");
						if (!renamingString.isEmpty()) {
							String[] mappings = renamingString.split(",");
							if (mappings.length > 0) {
								for (String mapping : mappings) {
									String[] parts = mapping.split("=");
									if (parts.length != 2) {
										throw new PepperModulePropertyException("Cannot parse the given property value '" + (String) getProperty(PROP_RENAME_EDGE_TYPE).getValue() + "' for property '" + PROP_TERMINAL_SEPARATOR + "', because it does not follow the form OLDNAME=NEWNAME (,OLDNAME=NEWNAME)*. Note, that neither an empty String nor the whitespace is allowed as sName.");
									}
									renamingTable.put(parts[0], parts[1]);
								}
							}
						}
					}
					renamingAnnotationName = renamingTable;
				}
			}
		}
		return (renamingAnnotationName);
	}

	public Set<String> getRelationReversed() {
		Set<String> result = new LinkedHashSet<>();
		String raw = ((String) getProperty(PROP_EDGE_REVERSE).getValue());
		for (String t : raw.split(",")) {
			String trimmed = t.trim();
			if (!trimmed.isEmpty()) {
				result.add(trimmed);
			}
		}
		return result;
	}
  
  public SplitHeuristic getSplitHeuristic() {
    SplitHeuristic result = SplitHeuristic.none;
    String raw = ((String) getProperty(PROP_SPLIT_HEURISITC).getValue());
    
    if(raw != null && !raw.isEmpty()) {
      try {
        result = SplitHeuristic.valueOf(raw.toLowerCase());
      } catch(IllegalArgumentException ex) {
      }
    }
    
    return result;
  }
  
  public Map<String,String> getManualSplits() {
    Map<String, String> result = new LinkedHashMap<>();
    String raw = ((String) getProperty(PROP_MANUAL_SPLITS).getValue());
    
    if(raw != null && !raw.isEmpty()) {
      for(String entry : Splitter.on(',').omitEmptyStrings().trimResults().split(raw)) {
        List<String> keyValue = Splitter.on('=').trimResults().limit(2).splitToList(entry);
        if(keyValue.size() == 2)
        {
          result.put(keyValue.get(0), keyValue.get(1));
        }
        else if(keyValue.size() == 1)
        {
          result.put(keyValue.get(0), "");
        }
      }
    }
    
    return result;
  }

}
