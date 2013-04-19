package org.motechproject.openmrs.ws.resource.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.motechproject.openmrs.ws.HttpException;
import org.motechproject.openmrs.ws.OpenMrsInstance;
import org.motechproject.openmrs.ws.RestClient;
import org.motechproject.openmrs.ws.resource.PersonResource;
import org.motechproject.openmrs.ws.resource.model.Attribute;
import org.motechproject.openmrs.ws.resource.model.Attribute.AttributeType;
import org.motechproject.openmrs.ws.resource.model.Attribute.AttributeTypeSerializer;
import org.motechproject.openmrs.ws.resource.model.AttributeTypeListResult;
import org.motechproject.openmrs.ws.resource.model.Concept;
import org.motechproject.openmrs.ws.resource.model.Concept.ConceptSerializer;
import org.motechproject.openmrs.ws.resource.model.Person;
import org.motechproject.openmrs.ws.resource.model.Person.PreferredAddress;
import org.motechproject.openmrs.ws.resource.model.Person.PreferredName;
import org.motechproject.openmrs.ws.util.JsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PersonResourceImpl implements PersonResource {

    private final RestClient restClient;
    private final OpenMrsInstance openmrsInstance;

    @Autowired
    public PersonResourceImpl(RestClient restClient, OpenMrsInstance openmrsInstance) {
        this.restClient = restClient;
        this.openmrsInstance = openmrsInstance;
    }

    @Override
    public Person getPersonById(String uuid) throws HttpException {
        String responseJson = null;
        responseJson = restClient.getJson(openmrsInstance.toInstancePathWithParams("/person/{uuid}?v=full", uuid));
        return (Person) JsonUtils.readJson(responseJson, Person.class);
    }

    @Override
    public Person createPerson(Person person) throws HttpException {
        String requestJson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").create().toJson(person);
        String responseJson = null;
        responseJson = restClient.postForJson(openmrsInstance.toInstancePath("/person"), requestJson);

        return (Person) JsonUtils.readJson(responseJson, Person.class);
    }

    @Override
    public void createPersonAttribute(String personUuid, Attribute attribute) throws HttpException {
        Gson gson = new GsonBuilder().registerTypeAdapter(AttributeType.class, new AttributeTypeSerializer()).create();
        String requestJson = gson.toJson(attribute);

        restClient.postForJson(openmrsInstance.toInstancePathWithParams("/person/{uuid}/attribute", personUuid),
                requestJson);
    }

    @Override
    public AttributeTypeListResult queryPersonAttributeTypeByName(String name) throws HttpException {
        String responseJson = null;
        responseJson = restClient.getJson(openmrsInstance.toInstancePathWithParams("/personattributetype?q={name}",
                name));

        AttributeTypeListResult result = (AttributeTypeListResult) JsonUtils.readJson(responseJson,
                AttributeTypeListResult.class);

        return result;
    }

    @Override
    public void deleteAttribute(String personParentUuid, Attribute attribute) throws HttpException {
        String attributeUuid = attribute.getUuid();

        restClient.delete(openmrsInstance.toInstancePathWithParams("/person/{parentUuid}/attribute/{uuid}?purge",
                personParentUuid, attributeUuid));

    }

    @Override
    public void updatePerson(Person person) throws HttpException {
        Gson gson = new GsonBuilder().registerTypeAdapter(Concept.class, new ConceptSerializer())
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").create();
        // uuid cannot be set on an update call
        String personUuid = person.getUuid();
        person.setUuid(null);

        String requestJson = gson.toJson(person);
        restClient.postWithEmptyResponseBody(openmrsInstance.toInstancePathWithParams("/person/{uuid}", personUuid),
                requestJson);
    }

    @Override
    public void updatePersonName(String personUuid, PreferredName name) throws HttpException {
        Gson gson = new GsonBuilder().create();
        // setting uuid and display to null so they are not included in request
        String nameUuid = name.getUuid();
        name.setDisplay(null);
        name.setUuid(null);

        String requestJson = gson.toJson(name);
        restClient.postWithEmptyResponseBody(
                openmrsInstance.toInstancePathWithParams("/person/{personUuid}/name/{nameUuid}", personUuid, nameUuid),
                requestJson);
    }

    @Override
    public void updatePersonAddress(String uuid, PreferredAddress addr) throws HttpException {
        Gson gson = new GsonBuilder().create();
        // setting uuid to null so it is not included in request
        String addrUuid = addr.getUuid();
        addr.setUuid(null);

        String requestJson = gson.toJson(addr);
        restClient.postWithEmptyResponseBody(
                openmrsInstance.toInstancePathWithParams("/person/{personUuid}/address/{addressUuid}", uuid, addrUuid),
                requestJson);
    }

    @Override
    public void removePerson(String personUuid) throws HttpException {
        restClient.delete(openmrsInstance.toInstancePathWithParams("/person/{uuid}?purge", personUuid));
    }
}
