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

import org.osgi.service.component.annotations.Component;

import de.hu_berlin.german.korpling.saltnpepper.pepper.modules.PepperExporter;
import de.hu_berlin.german.korpling.saltnpepper.pepper.modules.impl.PepperExporterImpl;

/**
 * This is a sample PepperExporter, which can be used for creating individual Exporters for the 
 * Pepper Framework. Therefore you have to take a look to todo's and adapt the code.
 * 
 * <ul>
 *  <li>the salt model to fill, manipulate or export can be accessed via SaltProject::this.getSaltProject()</li>
 * 	<li>special parameters given by Pepper workflow can be accessed via URI::this.getSpecialParams()</li>
 *  <li>a place to store temprorary datas for processing can be accessed via URI::this.getTemproraries()</li>
 *  <li>a place where resources of this bundle are, can be accessed via URL::this.getResources()</li>
 *  <li>a logService can be accessed via LogService::this.getLogService()</li>
 * </ul>
 * @author Florian Zipser
 * @version 1.0
 *
 */
@Component(name="Tiger2ExporterComponent", factory="PepperExporterComponentFactory")
public class Tiger2Exporter extends PepperExporterImpl implements PepperExporter
{
	public Tiger2Exporter(){
		super();
		
		//start: setting name of module
			setName("Tiger2Exporter");
		//end: setting name of module
		
		
		//set list of formats supported by this module
		this.addSupportedFormat("tiger2", "2.0.5", null);
		this.addSupportedFormat("tigerXML", "1.0", null);
		
	}
}
