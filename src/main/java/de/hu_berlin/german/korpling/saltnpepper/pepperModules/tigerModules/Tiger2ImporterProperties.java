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
package de.hu_berlin.german.korpling.saltnpepper.pepperModules.tigerModules;

import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;

import de.hu_berlin.german.korpling.saltnpepper.pepper.modules.PepperModuleProperties;
import de.hu_berlin.german.korpling.saltnpepper.pepper.modules.PepperModuleProperty;
import de.hu_berlin.german.korpling.saltnpepper.pepper.modules.exceptions.PepperModulePropertyException;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SSpan;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.STYPE_NAME;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SAnnotation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SRelation;
import de.hu_berlin.german.korpling.tiger2.Edge;
import de.hu_berlin.german.korpling.tiger2.Segment;

/**
 * This class provides simple methods to access properties to customize a
 * mapping of a <tiger2/> model to Salt or a Salt model to a <tiger2/> model.
 * This class also contains the names of all available properties.
 * 
 * @author Florian Zipser
 *
 */
public class Tiger2ImporterProperties extends PepperModuleProperties {

	/**
	 * 
	 */
	private static final long serialVersionUID = 142189621809587354l;

	/**
	 * This flag determines if a SSpan object shall be created for each segment.
	 * Must be mappable to a {@link Boolean} value.
	 */
	public static final String PROP_CREATE_SSPAN = "createSSpan4Segment";

	/**
	 * Property to determine, which {@link Edge} type shall be mapped to which
	 * kind of {@link SRelation}.
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

	public Tiger2ImporterProperties() {
		this.addProperty(new PepperModuleProperty<Boolean>(PROP_CREATE_SSPAN, Boolean.class, "This flag determines if a SSpan object shall be created for each segment. Must be mappable to a Boolean value.", false, false));
		this.addProperty(new PepperModuleProperty<String>(PROP_EDGE_2_SRELATION, String.class, "Property to determine, which Egde type shall be mapped to which kind of SRelation. A mapping has the syntax type=STYPE_NAME(, type=STYPE_NAME)*. For instance 'dep=" + STYPE_NAME.SPOINTING_RELATION + ", prim=" + STYPE_NAME.SDOMINANCE_RELATION + "'.", "secedge:"+STYPE_NAME.SDOMINANCE_RELATION, false));
		this.addProperty(new PepperModuleProperty<String>(PROP_TERMINAL_SEPARATOR, String.class, "Determines the separator between terminal nodes. The default separator is '" + DEFAULT_SEPARATOR + "'.", DEFAULT_SEPARATOR, false));
		this.addProperty(new PepperModuleProperty<String>(PROP_RENAME_EDGE_TYPE, String.class, "Gives a renaming table for the sType of a SRelation. The syntax of defining such a table is 'OLDNAME=NEWNAME (,OLDNAME=NEWNAME)*', for instance the property value prim=edge, sec=secedge, will rename all sType values from 'prim' to edge and 'sec' to secedge.", false));
		this.addProperty(new PepperModuleProperty<String>(PROP_RENAME_ANNOTATION_NAME, String.class, "Gives a renaming table for the name of an annotation, or more specific, which value the sName of the SAnnotation object shall get. The syntax of defining such a table is 'OLDNAME=NEWNAME (,OLDNAME=NEWNAME)*', for instance the property value prim=edge, sec=secedge, will rename all sType values from 'prim' to edge and 'sec' to secedge.", false));
	}

	public void reset() {
		renamingEdgeType = null;
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
	private Map<String, STYPE_NAME> edge2Relation = null;

	/**
	 * Returns a list containing all mappings from {@link Edge} types to
	 * derivates of {@link SRelation}.
	 * 
	 * @return
	 */
	public synchronized Map<String, STYPE_NAME> getPropEdge2SRelation() {
		if (edge2Relation == null) {
			edge2Relation = new Hashtable<String, STYPE_NAME>();
			if (getProperty(PROP_EDGE_2_SRELATION).getValue() != null) {
				String edgeTypes = getProperty(PROP_EDGE_2_SRELATION).getValue().toString();
				System.out.println("edgeTypes: " + edgeTypes);
				if ((edgeTypes != null) && (!edgeTypes.isEmpty())) {
					String[] mappings = edgeTypes.split(",");
					for (String mapping : mappings) {
						String[] parts = mapping.split(":");
						if ((parts[0] != null) && (!parts[0].isEmpty()) && (parts[1] != null) && (!parts[1].isEmpty())) {
							STYPE_NAME saltType = STYPE_NAME.get(parts[1].trim());
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
	private Map<String, String> renamingEdgeType = null;

	/**
	 * Returns a map containing all renamings for SType of {@link SRelation},
	 * with key= old value and value= new value.
	 */
	public Map<String, String> getRenamingMap_EdgeType() {
		if (renamingEdgeType == null) {
			synchronized (this) {
				if (renamingEdgeType == null) {// double check if STyperenaming
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
					renamingEdgeType = renamingTable;
				}
			}
		}
		return (renamingEdgeType);
	}

	/**
	 * Map containing {@link SAnnotation#getSName()} renaming, just to save
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
}
