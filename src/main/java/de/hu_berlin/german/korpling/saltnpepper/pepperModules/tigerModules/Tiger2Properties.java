/**
 * Copyright 2009 Humboldt University of Berlin, INRIA.
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

import java.util.Collections;
import java.util.Hashtable;
import java.util.Map;

import de.hu_berlin.german.korpling.saltnpepper.pepper.pepperModules.PepperModuleProperties;
import de.hu_berlin.german.korpling.saltnpepper.pepper.pepperModules.PepperModuleProperty;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SSpan;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.STYPE_NAME;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SRelation;
import de.hu_berlin.german.korpling.tiger2.Edge;
import de.hu_berlin.german.korpling.tiger2.Segment;

/**
 * This class provides simple methods to access properties to customize a mapping of a <tiger2/> model to Salt or a Salt model to
 * a <tiger2/> model. This class also contains the names of all available properties. 
 * 
 * @author Florian Zipser
 *
 */
public class Tiger2Properties extends PepperModuleProperties
{
	public static final String PREFIX_PROP= "pepperModules.tigerModules.";
	public static final String PREFIX_IMPORTER_PROP= PREFIX_PROP+ "importer.";
	
	/**
	 * This flag determines if a SSpan object shall be created for each segment.
	 * Must be mappable to a {@link Boolean} value.
	 */
	public static final String PROP_CREATE_SSPAN= PREFIX_IMPORTER_PROP+"createSSpan4Segment";
	
	/**
	 * Property to determine, which {@link Edge} type shall be mapped to which kind of {@link SRelation}.
	 * This is just a prefix of the real property, which has a suffix specifying the {@link Edge} type. For instance
	 * {@value #PROP_EDGE_2_SRELATION}.dep or {@value #PROP_EDGE_2_SRELATION}.prim.
	 */
	public static final String PROP_EDGE_2_SRELATION= PREFIX_IMPORTER_PROP+"map";
	
	/** The default separator to separate to tokens, when no default separator is given.*/
	public static final String DEFAULT_SEPARATOR=" ";
	/**
	 * Determines the separator between terminal nodes. The default separator is {@value #DEFAULT_SEPARATOR}. 
	 */
	public static final String PROP_TERMINAL_SEPARATOR= PREFIX_IMPORTER_PROP+"separator";
	
	public Tiger2Properties()
	{
		this.addProperty(new PepperModuleProperty<Boolean>(PROP_CREATE_SSPAN, Boolean.class, "This flag determines if a SSpan object shall be created for each segment. Must be mappable to a Boolean value.", false, false));
		this.addProperty(new PepperModuleProperty<String>(PROP_EDGE_2_SRELATION, String.class, "Property to determine, which Egde type shall be mapped to which kind of SRelation.This is just a prefix of the real property, which has a suffix specifying the Edge type. For instance "+PROP_EDGE_2_SRELATION+".dep or "+PROP_EDGE_2_SRELATION+".prim.",false));
		this.addProperty(new PepperModuleProperty<String>(PROP_TERMINAL_SEPARATOR, String.class, "Determines the separator between terminal nodes. The default separator is '"+DEFAULT_SEPARATOR+"'.",DEFAULT_SEPARATOR, false));
	}
	
	/**
	 * Returns if all {@link Segment} objects shall be mapped to {@link SSpan} objects. If the value {@value #PROP_CREATE_SSPAN} is
	 * not set, the default is <code>true</code>. 
	 * @return
	 */
	public boolean propCreateSSpan4Segment()
	{
		return((Boolean)this.getProperty(PROP_CREATE_SSPAN).getValue());
	}
	
	/**
	 * Returns a list containing all mappings from {@link Edge} types to derivates of {@link SRelation}. 
	 * @return
	 */
	public Map<String, STYPE_NAME> propEdge2SRelation()
	{
		Map<String, STYPE_NAME> retVal= Collections.synchronizedMap(new Hashtable<String, STYPE_NAME>());
		
		for (String propName :getPropertyNames())
		{
			if (propName instanceof String)
			{
				if (propName.toString().startsWith(PROP_EDGE_2_SRELATION))
				{
					String value= (String)getProperty(propName).getValue();
					if (value!= null){
						STYPE_NAME saltType = STYPE_NAME.get(value);
						String edgeType= getProperty(propName).getValue().toString().replace(PROP_EDGE_2_SRELATION+".", "");
						if (	(saltType!= null)&&
								(edgeType!= null))
							retVal.put(edgeType, saltType);
					}
				}
			}
		}
		return(retVal);
	}
	
	
	/**
	 * Returns the used separator to separate to tokens.
	 */
	public String getSeparator() {
		return((String)getProperty(PROP_TERMINAL_SEPARATOR).getValue());
	}
}
