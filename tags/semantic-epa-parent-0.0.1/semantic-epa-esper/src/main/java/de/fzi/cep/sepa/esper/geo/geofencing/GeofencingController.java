package de.fzi.cep.sepa.esper.geo.geofencing;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.fzi.cep.sepa.commons.Utils;
import de.fzi.cep.sepa.esper.config.EsperConfig;
import de.fzi.cep.sepa.model.builder.EpProperties;
import de.fzi.cep.sepa.model.builder.EpRequirements;
import de.fzi.cep.sepa.model.builder.StaticProperties;
import de.fzi.cep.sepa.model.impl.EpaType;
import de.fzi.cep.sepa.model.impl.EventSchema;
import de.fzi.cep.sepa.model.impl.EventStream;
import de.fzi.cep.sepa.model.impl.Response;
import de.fzi.cep.sepa.model.impl.eventproperty.EventProperty;
import de.fzi.cep.sepa.model.impl.graph.SepaDescription;
import de.fzi.cep.sepa.model.impl.graph.SepaInvocation;
import de.fzi.cep.sepa.model.impl.output.AppendOutputStrategy;
import de.fzi.cep.sepa.model.impl.output.OutputStrategy;
import de.fzi.cep.sepa.model.impl.staticproperty.DomainStaticProperty;
import de.fzi.cep.sepa.model.impl.staticproperty.MappingPropertyUnary;
import de.fzi.cep.sepa.model.impl.staticproperty.OneOfStaticProperty;
import de.fzi.cep.sepa.model.impl.staticproperty.Option;
import de.fzi.cep.sepa.model.impl.staticproperty.PropertyValueSpecification;
import de.fzi.cep.sepa.model.impl.staticproperty.StaticProperty;
import de.fzi.cep.sepa.model.impl.staticproperty.SupportedProperty;
import de.fzi.cep.sepa.model.util.SepaUtils;
import de.fzi.cep.sepa.model.vocabulary.Geo;
import de.fzi.cep.sepa.runtime.flat.declarer.FlatEpDeclarer;
import de.fzi.cep.sepa.client.util.StandardTransportFormat;

public class GeofencingController extends FlatEpDeclarer<GeofencingParameters> {

	@Override
	public SepaDescription declareModel() {
		EventStream stream1 = new EventStream();
		EventSchema schema = new EventSchema();
		EventProperty e1 = EpRequirements.domainPropertyReq(Geo.lat);
		EventProperty e2 = EpRequirements.domainPropertyReq(Geo.lng);
		EventProperty e3 = EpRequirements.stringReq();
		schema.setEventProperties(Arrays.asList(e1, e2, e3));
		
		SepaDescription desc = new SepaDescription("sepa/geofencing", "Geofencing", "Detects whether a location-based stream moves inside a (circular) area around a given point described as latitude-longitude pair.");
		desc.setEpaTypes(Arrays.asList(EpaType.GEO.name()));	
		
		stream1.setUri(EsperConfig.serverUrl +"/" +Utils.getRandomString());
		stream1.setEventSchema(schema);
	
		desc.addEventStream(stream1);
		
		List<OutputStrategy> strategies = new ArrayList<OutputStrategy>();
		List<EventProperty> additionalProperties = new ArrayList<>();
		additionalProperties.add(EpProperties.longEp("geofencingTime", "http://schema.org/DateTime"));
		additionalProperties.add(EpProperties.booleanEp("insideGeofence", "http://schema.org/Text"));
		AppendOutputStrategy appendOutput = new AppendOutputStrategy();
		appendOutput.setEventProperties(additionalProperties);
		strategies.add(appendOutput);
		desc.setOutputStrategies(strategies);
		
		List<StaticProperty> staticProperties = new ArrayList<StaticProperty>();
		
		OneOfStaticProperty operation = new OneOfStaticProperty("operation", "Enter/Leave Area", "Specifies the operation that should be detected: A location-based stream can enter or leave the selected area.");
		operation.addOption(new Option("Enter"));
		operation.addOption(new Option("Leave"));
		
		staticProperties.add(operation);
		staticProperties.add(StaticProperties.integerFreeTextProperty("radius", "Radius (m)", "Specifies the geofence size (the radius around the provided location) in meters.", new PropertyValueSpecification(0, 10000, 10)));
			
		SupportedProperty latSp = new SupportedProperty(Geo.lat, true);
		SupportedProperty lngSp = new SupportedProperty(Geo.lng, true);
		
		List<SupportedProperty> supportedProperties = Arrays.asList(latSp, lngSp);
		DomainStaticProperty dsp = new DomainStaticProperty("location", "Location", "Specifies the center of the geofence", supportedProperties);
		
		staticProperties.add(dsp);
		
		MappingPropertyUnary latMapping = new MappingPropertyUnary(URI.create(e1.getElementId()), "mapping-latitude", "Latitude Coordinate", "Specifies the latitude field of the stream.");
		staticProperties.add(latMapping);
		
		MappingPropertyUnary lngMapping = new MappingPropertyUnary(URI.create(e1.getElementId()), "mapping-longitude", "Longitude Coordinate", "Specifies the longitude field of the stream.");
		staticProperties.add(lngMapping);
		
		MappingPropertyUnary partitionMapping = new MappingPropertyUnary(URI.create(e3.getElementId()), "mapping-partition", "Partition Property", "Specifies a field that should be used to partition the stream (e.g., a vehicle plate number)");
		partitionMapping.setValueRequired(false);
		staticProperties.add(partitionMapping);
		
		desc.setStaticProperties(staticProperties);
		desc.setSupportedGrounding(StandardTransportFormat.getSupportedGrounding());
		return desc;
	}

	@Override
	public Response invokeRuntime(SepaInvocation invocationGraph) {
		String operation = SepaUtils.getOneOfProperty(invocationGraph, "operation");
		
		int radius = (int) Double.parseDouble(SepaUtils.getFreeTextStaticPropertyValue(invocationGraph, "radius"));
		
		DomainStaticProperty dsp = SepaUtils.getDomainStaticPropertyBy(invocationGraph, "location");
		double latitude = Double.parseDouble(SepaUtils.getSupportedPropertyValue(dsp, Geo.lat));
		double longitude = Double.parseDouble(SepaUtils.getSupportedPropertyValue(dsp, Geo.lng));
		
		GeofencingData geofencingData = new GeofencingData(latitude, longitude, radius);
		
		String latitudeMapping = SepaUtils.getMappingPropertyName(invocationGraph, "mapping-latitude");
		String longitudeMapping = SepaUtils.getMappingPropertyName(invocationGraph, "mapping-longitude");
		String partitionMapping = SepaUtils.getMappingPropertyName(invocationGraph, "mapping-partition");
		GeofencingParameters params = new GeofencingParameters(invocationGraph, getOperation(operation), geofencingData, latitudeMapping, longitudeMapping, partitionMapping);
		
		try {
			invokeEPRuntime(params, Geofencing::new, invocationGraph);
			return new Response(invocationGraph.getElementId(), true);
		} catch (Exception e) {
			e.printStackTrace();
			return new Response(invocationGraph.getElementId(), false, e.getMessage());
		}
	}

	private GeofencingOperation getOperation(String operation) {
		if (operation.equals("Enter")) return GeofencingOperation.ENTER;
		else return GeofencingOperation.LEAVE;
	}

}
